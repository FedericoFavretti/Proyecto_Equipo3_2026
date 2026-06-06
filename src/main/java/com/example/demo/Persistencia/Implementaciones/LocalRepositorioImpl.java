package com.example.demo.Persistencia.Implementaciones;

import java.sql.Array;
import java.sql.PreparedStatement;
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
                EstadoLocal.Pendiente.name()
        );
    }

    @Override
    public Optional<Local> buscarPorId(Long id) {
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
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Local (id, nombre, calle, numero, ciudad, codigoPostal, descripcion, estado, calificacionGlobal, estaAbierto, imagenes) VALUES (? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setLong(1, local.getId());
            ps.setString(2, local.getNombre());
            ps.setString(3, local.getDireccion().getCalle());
            ps.setString(4, local.getDireccion().getNumero());
            ps.setString(5, local.getDireccion().getCiudad());
            ps.setString(6, local.getDireccion().getCodigoPostal());
            ps.setString(7, local.getDescripcion());
            ps.setString(8, local.getEstadoLocal().name());
            ps.setDouble(9, local.getCalificacionGlobal());
            ps.setBoolean(10, local.getEstaAbierto());
            Array imagenesArray = connection.createArrayOf("varchar", local.getImagenes().toArray());
            ps.setArray(11, imagenesArray);
            return ps;
        });
    }

    @Override
    public void actualizar(Local local) {
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE Local SET  nombre = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, descripcion = ?, estado = ?, calificacionGlobal = ?, estaAbierto = ?, imagenes = ? WHERE id = ?"
            );
            ps.setString(1, local.getNombre());
            ps.setString(2, local.getDireccion().getCalle());
            ps.setString(3, local.getDireccion().getNumero());
            ps.setString(4, local.getDireccion().getCiudad());
            ps.setString(5, local.getDireccion().getCodigoPostal());
            ps.setString(6, local.getDescripcion());
            ps.setString(7, local.getEstadoLocal().name());
            ps.setDouble(8, local.getCalificacionGlobal());
            ps.setBoolean(9, local.getEstaAbierto());
            Array imagenesArray = connection.createArrayOf("varchar", local.getImagenes().toArray());
            ps.setArray(10, imagenesArray);
            ps.setLong(11, local.getId());
            return ps;
        });
    }

    @Override
    public void eliminar(Long id) {
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
                EstadoLocal.valueOf(rs.getString("estado")),
                rs.getDouble("calificacionGlobal"),
                rs.getBoolean("estaAbierto"),
                mapearImagenes(rs.getString("imagenes"))
        );
        local.setId(rs.getLong("id"));
        return local;
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
