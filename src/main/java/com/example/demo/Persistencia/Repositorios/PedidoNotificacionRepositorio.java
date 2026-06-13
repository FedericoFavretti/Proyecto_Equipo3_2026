package com.example.demo.Persistencia.Repositorios;

public interface PedidoNotificacionRepositorio {

    public void guardar(Long idNotificion, Long idPedido);

    public Long buscarPedido(Long idNotificacion);
}
