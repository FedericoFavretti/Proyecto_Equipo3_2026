package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificacionRepositorioImpl implements NotificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;
    private final ReclamoNotificacionRepositorio reclamoNotificacionRepositorio;
    private final PedidoNotificacionRepositorio pedidoNotificacionRepositorio;
    private final ReclamoRepositorio reclamoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public NotificacionRepositorioImpl(JdbcTemplate jdbcTemplate, ReclamoNotificacionRepositorio reclamoNotificacionRepositorio,  PedidoNotificacionRepositorio pedidoNotificacionRepositorio, ReclamoRepositorio reclamoRepositorio, PedidoRepositorio pedidoRepositorio) {
        this.jdbcTemplate = jdbcTemplate;
        this.reclamoNotificacionRepositorio = reclamoNotificacionRepositorio;
        this.pedidoNotificacionRepositorio = pedidoNotificacionRepositorio;
        this.reclamoRepositorio = reclamoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    @Override
    public List<Notificacion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Notificacion",
                (rs, row) -> mapearNotificacion(rs)
        );
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM Notificacion WHERE id = ?",
                (rs, row) -> mapearNotificacion(rs),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Notificacion notificacion) {
        KeyHolder keyHolder =  new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> { PreparedStatement ps = connection.prepareStatement( "INSERT INTO Notificacion (tipo, mensaje, canal, leida, fecha) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"}
                );
                notificacion.getTipo().toString();
                notificacion.getMensaje();
                notificacion.getCanal().toString();
                notificacion.getLeida();
                notificacion.getFecha();
                return ps;
        }, keyHolder);
        Long idNotificacion = keyHolder.getKey().longValue();
        if (notificacion.getTipo().equals(TipoNotificacion.Reclamo)) {
            reclamoNotificacionRepositorio.guardar(idNotificacion, notificacion.getReclamo().getId());
        } else if (notificacion.getTipo().equals(TipoNotificacion.Pedido)) {
            pedidoNotificacionRepositorio.guardar(idNotificacion, notificacion.getPedido().getId());
        }
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
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Notificacion WHERE id = ?", id);
    }

    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {
        TipoNotificacion tipo = TipoNotificacion.valueOf(rs.getString("tipo"));

        if (tipo == TipoNotificacion.Reclamo) {
            Long idReclamo = reclamoNotificacionRepositorio.obtenerReclamo(rs.getLong("id"));
            Reclamo reclamo = reclamoRepositorio.buscarPorId(idReclamo).orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
            return Notificacion.builder()
                    .id(rs.getLong("id"))
                    .tipo(TipoNotificacion.valueOf(rs.getString("tipo")))
                    .mensaje(rs.getString("mensaje"))
                    .canal(CanalNotificacion.valueOf(rs.getString("canal")))
                    .leida(rs.getBoolean("leida"))
                    .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                    .reclamo(reclamo)
                    .pedido(null)
                    .build();
        } else if (tipo == TipoNotificacion.Pedido) {
            Long idPedido = pedidoNotificacionRepositorio.buscarPedido(rs.getLong("id"));
            Pedido pedido = pedidoRepositorio.buscarPorId(idPedido).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            return Notificacion.builder()
                    .id(rs.getLong("id"))
                    .tipo(TipoNotificacion.valueOf(rs.getString("tipo")))
                    .mensaje(rs.getString("mensaje"))
                    .canal(CanalNotificacion.valueOf(rs.getString("canal")))
                    .leida( rs.getBoolean("leida"))
                    .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                    .reclamo(null)
                    .pedido(pedido)
                    .build();
        }
        return null;
    }
}
