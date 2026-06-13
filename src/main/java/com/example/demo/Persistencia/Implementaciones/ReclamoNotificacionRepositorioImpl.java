package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Persistencia.Repositorios.ReclamoNotificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReclamoNotificacionRepositorioImpl implements ReclamoNotificacionRepositorio {

    private final JdbcTemplate  jdbcTemplate;

    public ReclamoNotificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardar(Long idNotificion, Long idPedido) {
        jdbcTemplate.update("INSERT INTO reclamo_notificacion (idnotificacion, idreclamo)  VALUES (?, ?)",idNotificion,idPedido);
    }

    @Override
    public Long obtenerReclamo(Long idNotificion) {
        return jdbcTemplate.queryForObject("SELECT idreclamo FROM reclamo_notificacion WHERE idnotificacion = ?",new Object[]{idNotificion},Long.class);
    }


}
