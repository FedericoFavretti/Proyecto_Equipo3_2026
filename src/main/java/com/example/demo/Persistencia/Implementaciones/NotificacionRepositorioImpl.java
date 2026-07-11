package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
        jdbcTemplate.update(connection -> { PreparedStatement ps = connection.prepareStatement( "INSERT INTO Notificacion (tipo, mensaje, canal, leida, fecha, destinatarioTipo, destinatarioId) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new String[]{"id"}
        );
            ps.setString(1, notificacion.getTipo().toString());
            ps.setString(2, notificacion.getMensaje());
            ps.setString(3, notificacion.getCanal().toString());
            ps.setBoolean(4, notificacion.getLeida());
            ps.setTimestamp(5, Timestamp.valueOf(notificacion.getFecha()));
            ps.setString(6, notificacion.getDestinatarioTipo() != null ? notificacion.getDestinatarioTipo().toString() : null);
            if (notificacion.getDestinatarioId() != null) {
                ps.setLong(7, notificacion.getDestinatarioId());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        Long idNotificacion = keyHolder.getKey().longValue();
        notificacion.setId(idNotificacion);
        if (notificacion.getTipo().equals(TipoNotificacion.Reclamo)) {
            reclamoNotificacionRepositorio.guardar(idNotificacion, notificacion.getReclamo().getId());
        } else if (notificacion.getTipo().equals(TipoNotificacion.Pedido)) {
            pedidoNotificacionRepositorio.guardar(idNotificacion, notificacion.getPedido().getId());
        }
    }

    @Override
    public void actualizar(Notificacion notificacion) {
        jdbcTemplate.update("UPDATE Notificacion SET tipo = ?, mensaje = ?, canal = ?, leida = ?, fecha = ?, destinatarioTipo = ?, destinatarioId = ? WHERE id = ?",
                notificacion.getTipo().toString(),
                notificacion.getMensaje(),
                notificacion.getCanal().toString(),
                notificacion.getLeida(),
                Timestamp.valueOf(notificacion.getFecha()),
                notificacion.getDestinatarioTipo() != null ? notificacion.getDestinatarioTipo().toString() : null,
                notificacion.getDestinatarioId(),
                notificacion.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Notificacion WHERE id = ?", id);
    }

    @Override
    public List<Notificacion> listarPorDestinatario(TipoDestinatario destinatarioTipo, Long destinatarioId) {
        return jdbcTemplate.query(
                "SELECT * FROM Notificacion WHERE destinatarioTipo = ? AND destinatarioId = ? AND canal = ? ORDER BY fecha DESC",
                (rs, row) -> mapearNotificacion(rs),
                destinatarioTipo.toString(), destinatarioId, CanalNotificacion.Web.toString()
        );
    }

    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {
        TipoNotificacion tipo = TipoNotificacion.valueOf(rs.getString("tipo"));
        String destinatarioTipoStr = rs.getString("destinatarioTipo");
        TipoDestinatario destinatarioTipo =
                destinatarioTipoStr != null ? TipoDestinatario.valueOf(destinatarioTipoStr) : null;
        Long destinatarioId = (Long) rs.getObject("destinatarioId");

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
                    .destinatarioTipo(destinatarioTipo)
                    .destinatarioId(destinatarioId)
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
                    .destinatarioTipo(destinatarioTipo)
                    .destinatarioId(destinatarioId)
                    .build();
        } else if (tipo == TipoNotificacion.Local) {
            return Notificacion.builder()
                    .id(rs.getLong("id"))
                    .tipo(TipoNotificacion.valueOf(rs.getString("tipo")))
                    .mensaje(rs.getString("mensaje"))
                    .canal(CanalNotificacion.valueOf(rs.getString("canal")))
                    .leida(rs.getBoolean("leida"))
                    .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                    .reclamo(null)
                    .pedido(null)
                    .destinatarioTipo(destinatarioTipo)
                    .destinatarioId(destinatarioId)
                    .build();
        }
        return null;
    }
}