package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PromocionRepositorioImpl  implements PromocionRepositorio {
    private final PlatoRepositorio  platoRepositorio;
    private final JdbcTemplate jdbcTemplate;


    public PromocionRepositorioImpl(JdbcTemplate jdbcTemplate, PlatoRepositorio  platoRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.platoRepositorio = platoRepo;
    }

    @Override
    public List<Promocion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Promocion",
                (rs, row) -> new Promocion(
                        rs.getLong("id"),
                        rs.getDouble("descuento"),
                        rs.getDate("fechaInicio"),
                        rs.getDate("fechaFin"),
                        rs.getString("descripcion"),
                        platoRepositorio.buscarPorId(rs.getLong("idPlato")).orElseThrow(()-> new RuntimeException("Plato no encontrado"))
                )
        );
    }

    @Override
    public Optional<Promocion> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Promocion WHERE id = ?",
                (rs, row) -> new Promocion(
                        rs.getLong("id"),
                        rs.getDouble("descuento"),
                        rs.getDate("fechaInicio"),
                        rs.getDate("fechaFin"),
                        rs.getString("descripcion"),
                        platoRepositorio.buscarPorId(rs.getLong("idPlato")).orElseThrow(()-> new RuntimeException("Plato no encontrado"))
                ),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Promocion promocion) {
        jdbcTemplate.update("INSERT INTO Promocion (descuento, fechaInicio, fechaFin, descripcion, idPlato) VALUES (?, ?, ?, ?, ?)",
                promocion.getDescuento(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getDescripcion(),
                promocion.getPlato().getId()
        );
    }

    @Override
    public void actualizar(Promocion promocion) {
        jdbcTemplate.update("UPDATE Promocion SET descuento = ?, fechaInicio = ?, fechaFin = ?, descripcion = ?, idPlato = ? WHERE id = ?)",
                promocion.getDescuento(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.getDescripcion(),
                promocion.getPlato().getId(),
                promocion.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Promocion WHERE id = ?", id);
    }
}
