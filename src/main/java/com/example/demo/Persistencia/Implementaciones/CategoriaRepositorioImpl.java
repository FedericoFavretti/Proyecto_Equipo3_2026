package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Categoria;
import com.example.demo.Persistencia.Repositorios.CategoriaRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoriaRepositorioImpl implements CategoriaRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        return Categoria.builder()
                .id(rs.getLong("id"))
                .nombre(rs.getString("nombre"))
                .idLocal(rs.getLong("idlocal"))
                .build();
    }

    @Override
    public List<Categoria> listarPorLocal(Long idLocal) {
        return jdbcTemplate.query(
                "SELECT * FROM categoria WHERE idlocal = ? ORDER BY nombre ASC",
                (rs, row) -> mapearCategoria(rs), idLocal
        );
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM categoria WHERE id = ?",
                (rs, row) -> mapearCategoria(rs), id
        ).stream().findFirst();
    }

    @Override
    public Optional<Categoria> buscarPorNombreYLocal(String nombre, Long idLocal) {
        return jdbcTemplate.query(
                "SELECT * FROM categoria WHERE nombre = ? AND idlocal = ?",
                (rs, row) -> mapearCategoria(rs), nombre, idLocal
        ).stream().findFirst();
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        KeyHolder idGenerado = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO categoria (nombre, idlocal) VALUES (?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, categoria.getNombre());
            ps.setLong(2, categoria.getIdLocal());
            return ps;
        }, idGenerado);

        categoria.setId(idGenerado.getKey().longValue());
        return categoria;
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM categoria WHERE id = ?", id);
    }
}