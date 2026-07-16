package com.example.demo.Persistencia.Repositorios;

public interface ReclamoNotificacionRepositorio {

    void guardar(Long idNotificion, Long idReclamo);

    Long obtenerReclamo(Long idNotificion);

}
