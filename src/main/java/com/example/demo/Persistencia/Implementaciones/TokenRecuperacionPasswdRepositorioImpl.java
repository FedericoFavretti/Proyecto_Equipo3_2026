package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.TokenRecuperacionPasswd;
import com.example.demo.Persistencia.Repositorios.TokenRecuperacionPasswdRepositorio;
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
public class TokenRecuperacionPasswdRepositorioImpl implements TokenRecuperacionPasswdRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public TokenRecuperacionPasswdRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardar(TokenRecuperacionPasswd tokenRecuperacionPasswd) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO token_recuperacion_passwd (id_usuario, token_hash, fecha_creacion, fecha_expiracion, fecha_consumo, usado) VALUES (?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, tokenRecuperacionPasswd.getIdUsuario());
            ps.setString(2, tokenRecuperacionPasswd.getTokenHash());
            ps.setTimestamp(3, Timestamp.valueOf(tokenRecuperacionPasswd.getFechaCreacion()));
            ps.setTimestamp(4, Timestamp.valueOf(tokenRecuperacionPasswd.getFechaExpiracion()));
            if (tokenRecuperacionPasswd.getFechaConsumo() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(tokenRecuperacionPasswd.getFechaConsumo()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setBoolean(6, tokenRecuperacionPasswd.getUsado());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            tokenRecuperacionPasswd.setId(id.longValue());
        }
    }

    @Override
    public void invalidarActivosPorUsuario(Long idUsuario) {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(
                "UPDATE token_recuperacion_passwd SET usado = true, fecha_consumo = ? WHERE id_usuario = ? AND usado = false AND fecha_expiracion >= ?",
                ahora,
                idUsuario,
                ahora
        );
    }

    @Override
    public Optional<TokenRecuperacionPasswd> buscarVigentePorTokenHash(String tokenHash) {
        List<TokenRecuperacionPasswd> resultados = jdbcTemplate.query(
                "SELECT * FROM token_recuperacion_passwd WHERE token_hash = ? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> mapear(rs),
                tokenHash
        );
        return resultados.stream().findFirst();
    }

    @Override
    public void marcarComoUsado(Long id, LocalDateTime fechaConsumo) {
        jdbcTemplate.update(
                "UPDATE token_recuperacion_passwd SET usado = true, fecha_consumo = ? WHERE id = ?",
                Timestamp.valueOf(fechaConsumo),
                id
        );
    }

    private TokenRecuperacionPasswd mapear(ResultSet rs) throws SQLException {
        Timestamp fechaConsumo = rs.getTimestamp("fecha_consumo");
        return TokenRecuperacionPasswd.builder()
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
