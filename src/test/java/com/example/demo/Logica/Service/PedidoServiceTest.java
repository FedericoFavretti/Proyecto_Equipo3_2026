package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.PagoRechazadoException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.PedidoListadoMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private UsuarioRepositorio usuarioRepositorio;

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
                usuarioRepositorio
        );
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

    private Pedido pedidoPendiente() {
        return Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.now())
                .total(450.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("Tarjeta")
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
