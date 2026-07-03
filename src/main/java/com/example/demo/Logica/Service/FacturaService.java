package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import com.example.demo.Logica.Record.FacturaDetalleSnapshot;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.FacturaRepositorio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FacturaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacturaService.class);

    private final FacturaRepositorio facturaRepositorio;
    private final DetallePedidoRepositorio detallePedidoRepositorio;
    private final FacturaPdfGeneratorService facturaPdfGeneratorService;
    private final FacturaStorageService facturaStorageService;
    private final NotificacionPedidoService notificacionPedidoService;
    private final ObjectMapper objectMapper;
    private final int maxIntentos;
    private final long retryDelayMinutes;

    public FacturaService(
            FacturaRepositorio facturaRepositorio,
            DetallePedidoRepositorio detallePedidoRepositorio,
            FacturaPdfGeneratorService facturaPdfGeneratorService,
            FacturaStorageService facturaStorageService,
            NotificacionPedidoService notificacionPedidoService,
            ObjectMapper objectMapper,
            @Value("${app.facturas.max-intentos:3}") int maxIntentos,
            @Value("${app.facturas.retry-delay-minutes:15}") long retryDelayMinutes) {
        this.facturaRepositorio = facturaRepositorio;
        this.detallePedidoRepositorio = detallePedidoRepositorio;
        this.facturaPdfGeneratorService = facturaPdfGeneratorService;
        this.facturaStorageService = facturaStorageService;
        this.notificacionPedidoService = notificacionPedidoService;
        this.objectMapper = objectMapper;
        this.maxIntentos = maxIntentos;
        this.retryDelayMinutes = retryDelayMinutes;
    }

    @Transactional
    public Factura crearFacturaPendiente(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoRepositorio.buscarPorPedido(pedido.getId());

        Factura factura = Factura.builder()
                .numero("FAC-" + pedido.getId())
                .monto(pedido.getTotal())
                .archivoPdf(null)
                .estadoPdf(EstadoFacturaPdf.PENDIENTE)
                .intentosGeneracion(0)
                .ultimoErrorPdf(null)
                .fechaUltimoIntento(null)
                .proximoReintento(LocalDateTime.now())
                .fechaGeneracionPdf(null)
                .localNombreSnapshot(pedido.getLocal() != null ? pedido.getLocal().getNombre() : null)
                .localEmailSnapshot(pedido.getLocal() != null ? pedido.getLocal().getEmail() : null)
                .clienteNombreSnapshot(construirNombreCliente(pedido.getCliente()))
                .clienteEmailSnapshot(pedido.getCliente() != null ? pedido.getCliente().getEmail() : null)
                .direccionEntregaSnapshot(formatearDireccion(pedido.getDomicilioEntrega()))
                .medioPagoSnapshot(pedido.getMedioDePago())
                .detalleItemsJson(serializarDetalles(detalles))
                .pedido(pedido)
                .build();

        facturaRepositorio.guardar(factura);
        return factura;
    }

    @Transactional
    public void procesarFacturasPendientes() {
        List<Factura> facturas = facturaRepositorio.buscarPendientesDeProcesamiento(LocalDateTime.now());
        for (Factura factura : facturas) {
            procesarFactura(factura);
        }
    }

    @Transactional
    public void procesarFactura(Factura factura) {
        LocalDateTime ahora = LocalDateTime.now();
        factura.setEstadoPdf(EstadoFacturaPdf.GENERANDO);
        factura.setIntentosGeneracion(siguienteIntento(factura));
        factura.setFechaUltimoIntento(ahora);
        factura.setUltimoErrorPdf(null);
        factura.setProximoReintento(null);
        facturaRepositorio.actualizar(factura);

        try {
            List<FacturaDetalleSnapshot> detalles = deserializarDetalles(factura.getDetalleItemsJson());
            byte[] pdf = facturaPdfGeneratorService.generarFacturaPdf(factura, detalles);
            String ruta = facturaStorageService.guardarFacturaPdf(factura, pdf);

            factura.setArchivoPdf(ruta);
            factura.setEstadoPdf(EstadoFacturaPdf.GENERADA);
            factura.setFechaGeneracionPdf(LocalDateTime.now());
            factura.setUltimoErrorPdf(null);
            factura.setProximoReintento(null);
            facturaRepositorio.actualizar(factura);

            try {
                notificacionPedidoService.notificarFacturaGenerada(factura, pdf);
            } catch (Exception ex) {
                LOGGER.error("La factura {} se generó pero falló la notificación al cliente: {}", factura.getNumero(), ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            LOGGER.error("Error generando factura {}: {}", factura.getNumero(), ex.getMessage(), ex);
            manejarErrorDeGeneracion(factura, ex);
        }
    }

    private void manejarErrorDeGeneracion(Factura factura, Exception ex) {
        factura.setUltimoErrorPdf(truncarMensaje(ex.getMessage()));

        if (factura.getIntentosGeneracion() != null && factura.getIntentosGeneracion() >= maxIntentos) {
            factura.setEstadoPdf(EstadoFacturaPdf.ERROR_FINAL);
            factura.setProximoReintento(null);
        } else {
            factura.setEstadoPdf(EstadoFacturaPdf.ERROR_REINTENTABLE);
            factura.setProximoReintento(LocalDateTime.now().plusMinutes(retryDelayMinutes));
        }

        facturaRepositorio.actualizar(factura);
    }

    private int siguienteIntento(Factura factura) {
        return factura.getIntentosGeneracion() == null ? 1 : factura.getIntentosGeneracion() + 1;
    }

    private String serializarDetalles(List<DetallePedido> detalles) {
        List<FacturaDetalleSnapshot> snapshot = detalles.stream()
                .map(detalle -> new FacturaDetalleSnapshot(
                        detalle.getPlato() != null ? detalle.getPlato().getNombre() : null,
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el detalle de la factura.", e);
        }
    }

    private List<FacturaDetalleSnapshot> deserializarDetalles(String detalleItemsJson) {
        if (detalleItemsJson == null || detalleItemsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(detalleItemsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo leer el detalle congelado de la factura.", e);
        }
    }

    private String construirNombreCliente(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        String apellido = cliente.getApellido() != null ? cliente.getApellido().trim() : "";
        String nombreCompleto = (nombre + " " + apellido).trim();
        return nombreCompleto.isBlank() ? cliente.getEmail() : nombreCompleto;
    }

    private String formatearDireccion(DtDireccion direccion) {
        if (direccion == null) {
            return null;
        }
        return Stream.of(direccion.getCalle(), direccion.getNumero(), direccion.getCiudad(), direccion.getCodigoPostal())
                .filter(valor -> valor != null && !valor.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    private String truncarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "Error no especificado al generar la factura.";
        }
        return mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
    }
}
