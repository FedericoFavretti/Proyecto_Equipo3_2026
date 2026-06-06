package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepositorio,
                clienteRepositorio,
                localRepositorio,
                detallePedidoRepositorio,
                platoRepositorio
        );
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
}
