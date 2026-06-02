package com.example.demo.Persistencia.Implementaciones;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LocalRepositorioImpl implements LocalRepositorio {
    private final JdbcTemplate jdbcTemplate;

    public LocalRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public List<Local> listarTodos() {
        return jdbcTemplate.query(
                "SELECT * FROM Local",
                (rs, row) -> mapearLocal(rs)
        );
    }

    @Override
    public List<Local> listarPendientes() {
        return jdbcTemplate.query(
                "SELECT * FROM Local WHERE estado = ?",
                (rs, row) -> mapearLocal(rs),
                EstadoLocal.PENDIENTE.name()
        );
    }

    @Override
    public Optional<Local> buscarPorId(long id) {
        return jdbcTemplate.query(
                "SELECT * FROM Local WHERE id = ?",
                (rs, row) -> mapearLocal(rs), id
        ).stream().findFirst();
    }

    @Override
    public Optional<Local> buscarPorNombre(String nombre) {
        return jdbcTemplate.query(
                "SELECT * FROM Local WHERE LOWER(nombre) = LOWER(?)",
                (rs, row) -> mapearLocal(rs), nombre
        ).stream().findFirst();
    }

    @Override
    public void guardar(Local local) {
        jdbcTemplate.update("INSERT INTO Local (email, nombre, calle, numero, ciudad, codigoPostal, descripcion, estado, calificacionGlobal, estaAbierto, imagenes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                local.getEmail(),
                local.getNombre(),
                local.getDireccion().getCalle(),
                local.getDireccion().getNumero(),
                local.getDireccion().getCiudad(),
                local.getDireccion().getCodigoPostal(),
                local.getDescripcion(),
                local.getEstadoLocal().name(),
                local.getCalificacionGlobal(),
                local.getEstaAbierto(),
                String.join(",", local.getImagenes())
        );
    }

    @Override
    public void actualizar(Local local) {
        jdbcTemplate.update(
                "UPDATE Local SET email = ?, nombre = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, descripcion = ?, estado = ?, calificacionGlobal = ?, estaAbierto = ?, imagenes = ? WHERE id = ?",
                local.getEmail(),
                local.getNombre(),
                local.getDireccion().getCalle(),
                local.getDireccion().getNumero(),
                local.getDireccion().getCiudad(),
                local.getDireccion().getCodigoPostal(),
                local.getDescripcion(),
                local.getEstadoLocal().name(),
                local.getCalificacionGlobal(),
                local.getEstaAbierto(),
                String.join(",", local.getImagenes()),
                local.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Local WHERE id = ?", id);
    }

    private Local mapearLocal(ResultSet rs) throws SQLException {
        Local local = new Local(
                rs.getString("nombre"),
                new DtDireccion(
                        rs.getString("calle"),
                        rs.getString("numero"),
                        rs.getString("ciudad"),
                        rs.getString("codigoPostal")
                ),
                rs.getString("descripcion"),
                mapearEstadoLocal(rs.getString("estado")),
                rs.getDouble("calificacionGlobal"),
                rs.getBoolean("estaAbierto"),
                mapearImagenes(rs.getString("imagenes"))
        );
        local.setEmail(rs.getString("email"));
        return local;
    }

    private EstadoLocal mapearEstadoLocal(String estado) {
        return EstadoLocal.valueOf(estado.trim().toUpperCase());
    }

    private List<String> mapearImagenes(String imagenes) {
        if (imagenes == null || imagenes.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(imagenes.split(","))
                .map(String::trim)
                .filter(imagen -> !imagen.isBlank())
                .toList();
    }
}
