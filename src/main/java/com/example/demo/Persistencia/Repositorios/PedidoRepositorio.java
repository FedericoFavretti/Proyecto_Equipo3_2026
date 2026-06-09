package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Enums.EstadoPedido;

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
    boolean existePedidoPendientePorLocal(Long idLocal);
}
