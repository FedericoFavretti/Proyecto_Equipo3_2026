package com.example.demo.Persistencia.Implementaciones;


import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Persistencia.Repositorios.CategoriaRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
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


    private List<String> mapearImagenes(ResultSet rs) throws SQLException {
        Array array = rs.getArray("imagenes");
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList((String[]) array.getArray());
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
                        "p.imagenes, p.disponible, p.idLocal, p.idcategoria FROM plato p WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (filtro.getNombre() != null && !filtro.getNombre().isEmpty()) {
            sql.append(" AND p.nombre LIKE ?");
            params.add("%" + filtro.getNombre() + "%");
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
    public Plato guardar(Plato plato) {
        KeyHolder idGenerado = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO plato (nombre, descripcion, precio, imagenes, disponible, idLocal, idcategoria) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setArray(4, connection.createArrayOf("varchar", plato.getImagenes().toArray()));
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            ps.setLong(7, plato.getCategoria().getId());
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
                            "imagenes = ?, disponible = ?, idLocal = ?, idcategoria = ? WHERE id = ?"
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setArray(4, connection.createArrayOf("varchar", plato.getImagenes().toArray()));
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            ps.setLong(7, plato.getCategoria().getId());
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

        return new Plato(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                categoriaRepositorio.buscarPorId(rs.getLong("idcategoria")).orElseThrow(()->new RuntimeException("Categoria no encontrada.")),
                rs.getDouble("precio"),
                mapearImagenes(rs),
                rs.getBoolean("disponible"),
                localRepositorio.buscarPorId(rs.getLong("idLocal"))
                        .orElseThrow(() -> new RuntimeException("Local no encontrado"))
        );
    }
}

