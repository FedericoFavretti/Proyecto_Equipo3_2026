package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Persistencia.Repositorios.PedidoNotificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoNotificacionRepositorioImpl implements PedidoNotificacionRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public PedidoNotificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void guardar(Long idNotificion, Long idPedido) {
        jdbcTemplate.update("INSERT INTO pedido_notificacion (idnotificacion, idpedido) VALUES (?, ?)", idNotificion,idPedido);
    }

    @Override
    public Long buscarPedido(Long idNotificacion) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT idpedido FROM pedido_notificacion  WHERE idnotificacion = ?",
                    Long.class,
                    idNotificacion
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
