package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Record.PlatoVendidoEstadisticaProjection;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositorio {
    List<Pedido> listarTodos();
    Optional<Pedido> buscarPorId(Long id);
    void guardar(Pedido pedido);
    void actualizar(Pedido pedido);
    void eliminar(Long id);
    void actualizarDatosMp(Long pedidoId, String mpPreferenciaId, String mpInitPoint);
    void actualizarPago(Long pedidoId, Boolean pagoSimulado, EstadoPedido estado);
    List<Pedido> listarPorLocal(Long idLocal);
    List<PedidoListadoView> listarRecibidosPorLocal(Long idLocal, DtPedidoListadoFiltro filtro);
    List<PedidoListadoView> listarHistorialPorCliente(Long idCliente, DtPedidoListadoFiltro filtro);
    boolean existePedidoPorCliente(Long idCliente);
    boolean existePedidoDeClienteEnLocal(Long idCliente, Long idLocal);
    boolean existePedidoActivoPorCliente(Long idCliente);
    boolean existePedidoPendientePorLocal(Long idLocal);
    boolean existePedidoValidoParaEstadisticasEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva);
    List<PlatoVendidoEstadisticaProjection> obtenerPlatosMasPedidosEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva, int limite);
    List<PlatoVendidoEstadisticaProjection> obtenerVentasPorPlatoEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva);
    Double obtenerVentasParaEstadisticasEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva);
    List<Pedido> buscarEnCaminoVencidos(LocalDateTime ahora);
    void marcarPagoAprobado(Long pedidoId);
    List<Pedido> buscarPendientesMercadoPagoVencidos(LocalDateTime limite);
}