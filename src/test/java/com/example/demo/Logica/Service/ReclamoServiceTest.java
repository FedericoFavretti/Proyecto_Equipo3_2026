package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.EstadoReclamo;
import com.example.demo.Logica.Exceptions.AccessDeniedException;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Mappers.PedidoMapper;
import com.example.demo.Logica.Mappers.ReclamoMapper;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReclamoServiceTest {

    @Mock
    private ReclamoRepositorio reclamoRepositorio;
    @Mock
    private PedidoRepositorio pedidoRepositorio;
    @Mock
    private ReclamoMapper reclamoMapper;
    @Mock
    private PedidoMapper pedidoMapper;
    @Mock
    private NotificarReclamoService notificarReclamoService;

    @Test
    void reclamarAceptaPedidoConfirmadoDelClienteSinReclamoPrevio() {
        ReclamoService reclamoService = crearServicio();
        Pedido pedido = pedidoDe("cliente@foodly.com", EstadoPedido.Confirmado);
        DtReclamo dtReclamo = dtReclamoBase(44L);
        DtPedido dtPedido = DtPedido.builder().id(44L).build();
        Reclamo reclamoMapeado = Reclamo.builder().pedido(pedido).build();

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));
        when(reclamoRepositorio.buscarReclamoPorPedido(44L)).thenReturn(Optional.empty());
        when(pedidoMapper.mapearDtPedidoDeClase(pedido)).thenReturn(dtPedido);
        when(reclamoMapper.mapearReclamoDeDt(dtReclamo)).thenReturn(reclamoMapeado);

        reclamoService.reclamar("cliente@foodly.com", dtReclamo);

        assertThat(dtReclamo.getEstado()).isEqualTo(EstadoReclamo.Pendiente);
        assertThat(dtReclamo.getMontoReintegro()).isEqualTo(350.0);
        assertThat(dtReclamo.getDtPedido()).isSameAs(dtPedido);
        assertThat(dtReclamo.getFecha()).isNotNull();
        verify(reclamoRepositorio).guardar(reclamoMapeado);
        verify(notificarReclamoService).notificarReclamo(reclamoMapeado);
    }

    @Test
    void reclamarAceptaPedidoEntregadoDelClienteSinReclamoPrevio() {
        ReclamoService reclamoService = crearServicio();
        Pedido pedido = pedidoDe(45L, "cliente@foodly.com", EstadoPedido.Entregado);
        DtReclamo dtReclamo = dtReclamoBase(45L);
        DtPedido dtPedido = DtPedido.builder().id(45L).build();
        Reclamo reclamoMapeado = Reclamo.builder().pedido(pedido).build();

        when(pedidoRepositorio.buscarPorId(45L)).thenReturn(Optional.of(pedido));
        when(reclamoRepositorio.buscarReclamoPorPedido(45L)).thenReturn(Optional.empty());
        when(pedidoMapper.mapearDtPedidoDeClase(pedido)).thenReturn(dtPedido);
        when(reclamoMapper.mapearReclamoDeDt(dtReclamo)).thenReturn(reclamoMapeado);

        reclamoService.reclamar("cliente@foodly.com", dtReclamo);

        verify(reclamoRepositorio).guardar(reclamoMapeado);
        verify(notificarReclamoService).notificarReclamo(reclamoMapeado);
    }

    @Test
    void reclamarRechazaPedidoQueNoEstaConfirmadoNiEntregado() {
        ReclamoService reclamoService = crearServicio();
        Pedido pedido = pedidoDe("cliente@foodly.com", EstadoPedido.Pendiente);
        DtReclamo dtReclamo = dtReclamoBase(44L);

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> reclamoService.reclamar("cliente@foodly.com", dtReclamo))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se pueden realizar reclamos sobre pedidos confirmados o entregados.");

        verify(reclamoRepositorio, never()).buscarReclamoPorPedido(44L);
        verify(reclamoRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reclamarRechazaPedidoAjeno() {
        ReclamoService reclamoService = crearServicio();
        Pedido pedido = pedidoDe("otra@foodly.com", EstadoPedido.Confirmado);
        DtReclamo dtReclamo = dtReclamoBase(44L);

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> reclamoService.reclamar("cliente@foodly.com", dtReclamo))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No puede realizar reclamos sobre pedidos que no le pertenecen.");

        verify(reclamoRepositorio, never()).buscarReclamoPorPedido(44L);
    }

    @Test
    void reclamarRechazaReclamoDuplicado() {
        ReclamoService reclamoService = crearServicio();
        Pedido pedido = pedidoDe("cliente@foodly.com", EstadoPedido.Confirmado);
        DtReclamo dtReclamo = dtReclamoBase(44L);

        when(pedidoRepositorio.buscarPorId(44L)).thenReturn(Optional.of(pedido));
        when(reclamoRepositorio.buscarReclamoPorPedido(44L)).thenReturn(Optional.of(Reclamo.builder().id(9L).build()));

        assertThatThrownBy(() -> reclamoService.reclamar("cliente@foodly.com", dtReclamo))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Ya existe un reclamo para este pedido.");

        verify(reclamoRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
        verify(notificarReclamoService, never()).notificarReclamo(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reclamarRechazaCuandoFaltaIdPedido() {
        ReclamoService reclamoService = crearServicio();
        DtReclamo dtReclamo = DtReclamo.builder()
                .motivo("El pedido llegó frío")
                .dtPedido(DtPedido.builder().build())
                .build();

        assertThatThrownBy(() -> reclamoService.reclamar("cliente@foodly.com", dtReclamo))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe completar todos los datos.");
    }

    private ReclamoService crearServicio() {
        return new ReclamoService(
                reclamoRepositorio,
                pedidoRepositorio,
                reclamoMapper,
                pedidoMapper,
                notificarReclamoService
        );
    }

    private Pedido pedidoDe(String emailCliente, EstadoPedido estadoPedido) {
        return pedidoDe(44L, emailCliente, estadoPedido);
    }

    private Pedido pedidoDe(Long idPedido, String emailCliente, EstadoPedido estadoPedido) {
        return Pedido.builder()
                .id(idPedido)
                .cliente(Cliente.builder().email(emailCliente).build())
                .estado(estadoPedido)
                .total(350.0)
                .fecha(LocalDateTime.of(2026, 7, 6, 12, 0))
                .build();
    }

    private DtReclamo dtReclamoBase(Long idPedido) {
        return DtReclamo.builder()
                .motivo("El pedido llegó frío")
                .tipoCompensacion("Reintegro")
                .dtPedido(DtPedido.builder().id(idPedido).build())
                .build();
    }
}
