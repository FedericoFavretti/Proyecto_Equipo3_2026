package com.example.demo.Persistencia.Implementaciones;


import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtFiltro;
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

    public PlatoRepositorioImpl(JdbcTemplate jdbcTemplate, LocalRepositorio localRepo) {
        this.localRepositorio = localRepo;
        this.jdbcTemplate = jdbcTemplate;
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
                (rs, row) -> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        mapearImagenes(rs),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal"))
                                .orElseThrow(() -> new RuntimeException("Local no encontrado"))
                )
        );
    }

    @Override
    public Optional<Plato> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM plato WHERE id = ?",
                (rs, row) -> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        mapearImagenes(rs),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal"))
                                .orElseThrow(() -> new RuntimeException("Local no encontrado"))
                ), id
        ).stream().findFirst();
    }


    @Override
    public Optional<Plato> buscarPorNombre(String nombre) {
        return jdbcTemplate.query("SELECT * FROM plato WHERE nombre = ?",
                (rs, row) -> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        mapearImagenes(rs),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal"))
                                .orElseThrow(() -> new RuntimeException("Local no encontrado"))
                ), nombre
        ).stream().findFirst();
    }

    @Override
    public List<Plato> buscarConFiltros(DtFiltro filtro) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.nombre, p.descripcion, p.precio, " +
                        "p.imagenes, p.disponible, p.idLocal FROM plato p WHERE 1=1"
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
                (rs, row) -> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        mapearImagenes(rs),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal"))
                                .orElseThrow(() -> new RuntimeException("Local no encontrado"))
                ),
                params.toArray()
        );
    }

    @Override
    public Plato guardar(Plato plato) {
        KeyHolder idGenerado = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO plato (nombre, descripcion, precio, imagenes, disponible, idLocal) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setArray(4, connection.createArrayOf("varchar", plato.getImagenes().toArray())); // ✅ array PostgreSQL
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            return ps;
        }, idGenerado);

        plato.setId(idGenerado.getKey().longValue());
        return plato;
    }

    @Override
    public Plato actualizar(Plato plato) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE plato SET nombre = ?, descripcion = ?, precio = ?, " +
                            "imagenes = ?, disponible = ?, idLocal = ? WHERE id = ?"
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setArray(4, connection.createArrayOf("varchar", plato.getImagenes().toArray()));
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            ps.setLong(7, plato.getId());
            return ps;
        });

        return plato;
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM plato WHERE id = ?", id);
    }
}