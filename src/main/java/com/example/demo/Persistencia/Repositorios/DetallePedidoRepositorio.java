package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.DetallePedido;

import java.util.List;
import java.util.Optional;

public interface DetallePedidoRepositorio {
    List<DetallePedido> listarTodos();
    Optional<DetallePedido> buscarPorId(long id);
    void guardar(DetallePedido detallePedido);
    void actualizar(DetallePedido detallePedido);
    void eliminar(long id);
}
