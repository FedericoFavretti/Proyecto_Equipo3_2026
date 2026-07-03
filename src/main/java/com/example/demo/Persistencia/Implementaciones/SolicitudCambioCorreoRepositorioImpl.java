package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.SolicitudCambioCorreo;
import com.example.demo.Persistencia.Repositorios.SolicitudCambioCorreoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudCambioCorreoRepositorioImpl implements SolicitudCambioCorreoRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public SolicitudCambioCorreoRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardar(SolicitudCambioCorreo solicitudCambioCorreo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO solicitud_cambio_correo (id_usuario, correo_nuevo, token_hash, fecha_creacion, fecha_expiracion, fecha_consumo, usado) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, solicitudCambioCorreo.getIdUsuario());
            ps.setString(2, solicitudCambioCorreo.getCorreoNuevo());
            ps.setString(3, solicitudCambioCorreo.getTokenHash());
            ps.setTimestamp(4, Timestamp.valueOf(solicitudCambioCorreo.getFechaCreacion()));
            ps.setTimestamp(5, Timestamp.valueOf(solicitudCambioCorreo.getFechaExpiracion()));
            if (solicitudCambioCorreo.getFechaConsumo() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(solicitudCambioCorreo.getFechaConsumo()));
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }
            ps.setBoolean(7, solicitudCambioCorreo.getUsado());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            solicitudCambioCorreo.setId(id.longValue());
        }
    }

    @Override
    public void invalidarActivasPorUsuario(Long idUsuario) {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
                "UPDATE solicitud_cambio_correo SET usado = true, fecha_consumo = ? WHERE id_usuario = ? AND usado = false AND fecha_expiracion >= ?",
                ahora,
                idUsuario,
                ahora
        );
    }

    @Override
    public Optional<SolicitudCambioCorreo> buscarVigentePorTokenHash(String tokenHash) {
        List<SolicitudCambioCorreo> resultados = jdbcTemplate.query(
                "SELECT * FROM solicitud_cambio_correo WHERE token_hash = ? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> mapear(rs),
                tokenHash
        );
        return resultados.stream().findFirst();
    }

    @Override
    public void marcarComoUsada(Long id, LocalDateTime fechaConsumo) {
        jdbcTemplate.update(
                "UPDATE solicitud_cambio_correo SET usado = true, fecha_consumo = ? WHERE id = ?",
                Timestamp.valueOf(fechaConsumo),
                id
        );
    }

    private SolicitudCambioCorreo mapear(ResultSet rs) throws SQLException {
        Timestamp fechaConsumo = rs.getTimestamp("fecha_consumo");
        return SolicitudCambioCorreo.builder()
                .id(rs.getLong("id"))
                .idUsuario(rs.getLong("id_usuario"))
                .correoNuevo(rs.getString("correo_nuevo"))
                .tokenHash(rs.getString("token_hash"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .fechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime())
                .fechaConsumo(fechaConsumo != null ? fechaConsumo.toLocalDateTime() : null)
                .usado(rs.getBoolean("usado"))
                .build();
    }
}
