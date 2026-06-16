package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.shared.DtDetallePedido;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Mappers.DetallePedidoMapper;
import com.example.demo.Logica.Mappers.PedidoListadoMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PedidoService {
    private static final String MENSAJE_SIN_PLATOS =
            "Debe agregar al menos un plato para realizar el pedido.";
    private static final String MENSAJE_CANTIDAD_INVALIDA =
            "La cantidad debe ser un número entero mayor a cero.";
    private static final String MENSAJE_LOCAL_CERRADO =
            "Lo sentimos, el local seleccionado cerró y no acepta más pedidos por el momento.";
    private static final String MENSAJE_PLATO_OTRO_LOCAL =
            "El plato seleccionado no pertenece al local indicado.";
    private static final String MENSAJE_PLATO_NO_DISPONIBLE =
            "El plato seleccionado no está disponible.";
    private static final String MENSAJE_TIEMPO_REQUERIDO =
            "Debe ingresar el tiempo estimado de entrega para confirmar el pedido.";
    private static final String MENSAJE_PAGO_FALLIDO =
            "No se pudo procesar el pago. El pedido no ha sido confirmado. Por favor, inténtelo nuevamente.";

    private final PedidoRepositorio pedidoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final LocalRepositorio localRepositorio;
    private final DetallePedidoRepositorio detallePedidoRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final FacturaService facturaService;
    private final PagoSimuladoService pagoSimuladoService;
    private final NotificacionPedidoService notificacionPedidoService;
    private final PedidoListadoMapper pedidoListadoMapper;
    private final DetallePedidoMapper detallePedidoMapper;


    @Value("${mercadopago.back-url-success}")
    private String backUrlSuccess;
    @Value("${mercadopago.back-url-failure}")
    private String backUrlFailure;
    @Value("${mercadopago.back-url-pending}")
    private String backUrlPending;
    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;

    public PedidoService(
            PedidoRepositorio pedidoRepositorio,
            ClienteRepositorio clienteRepositorio,
            LocalRepositorio localRepositorio,
            DetallePedidoRepositorio detallePedidoRepositorio,
            PlatoRepositorio platoRepositorio,
            FacturaService facturaService,
            PagoSimuladoService pagoSimuladoService,
            NotificacionPedidoService notificacionPedidoService,
            PedidoListadoMapper pedidoListadoMapper, DetallePedidoMapper detallePedidoMapper) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.detallePedidoRepositorio = detallePedidoRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.facturaService = facturaService;
        this.pagoSimuladoService = pagoSimuladoService;
        this.notificacionPedidoService = notificacionPedidoService;
        this.pedidoListadoMapper = pedidoListadoMapper;
        this.detallePedidoMapper = detallePedidoMapper;
    }

    @Transactional
    public Pedido confirmarPedido(long idPedido, Long tiempoEstimadoEntregaMinutos) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getEstado().equals(EstadoPedido.Pendiente)) {
            throw new RuntimeException("Solo se pueden confirmar pedidos en estado Pendiente.");
        }

        if (tiempoEstimadoEntregaMinutos == null || tiempoEstimadoEntregaMinutos <= 0) {
            throw new IllegalArgumentException(MENSAJE_TIEMPO_REQUERIDO);
        }

        pedido.setTiempoEstEntrega(Duration.ofMinutes(tiempoEstimadoEntregaMinutos));

        if (!pagoSimuladoService.procesarPago(pedido)) {
            throw new RuntimeException(MENSAJE_PAGO_FALLIDO);
        }

        pedido.setPagoSimulado(true);
        pedido.setEstado(EstadoPedido.Confirmado);
        pedidoRepositorio.actualizar(pedido);

        Factura factura = facturaService.generarYGuardarFactura(pedido);
        notificacionPedidoService.notificarConfirmacion(pedido, factura);

        return pedido;
    }

    @Transactional
    public void rechazarPedido(long idPedido) {

    }

    @Transactional
    public Pedido realizarPedido(DtPedidoConDetalles dtPedidoConDetalles) {
        DtPedido dtPedido = dtPedidoConDetalles.getDtPedido();
        validarPedidoConDetalles(dtPedidoConDetalles);

        Local local = localRepositorio.buscarPorId(dtPedido.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (!Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new RuntimeException(MENSAJE_LOCAL_CERRADO);
        }

        Cliente cliente = clienteRepositorio.buscarPorId(dtPedido.getDtCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<DetallePedido> detalles = detallePedidoMapper.mapearDetallesPedidoDeDt(dtPedidoConDetalles.getDetalles());
        double total = detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        Pedido pedido = construirPedido(dtPedido, local, cliente, detalles, total);

        pedidoRepositorio.guardar(pedido);

        detalles.forEach(detallePedidoRepositorio::guardar);

        return pedido;
    }

    @Transactional
    public void cancelarPedido(Long idPedido) {

    }

    @Transactional(readOnly = true)
    public List<DtPedidoListadoResponse> listarPedidos(Long idLocal, DtPedidoListadoFiltro filtro) {
        localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        validarFiltroListado(filtro);

        return pedidoRepositorio.listarRecibidosPorLocal(idLocal, filtro).stream()
                .map(pedidoListadoMapper::toResponse)
                .toList();
    }

    public void procesarPagoConfirmado(String paymentId) {
        try {
            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));

            if ("approved".equals(payment.getStatus())) {
                Long pedidoId = Long.parseLong(payment.getExternalReference());
                pedidoRepositorio.actualizarPago(pedidoId, true, EstadoPedido.Confirmado);
            }
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error procesando notificación: " + e.getMessage(), e);
        }
    }

    private void validarPedidoConDetalles(DtPedidoConDetalles dtPedidoConDetalles) {
        if (dtPedidoConDetalles.getDtPedido() == null || dtPedidoConDetalles.getDetalles() == null || dtPedidoConDetalles.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(MENSAJE_SIN_PLATOS);
        }
    }



    private Pedido construirPedido(
            DtPedido dtPedido,
            Local local,
            Cliente cliente,
            List<DetallePedido> detalles,
            double total) {
        Pedido pedido = Pedido.builder()
                .fecha(LocalDateTime.now())
                .tiempoEstEntrega(dtPedido.getTiempoEstEntrega())
                .total(total)
                .domicilioEntrega(dtPedido.getDomicilioEntrega())
                .medioDePago(dtPedido.getMedioDePago())
                .pagoSimulado(dtPedido.getPagoSimulado())
                .estado(EstadoPedido.Pendiente)
                .local(local)
                .cliente(cliente)
                .detalles(detalles)
                .mpPreferenciaId(dtPedido.getMpPreferenciaId())
                .mpInitPoint(dtPedido.getMpInitPoint())
                .build();

        detalles.forEach(detalle -> detalle.setPedido(pedido));

        return pedido;
    }

    private void validarCantidad(DtDetallePedido detalleSolicitado) {
        if (detalleSolicitado == null || detalleSolicitado.getCantidad() <= 0) {
            throw new IllegalArgumentException(MENSAJE_CANTIDAD_INVALIDA);
        }
    }

    private void validarFiltroListado(DtPedidoListadoFiltro filtro) {
        if (filtro == null) {
            return;
        }

        if (filtro.getFechaDesde() != null
                && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        if (filtro.getOrdenarPor() != null) {
            List<String> camposValidos = List.of("fecha", "total", "estado");
            if (!camposValidos.contains(filtro.getOrdenarPor().toLowerCase())) {
                throw new IllegalArgumentException("El campo de orden no es válido.");
            }
        }

        if (filtro.getDireccion() != null) {
            List<String> direccionesValidas = List.of("asc", "desc");
            if (!direccionesValidas.contains(filtro.getDireccion().toLowerCase())) {
                throw new IllegalArgumentException("La dirección de orden no es válida.");
            }
        }
    }
}

