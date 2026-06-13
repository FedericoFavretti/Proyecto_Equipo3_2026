package com.example.demo.Persistencia.Repositorios;

public interface ReclamoNotificacionRepositorio {

    public void guardar(Long idNotificion, Long idReclamo);

    public Long obtenerReclamo(Long idNotificion);

}
