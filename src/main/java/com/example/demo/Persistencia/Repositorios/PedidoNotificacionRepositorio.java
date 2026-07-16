package com.example.demo.Persistencia.Repositorios;

public interface PedidoNotificacionRepositorio {

    void guardar(Long idNotificion, Long idPedido);

    Long buscarPedido(Long idNotificacion);
}
