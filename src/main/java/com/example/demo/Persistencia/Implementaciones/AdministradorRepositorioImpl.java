package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Persistencia.Repositorios.AdministradorRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AdministradorRepositorioImpl implements AdministradorRepositorio {

    private static final String SELECT_ADMINISTRADOR_CON_USUARIO = """
            SELECT u.id, u.email, u.passwd, u.foto, u.estado, u.tipo, a.nivelAcceso
            FROM usuario u
            JOIN Administrador a ON a.id = u.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public AdministradorRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Administrador> listarTodos() {
        return jdbcTemplate.query(
                SELECT_ADMINISTRADOR_CON_USUARIO,
                (rs, rowNum) -> mapearAdministrador(rs)
        );
    }

    @Override
    public Optional<Administrador> buscarPorId(Long id) {
        return jdbcTemplate.query(
                SELECT_ADMINISTRADOR_CON_USUARIO + " WHERE u.id = ?",
                (rs, rowNum) -> mapearAdministrador(rs),
                id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Administrador administrador) {
        jdbcTemplate.update(
                "INSERT INTO Administrador (id, nivelAcceso) VALUES (?, ?)",
                administrador.getId(),
                administrador.getNivelAcceso()
        );
    }

    @Override
    public void actualizar(Administrador administrador) {
        jdbcTemplate.update(
                "UPDATE Administrador SET nivelAcceso = ? WHERE id = ?",
                administrador.getNivelAcceso(),
                administrador.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Administrador WHERE id = ?", id);
    }

    private Administrador mapearAdministrador(ResultSet rs) throws SQLException {
        Administrador administrador = new Administrador();
        administrador.setId(rs.getLong("id"));
        administrador.setEmail(rs.getString("email"));
        administrador.setPasswd(rs.getString("passwd"));
        administrador.setFoto(rs.getString("foto"));
        administrador.setTipo(rs.getString("tipo"));
        administrador.setNivelAcceso(rs.getString("nivelAcceso"));
        String estado = rs.getString("estado");
        if (estado != null && !estado.isBlank()) {
            administrador.setEstado(EstadoCuenta.valueOf(estado));
        }
        return administrador;
    }
}
