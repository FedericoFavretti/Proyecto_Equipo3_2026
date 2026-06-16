package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;

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
    boolean existePedidoPendientePorLocal(Long idLocal);
}

