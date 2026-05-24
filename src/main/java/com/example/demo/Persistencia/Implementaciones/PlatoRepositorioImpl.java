package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
    public void guardar(Plato plato) {
        jdbcTemplate.update("INSERT INTO Plato (nombre, descripcion, precio, imagenes, disponible, idLocal) VALUES (?, ?, ?, ?, ?, ?)",
                plato.getNombre(),
                plato.getDescripcion(),
                plato.getPrecio(),
                String.join(",", plato.getImagenes()),
                plato.getDisponible(),
                plato.getLocal().getId()
        );
    }

    @Override
    public void actualizar(Plato plato) {
        jdbcTemplate.update("UPDATE Plato SET nombre = ?, descripcion = ?, precio = ?, imagenes = ?, disponible = ?, idLocal = ? WHERE id = ?",
                plato.getNombre(),
                plato.getDescripcion(),
                plato.getPrecio(),
                String.join(",", plato.getImagenes()),
                plato.getDisponible(),
                plato.getLocal().getId(),
                plato.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Plato WHERE id = ?", id);
    }
}
