package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificacionRepositorioImpl implements NotificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;


    public NotificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Notificacion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Notificacion",
                (rs, row) -> new Notificacion(
                        rs.getLong("id"),
                        TipoNotificacion.valueOf(rs.getString("tipo")),
                        rs.getString("mensaje"),
                        CanalNotificacion.valueOf(rs.getString("canal")),
                        rs.getBoolean("leida"),
                        rs.getDate("fecha"),
                        null,
                        null
                )
        );
    }

    @Override
    public Optional<Notificacion> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Notificacion WHERE id = ?",
                (rs, row) -> new Notificacion(
                        rs.getLong("id"),
                        TipoNotificacion.valueOf(rs.getString("tipo")),
                        rs.getString("mensaje"),
                        CanalNotificacion.valueOf(rs.getString("canal")),
                        rs.getBoolean("leida"),
                        rs.getDate("fecha"),
                        null,
                        null
                ),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Notificacion notificacion) {
        jdbcTemplate.update("INSERT INTO Notificacion (tipo, mensaje, canal, leida, fecha) VALUES (?, ?, ?, ?, ?)",
                notificacion.getTipo().toString(),
                notificacion.getMensaje(),
                notificacion.getCanal().toString(),
                notificacion.getLeida(),
                notificacion.getFecha()
        );
    }

    @Override
    public void actualizar(Notificacion notificacion) {
        jdbcTemplate.update("UPDATE Notificacion SET tipo = ?, mensaje = ?, canal = ?, leida = ?, fecha = ? WHERE id = ?)",
                notificacion.getTipo().toString(),
                notificacion.getMensaje(),
                notificacion.getCanal().toString(),
                notificacion.getLeida(),
                notificacion.getFecha(),
                notificacion.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Notificacion WHERE id = ?", id);
    }
}
