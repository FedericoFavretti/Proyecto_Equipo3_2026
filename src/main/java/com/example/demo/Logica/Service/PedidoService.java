package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.*;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtDetallePedido;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ExternalServiceException;
import com.example.demo.Logica.Exceptions.PagoRechazadoException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.PedidoListadoMapper;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import com.example.demo.Utils.PaginacionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.Persistencia.Repositorios.*;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Map;

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
    private static final String MENSAJE_SIN_PEDIDOS_CLIENTE =
            "Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!";
    private static final String MENSAJE_FILTROS_SIN_RESULTADOS =
            "No se encontraron pedidos que coincidan con los criterios seleccionados.";
    private static final String MENSAJE_SOLO_PENDIENTE_CONFIRMAR =
            "Solo se pueden confirmar pedidos en estado Pendiente.";
    private static final String MENSAJE_SOLO_PENDIENTE_RECHAZAR =
            "Solo se pueden rechazar pedidos en estado Pendiente.";
    private static final String MENSAJE_MOTIVO_RECHAZO_REQUERIDO =
            "Debe seleccionar o escribir un motivo de rechazo antes de continuar.";
    private static final String MENSAJE_PEDIDO_NO_PENDIENTE =
            "El pedido no se encuentra en estado pendiente.";
    private static final String MENSAJE_PEDIDO_AJENO =
            "No tiene permisos para operar sobre un pedido de otro cliente.";
    private static final String MENSAJE_REINTENTO_SOLO_MP_PENDIENTE =
            "Solo se puede reintentar el pago de pedidos pendientes de Mercado Pago sin acreditar.";
    private static final String MENSAJE_REINTENTO_SIN_DETALLES =
            "No se pudo reintentar el pago porque el pedido no tiene detalles asociados.";
    private static final String MENSAJE_REINTENTO_LOCAL_CERRADO =
            "El local cerró y no puede continuar con el pago en este momento. Espere a que el local vuelva a abrir para reintentar el pago.";
    private static final String MENSAJE_LOCAL_REQUERIDO = "Debe indicar el local del pedido.";
    private static final String MENSAJE_CLIENTE_REQUERIDO = "Debe indicar el cliente del pedido.";
    private static final String MENSAJE_FECHA_DESDE_INVALIDA =
            "La fecha desde no puede ser posterior a la fecha hasta.";
    private static final String MENSAJE_ORDEN_INVALIDO = "El campo de orden no es válido.";
    private static final String MENSAJE_DIRECCION_ORDEN_INVALIDA = "La dirección de orden no es válida.";
    private static final String MENSAJE_ERROR_NOTIFICACION_PAGO = "Error procesando notificación de pago.";
    private static final String MENSAJE_PAGO_NO_ACREDITADO =
            "El pedido no puede confirmarse: el pago no fue acreditado por Mercado Pago.";
    private static final String MEDIO_PAGO_EFECTIVO = "EFECTIVO";
    private static final long MINUTOS_LIMITE_PAGO_MP = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(PedidoService.class);
    private final PedidoRepositorio pedidoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final LocalRepositorio localRepositorio;
    private final DetallePedidoRepositorio detallePedidoRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final PromocionRepositorio promocionRepositorio;
    private final FacturaService facturaService;
    private final PagoSimuladoService pagoSimuladoService;
    private final NotificacionPedidoService notificacionPedidoService;
    private final PedidoListadoMapper pedidoListadoMapper;
    private final UsuarioRepositorio usuarioRepositorio;

    @Value("${mercadopago.back-url-success}")
    private  String backUrlSuccess;
    @Value("${mercadopago.back-url-failure}")
    private String backUrlFailure;
    @Value("${mercadopago.back-url-pending}")
    private String backUrlPending;
    @Value("${mercadopago.back-url-success-mobile}")
    private String backUrlSuccessMobile;
    @Value("${mercadopago.back-url-failure-mobile}")
    private String backUrlFailureMobile;
    @Value("${mercadopago.back-url-pending-mobile}")
    private String backUrlPendingMobile;
    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;
    @Value("${mercadopago.access-token}")
    private String mpAccessToken;

    @Autowired
    private RestTemplate restTemplate;

    public PedidoService(
            PedidoRepositorio pedidoRepositorio,
            ClienteRepositorio clienteRepositorio,
            LocalRepositorio localRepositorio,
            DetallePedidoRepositorio detallePedidoRepositorio,
            PlatoRepositorio platoRepositorio,
            PromocionRepositorio promocionRepositorio,
            FacturaService facturaService,
            PagoSimuladoService pagoSimuladoService,
            NotificacionPedidoService notificacionPedidoService,
            PedidoListadoMapper pedidoListadoMapper, UsuarioRepositorio usuarioRepositorio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.detallePedidoRepositorio = detallePedidoRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.facturaService = facturaService;
        this.pagoSimuladoService = pagoSimuladoService;
        this.notificacionPedidoService = notificacionPedidoService;
        this.pedidoListadoMapper = pedidoListadoMapper;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public Pedido confirmarPedido(long idPedido, Long tiempoEstimadoEntregaMinutos) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idPedido));

        if (!pedido.getEstado().equals(EstadoPedido.Pendiente)) {
            throw new BusinessRuleException(MENSAJE_SOLO_PENDIENTE_CONFIRMAR);
        }

        if (tiempoEstimadoEntregaMinutos == null || tiempoEstimadoEntregaMinutos <= 0) {
            throw new BusinessRuleException(MENSAJE_TIEMPO_REQUERIDO);
        }

        pedido.setTiempoEstEntrega(Duration.ofMinutes(tiempoEstimadoEntregaMinutos));

        if (!Boolean.TRUE.equals(pedido.getPagado())) {
            boolean esMedioSimulado = MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedido.getMedioDePago());

            if (!esMedioSimulado) {
                throw new BusinessRuleException(MENSAJE_PAGO_NO_ACREDITADO);
            }
            if (!pagoSimuladoService.procesarPago(pedido)) {
                throw new PagoRechazadoException(MENSAJE_PAGO_FALLIDO);
            }
            pedido.setPagoSimulado(true);
            pedido.setPagado(true);
        }

        pedido.setEstado(EstadoPedido.Confirmado);
        pedidoRepositorio.actualizar(pedido);

        Factura factura = facturaService.crearFacturaPendiente(pedido);
        notificacionPedidoService.notificarConfirmacion(pedido, factura);

        return pedido;
    }

    @Transactional
    public void rechazarPedido(long idPedido, String motivo) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idPedido));

        if (!pedido.getEstado().equals(EstadoPedido.Pendiente)) {
            throw new BusinessRuleException(MENSAJE_SOLO_PENDIENTE_RECHAZAR);
        }

        if (motivo == null || motivo.isBlank()) {
            throw new BusinessRuleException(MENSAJE_MOTIVO_RECHAZO_REQUERIDO);
        }

        String motivoNormalizado = motivo.trim();
        pedido.setEstado(EstadoPedido.Rechazado);
        pedido.setMotivoRechazo(motivoNormalizado);
        pedidoRepositorio.actualizar(pedido);
        notificacionPedidoService.notificarRechazo(pedido, motivoNormalizado);
    }

    @Transactional
    public Pedido realizarPedido(DtPedidoConDetalles dtPedidoConDetalles) {
        return realizarPedido(dtPedidoConDetalles, false);
    }

    @Transactional
    public Pedido realizarPedido(DtPedidoConDetalles dtPedidoConDetalles, boolean esClienteMobile) {
        validarPedidoConDetalles(dtPedidoConDetalles);
        DtPedido dtPedido = dtPedidoConDetalles.getDtPedido();

        Local local = localRepositorio.buscarPorId(dtPedido.getDtLocal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Local", dtPedido.getDtLocal().getId()));

        if (!Boolean.TRUE.equals(local.getEstaAbierto())) {
            throw new BusinessRuleException(MENSAJE_LOCAL_CERRADO);
        }

        Cliente cliente = clienteRepositorio.buscarPorId(dtPedido.getDtCliente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dtPedido.getDtCliente().getId()));

        LocalDateTime fechaPedido = LocalDateTime.now();
        List<DetallePedido> detalles = construirDetalles(dtPedidoConDetalles.getDetalles(), local, fechaPedido);
        double total = detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
        Pedido pedido = Pedido.builder()
                .fecha(fechaPedido)
                .tiempoEstEntrega(null)
                .total(total)
                .domicilioEntrega(dtPedido.getDomicilioEntrega())
                .medioDePago(dtPedido.getMedioDePago())
                .pagoSimulado(Boolean.TRUE.equals(dtPedido.getPagoSimulado()))
                .pagado(false)
                .estado(EstadoPedido.Pendiente)
                .local(local)
                .cliente(cliente)
                .build();

        pedidoRepositorio.guardar(pedido);

        detalles.forEach(detalle -> {
            detalle.setPedido(pedido);
            detallePedidoRepositorio.guardar(detalle);
        });

        boolean esMedioSimulado = MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedido.getMedioDePago());

        if (esMedioSimulado) {

            notificacionPedidoService.notificarPedido(pedido);
        } else {
            crearPreferenciaPago(pedido, detalles, esClienteMobile);
        }

        return pedido;
    }

    private void crearPreferenciaPago(Pedido pedido, List<DetallePedido> detalles, boolean esClienteMobile) {
        try {
            List<Map<String, Object>> items = detalles.stream()
                    .map(detalle -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("title", detalle.getPlato().getNombre());
                        item.put("quantity", detalle.getCantidad());
                        item.put("unit_price", detalle.getPrecioUnitario());
                        item.put("currency_id", "UYU");
                        return item;
                    }).toList();

            String successUrl = elegirBackUrl(esClienteMobile, backUrlSuccessMobile, backUrlSuccess);
            String failureUrl = elegirBackUrl(esClienteMobile, backUrlFailureMobile, backUrlFailure);
            String pendingUrl = elegirBackUrl(esClienteMobile, backUrlPendingMobile, backUrlPending);

            Map<String, Object> backUrls = new HashMap<>();
            backUrls.put("success", successUrl);
            backUrls.put("failure", failureUrl);
            backUrls.put("pending", pendingUrl);

            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            body.put("back_urls", backUrls);
            body.put("auto_return", "approved");
            body.put("external_reference", pedido.getId().toString());
            body.put("notification_url", webhookUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + mpAccessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

            LOGGER.info("Creando preferencia MP. esClienteMobile={}, backUrlSuccess={}, backUrlFailure={}, backUrlPending={}, webhookUrl={}",
                    esClienteMobile, successUrl, failureUrl, pendingUrl, webhookUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.mercadopago.com/checkout/preferences",
                    httpEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String prefId = (String) responseBody.get("id");
            String initPoint = (String) responseBody.get("init_point");

            pedido.setMpPreferenciaId(prefId);
            pedido.setMpInitPoint(initPoint);
            pedidoRepositorio.actualizarDatosMp(pedido.getId(), prefId, initPoint);

        } catch (HttpClientErrorException e) {
            LOGGER.error("Error HTTP al crear preferencia MP. Status: {} | Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("No se pudo generar la preferencia de pago en Mercado Pago.", e);
        } catch (Exception e) {
            LOGGER.error("Error inesperado al crear preferencia MP: {}", e.getMessage(), e);
            throw new ExternalServiceException("No se pudo generar la preferencia de pago en Mercado Pago.", e);
        }
    }

    private String elegirBackUrl(boolean esClienteMobile, String urlMobile, String urlWeb) {
        if (esClienteMobile && urlMobile != null && !urlMobile.isBlank()) {
            return urlMobile;
        }
        return urlWeb;
    }

    @Transactional
    public void cancelarPedido(Long idPedido) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idPedido));
        cancelarPedidoInterno(pedido);
    }

    @Transactional
    public void cancelarPedidoDeCliente(String emailAutenticado, Long idPedido) {
        Cliente cliente = obtenerClienteAutenticado(emailAutenticado);
        Pedido pedido = obtenerPedidoPropio(cliente, idPedido);
        cancelarPedidoInterno(pedido);
    }

    @Transactional
    public Pedido reintentarPago(String emailAutenticado, Long idPedido) {
        return reintentarPago(emailAutenticado, idPedido, false);
    }

    @Transactional
    public Pedido reintentarPago(String emailAutenticado, Long idPedido, boolean esClienteMobile) {
        Cliente cliente = obtenerClienteAutenticado(emailAutenticado);
        Pedido pedido = obtenerPedidoPropio(cliente, idPedido);

        if (!esPedidoPendienteDePagoMercadoPago(pedido)) {
            throw new BusinessRuleException(MENSAJE_REINTENTO_SOLO_MP_PENDIENTE);
        }

        if (pedido.getLocal() == null || !Boolean.TRUE.equals(pedido.getLocal().getEstaAbierto())) {
            throw new BusinessRuleException(MENSAJE_REINTENTO_LOCAL_CERRADO);
        }

        List<DetallePedido> detalles = detallePedidoRepositorio.buscarPorPedido(idPedido);
        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessRuleException(MENSAJE_REINTENTO_SIN_DETALLES);
        }

        crearPreferenciaPago(pedido, detalles, esClienteMobile);
        return pedido;
    }

    public DtPagina<DtPedidoListadoResponse> listarPedidos(Long idLocal, DtPedidoListadoFiltro filtro) {
        localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException("Local", idLocal));
        validarFiltroListado(filtro);

        List<DtPedidoListadoResponse> pedidos = pedidoRepositorio.listarRecibidosPorLocal(idLocal, filtro).stream()
                .map(pedidoListadoMapper::toResponse)
                .toList();

        Integer pagina = filtro != null ? filtro.getPagina() : null;
        Integer tamanio = filtro != null ? filtro.getTamanio() : null;
        return PaginacionUtils.paginar(pedidos, pagina, tamanio);
    }

    @Transactional(readOnly = true)
    public DtPagina<DtPedidoListadoResponse> buscarYListarHistorialPedidosPropios(String emailAutenticado, DtPedidoListadoFiltro filtro) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (!(usuario instanceof Cliente cliente)) {
            throw new IllegalStateException("Solo los clientes pueden consultar su propio historial de pedidos.");
        }

        Long idCliente = cliente.getId();

        clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", idCliente));
        validarFiltroListado(filtro);

        List<DtPedidoListadoResponse> pedidos = pedidoRepositorio.listarHistorialPorCliente(idCliente, filtro).stream()
                .map(pedidoListadoMapper::toResponse)
                .toList();

        if (!pedidos.isEmpty()) {
            Integer pagina = filtro != null ? filtro.getPagina() : null;
            Integer tamanio = filtro != null ? filtro.getTamanio() : null;
            return PaginacionUtils.paginar(pedidos, pagina, tamanio);
        }

        if (tieneFiltrosAplicados(filtro)) {
            throw new BusinessRuleException(MENSAJE_FILTROS_SIN_RESULTADOS);
        }

        if (!pedidoRepositorio.existePedidoPorCliente(idCliente)) {
            throw new BusinessRuleException(MENSAJE_SIN_PEDIDOS_CLIENTE);
        }

        throw new BusinessRuleException(MENSAJE_FILTROS_SIN_RESULTADOS);
    }

    @Transactional
    public void marcarPedidosComoEntregados() {
        List<Pedido> pedidos = pedidoRepositorio.buscarEnCaminoVencidos(LocalDateTime.now());
        for (Pedido pedido : pedidos) {
            pedido.setEstado(EstadoPedido.Entregado);
            pedidoRepositorio.actualizar(pedido);
        }
    }

    @Transactional
    public void cancelarPedidosMercadoPagoAbandonados() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_LIMITE_PAGO_MP);
        List<Pedido> pedidos = pedidoRepositorio.buscarPendientesMercadoPagoVencidos(limite);
        for (Pedido pedido : pedidos) {
            pedido.setEstado(EstadoPedido.Cancelado);
            pedidoRepositorio.actualizar(pedido);
            LOGGER.info("Pedido {} cancelado automáticamente por falta de pago en Mercado Pago.", pedido.getId());
        }
    }

    @Transactional
    public void procesarPagoConfirmado(String paymentId) {
        try {
            LOGGER.info("Consultando pago en Mercado Pago. paymentId={}", paymentId);
            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
            LOGGER.info("Respuesta de MP para paymentId={}: status={}, statusDetail={}, externalReference={}",
                    paymentId, payment.getStatus(), payment.getStatusDetail(), payment.getExternalReference());

            if ("approved".equals(payment.getStatus())) {
                Long pedidoId = Long.parseLong(payment.getExternalReference());
                pedidoRepositorio.buscarPorId(pedidoId).ifPresentOrElse(pedido -> {
                    if (pedido.getEstado() != EstadoPedido.Cancelado) {
                        boolean pagoRecienConfirmado = pedidoRepositorio.marcarPagoAprobado(pedidoId);
                        if (pagoRecienConfirmado) {
                            pedido.setPagado(true);
                            notificacionPedidoService.notificarPedido(pedido);
                            LOGGER.info("Pedido {} marcado como pagado.", pedidoId);
                        } else {
                            LOGGER.info("Pedido {} ya estaba marcado como pagado, se ignora.", pedidoId);
                        }
                    } else {
                        LOGGER.info("Pedido {} está cancelado, se ignora la confirmación de pago.", pedidoId);
                    }
                }, () -> LOGGER.warn("Notificación de pago recibida para un pedido inexistente: {}", pedidoId));
            } else {
                LOGGER.info("Pago {} no está aprobado todavía (status={}), no se actualiza el pedido.",
                        paymentId, payment.getStatus());
            }
        } catch (MPApiException e) {
            LOGGER.error("Error de la API de Mercado Pago al consultar paymentId={}. Status HTTP: {} | Body: {}",
                    paymentId, e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : "N/A",
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage());
            throw new ExternalServiceException(MENSAJE_ERROR_NOTIFICACION_PAGO, e);
        } catch (MPException e) {
            LOGGER.error("Error de conexión con Mercado Pago al consultar paymentId={}: {}", paymentId, e.getMessage(), e);
            throw new ExternalServiceException(MENSAJE_ERROR_NOTIFICACION_PAGO, e);
        }
    }

    private List<DetallePedido> construirDetalles(List<DtDetallePedido> detallesSolicitados, Local local, LocalDateTime fechaPedido) {
        List<DetallePedido> detalles = new ArrayList<>();

        for (DtDetallePedido detalleSolicitado : detallesSolicitados) {
            validarCantidad(detalleSolicitado);

            if (detalleSolicitado.getDtPlato() == null || detalleSolicitado.getDtPlato().getId() == null) {
                throw new BusinessRuleException(MENSAJE_SIN_PLATOS);
            }

            Plato plato = platoRepositorio.buscarPorId(detalleSolicitado.getDtPlato().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plato", detalleSolicitado.getDtPlato().getId()));

            if (!Boolean.TRUE.equals(plato.getDisponible())) {
                throw new BusinessRuleException(MENSAJE_PLATO_NO_DISPONIBLE);
            }

            if (plato.getLocal() == null || !plato.getLocal().getId().equals(local.getId())) {
                throw new BusinessRuleException(MENSAJE_PLATO_OTRO_LOCAL);
            }

            double precioUnitario = obtenerPrecioUnitarioParaPedido(plato, fechaPedido);
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

    private double obtenerPrecioUnitarioParaPedido(Plato plato, LocalDateTime fechaPedido) {
        return promocionRepositorio.buscarPorPlato(plato.getId()).stream()
                .filter(promocion -> esPromocionVigente(promocion, fechaPedido))
                .max(Comparator.comparing(Promocion::getDescuento))
                .map(promocion -> plato.getPrecio() * (1 - promocion.getDescuento() / 100))
                .orElse(plato.getPrecio());
    }

    private boolean esPromocionVigente(Promocion promocion, LocalDateTime fechaPedido) {
        return promocion.getFechaInicio() != null
                && promocion.getFechaFin() != null
                && !promocion.getFechaInicio().isAfter(fechaPedido)
                && !promocion.getFechaFin().isBefore(fechaPedido);
    }

    private void validarPedidoConDetalles(DtPedidoConDetalles dtPedidoConDetalles) {
        if (dtPedidoConDetalles == null
                || dtPedidoConDetalles.getDtPedido() == null
                || dtPedidoConDetalles.getDetalles() == null
                || dtPedidoConDetalles.getDetalles().isEmpty()) {
            throw new BusinessRuleException(MENSAJE_SIN_PLATOS);
        }

        if (dtPedidoConDetalles.getDtPedido().getDtLocal() == null
                || dtPedidoConDetalles.getDtPedido().getDtLocal().getId() == null) {
            throw new BusinessRuleException(MENSAJE_LOCAL_REQUERIDO);
        }

        if (dtPedidoConDetalles.getDtPedido().getDtCliente() == null
                || dtPedidoConDetalles.getDtPedido().getDtCliente().getId() == null) {
            throw new BusinessRuleException(MENSAJE_CLIENTE_REQUERIDO);
        }
    }

    private void validarCantidad(DtDetallePedido detalleSolicitado) {
        if (detalleSolicitado == null || detalleSolicitado.getCantidad() <= 0) {
            throw new BusinessRuleException(MENSAJE_CANTIDAD_INVALIDA);
        }
    }

    private void validarFiltroListado(DtPedidoListadoFiltro filtro) {
        if (filtro == null) {
            return;
        }

        if (filtro.getFechaDesde() != null
                && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new BusinessRuleException(MENSAJE_FECHA_DESDE_INVALIDA);
        }

        if (filtro.getOrdenarPor() != null) {
            List<String> camposValidos = List.of("fecha", "total", "estado");
            if (!camposValidos.contains(filtro.getOrdenarPor().toLowerCase())) {
                throw new BusinessRuleException(MENSAJE_ORDEN_INVALIDO);
            }
        }

        if (filtro.getDireccion() != null) {
            List<String> direccionesValidas = List.of("asc", "desc");
            if (!direccionesValidas.contains(filtro.getDireccion().toLowerCase())) {
                throw new BusinessRuleException(MENSAJE_DIRECCION_ORDEN_INVALIDA);
            }
        }
    }

    private boolean tieneFiltrosAplicados(DtPedidoListadoFiltro filtro) {
        return filtro != null && (filtro.getEstado() != null
                || filtro.getFechaDesde() != null
                || filtro.getFechaHasta() != null
                || filtro.getIdLocal() != null
                || (filtro.getNombreLocal() != null && !filtro.getNombreLocal().isBlank()));
    }

    private Cliente obtenerClienteAutenticado(String emailAutenticado) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (!(usuario instanceof Cliente cliente)) {
            throw new IllegalStateException("Solo los clientes pueden operar sobre sus propios pedidos.");
        }

        return cliente;
    }

    private Pedido obtenerPedidoPropio(Cliente cliente, Long idPedido) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", idPedido));

        if (pedido.getCliente() == null || !cliente.getId().equals(pedido.getCliente().getId())) {
            throw new BusinessRuleException(MENSAJE_PEDIDO_AJENO);
        }

        return pedido;
    }

    private void cancelarPedidoInterno(Pedido pedido) {
        if (!pedido.getEstado().equals(EstadoPedido.Pendiente)) {
            throw new BusinessRuleException(MENSAJE_PEDIDO_NO_PENDIENTE);
        }
        boolean localYaFueNotificadoDelPedido = yaSeNotificoAlLocal(pedido);
        pedido.setEstado(EstadoPedido.Cancelado);
        pedidoRepositorio.actualizar(pedido);
        if (localYaFueNotificadoDelPedido) {
            notificacionPedidoService.notificarPedidoCancelado(pedido);
        }
    }

    private boolean yaSeNotificoAlLocal(Pedido pedido) {
        boolean esMedioSimulado = MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedido.getMedioDePago());
        return esMedioSimulado || Boolean.TRUE.equals(pedido.getPagado());
    }

    private boolean esPedidoPendienteDePagoMercadoPago(Pedido pedido) {
        return pedido.getEstado() == EstadoPedido.Pendiente
                && !Boolean.TRUE.equals(pedido.getPagado())
                && pedido.getMedioDePago() != null
                && !MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedido.getMedioDePago());
    }
}
