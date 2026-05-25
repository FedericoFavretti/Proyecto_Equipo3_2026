package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class PlatoRepositorioImpl implements PlatoRepositorio {
    private final LocalRepositorio localRepositorio;
    private final JdbcTemplate jdbcTemplate;

    public PlatoRepositorioImpl(JdbcTemplate jdbcTemplate, LocalRepositorio localRepo) {
        this.localRepositorio = localRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Plato> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Plaro",
                (rs, row)-> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        new ArrayList<>(Collections.singleton(rs.getString("imagenes"))),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal")).orElseThrow(() -> new RuntimeException("Plato no encontrado"))
                )
        );
    }

    @Override
    public Optional<Plato> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Plato WHERE id = ?",
                (rs, row)-> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        new ArrayList<>(Collections.singleton(rs.getString("imagenes"))),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal")).orElseThrow(() -> new RuntimeException("Plato no encontrado"))
                ),id
        ).stream().findFirst();
    }

    @Override
    public Plato guardar(Plato plato) {
        KeyHolder idGenerado = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Plato (nombre, descripcion, precio, imagenes, disponible, idLocal) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, plato.getNombre());
            ps.setString(2, plato.getDescripcion());
            ps.setDouble(3, plato.getPrecio());
            ps.setString(4, String.join(",", plato.getImagenes()));
            ps.setBoolean(5, plato.getDisponible());
            ps.setLong(6, plato.getLocal().getId());
            return ps;
        }, idGenerado);

        plato.setId(idGenerado.getKey().longValue());
        return plato;
    }

    @Override
    public Plato actualizar(Plato plato) {
        int filasAfectadas = jdbcTemplate.update(
                "UPDATE Plato SET nombre = ?, descripcion = ?, precio = ?, imagenes = ?, disponible = ?, idLocal = ? WHERE id = ?",
                plato.getNombre(),
                plato.getDescripcion(),
                plato.getPrecio(),
                String.join(",", plato.getImagenes()),
                plato.getDisponible(),
                plato.getLocal().getId(),
                plato.getId()
        );

        if (filasAfectadas == 0) {
            throw new RuntimeException("El plato con nombre "+plato.getNombre()+" no fue encontrado.");
        }

        return plato;
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Plato WHERE id = ?", id);
    }

    @Override
    public Optional<Plato> buscarPorNombre(String nombre) {
        return jdbcTemplate.query("SELECT * FROM Plato WHERE nombre = ?",
                (rs, row)-> new Plato(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        new ArrayList<>(Collections.singleton(rs.getString("imagenes"))),
                        rs.getBoolean("disponible"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal")).orElseThrow(() -> new RuntimeException("Plato no encontrado"))
                ),nombre
        ).stream().findFirst();
    }
}
