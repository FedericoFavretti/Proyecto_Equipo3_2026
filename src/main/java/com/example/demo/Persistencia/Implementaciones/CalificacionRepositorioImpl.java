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
import java.util.List;
import java.util.Optional;

@Repository
public class CalificacionRepositorioImpl implements CalificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;
    private final ClienteRepositorio clienteRepositorio;
    private final LocalRepositorio localRepositorio;
    private final ClienteCalificacionRepositorio clienteCalificacionRepositorio;
    private final LocalCalificacionRepositorio localCalificacionRepositorio;

    public CalificacionRepositorioImpl(JdbcTemplate jdbcTemplate, ClienteRepositorio clienteRepositorio, LocalRepositorio localRepositorio,  ClienteCalificacionRepositorio clienteCalificacionRepositorio,  LocalCalificacionRepositorio localCalificacionRepositorio) {
        this.jdbcTemplate = jdbcTemplate;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.clienteCalificacionRepositorio = clienteCalificacionRepositorio;
        this.localCalificacionRepositorio = localCalificacionRepositorio;
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Calificacion (puntaje, comentario, fecha, tipo, archivada) VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"}
                    );
                    ps.setDouble(1, calificacion.getPuntaje());
                    ps.setString(2, calificacion.getComentario());
                    ps.setDate(3, java.sql.Date.valueOf(calificacion.getFecha().toLocalDate()));
                    ps.setString(4, calificacion.getTipo().name());
                    ps.setBoolean(5, Boolean.TRUE.equals(calificacion.getArchivada()));
                    return ps;
        }, keyHolder);
        Long idCalificacion = keyHolder.getKey().longValue();
        if (calificacion.getCliente() != null && calificacion.getCliente().getId() != null) {
            clienteCalificacionRepositorio.calificar(calificacion.getCliente().getId(), idCalificacion);
        }
        if (calificacion.getLocal() != null && calificacion.getLocal().getId() != null) {
            localCalificacionRepositorio.calificar(calificacion.getLocal().getId(), idCalificacion);
        }
    }

    @Override
    public void actualizar(Calificacion calificacion) {
       jdbcTemplate.update("UPDATE Calificacion SET  puntaje = ?, comentario = ?, fecha = ?, tipo = ?, archivada = ? WHERE id = ?",
               calificacion.getPuntaje(),
               calificacion.getComentario(),
               calificacion.getFecha(),
               calificacion.getTipo().name(),
               Boolean.TRUE.equals(calificacion.getArchivada()),
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
                JOIN local_calificacion lc ON lc.idcalificacion = c.id
                WHERE lc.idlocal = ? AND c.tipo = ? AND c.archivada = FALSE
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
                JOIN cliente_calificacion lc ON lc.idcalificacion = c.id
                WHERE lc.idcliente = ? AND c.tipo = ? AND c.archivada = FALSE
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
                JOIN cliente_calificacion cc ON cc.idcalificacion = c.id
                JOIN local_calificacion lc ON lc.idcalificacion = c.id
                WHERE cc.idcliente = ? AND lc.idlocal = ? AND c.tipo = ? AND c.archivada = FALSE
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
                JOIN local_calificacion cl ON cl.idcalificacion = c.id
                JOIN cliente_calificacion cc ON cc.idcalificacion = c.id
                WHERE cc.idcliente = ? AND cl.idlocal = ? AND c.tipo = ? AND c.archivada = FALSE
                ORDER BY c.fecha DESC, c.id DESC
                """,
                (rs, row) -> calificacionMapper(rs, row),
                idCliente,
                idLocal,
                TipoCalificacion.Local_a_cliente.toString()
        ).stream().findFirst();
    }

    private Calificacion calificacionMapper(ResultSet rs, int row) throws SQLException {
        TipoCalificacion tipo = TipoCalificacion.valueOf(rs.getString("tipo"));
        Long idCalificacion = rs.getLong("id");
        Cliente cliente = obtenerClienteAsociado(idCalificacion)
                .flatMap(clienteRepositorio::buscarPorId)
                .orElse(null);
        Local local = obtenerLocalAsociado(idCalificacion)
                .flatMap(localRepositorio::buscarPorId)
                .orElse(null);

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

    @Override
    public void archivarPorCliente(Long idCliente) {
        jdbcTemplate.update("""
                UPDATE calificacion
                SET archivada = TRUE
                WHERE id IN (
                    SELECT idcalificacion
                    FROM cliente_calificacion
                    WHERE idcliente = ?
                )
                """, idCliente);
    }

    @Override
    public List<Long> obtenerLocalesAfectadosPorArchivoDeCliente(Long idCliente) {
        return jdbcTemplate.query("""
                SELECT DISTINCT lc.idlocal
                FROM local_calificacion lc
                JOIN cliente_calificacion cc ON cc.idcalificacion = lc.idcalificacion
                WHERE cc.idcliente = ?
                """,
                (rs, row) -> rs.getLong("idlocal"),
                idCliente
        );
    }

    private Optional<Long> obtenerClienteAsociado(Long idCalificacion) {
        return jdbcTemplate.query(
                "SELECT idcliente FROM cliente_calificacion WHERE idcalificacion = ?",
                (rs, row) -> rs.getLong("idcliente"),
                idCalificacion
        ).stream().findFirst();
    }

    private Optional<Long> obtenerLocalAsociado(Long idCalificacion) {
        return jdbcTemplate.query(
                "SELECT idlocal FROM local_calificacion WHERE idcalificacion = ?",
                (rs, row) -> rs.getLong("idlocal"),
                idCalificacion
        ).stream().findFirst();
    }
}
