package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.TokenActivacionCuenta;
import com.example.demo.Persistencia.Repositorios.TokenActivacionCuentaRepositorio;
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
public class TokenActivacionCuentaRepositorioImpl implements TokenActivacionCuentaRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public TokenActivacionCuentaRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardar(TokenActivacionCuenta tokenActivacionCuenta) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO token_activacion_cuenta (id_usuario, token_hash, fecha_creacion, fecha_expiracion, fecha_consumo, usado) VALUES (?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, tokenActivacionCuenta.getIdUsuario());
            ps.setString(2, tokenActivacionCuenta.getTokenHash());
            ps.setTimestamp(3, Timestamp.valueOf(tokenActivacionCuenta.getFechaCreacion()));
            ps.setTimestamp(4, Timestamp.valueOf(tokenActivacionCuenta.getFechaExpiracion()));
            if (tokenActivacionCuenta.getFechaConsumo() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(tokenActivacionCuenta.getFechaConsumo()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setBoolean(6, tokenActivacionCuenta.getUsado());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            tokenActivacionCuenta.setId(id.longValue());
        }
    }

    @Override
    public void invalidarActivosPorUsuario(Long idUsuario) {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
                "UPDATE token_activacion_cuenta SET usado = true, fecha_consumo = ? WHERE id_usuario = ? AND usado = false AND fecha_expiracion >= ?",
                ahora,
                idUsuario,
                ahora
        );
    }

    @Override
    public Optional<TokenActivacionCuenta> buscarVigentePorTokenHash(String tokenHash) {
        List<TokenActivacionCuenta> resultados = jdbcTemplate.query(
                "SELECT * FROM token_activacion_cuenta WHERE token_hash = ? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> mapear(rs),
                tokenHash
        );
        return resultados.stream().findFirst();
    }

    @Override
    public void marcarComoUsado(Long id, LocalDateTime fechaConsumo) {
        jdbcTemplate.update(
                "UPDATE token_activacion_cuenta SET usado = true, fecha_consumo = ? WHERE id = ?",
                Timestamp.valueOf(fechaConsumo),
                id
        );
    }

    private TokenActivacionCuenta mapear(ResultSet rs) throws SQLException {
        Timestamp fechaConsumo = rs.getTimestamp("fecha_consumo");
        return TokenActivacionCuenta.builder()
                .id(rs.getLong("id"))
                .idUsuario(rs.getLong("id_usuario"))
                .tokenHash(rs.getString("token_hash"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .fechaExpiracion(rs.getTimestamp("fecha_expiracion").toLocalDateTime())
                .fechaConsumo(fechaConsumo != null ? fechaConsumo.toLocalDateTime() : null)
                .usado(rs.getBoolean("usado"))
                .build();
    }
}