package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtDetallePedido;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Mappers.PedidoMapper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private final PedidoMapper pedidoMapper;

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
            NotificacionPedidoService notificacionPedidoService, PedidoMapper  pedidoMapper) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.detallePedidoRepositorio = detallePedidoRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.facturaService = facturaService;
        this.pagoSimuladoService = pagoSimuladoService;
        this.notificacionPedidoService = notificacionPedidoService;
        this.pedidoMapper = pedidoMapper;
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
    public Pedido realizarPedido(DtPedido dtPedido) {
        validarPedidoConDetalles(dtPedido);

        Local local = localRepositorio.buscarPorId(dtPedido.getDtLocal().getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (!Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new RuntimeException(MENSAJE_LOCAL_CERRADO);
        }

        Cliente cliente = clienteRepositorio.buscarPorId(dtPedido.getDtCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<DetallePedido> detalles = construirDetallesPedido(dtPedido.getDetalles(), local);
        double total = detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        Pedido pedido = pedidoMapper.mapearPedidoDeDt(dtPedido);

        pedidoRepositorio.guardar(pedido);

        detalles.forEach(detalle -> {
            detalle.setPedido(pedido);
            detallePedidoRepositorio.guardar(detalle);
        });

        return pedido;
    }

    @Transactional
    public void cancelarPedido(Long idPedido) {

    }

    @Transactional
    public List<DtPedido> listarPedidos(Long idLocal) {
        localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));
        List<DtPedido> dtPedidos = new ArrayList<>();

        return pedidoRepositorio.listarPorLocal(idLocal).stream()
                .map(pedidoMapper::mapearDtPedidoDeClase)
                .collect(Collectors.toList());
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

    private void validarPedidoConDetalles(DtPedido dtPedido) {
        if (dtPedido == null || dtPedido.getDetalles() == null || dtPedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(MENSAJE_SIN_PLATOS);
        }
    }

    private List<DetallePedido> construirDetallesPedido(List<DtDetallePedido> detallesSolicitados, Local local) {
        List<DetallePedido> detalles = new ArrayList<>();

        for (DtDetallePedido detalleSolicitado : detallesSolicitados) {
            validarCantidad(detalleSolicitado);

            Long idPlato = detalleSolicitado.getDtPlato() != null
                    ? detalleSolicitado.getDtPlato().getId()
                    : null;

            Plato plato = platoRepositorio.buscarPorId(idPlato)
                    .orElseThrow(() -> new RuntimeException("Plato no encontrado"));

            if (plato.getLocal() == null || !plato.getLocal().getId().equals(local.getId())) {
                throw new IllegalArgumentException(MENSAJE_PLATO_OTRO_LOCAL);
            }

            if (!Boolean.TRUE.equals(plato.getDisponible())) {
                throw new IllegalArgumentException(MENSAJE_PLATO_NO_DISPONIBLE);
            }

            double precioUnitario = plato.getPrecio();
            double subtotal = precioUnitario * detalleSolicitado.getCantidad();

            detalles.add(DetallePedido.builder()
                    .cantidad(detalleSolicitado.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .plato(plato)
                    .build());
        }

        return detalles;
    }

    private void validarCantidad(DtDetallePedido detalleSolicitado) {
        if (detalleSolicitado == null || detalleSolicitado.getCantidad() <= 0) {
            throw new IllegalArgumentException(MENSAJE_CANTIDAD_INVALIDA);
        }
    }
}
