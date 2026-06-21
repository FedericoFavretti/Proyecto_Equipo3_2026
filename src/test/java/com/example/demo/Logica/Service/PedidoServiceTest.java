package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtDetallePedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.DetallePedidoMapper;
import com.example.demo.Logica.Mappers.PedidoListadoMapper;
import com.example.demo.Logica.Mappers.PedidoMapper;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private FacturaService facturaService;
    @Mock
    private PagoSimuladoService pagoSimuladoService;
    @Mock
    private NotificacionPedidoService notificacionPedidoService;
    @Mock
    private PedidoListadoMapper pedidoListadoMapper;
    @Mock
    private DetallePedidoMapper detallePedidoMapper;
    @Mock
    private PedidoMapper pedidoMapper;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepositorio,
                clienteRepositorio,
                localRepositorio,
                detallePedidoRepositorio,
                platoRepositorio,
                facturaService,
                pagoSimuladoService,
                notificacionPedidoService,
                pedidoListadoMapper,
                detallePedidoMapper,
                pedidoMapper
        );
    }

    @Test
    void confirmarPedidoRechazaCuandoNoSeIngresaTiempoEstimado() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarPedido(44L, null))
                .isInstanceOf(IllegalArgumentException.class)
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
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se pudo procesar el pago. El pedido no ha sido confirmado. Por favor, inténtelo nuevamente.");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Pendiente);
        assertThat(pedido.getTiempoEstEntrega()).isEqualTo(Duration.ofMinutes(25));
        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(facturaService, notificacionPedidoService);
    }

    @Test
    void confirmarPedidoConfirmaGeneraFacturaYNotifica() {
        Pedido pedido = pedidoPendiente();
        Factura factura = Factura.builder()
                .id(500L)
                .numero("FAC-44")
                .monto(pedido.getTotal())
                .archivoPdf("facturas/pedido-44.pdf")
                .pedido(pedido)
                .build();

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));
        when(pagoSimuladoService.procesarPago(any(Pedido.class))).thenReturn(true);
        when(facturaService.generarYGuardarFactura(any(Pedido.class))).thenReturn(factura);

        Pedido confirmado = pedidoService.confirmarPedido(44L, 35L);

        assertThat(confirmado.getEstado()).isEqualTo(EstadoPedido.Confirmado);
        assertThat(confirmado.getPagoSimulado()).isTrue();
        assertThat(confirmado.getTiempoEstEntrega()).isEqualTo(Duration.ofMinutes(35));
        verify(pedidoRepositorio).actualizar(eq(pedido));
        verify(facturaService).generarYGuardarFactura(eq(pedido));
        verify(notificacionPedidoService).notificarConfirmacion(eq(pedido), eq(factura));
    }

    @Test
    void rechazarPedidoRechazaCuandoNoSeIngresaMotivo() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.rechazarPedido(44L, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe seleccionar o escribir un motivo de rechazo antes de continuar.");

        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(notificacionPedidoService);
    }

    @Test
    void rechazarPedidoRechazaCuandoPedidoNoExiste() {
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.rechazarPedido(44L, "Sin disponibilidad"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pedido no encontrado");

        verify(pedidoRepositorio, never()).actualizar(any(Pedido.class));
        verifyNoInteractions(notificacionPedidoService);
    }

    @Test
    void rechazarPedidoMarcaEstadoYNotificaConMotivo() {
        Pedido pedido = pedidoPendiente();
        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        pedidoService.rechazarPedido(44L, " Sin disponibilidad ");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.Rechazado);
        verify(pedidoRepositorio).actualizar(eq(pedido));
        verify(notificacionPedidoService).notificarRechazo(eq(pedido), eq("Sin disponibilidad"));
    }

    @Test
    void realizarPedidoRechazaCuandoNoHayPlatos() {
        DtPedidoConDetalles solicitud = pedidoSinDetalles();

        assertThatThrownBy(() -> pedidoService.realizarPedido(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe agregar al menos un plato para realizar el pedido.");

        verifyNoInteractions(localRepositorio, clienteRepositorio, platoRepositorio, pedidoRepositorio, detallePedidoRepositorio);
    }

    @Test
    void realizarPedidoRechazaCantidadInvalida() {
        DtPedidoConDetalles solicitud = pedidoValido();
        solicitud.getDetalles().getFirst().setCantidad(0);

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));

        assertThatThrownBy(() -> pedidoService.realizarPedido(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La cantidad debe ser un número entero mayor a cero.");

        verify(pedidoRepositorio, never()).guardar(any(Pedido.class));
        verify(detallePedidoRepositorio, never()).guardar(any(DetallePedido.class));
    }

    @Test
    void realizarPedidoRecalculaTotalYPersisteCabeceraYDetalles() {
        DtPedidoConDetalles solicitud = pedidoValido();
        solicitud.getDtPedido().setTotal(999.0);

        Local local = localAbierto();
        Cliente cliente = cliente();
        Plato plato = platoDelLocal();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente));
        when(platoRepositorio.buscarPorId(100L)).thenReturn(Optional.of(plato));
        doAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(77L);
            return null;
        }).when(pedidoRepositorio).guardar(any(Pedido.class));

        Pedido pedidoGuardado = pedidoService.realizarPedido(solicitud);

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepositorio).guardar(pedidoCaptor.capture());

        Pedido cabecera = pedidoCaptor.getValue();
        assertThat(cabecera.getEstado()).isEqualTo(EstadoPedido.Pendiente);
        assertThat(cabecera.getTotal()).isEqualTo(30.0);
        assertThat(cabecera.getCliente()).isSameAs(cliente);
        assertThat(cabecera.getLocal()).isSameAs(local);

        ArgumentCaptor<DetallePedido> detalleCaptor = ArgumentCaptor.forClass(DetallePedido.class);
        verify(detallePedidoRepositorio).guardar(detalleCaptor.capture());

        DetallePedido detalle = detalleCaptor.getValue();
        assertThat(detalle.getCantidad()).isEqualTo(2);
        assertThat(detalle.getPrecioUnitario()).isEqualTo(15.0);
        assertThat(detalle.getSubtotal()).isEqualTo(30.0);
        assertThat(detalle.getPlato()).isSameAs(plato);
        assertThat(detalle.getPedido()).isSameAs(cabecera);
        assertThat(detalle.getPedido().getId()).isEqualTo(77L);

        assertThat(pedidoGuardado).isSameAs(cabecera);
        assertThat(pedidoGuardado.getId()).isEqualTo(77L);
    }

    @Test
    void realizarPedidoRechazaPlatoDeOtroLocal() {
        DtPedidoConDetalles solicitud = pedidoValido();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(platoRepositorio.buscarPorId(100L)).thenReturn(Optional.of(platoDeOtroLocal()));

        assertThatThrownBy(() -> pedidoService.realizarPedido(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El plato seleccionado no pertenece al local indicado.");

        verify(pedidoRepositorio, never()).guardar(any(Pedido.class));
        verify(detallePedidoRepositorio, never()).guardar(any(DetallePedido.class));
    }

    @Test
    void listarPedidosRetornaResumenesConFiltro() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .estado(EstadoPedido.Pendiente)
                .fechaDesde(LocalDate.of(2026, 6, 1))
                .fechaHasta(LocalDate.of(2026, 6, 30))
                .ordenarPor("fecha")
                .direccion("desc")
                .build();

        PedidoListadoView view = PedidoListadoView.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .total(30.0)
                .cantidadItems(2)
                .build();

        DtPedidoListadoResponse response = DtPedidoListadoResponse.builder()
                .id(77L)
                .estado(EstadoPedido.Pendiente)
                .total(30.0)
                .cantidadItems(2)
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));
        when(pedidoRepositorio.listarRecibidosPorLocal(10L, filtro)).thenReturn(List.of(view));
        when(pedidoListadoMapper.toResponse(view)).thenReturn(response);

        List<DtPedidoListadoResponse> pedidos = pedidoService.listarPedidos(10L, filtro);

        assertThat(pedidos).containsExactly(response);
    }

    @Test
    void listarPedidosRechazaCampoDeOrdenInvalido() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .ordenarPor("cliente")
                .direccion("desc")
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));

        assertThatThrownBy(() -> pedidoService.listarPedidos(10L, filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El campo de orden no es válido.");
    }

    @Test
    void listarPedidosRechazaRangoDeFechasInvalido() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .fechaDesde(LocalDate.of(2026, 6, 30))
                .fechaHasta(LocalDate.of(2026, 6, 1))
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));

        assertThatThrownBy(() -> pedidoService.listarPedidos(10L, filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La fecha desde no puede ser posterior a la fecha hasta.");
    }

    @Test
    void buscarYListarHistorialPedidosPropiosRetornaResumenesConLocal() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .estado(EstadoPedido.Confirmado)
                .idLocal(10L)
                .ordenarPor("fecha")
                .direccion("desc")
                .build();

        PedidoListadoView view = PedidoListadoView.builder()
                .id(88L)
                .estado(EstadoPedido.Confirmado)
                .total(45.0)
                .localId(10L)
                .localNombre("La Cocina")
                .cantidadItems(3)
                .build();

        DtPedidoListadoResponse response = DtPedidoListadoResponse.builder()
                .id(88L)
                .estado(EstadoPedido.Confirmado)
                .total(45.0)
                .cantidadItems(3)
                .build();

        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(pedidoRepositorio.listarHistorialPorCliente(20L, filtro)).thenReturn(List.of(view));
        when(pedidoListadoMapper.toResponse(view)).thenReturn(response);

        List<DtPedidoListadoResponse> pedidos = pedidoService.buscarYListarHistorialPedidosPropios(20L, filtro);

        assertThat(pedidos).containsExactly(response);
    }

    @Test
    void buscarYListarHistorialPedidosPropiosInformaCuandoNoTienePedidos() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder().build();

        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(pedidoRepositorio.listarHistorialPorCliente(20L, filtro)).thenReturn(List.of());
        when(pedidoRepositorio.existePedidoPorCliente(20L)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.buscarYListarHistorialPedidosPropios(20L, filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!");
    }

    @Test
    void buscarYListarHistorialPedidosPropiosInformaCuandoFiltrosNoTienenResultados() {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .estado(EstadoPedido.Rechazado)
                .build();

        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(pedidoRepositorio.listarHistorialPorCliente(20L, filtro)).thenReturn(List.of());

        assertThatThrownBy(() -> pedidoService.buscarYListarHistorialPedidosPropios(20L, filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontraron pedidos que coincidan con los criterios seleccionados.");
    }

    private DtPedidoConDetalles pedidoSinDetalles() {
        DtLocal local = new DtLocal();
        local.setId(10L);
        DtCliente cliente = new DtCliente();
        cliente.setId(20L);

        return DtPedidoConDetalles.builder()
                .dtPedido(DtPedido.builder()
                        .dtLocal(local)
                        .dtCliente(cliente)
                        .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                        .medioDePago("Efectivo")
                        .build())
                .detalles(List.of())
                .build();
    }

    private DtPedidoConDetalles pedidoValido() {
        DtLocal local = new DtLocal();
        local.setId(10L);
        DtCliente cliente = new DtCliente();
        cliente.setId(20L);

        DtPedido dtPedido = DtPedido.builder()
                .dtLocal(local)
                .dtCliente(cliente)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Efectivo")
                .build();

        DtDetallePedido detalle = DtDetallePedido.builder()
                .cantidad(2)
                .precioUnitario(999.0)
                .subtotal(999.0)
                .dtPlato(DtPlato.builder().id(100L).build())
                .dtPedido(dtPedido)
                .build();

        return DtPedidoConDetalles.builder()
                .dtPedido(dtPedido)
                .detalles(List.of(detalle))
                .build();
    }

    private Local localAbierto() {
        return Local.builder()
                .id(10L)
                .nombre("La Cocina")
                .estaAbierto(true)
                .build();
    }

    private Cliente cliente() {
        return Cliente.builder()
                .id(20L)
                .nombre("Ana")
                .apellido("Pérez")
                .activo(true)
                .build();
    }

    private Plato platoDelLocal() {
        return Plato.builder()
                .id(100L)
                .nombre("Milanesa")
                .precio(15.0)
                .disponible(true)
                .local(Local.builder().id(10L).build())
                .build();
    }

    private Plato platoDeOtroLocal() {
        return Plato.builder()
                .id(100L)
                .nombre("Milanesa")
                .precio(15.0)
                .disponible(true)
                .local(Local.builder().id(99L).build())
                .build();
    }

    private Pedido pedidoPendiente() {
        return Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.now())
                .total(450.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Tarjeta")
                .pagoSimulado(false)
                .estado(EstadoPedido.Pendiente)
                .local(localAbierto())
                .cliente(Cliente.builder()
                        .id(20L)
                        .email("ana@test.com")
                        .nombre("Ana")
                        .apellido("Pérez")
                        .activo(true)
                        .build())
                .build();
    }
}
