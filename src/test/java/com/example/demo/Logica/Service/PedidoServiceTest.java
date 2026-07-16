package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtDetallePedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.PagoRechazadoException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.PedidoListadoMapper;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepositorio pedidoRepositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private LocalRepositorio localRepositorio;
    @Mock
    private DetallePedidoRepositorio detallePedidoRepositorio;
    @Mock
    private PlatoRepositorio platoRepositorio;
    @Mock
    private PromocionRepositorio promocionRepositorio;
    @Mock
    private FacturaService facturaService;
    @Mock
    private PagoSimuladoService pagoSimuladoService;
    @Mock
    private NotificacionPedidoService notificacionPedidoService;
    @Mock
    private PedidoListadoMapper pedidoListadoMapper;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private RestTemplate restTemplate;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepositorio,
                clienteRepositorio,
                localRepositorio,
                detallePedidoRepositorio,
                platoRepositorio,
                promocionRepositorio,
                facturaService,
                pagoSimuladoService,
                notificacionPedidoService,
                pedidoListadoMapper,
                usuarioRepositorio
        );

        ReflectionTestUtils.setField(pedidoService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(pedidoService, "backUrlSuccess", "https://foodly.com/pago-ok");
        ReflectionTestUtils.setField(pedidoService, "backUrlFailure", "https://foodly.com/pago-error");
        ReflectionTestUtils.setField(pedidoService, "backUrlPending", "https://foodly.com/pago-pendiente");
        ReflectionTestUtils.setField(pedidoService, "webhookUrl", "https://foodly.com/webhook/mp");
        ReflectionTestUtils.setField(pedidoService, "mpAccessToken", "token-de-prueba");
    }

    @Test
    void confirmarPedidoRechazaCuandoNoSeIngresaTiempoEstimado() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarPedido(44L, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe ingresar el tiempo estimado de entrega para confirmar el pedido.");

        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(facturaService, pagoSimuladoService, notificacionPedidoService);
    }

    @Test
    void confirmarPedidoRechazaCuandoFallaElPagoSimulado() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));
        when(pagoSimuladoService.procesarPago(any(Pedido.class))).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.confirmarPedido(44L, 25L))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("No se pudo procesar el pago.");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Pendiente);
        assertThat(pedido.getTiempoEstEntrega()).isEqualTo(Duration.ofMinutes(25));
        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(facturaService, notificacionPedidoService);
    }

    @Test
    void confirmarPedidoConfirmaCreaFacturaPendienteYNotifica() {
        Pedido pedido = pedidoPendiente();
        Factura factura = Factura.builder()
                .id(500L)
                .numero("FAC-44")
                .montoTotal(pedido.getTotal())
                .estadoPdf(EstadoFacturaPdf.PENDIENTE)
                .pedido(pedido)
                .build();

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));
        when(pagoSimuladoService.procesarPago(any(Pedido.class))).thenReturn(true);
        when(facturaService.crearFacturaPendiente(any(Pedido.class))).thenReturn(factura);

        Pedido confirmado = pedidoService.confirmarPedido(44L, 35L);

        assertThat(confirmado.getEstado()).isEqualTo(EstadoPedido.Confirmado);
        assertThat(confirmado.getPagoSimulado()).isTrue();
        assertThat(confirmado.getTiempoEstEntrega()).isEqualTo(Duration.ofMinutes(35));
        verify(pedidoRepositorio).actualizar(eq(pedido));
        verify(facturaService).crearFacturaPendiente(eq(pedido));
        verify(notificacionPedidoService).notificarConfirmacion(eq(pedido), eq(factura));
    }

    @Test
    void confirmarPedidoRechazaCuandoPedidoNoExiste() {
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.confirmarPedido(44L, 30L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pedido no encontrado con id: 44");

        verifyNoInteractions(facturaService, pagoSimuladoService, notificacionPedidoService);
    }

    @Test
    void rechazarPedidoGuardaMotivoRechazoYPersisteNotificacion() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        pedidoService.rechazarPedido(44L, "  Sin disponibilidad de ingredientes  ");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Rechazado);
        assertThat(pedido.getMotivoRechazo()).isEqualTo("Sin disponibilidad de ingredientes");
        verify(pedidoRepositorio).actualizar(eq(pedido));
        verify(notificacionPedidoService).notificarRechazo(eq(pedido), eq("Sin disponibilidad de ingredientes"));
        verifyNoInteractions(facturaService, pagoSimuladoService);
    }

    @Test
    void rechazarPedidoRechazaCuandoMotivoEsVacio() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.rechazarPedido(44L, "   "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe seleccionar o escribir un motivo de rechazo antes de continuar.");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Pendiente);
        assertThat(pedido.getMotivoRechazo()).isNull();
        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(notificacionPedidoService, facturaService, pagoSimuladoService);
    }

    @Test
    void rechazarPedidoRechazaCuandoPedidoNoExiste() {
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.rechazarPedido(44L, "Sin stock"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pedido no encontrado con id: 44");

        verifyNoInteractions(notificacionPedidoService, facturaService, pagoSimuladoService);
    }

    @Test
    void buscarYListarHistorialPedidosPropiosIncluyePedidoPendienteDePagoDelCliente() {
        Cliente cliente = pedidoPendiente().getCliente();
        PedidoListadoView pedidoListadoView = PedidoListadoView.builder()
                .id(88L)
                .fecha(LocalDateTime.now())
                .estado(EstadoPedido.Pendiente)
                .total(450.0)
                .localId(10L)
                .localNombre("La Cocina")
                .cantidadItems(2)
                .pagado(false)
                .build();
        DtPedidoListadoResponse response = DtPedidoListadoResponse.builder()
                .id(88L)
                .estado(EstadoPedido.Pendiente)
                .total(450.0)
                .pagado(false)
                .local(DtLocalResumenResponse.builder()
                        .id(10L)
                        .nombre("La Cocina")
                        .build())
                .build();

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(clienteRepositorio.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.listarHistorialPorCliente(cliente.getId(), null)).thenReturn(List.of(pedidoListadoView));
        when(pedidoListadoMapper.toResponse(pedidoListadoView)).thenReturn(response);

        DtPagina<DtPedidoListadoResponse> historial = pedidoService
                .buscarYListarHistorialPedidosPropios("ana@test.com", null);

        assertThat(historial.getContenido()).hasSize(1);
        assertThat(historial.getContenido().getFirst().getId()).isEqualTo(88L);
        assertThat(historial.getContenido().getFirst().getPagado()).isFalse();
        assertThat(historial.getContenido().getFirst().getEstado()).isEqualTo(EstadoPedido.Pendiente);
        verify(pedidoRepositorio).listarHistorialPorCliente(cliente.getId(), null);
    }

    @Test
    void realizarPedidoAplicaPromocionVigenteEnDetallesYTotal() {
        Local local = Local.builder()
                .id(10L)
                .nombre("La Cocina")
                .estaAbierto(true)
                .build();
        Cliente cliente = Cliente.builder()
                .id(20L)
                .email("ana@test.com")
                .nombre("Ana")
                .apellido("Perez")
                .activo(true)
                .build();
        Plato plato = Plato.builder()
                .id(30L)
                .nombre("Milanesa")
                .precio(100.0)
                .disponible(true)
                .local(local)
                .build();
        Promocion promocion = Promocion.builder()
                .id(40L)
                .descuento(25.0)
                .fechaInicio(LocalDateTime.now().minusHours(1))
                .fechaFin(LocalDateTime.now().plusHours(1))
                .plato(plato)
                .build();
        DtPedidoConDetalles solicitud = DtPedidoConDetalles.builder()
                .dtPedido(DtPedido.builder()
                        .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                        .medioDePago("EFECTIVO")
                        .dtLocal(DtLocal.builder().id(10L).build())
                        .dtCliente(DtCliente.builder().id(20L).build())
                        .build())
                .detalles(List.of(DtDetallePedido.builder()
                        .cantidad(2)
                        .dtPlato(DtPlato.builder().id(30L).build())
                        .build()))
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente));
        when(platoRepositorio.buscarPorId(30L)).thenReturn(Optional.of(plato));
        when(promocionRepositorio.buscarPorPlato(30L)).thenReturn(List.of(promocion));
        doAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(99L);
            return null;
        }).when(pedidoRepositorio).guardar(any(Pedido.class));

        Pedido pedido = pedidoService.realizarPedido(solicitud);

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        ArgumentCaptor<DetallePedido> detalleCaptor = ArgumentCaptor.forClass(DetallePedido.class);

        verify(pedidoRepositorio).guardar(pedidoCaptor.capture());
        verify(detallePedidoRepositorio).guardar(detalleCaptor.capture());
        verify(notificacionPedidoService).notificarPedido(eq(pedido));

        assertThat(pedido.getId()).isEqualTo(99L);
        assertThat(pedido.getTotal()).isEqualTo(150.0);
        assertThat(pedidoCaptor.getValue().getTotal()).isEqualTo(150.0);
        assertThat(detalleCaptor.getValue().getPrecioUnitario()).isEqualTo(75.0);
        assertThat(detalleCaptor.getValue().getSubtotal()).isEqualTo(150.0);
    }

    @Test
    void realizarPedidoIgnoraPromocionFueraDeVigencia() {
        Local local = Local.builder()
                .id(10L)
                .nombre("La Cocina")
                .estaAbierto(true)
                .build();
        Cliente cliente = Cliente.builder()
                .id(20L)
                .email("ana@test.com")
                .nombre("Ana")
                .apellido("Perez")
                .activo(true)
                .build();
        Plato plato = Plato.builder()
                .id(30L)
                .nombre("Milanesa")
                .precio(100.0)
                .disponible(true)
                .local(local)
                .build();
        Promocion promocionVencida = Promocion.builder()
                .id(40L)
                .descuento(25.0)
                .fechaInicio(LocalDateTime.now().minusDays(3))
                .fechaFin(LocalDateTime.now().minusDays(1))
                .plato(plato)
                .build();
        DtPedidoConDetalles solicitud = DtPedidoConDetalles.builder()
                .dtPedido(DtPedido.builder()
                        .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                        .medioDePago("EFECTIVO")
                        .dtLocal(DtLocal.builder().id(10L).build())
                        .dtCliente(DtCliente.builder().id(20L).build())
                        .build())
                .detalles(List.of(DtDetallePedido.builder()
                        .cantidad(2)
                        .dtPlato(DtPlato.builder().id(30L).build())
                        .build()))
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente));
        when(platoRepositorio.buscarPorId(30L)).thenReturn(Optional.of(plato));
        when(promocionRepositorio.buscarPorPlato(30L)).thenReturn(List.of(promocionVencida));
        doAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(100L);
            return null;
        }).when(pedidoRepositorio).guardar(any(Pedido.class));

        pedidoService.realizarPedido(solicitud);

        ArgumentCaptor<DetallePedido> detalleCaptor = ArgumentCaptor.forClass(DetallePedido.class);
        verify(detallePedidoRepositorio).guardar(detalleCaptor.capture());

        assertThat(detalleCaptor.getValue().getPrecioUnitario()).isEqualTo(100.0);
        assertThat(detalleCaptor.getValue().getSubtotal()).isEqualTo(200.0);
    }

    @Test
    void realizarPedidoConMercadoPagoGeneraPreferenciaYNoNotificaAlLocalTodavia() {
        Local local = Local.builder()
                .id(10L)
                .nombre("La Cocina")
                .estaAbierto(true)
                .build();
        Cliente cliente = Cliente.builder()
                .id(20L)
                .email("ana@test.com")
                .nombre("Ana")
                .apellido("Perez")
                .activo(true)
                .build();
        Plato plato = Plato.builder()
                .id(30L)
                .nombre("Milanesa")
                .precio(100.0)
                .disponible(true)
                .local(local)
                .build();
        DtPedidoConDetalles solicitud = DtPedidoConDetalles.builder()
                .dtPedido(DtPedido.builder()
                        .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                        .medioDePago("Mercado Pago")
                        .dtLocal(DtLocal.builder().id(10L).build())
                        .dtCliente(DtCliente.builder().id(20L).build())
                        .build())
                .detalles(List.of(DtDetallePedido.builder()
                        .cantidad(1)
                        .dtPlato(DtPlato.builder().id(30L).build())
                        .build()))
                .build();
        Map<String, Object> respuestaMp = Map.of(
                "id", "PREF-123",
                "init_point", "https://mercadopago.com/checkout/PREF-123"
        );

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente));
        when(platoRepositorio.buscarPorId(30L)).thenReturn(Optional.of(plato));
        when(promocionRepositorio.buscarPorPlato(30L)).thenReturn(List.of());
        doAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(101L);
            return null;
        }).when(pedidoRepositorio).guardar(any(Pedido.class));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(respuestaMp, HttpStatus.OK));

        Pedido pedido = pedidoService.realizarPedido(solicitud);

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Pendiente);
        assertThat(pedido.getMpPreferenciaId()).isEqualTo("PREF-123");
        assertThat(pedido.getMpInitPoint()).isEqualTo("https://mercadopago.com/checkout/PREF-123");
        verify(pedidoRepositorio).actualizarDatosMp(101L, "PREF-123", "https://mercadopago.com/checkout/PREF-123");
        // A diferencia del pago en efectivo, con Mercado Pago el local todavía NO se notifica:
        // recién se entera cuando procesarPagoConfirmado confirme el pago por webhook.
        verifyNoInteractions(notificacionPedidoService);
    }

    @Test
    void reintentarPagoGeneraNuevaPreferenciaCuandoPedidoPendienteSinAcreditar() {
        Cliente cliente = Cliente.builder().id(20L).email("ana@test.com").activo(true).build();
        Local local = Local.builder().id(10L).nombre("La Cocina").estaAbierto(true).build();
        Pedido pedido = Pedido.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .pagado(false)
                .medioDePago("Mercado Pago")
                .cliente(cliente)
                .local(local)
                .build();
        DetallePedido detalle = DetallePedido.builder()
                .id(1L)
                .cantidad(1)
                .precioUnitario(100.0)
                .subtotal(100.0)
                .plato(Plato.builder().id(30L).nombre("Milanesa").build())
                .pedido(pedido)
                .build();
        Map<String, Object> respuestaMp = Map.of(
                "id", "PREF-456",
                "init_point", "https://mercadopago.com/checkout/PREF-456"
        );

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.buscarPorId(77L)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepositorio.buscarPorPedido(77L)).thenReturn(List.of(detalle));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(respuestaMp, HttpStatus.OK));

        Pedido resultado = pedidoService.reintentarPago("ana@test.com", 77L);

        assertThat(resultado.getMpPreferenciaId()).isEqualTo("PREF-456");
        assertThat(resultado.getMpInitPoint()).isEqualTo("https://mercadopago.com/checkout/PREF-456");
        verify(pedidoRepositorio).actualizarDatosMp(77L, "PREF-456", "https://mercadopago.com/checkout/PREF-456");
    }

    @Test
    void reintentarPagoRechazaSiElPedidoNoEsPropio() {
        Cliente cliente = Cliente.builder().id(20L).email("ana@test.com").activo(true).build();
        Cliente otroCliente = Cliente.builder().id(99L).email("otro@test.com").activo(true).build();
        Pedido pedidoAjeno = Pedido.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .pagado(false)
                .medioDePago("Mercado Pago")
                .cliente(otroCliente)
                .build();

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.buscarPorId(77L)).thenReturn(Optional.of(pedidoAjeno));

        assertThatThrownBy(() -> pedidoService.reintentarPago("ana@test.com", 77L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No tiene permisos para operar sobre un pedido de otro cliente.");

        verifyNoInteractions(restTemplate, detallePedidoRepositorio);
    }

    @Test
    void reintentarPagoRechazaSiElPedidoEsEnEfectivo() {
        Cliente cliente = Cliente.builder().id(20L).email("ana@test.com").activo(true).build();
        Pedido pedidoEfectivo = Pedido.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .pagado(false)
                .medioDePago("EFECTIVO")
                .cliente(cliente)
                .build();

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.buscarPorId(77L)).thenReturn(Optional.of(pedidoEfectivo));

        assertThatThrownBy(() -> pedidoService.reintentarPago("ana@test.com", 77L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se puede reintentar el pago de pedidos pendientes de Mercado Pago sin acreditar.");

        verifyNoInteractions(restTemplate, detallePedidoRepositorio);
    }

    @Test
    void reintentarPagoRechazaSiElPedidoYaFuePagado() {
        Cliente cliente = Cliente.builder().id(20L).email("ana@test.com").activo(true).build();
        Pedido pedidoPagado = Pedido.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .pagado(true)
                .medioDePago("Mercado Pago")
                .cliente(cliente)
                .build();

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.buscarPorId(77L)).thenReturn(Optional.of(pedidoPagado));

        assertThatThrownBy(() -> pedidoService.reintentarPago("ana@test.com", 77L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se puede reintentar el pago de pedidos pendientes de Mercado Pago sin acreditar.");

        verifyNoInteractions(restTemplate, detallePedidoRepositorio);
    }

    @Test
    void reintentarPagoRechazaSiElPedidoNoTieneDetallesAsociados() {
        Cliente cliente = Cliente.builder().id(20L).email("ana@test.com").activo(true).build();
        Local local = Local.builder().id(10L).nombre("La Cocina").estaAbierto(true).build();
        Pedido pedido = Pedido.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .pagado(false)
                .medioDePago("Mercado Pago")
                .cliente(cliente)
                .local(local)
                .build();

        when(usuarioRepositorio.buscarPorEmail("ana@test.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.buscarPorId(77L)).thenReturn(Optional.of(pedido));
        when(detallePedidoRepositorio.buscarPorPedido(77L)).thenReturn(List.of());

        assertThatThrownBy(() -> pedidoService.reintentarPago("ana@test.com", 77L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No se pudo reintentar el pago porque el pedido no tiene detalles asociados.");

        verifyNoInteractions(restTemplate);
    }

    @Test
    void marcarPedidosComoEntregadosActualizaLosPedidosVencidos() {
        Pedido pedidoVencido1 = Pedido.builder().id(1L).estado(EstadoPedido.Confirmado).build();
        Pedido pedidoVencido2 = Pedido.builder().id(2L).estado(EstadoPedido.Confirmado).build();
        when(pedidoRepositorio.buscarEnCaminoVencidos(any(LocalDateTime.class)))
                .thenReturn(List.of(pedidoVencido1, pedidoVencido2));

        pedidoService.marcarPedidosComoEntregados();

        assertThat(pedidoVencido1.getEstado()).isEqualTo(EstadoPedido.Entregado);
        assertThat(pedidoVencido2.getEstado()).isEqualTo(EstadoPedido.Entregado);
        verify(pedidoRepositorio).actualizar(pedidoVencido1);
        verify(pedidoRepositorio).actualizar(pedidoVencido2);
        // El marcado automático no envía ninguna notificación, a ninguna de las partes.
        verifyNoInteractions(notificacionPedidoService);
    }

    @Test
    void marcarPedidosComoEntregadosNoHaceNadaSiNoHayPedidosVencidos() {
        when(pedidoRepositorio.buscarEnCaminoVencidos(any(LocalDateTime.class))).thenReturn(List.of());

        pedidoService.marcarPedidosComoEntregados();

        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
    }

    @Test
    void cancelarPedidosMercadoPagoAbandonadosCancelaSinNotificarANadie() {
        Pedido pedidoAbandonado = Pedido.builder()
                .id(55L)
                .estado(EstadoPedido.Pendiente)
                .medioDePago("Mercado Pago")
                .pagado(false)
                .build();
        when(pedidoRepositorio.buscarPendientesMercadoPagoVencidos(any(LocalDateTime.class)))
                .thenReturn(List.of(pedidoAbandonado));

        pedidoService.cancelarPedidosMercadoPagoAbandonados();

        assertThat(pedidoAbandonado.getEstado()).isEqualTo(EstadoPedido.Cancelado);
        verify(pedidoRepositorio).actualizar(pedidoAbandonado);
        // A diferencia de cancelarPedidoDeCliente (cancelación manual), acá no se notifica
        // al local: nunca llegó a enterarse de que el pedido existía.
        verifyNoInteractions(notificacionPedidoService);
    }

    @Test
    void cancelarPedidosMercadoPagoAbandonadosNoHaceNadaSiNoHayPedidosVencidos() {
        when(pedidoRepositorio.buscarPendientesMercadoPagoVencidos(any(LocalDateTime.class))).thenReturn(List.of());

        pedidoService.cancelarPedidosMercadoPagoAbandonados();

        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
    }

    private Pedido pedidoPendiente() {
        return Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.now())
                .total(450.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("EFECTIVO")
                .pagoSimulado(false)
                .pagado(false)
                .estado(EstadoPedido.Pendiente)
                .local(Local.builder()
                        .id(10L)
                        .nombre("La Cocina")
                        .email("local@test.com")
                        .estaAbierto(true)
                        .build())
                .cliente(Cliente.builder()
                        .id(20L)
                        .email("ana@test.com")
                        .nombre("Ana")
                        .apellido("Perez")
                        .activo(true)
                        .build())
                .build();
    }
}
