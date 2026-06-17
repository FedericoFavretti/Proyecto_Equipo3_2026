package com.example.demo.Persistencia.Implementaciones;


import com.example.demo.Persistencia.Repositorios.TokenBlacklistRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class TokenBlacklistRepositorioImpl implements TokenBlacklistRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public TokenBlacklistRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void agregar(String token, LocalDateTime expiracion) {
        jdbcTemplate.update(
                "INSERT INTO token_blacklist (token, expiracion) VALUES (?, ?) ON CONFLICT DO NOTHING",
                token, expiracion
        );
    }

    public boolean estaEnBlacklist(String token) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM token_blacklist WHERE token = ?",
                Integer.class, token
        );
        return count != null && count > 0;
    }

    public void limpiarExpirados() {
        jdbcTemplate.update(
                "DELETE FROM token_blacklist WHERE expiracion < ?",
                LocalDateTime.now()
        );
    }
}