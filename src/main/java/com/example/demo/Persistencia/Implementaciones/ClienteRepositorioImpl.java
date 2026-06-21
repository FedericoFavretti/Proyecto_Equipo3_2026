package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id WHERE c.activo = true",
                (rs, row) -> mapearCliente(rs)
        );
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id  WHERE u.id = ?",
                (rs, row) -> mapearCliente(rs), id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Cliente cliente) {
        jdbcTemplate.update(
                "INSERT INTO Cliente (id, documento, nombre, apellido, calle, numero, ciudad, codigoPostal, calificacionGlobal, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                cliente.getId(),
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
                "UPDATE Cliente SET documento = ?, nombre = ?, apellido = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, calificacionGlobal = ?, activo = ? WHERE id = ?",
                cliente.getDocumento(),
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

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Timestamp sesionesInvalidadasTs = rs.getTimestamp("sesiones_invalidadas_desde");
        return Cliente.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .passwd(rs.getString("passwd"))
                .foto(rs.getString("foto"))
                .estado(EstadoCuenta.valueOf(rs.getString("estado")))
                .sesionesInvalidadasDesde(
                        sesionesInvalidadasTs != null ? sesionesInvalidadasTs.toLocalDateTime() : null
                )
                .documento(rs.getString("documento"))
                .nombre(rs.getString("nombre"))
                .apellido(rs.getString("apellido"))
                .direccion(DtDireccion.builder()
                        .calle(rs.getString("calle"))
                        .ciudad(rs.getString("ciudad"))
                        .numero(rs.getString("numero"))
                        .codigoPostal(rs.getString("codigoPostal"))
                        .build()
                )
                .calificacionGlobal(rs.getDouble("calificacionGlobal"))
                .activo(rs.getBoolean("activo")
                ).build();
    }
}

