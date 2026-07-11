package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Persistencia.Repositorios.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class CalificacionRepositorioImpl implements CalificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;
    private final ClienteRepositorio clienteRepositorio;
    private final LocalRepositorio localRepositorio;

    public CalificacionRepositorioImpl(JdbcTemplate jdbcTemplate, ClienteRepositorio clienteRepositorio, LocalRepositorio localRepositorio) {
        this.jdbcTemplate = jdbcTemplate;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
    }

    @Override
    public List<Calificacion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Calificacion",
                (rs, row)-> calificacionMapper(rs, row)
        );
    }

    @Override
    public Optional<Calificacion> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM Calificacion WHERE id = ?",
                (rs, row) -> calificacionMapper(rs, row),
                id
        ).stream().findFirst();
    }

    @Override
    public List<Calificacion> buscarPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(id -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "SELECT * FROM Calificacion WHERE id IN (" + placeholders + ")";
        return jdbcTemplate.query(sql, (rs, row) -> calificacionMapper(rs, row), ids.toArray());
    }

    @Override
    public void guardar(Calificacion calificacion) {
        validarClienteYLocal(calificacion);

        Long idClienteEmisor = null;
        Long idLocalReceptor = null;
        Long idLocalEmisor = null;
        Long idClienteReceptor = null;

        if (calificacion.getTipo() == TipoCalificacion.Cliente_a_local) {
            idClienteEmisor = calificacion.getCliente().getId();
            idLocalReceptor = calificacion.getLocal().getId();
        } else {
            idLocalEmisor = calificacion.getLocal().getId();
            idClienteReceptor = calificacion.getCliente().getId();
        }

        Long fIdClienteEmisor = idClienteEmisor;
        Long fIdLocalReceptor = idLocalReceptor;
        Long fIdLocalEmisor = idLocalEmisor;
        Long fIdClienteReceptor = idClienteReceptor;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO Calificacion
                        (puntaje, comentario, fecha, tipo, archivada, idcliente_emisor, idlocal_receptor, idlocal_emisor, idcliente_receptor)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setDouble(1, calificacion.getPuntaje());
            ps.setString(2, calificacion.getComentario());
            ps.setDate(3, java.sql.Date.valueOf(calificacion.getFecha().toLocalDate()));
            ps.setString(4, calificacion.getTipo().name());
            ps.setBoolean(5, Boolean.TRUE.equals(calificacion.getArchivada()));
            setNullableLong(ps, 6, fIdClienteEmisor);
            setNullableLong(ps, 7, fIdLocalReceptor);
            setNullableLong(ps, 8, fIdLocalEmisor);
            setNullableLong(ps, 9, fIdClienteReceptor);
            return ps;
        }, keyHolder);
        calificacion.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void actualizar(Calificacion calificacion) {
        validarClienteYLocal(calificacion);

        Long idClienteEmisor = null;
        Long idLocalReceptor = null;
        Long idLocalEmisor = null;
        Long idClienteReceptor = null;

        if (calificacion.getTipo() == TipoCalificacion.Cliente_a_local) {
            idClienteEmisor = calificacion.getCliente().getId();
            idLocalReceptor = calificacion.getLocal().getId();
        } else {
            idLocalEmisor = calificacion.getLocal().getId();
            idClienteReceptor = calificacion.getCliente().getId();
        }

        jdbcTemplate.update(
                """
                UPDATE Calificacion
                SET puntaje = ?, comentario = ?, fecha = ?, tipo = ?, archivada = ?,
                    idcliente_emisor = ?, idlocal_receptor = ?, idlocal_emisor = ?, idcliente_receptor = ?
                WHERE id = ?
                """,
                calificacion.getPuntaje(),
                calificacion.getComentario(),
                calificacion.getFecha(),
                calificacion.getTipo().name(),
                Boolean.TRUE.equals(calificacion.getArchivada()),
                idClienteEmisor,
                idLocalReceptor,
                idLocalEmisor,
                idClienteReceptor,
                calificacion.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Calificacion WHERE id = ?", id);
    }

    @Override
    public List<Calificacion> listarPorLocal(Long idLocal) {
        return jdbcTemplate.query(
                """
                SELECT c.*
                FROM calificacion c
                WHERE c.idlocal_receptor = ? AND c.tipo = ? AND c.archivada = FALSE
                ORDER BY c.fecha DESC, c.id DESC
                """,
                (rs, row) -> calificacionMapper(rs, row),
                idLocal,
                TipoCalificacion.Cliente_a_local.toString()
        );
    }

    @Override
    public List<Calificacion> listarPorCliente(Long idCliente){
        return jdbcTemplate.query(
                """
                SELECT c.*
                FROM calificacion c
                WHERE c.idcliente_receptor = ? AND c.tipo = ? AND c.archivada = FALSE
                ORDER BY c.fecha DESC, c.id DESC
                """,
                (rs, row) -> calificacionMapper(rs, row),
                idCliente,
                TipoCalificacion.Local_a_cliente.toString()
        );
    }

    @Override
    public Optional<Calificacion> buscarCalificacionClienteALocal(Long idCliente, Long idLocal) {
        return jdbcTemplate.query(
                """
                SELECT c.*
                FROM calificacion c
                WHERE c.idcliente_emisor = ? AND c.idlocal_receptor = ? AND c.tipo = ? AND c.archivada = FALSE
                ORDER BY c.fecha DESC, c.id DESC
                """,
                (rs, row) -> calificacionMapper(rs, row),
                idCliente,
                idLocal,
                TipoCalificacion.Cliente_a_local.toString()
        ).stream().findFirst();
    }

    @Override
    public Optional<Calificacion> buscarCalificacionLocalACliente(Long idCliente, Long idLocal) {
        return jdbcTemplate.query(
                """
                SELECT c.*
                FROM calificacion c
                WHERE c.idlocal_emisor = ? AND c.idcliente_receptor = ? AND c.tipo = ? AND c.archivada = FALSE
                ORDER BY c.fecha DESC, c.id DESC
                """,
                (rs, row) -> calificacionMapper(rs, row),
                idLocal,
                idCliente,
                TipoCalificacion.Local_a_cliente.toString()
        ).stream().findFirst();
    }

    @Override
    public void archivarPorCliente(Long idCliente) {
        jdbcTemplate.update("""
                UPDATE calificacion
                SET archivada = TRUE
                WHERE idcliente_emisor = ? OR idcliente_receptor = ?
                """, idCliente, idCliente);
    }

    @Override
    public List<Long> obtenerLocalesAfectadosPorArchivoDeCliente(Long idCliente) {
        return jdbcTemplate.query("""
                SELECT DISTINCT idlocal_receptor AS idlocal
                FROM calificacion
                WHERE idcliente_emisor = ? AND idlocal_receptor IS NOT NULL
                UNION
                SELECT DISTINCT idlocal_emisor AS idlocal
                FROM calificacion
                WHERE idcliente_receptor = ? AND idlocal_emisor IS NOT NULL
                """,
                (rs, row) -> rs.getLong("idlocal"),
                idCliente,
                idCliente
        );
    }

    @Override
    public void archivarPorLocal(Long idLocal) {
        jdbcTemplate.update("""
                UPDATE calificacion
                SET archivada = TRUE
                WHERE idlocal_emisor = ? OR idlocal_receptor = ?
                """, idLocal, idLocal);
    }

    @Override
    public List<Long> obtenerClientesAfectadosPorArchivoDeLocal(Long idLocal) {
        return jdbcTemplate.query("""
                SELECT DISTINCT idcliente_receptor AS idcliente
                FROM calificacion
                WHERE idlocal_emisor = ? AND idcliente_receptor IS NOT NULL
                UNION
                SELECT DISTINCT idcliente_emisor AS idcliente
                FROM calificacion
                WHERE idlocal_receptor = ? AND idcliente_emisor IS NOT NULL
                """,
                (rs, row) -> rs.getLong("idcliente"),
                idLocal,
                idLocal
        );
    }

    private Calificacion calificacionMapper(ResultSet rs, int row) throws SQLException {
        TipoCalificacion tipo = TipoCalificacion.valueOf(rs.getString("tipo"));
        Long idCalificacion = rs.getLong("id");

        Long idClienteEmisor = getNullableLong(rs, "idcliente_emisor");
        Long idLocalReceptor = getNullableLong(rs, "idlocal_receptor");
        Long idLocalEmisor = getNullableLong(rs, "idlocal_emisor");
        Long idClienteReceptor = getNullableLong(rs, "idcliente_receptor");

        Long idClienteAsociado = idClienteEmisor != null ? idClienteEmisor : idClienteReceptor;
        Long idLocalAsociado = idLocalReceptor != null ? idLocalReceptor : idLocalEmisor;

        Cliente cliente = idClienteAsociado != null
                ? clienteRepositorio.buscarPorId(idClienteAsociado).orElse(null)
                : null;
        Local local = idLocalAsociado != null
                ? localRepositorio.buscarPorId(idLocalAsociado).orElse(null)
                : null;

        return Calificacion.builder()
                .id(idCalificacion)
                .puntaje(rs.getInt("puntaje"))
                .comentario(rs.getString("comentario"))
                .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                .tipo(tipo)
                .cliente(cliente)
                .local(local)
                .archivada(rs.getBoolean("archivada"))
                .build();
    }

    private void validarClienteYLocal(Calificacion calificacion) {
        if (calificacion.getCliente() == null || calificacion.getCliente().getId() == null) {
            throw new IllegalArgumentException("La calificación debe tener un cliente asociado.");
        }
        if (calificacion.getLocal() == null || calificacion.getLocal().getId() == null) {
            throw new IllegalArgumentException("La calificación debe tener un local asociado.");
        }
        if (calificacion.getTipo() == null) {
            throw new IllegalArgumentException("La calificación debe tener un tipo (Cliente_a_local o Local_a_cliente).");
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(index, value);
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }

    private Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}