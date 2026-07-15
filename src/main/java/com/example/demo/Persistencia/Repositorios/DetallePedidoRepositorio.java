package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.DetallePedido;

import java.util.List;
import java.util.Optional;

public interface DetallePedidoRepositorio {
    List<DetallePedido> listarTodos();
    Optional<DetallePedido> buscarPorId(Long id);
    void guardar(DetallePedido detallePedido);
    void actualizar(DetallePedido detallePedido);
    void eliminar(Long id);
    List<DetallePedido> buscarPorPedido(Long idPedido);
    boolean platoTienePedidosAsociados(Long idPlato);
    Long buscarPorPlato(Long idPlato);
}
