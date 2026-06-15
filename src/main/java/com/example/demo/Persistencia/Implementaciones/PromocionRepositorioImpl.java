package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                (rs, row) -> mapearPromocion(rs)
        );
    }

    @Override
    public Optional<Promocion> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Promocion WHERE id = ?",
                (rs, row) -> mapearPromocion(rs),id
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

    @Override
    public List<Promocion> buscarActivasConFiltros(DtFiltro filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT pr.*
                FROM promocion pr
                INNER JOIN plato p ON p.id = pr.idPlato
                WHERE pr.fechaInicio <= CURRENT_DATE
                  AND pr.fechaFin >= CURRENT_DATE
                """);
        List<Object> params = new ArrayList<>();

        if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
            sql.append(" AND (p.nombre LIKE ? OR pr.descripcion LIKE ?)");
            String termino = "%" + filtro.getNombre() + "%";
            params.add(termino);
            params.add(termino);
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
            sql.append(" ORDER BY ").append(String.join(", ", orden));
        }

        return jdbcTemplate.query(sql.toString(),
                (rs, row) -> mapearPromocion(rs),
                params.toArray());
    }

    private Promocion mapearPromocion(ResultSet rs) throws SQLException {
        return new Promocion( rs.getLong("id"),
                rs.getDouble("descuento"),
                rs.getDate("fechaInicio"),
                rs.getDate("fechaFin"),
                rs.getString("descripcion"),
                platoRepositorio.buscarPorId(rs.getLong("idPlato")).orElseThrow(()-> new RuntimeException("Plato no encontrado")));
    }
}

