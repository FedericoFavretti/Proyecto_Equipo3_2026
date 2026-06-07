package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtDetallePedido;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;

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
                notificacionPedidoService
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
    void realizarPedidoRechazaCuandoNoHayPlatos() {
        DtPedido solicitud = pedidoSinDetalles();

        assertThatThrownBy(() -> pedidoService.realizarPedido(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe agregar al menos un plato para realizar el pedido.");

        verifyNoInteractions(localRepositorio, clienteRepositorio, platoRepositorio, pedidoRepositorio, detallePedidoRepositorio);
    }

    @Test
    void realizarPedidoRechazaCantidadInvalida() {
        DtPedido solicitud = pedidoValido();
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
        DtPedido solicitud = pedidoValido();
        solicitud.setTotal(999.0);

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
        assertThat(pedidoGuardado.getDetalles()).hasSize(1);
    }

    @Test
    void realizarPedidoRechazaPlatoDeOtroLocal() {
        DtPedido solicitud = pedidoValido();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localAbierto()));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(platoRepositorio.buscarPorId(100L)).thenReturn(Optional.of(platoDeOtroLocal()));

        assertThatThrownBy(() -> pedidoService.realizarPedido(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El plato seleccionado no pertenece al local indicado.");

        verify(pedidoRepositorio, never()).guardar(any(Pedido.class));
        verify(detallePedidoRepositorio, never()).guardar(any(DetallePedido.class));
    }

    private DtPedido pedidoSinDetalles() {
        DtLocal local = new DtLocal();
        local.setId(10L);
        DtCliente cliente = new DtCliente();
        cliente.setId(20L);

        return DtPedido.builder()
                .dtLocal(local)
                .dtCliente(cliente)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Efectivo")
                .detalles(List.of())
                .build();
    }

    private DtPedido pedidoValido() {
        DtLocal local = new DtLocal();
        local.setId(10L);
        DtCliente cliente = new DtCliente();
        cliente.setId(20L);

        DtDetallePedido detalle = DtDetallePedido.builder()
                .cantidad(2)
                .precioUnitario(999.0)
                .subtotal(999.0)
                .dtPlato(DtPlato.builder().id(100L).build())
                .build();

        return DtPedido.builder()
                .dtLocal(local)
                .dtCliente(cliente)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Efectivo")
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
                .fecha(new java.util.Date())
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
