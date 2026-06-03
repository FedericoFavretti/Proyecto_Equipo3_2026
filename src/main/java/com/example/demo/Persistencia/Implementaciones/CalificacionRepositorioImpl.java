package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CalificacionRepositorioImpl implements CalificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;


    public CalificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Calificacion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Calificacion",
                (rs, row)-> new Calificacion(
                        rs.getLong("id"),
                        rs.getInt("puntaje"),
                        rs.getString("comentario"),
                        rs.getDate("fecha"),
                        TipoCalificacion.valueOf(rs.getString("tipo")),
                        null,
                        null
                )
        );
    }

    @Override
    public Optional<Calificacion> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM Calificacion WHERE id = ? ",
                (rs, row)-> new Calificacion(
                        rs.getLong("id"),
                        rs.getInt("puntaje"),
                        rs.getString("comentario"),
                        rs.getDate("fecha"),
                        TipoCalificacion.valueOf(rs.getString("tipo")),
                        null,
                        null
                ), id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Calificacion calificacion) {
        jdbcTemplate.update("INSERT INTO Calificacion (puntaje, comentario, fecha, tipo) VALUES (?, ?, ?, ?)",
                calificacion.getPuntaje(),
                calificacion.getComentario(),
                calificacion.getFecha(),
                calificacion.getTipo().toString()
        );
    }

    @Override
    public void actualizar(Calificacion calificacion) {
       jdbcTemplate.update("UPDATE Calificacion SET  puntaje = ?, comentario = ?, fecha = ?, tipo = ? WHERE id = ?",
               calificacion.getPuntaje(),
               calificacion.getComentario(),
               calificacion.getFecha(),
               calificacion.getTipo(),
               calificacion.getId()
       );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Calificacion WHERE id = ?", id);
    }
}
