package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositorio {
    List<Pedido> listarTodos();
    Optional<Pedido> buscarPorId(long id);
    void guardar(Pedido pedido);
    void actualizar(Pedido pedido);
    void eliminar(long id);
    List<Pedido> listarPorLocal(long idLocal);
}
