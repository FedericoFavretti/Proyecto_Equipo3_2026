package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public class ClienteRepositorioImpl implements ClienteRepositorio {
    private final JdbcTemplate jdbcTemplate;


    public ClienteRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Cliente> listarTodos() {
        return jdbcTemplate.query(
                "SELECT * FROM Cliente",
                (rs, row)-> new Cliente(
                        rs.getString("documento"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        new DtDireccion(
                                rs.getString("calle"),
                                rs.getString("numero"),
                                rs.getString("ciudad"),
                                rs.getString("codigoPostal")
                        ),
                        rs.getDouble("calificacionGlobal"),
                        rs.getBoolean("activo")
                )
        );
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id  WHERE u.id = ?",
                (rs, row) -> new Cliente(
                        rs.getString("documento"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        new DtDireccion(
                                rs.getString("calle"),
                                rs.getString("numero"),
                                rs.getString("ciudad"),
                                rs.getString("codigoPostal")
                        ),
                        rs.getDouble("calificacionGlobal"),
                        rs.getBoolean("activo")
                ), id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Cliente cliente) {
        jdbcTemplate.update(
                "INSERT INTO Cliente (documento, nombre, apellido, calle, numero, ciudad, codigoPostal, calificacionGlobal, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                cliente.getDocumento(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDireccion().getCalle(),
                cliente.getDireccion().getNumero(),
                cliente.getDireccion().getCiudad(),
                cliente.getDireccion().getCodigoPostal(),
                cliente.getCalificacionGlobal(),
                cliente.getActivo()
        );
    }

    @Override
    public void actualizar(Cliente cliente) {
        jdbcTemplate.update(
                "UPDATE Cliente SET documento = ?, nombre = ?, apellido = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, calificacionGlobal = ?, activo = ?, WHERE id = ?",
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDireccion().getCalle(),
                cliente.getDireccion().getNumero(),
                cliente.getDireccion().getCiudad(),
                cliente.getDireccion().getCodigoPostal(),
                cliente.getCalificacionGlobal(),
                cliente.getActivo(),
                cliente.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Cliente WHERE id = ?", id);
    }

    @Override
    public boolean existeDocumento(String documento) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cliente WHERE documento = ?",
                Integer.class, documento
        );
        return count != null && count > 0;
    }
}
