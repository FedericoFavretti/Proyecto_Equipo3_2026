package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.DeviceToken;
import com.example.demo.Persistencia.Repositorios.DeviceTokenRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DeviceTokenRepositorioImpl implements DeviceTokenRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public DeviceTokenRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardarOActualizar(DeviceToken token) {

        int updated = jdbcTemplate.update(
                "UPDATE device_tokens SET activo = true, fecha_registro = ? WHERE usuario_id = ? AND token = ?",
                Timestamp.valueOf(LocalDateTime.now()), token.getUsuarioId(), token.getToken()
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO device_tokens (usuario_id, token, plataforma, fecha_registro, activo) VALUES (?, ?, ?, ?, true)",
                    token.getUsuarioId(), token.getToken(), token.getPlataforma(),
                    Timestamp.valueOf(LocalDateTime.now())
            );
        }
    }

    @Override
    public List<DeviceToken> buscarActivosPorUsuario(Long usuarioId) {
        return jdbcTemplate.query(
                "SELECT * FROM device_tokens WHERE usuario_id = ? AND activo = true",
                (rs, row) -> mapear(rs),
                usuarioId
        );
    }

    @Override
    public void desactivarPorToken(String token) {
        jdbcTemplate.update("UPDATE device_tokens SET activo = false WHERE token = ?", token);
    }

    @Override
    public void desactivarPorUsuario(Long usuarioId) {
        jdbcTemplate.update("UPDATE device_tokens SET activo = false WHERE usuario_id = ?", usuarioId);
    }

    private DeviceToken mapear(ResultSet rs) throws SQLException {
        return DeviceToken.builder()
                .id(rs.getLong("id"))
                .usuarioId(rs.getLong("usuario_id"))
                .token(rs.getString("token"))
                .plataforma(rs.getString("plataforma"))
                .fechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime())
                .activo(rs.getBoolean("activo"))
                .build();
    }
}
