package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.TokenRecuperacionPasswd;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenRecuperacionPasswdRepositorioImplTest {

    @Test
    void invalidarActivosPorUsuarioActualizaSoloTokensNoConsumidos() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        TokenRecuperacionPasswdRepositorioImpl repositorio = new TokenRecuperacionPasswdRepositorioImpl(jdbcTemplate);

        repositorio.invalidarActivosPorUsuario(10L);

        verify(jdbcTemplate).update(
                eq("UPDATE token_recuperacion_passwd SET usado = true, fecha_consumo = ? WHERE id_usuario = ? AND usado = false AND fecha_expiracion >= ?"),
                any(Timestamp.class),
                eq(10L),
                any(Timestamp.class)
        );
    }

    @Test
    void marcarComoUsadoActualizaEstadoYFechaConsumo() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        TokenRecuperacionPasswdRepositorioImpl repositorio = new TokenRecuperacionPasswdRepositorioImpl(jdbcTemplate);
        LocalDateTime fechaConsumo = LocalDateTime.of(2026, 6, 28, 18, 0);

        repositorio.marcarComoUsado(55L, fechaConsumo);

        verify(jdbcTemplate).update(
                eq("UPDATE token_recuperacion_passwd SET usado = true, fecha_consumo = ? WHERE id = ?"),
                eq(Timestamp.valueOf(fechaConsumo)),
                eq(55L)
        );
    }

    @Test
    void buscarVigentePorTokenHashMapeaEntidadCompleta() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        TokenRecuperacionPasswdRepositorioImpl repositorio = new TokenRecuperacionPasswdRepositorioImpl(jdbcTemplate);
        ResultSet rs = mock(ResultSet.class);
        LocalDateTime creada = LocalDateTime.of(2026, 6, 28, 17, 0);
        LocalDateTime expira = LocalDateTime.of(2026, 6, 28, 17, 30);

        when(rs.getLong("id")).thenReturn(5L);
        when(rs.getLong("id_usuario")).thenReturn(10L);
        when(rs.getString("token_hash")).thenReturn("hash");
        when(rs.getTimestamp("fecha_creacion")).thenReturn(Timestamp.valueOf(creada));
        when(rs.getTimestamp("fecha_expiracion")).thenReturn(Timestamp.valueOf(expira));
        when(rs.getTimestamp("fecha_consumo")).thenReturn(null);
        when(rs.getBoolean("usado")).thenReturn(false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RowMapper<TokenRecuperacionPasswd>> rowMapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        when(jdbcTemplate.query(
                eq("SELECT * FROM token_recuperacion_passwd WHERE token_hash = ? ORDER BY id DESC LIMIT 1"),
                rowMapperCaptor.capture(),
                eq("hash")
        )).thenAnswer(invocation -> java.util.List.of(rowMapperCaptor.getValue().mapRow(rs, 0)));

        Optional<TokenRecuperacionPasswd> encontrado = repositorio.buscarVigentePorTokenHash("hash");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(5L);
        assertThat(encontrado.get().getIdUsuario()).isEqualTo(10L);
        assertThat(encontrado.get().getTokenHash()).isEqualTo("hash");
        assertThat(encontrado.get().getFechaCreacion()).isEqualTo(creada);
        assertThat(encontrado.get().getFechaExpiracion()).isEqualTo(expira);
        assertThat(encontrado.get().getFechaConsumo()).isNull();
        assertThat(encontrado.get().getUsado()).isFalse();
    }
}
