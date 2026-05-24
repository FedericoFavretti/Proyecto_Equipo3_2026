package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                (rs, row)-> new Local(
                        rs.getLong("id"),
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
                        new ArrayList<>(Collections.singleton(rs.getString("imagenes")))
                )
        );
    }

    @Override
    public Optional<Local> buscarPorId(long id) {
        return jdbcTemplate.query(
                "SELECT u,*, l.* FROM usuarios u JOIN locales l ON u.id = l.id WHERE u.id = ?",
                (rs, row) -> new Local(
                        rs.getLong("id"),
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
                        new ArrayList<>(Collections.singleton(rs.getString("imagenes")))
                ), id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Local local) {
        jdbcTemplate.update("INSERT INTO Local (nombre, calle, numero, ciudad, codigoPostal, descripcion, estado, calificacionGlobal, estaAbierto, imagenes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                local.getNombre(),
                local.getDireccion().getCalle(),
                local.getDireccion().getNumero(),
                local.getDireccion().getCiudad(),
                local.getDireccion().getCodigoPostal(),
                local.getDireccion(),
                local.getEstado().toString(),
                local.getCalificacionGlobal(),
                local.getEstaAbierto(),
                String.join(",", local.getImagenes())
        );
    }

    @Override
    public void actualizar(Local local) {
        jdbcTemplate.update(
                "UPDATE Local SET nombre = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, descripcion = ?, estado = ?, calificacionGlobal = ?, estaAbierto = ?, imagenes = ? WHERE id = ?",
                local.getNombre(),
                local.getDireccion().getCalle(),
                local.getDireccion().getNumero(),
                local.getDireccion().getCiudad(),
                local.getDireccion().getCodigoPostal(),
                local.getDireccion(),
                local.getEstado().toString(),
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
}
