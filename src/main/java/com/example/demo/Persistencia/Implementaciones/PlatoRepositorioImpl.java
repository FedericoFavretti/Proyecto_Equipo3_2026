package com.example.demo.Persistencia.Implementaciones;


import com.example.demo.Logica.Clases.Categoria;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Persistencia.Repositorios.CategoriaRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Logica.Enums.EstadoPedido;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class PlatoRepositorioImpl implements PlatoRepositorio {

    private final LocalRepositorio localRepositorio;
    private final JdbcTemplate jdbcTemplate;
    private final CategoriaRepositorio categoriaRepositorio;

    public PlatoRepositorioImpl(JdbcTemplate jdbcTemplate, LocalRepositorio localRepo, CategoriaRepositorio categoriaRepositorio) {
        this.localRepositorio = localRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Override
    public List<Plato> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM plato",
                (rs, row) ->mapearPlato(rs)
        );
    }

    @Override
    public Optional<Plato> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM plato WHERE id = ?",
                (rs, row) -> mapearPlato(rs), id
        ).stream().findFirst();
    }


    @Override
    public Optional<Plato> buscarPorNombre(String nombre) {
        return jdbcTemplate.query("SELECT * FROM plato WHERE nombre = ?",
                (rs, row) -> mapearPlato(rs), nombre
        ).stream().findFirst();
    }

    @Override
    public List<Plato> buscarConFiltros(DtFiltro filtro) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.nombre, p.descripcion, p.precio, " +
                        "p.imagen, p.disponible, p.idLocal, p.idcategoria " +
                        "FROM plato p LEFT JOIN categoria c ON c.id = p.idcategoria WHERE 1=1" +
                        " AND p.disponible = true"
        );
        List<Object> params = new ArrayList<>();

        if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
            sql.append("""
                     AND to_tsvector(
                        'spanish_unaccent',
                        coalesce(p.nombre, '') || ' ' || coalesce(c.nombre, '')
                    ) @@ websearch_to_tsquery('spanish_unaccent', ?)
                    """);
            params.add(filtro.getNombre().trim());
        }

        if (filtro.getDtLocal() != null) {
            sql.append(" AND p.idLocal = ?");
            params.add(filtro.getDtLocal().getId());
        }

        if (Boolean.TRUE.equals(filtro.getPromocionActiva())) {
            sql.append("""
                     AND EXISTS (
                        SELECT 1
                        FROM promocion pr
                        WHERE pr.idPlato = p.id
                          AND pr.fechaInicio <= CURRENT_DATE
                          AND pr.fechaFin >= CURRENT_DATE
                    )
                    """);
        }

        List<String> orden = new ArrayList<>();

        if (Boolean.TRUE.equals(filtro.getPrecioMasBajo())) {
            orden.add("p.precio ASC");
        } else if (Boolean.TRUE.equals(filtro.getPrecioMasAlto())) {
            orden.add("p.precio DESC");
        }

        if (Boolean.TRUE.equals(filtro.getAlfabetico())) {
            orden.add("p.nombre ASC");
        }

        if (!orden.isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(String.join(", ", orden));
        }

        return jdbcTemplate.query(sql.toString(),
                (rs, row) -> mapearPlato(rs),
                params.toArray()
        );
    }

    @Override
    public List<Plato> buscarMasPedidos(int limite) {
        String sql = """
                SELECT p.id, p.nombre, p.descripcion, p.precio, p.imagen, p.disponible, p.idLocal, p.idcategoria
                FROM plato p
                JOIN (
                    SELECT dp.idplato, SUM(dp.cantidad) AS cantidad_total
                    FROM detallepedido dp
                    JOIN pedido pe ON pe.id = dp.idpedido
                    WHERE pe.estado IN (?, ?)
                    GROUP BY dp.idplato
                ) conteo ON conteo.idplato = p.id
                WHERE p.disponible = true
                ORDER BY conteo.cantidad_total DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(
                sql,
                (rs, row) -> mapearPlato(rs),
                EstadoPedido.Confirmado.name(),
                EstadoPedido.Entregado.name(),
                limite
        );
    }

    @Override
    public Plato guardar(Plato plato) {
        KeyHolder idGenerado = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO plato (nombre, descripcion, precio, imagen, disponible, idLocal, idcategoria) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setString(4, plato.getImagen());
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            if (plato.getCategoria() != null && plato.getCategoria().getId() != null) {
                ps.setLong(7, plato.getCategoria().getId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            return ps;
        }, idGenerado);

        plato.setId(idGenerado.getKey().longValue());
        return plato;
    }
    @Override
    public List<Plato> buscarPlatosDelocal(Long idLocal){
        return jdbcTemplate.query("SELECT * FROM plato WHERE idLocal = ?",
                (rs, row) -> mapearPlato(rs), idLocal
        );
    }
    @Override
    public Plato actualizar(Plato plato) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE plato SET nombre = ?, descripcion = ?, precio = ?, " +
                            "imagen = ?, disponible = ?, idLocal = ?, idcategoria = ? WHERE id = ?"
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setString(4, plato.getImagen());
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            if (plato.getCategoria() != null && plato.getCategoria().getId() != null) {
                ps.setLong(7, plato.getCategoria().getId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.setLong(8, plato.getId());
            return ps;
        });

        return plato;
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM plato WHERE id = ?", id);
    }

    private Plato mapearPlato(ResultSet rs) throws SQLException {

        Long idCategoria = rs.getLong("idcategoria");
        Categoria categoria = null;
        if (!rs.wasNull()) {
            categoria = categoriaRepositorio.buscarPorId(idCategoria)
                    .orElse(null);
        }

        return new Plato(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                categoria,
                rs.getDouble("precio"),
                rs.getString("imagen"),
                rs.getBoolean("disponible"),
                localRepositorio.buscarPorId(rs.getLong("idLocal"))
                        .orElseThrow(() -> new RuntimeException("Local no encontrado"))
        );
    }
}

