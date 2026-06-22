package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.CodigoVerificacion;
import com.example.demo.Persistencia.Repositorios.CodigoVerificacionRepositorio;
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
public class CodigoVerificacionRepositorioImpl implements CodigoVerificacionRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public CodigoVerificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void guardar(CodigoVerificacion codigoVerificacion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO codigo_verificacion (idusuario, codigo, fechaexpiracion, intentosfallidos, bloqueadohasta, usado) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, codigoVerificacion.getIdUsuario());
            ps.setString(2, codigoVerificacion.getCodigo());
            ps.setTimestamp(3, Timestamp.valueOf(codigoVerificacion.getFechaExpiracion()));
            ps.setInt(4, codigoVerificacion.getIntentosFallidos());
            if (codigoVerificacion.getBloqueadoHasta() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(codigoVerificacion.getBloqueadoHasta()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setBoolean(6, codigoVerificacion.getUsado());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            codigoVerificacion.setId(id.longValue());
        }
    }

    @Override
    public Optional<CodigoVerificacion> buscarVigentePorUsuario(Long idUsuario) {
        List<CodigoVerificacion> resultados = jdbcTemplate.query(
                "SELECT * FROM codigo_verificacion WHERE idusuario = ? AND usado = false ORDER BY id DESC LIMIT 1",
                (rs, row) -> mapearCodigoVerificacion(rs),
                idUsuario
        );
        return resultados.stream().findFirst();
    }

    @Override
    public void actualizar(CodigoVerificacion codigoVerificacion) {
        jdbcTemplate.update(
                "UPDATE codigo_verificacion SET intentosfallidos = ?, bloqueadohasta = ?, usado = ? WHERE id = ?",
                codigoVerificacion.getIntentosFallidos(),
                codigoVerificacion.getBloqueadoHasta() != null ? Timestamp.valueOf(codigoVerificacion.getBloqueadoHasta()) : null,
                codigoVerificacion.getUsado(),
                codigoVerificacion.getId()
        );
    }

    private CodigoVerificacion mapearCodigoVerificacion(ResultSet rs) throws SQLException {
        Timestamp bloqueadoHasta = rs.getTimestamp("bloqueadohasta");
        return CodigoVerificacion.builder()
                .id(rs.getLong("id"))
                .idUsuario(rs.getLong("idusuario"))
                .codigo(rs.getString("codigo"))
                .fechaExpiracion(rs.getTimestamp("fechaexpiracion").toLocalDateTime())
                .intentosFallidos(rs.getInt("intentosfallidos"))
                .bloqueadoHasta(bloqueadoHasta != null ? bloqueadoHasta.toLocalDateTime() : null)
                .usado(rs.getBoolean("usado"))
                .build();
    }
}