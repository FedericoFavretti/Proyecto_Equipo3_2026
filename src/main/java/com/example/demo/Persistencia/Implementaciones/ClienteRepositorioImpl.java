package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Repository
public class ClienteRepositorioImpl implements ClienteRepositorio {
    private final JdbcTemplate jdbcTemplate;


    public ClienteRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Cliente> listarTodos() {
        return jdbcTemplate.query(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id WHERE c.documento NOT ILIKE 'ANON-%'",
                (rs, row)-> mapearCliente(rs)
        );
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id  WHERE u.id = ? ",
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

    @Override
    public List<Cliente> buscarConFiltros(DtFiltroUsuario filtro) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id WHERE c.documento NOT ILIKE 'ANON-%' AND 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (filtro != null && filtro.getTexto() != null && !filtro.getTexto().isBlank()) {
            sql.append(" AND (u.email ILIKE ? OR c.nombre ILIKE ? OR c.apellido ILIKE ?)");
            String termino = "%" + filtro.getTexto() + "%";
            params.add(termino);
            params.add(termino);
            params.add(termino);
        }

        if (filtro != null && filtro.getEstado() != null) {
            sql.append(" AND u.estado = ?");
            params.add(filtro.getEstado().name());
        }

        sql.append(" ORDER BY ").append(resolverCampoOrdenCliente(filtro));
        sql.append(" ").append(resolverDireccionOrden(filtro));

        return jdbcTemplate.query(sql.toString(), (rs, row) -> mapearCliente(rs), params.toArray());
    }

    @Override
    public List<Cliente> buscarClientesDelLocal(Long idLocal, DtFiltroClienteLocal filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT u.*, c.*
                FROM usuario u
                JOIN cliente c ON u.id = c.id
                JOIN pedido p ON p.idcliente = c.id
                WHERE p.idlocal = ? AND c.documento NOT ILIKE 'ANON-%'
                """);
        List<Object> params = new ArrayList<>();
        params.add(idLocal);

        if (filtro != null && filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
            sql.append(" AND (c.nombre ILIKE ? OR c.apellido ILIKE ?)");
            String termino = "%" + filtro.getNombre() + "%";
            params.add(termino);
            params.add(termino);
        }

        if (filtro != null && filtro.getCalificacionMinima() != null) {
            sql.append(" AND c.calificacionGlobal >= ?");
            params.add(filtro.getCalificacionMinima());
        }

        sql.append(" ORDER BY c.calificacionGlobal");
        sql.append(" ").append(
                filtro != null && "asc".equalsIgnoreCase(filtro.getDireccion()) ? "ASC" : "DESC"
        );

        return jdbcTemplate.query(sql.toString(), (rs, row) -> mapearCliente(rs), params.toArray());
    }

    private String resolverCampoOrdenCliente(DtFiltroUsuario filtro) {
        if (filtro == null || filtro.getOrdenarPor() == null) {
            return "c.calificacionGlobal";
        }
        return "calificacion".equalsIgnoreCase(filtro.getOrdenarPor()) ? "c.calificacionGlobal" : "c.calificacionGlobal";
    }

    private String resolverDireccionOrden(DtFiltroUsuario filtro) {
        if (filtro == null || filtro.getDireccion() == null) {
            return "DESC";
        }
        return "asc".equalsIgnoreCase(filtro.getDireccion()) ? "ASC" : "DESC";
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Timestamp sesionesInvalidadasTs = rs.getTimestamp("sesiones_invalidadas_desde");
        return Cliente.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .passwd(rs.getString("passwd"))
                .foto(rs.getString("foto"))
                .estado(EstadoCuenta.valueOf(rs.getString("estado")))
                .tipo(rs.getString("tipo"))
                .autenticadoConGoogle(rs.getObject("autenticado_con_google", Boolean.class))
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

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return jdbcTemplate.query(
                "SELECT u.*, c.* FROM usuario u JOIN cliente c ON u.id = c.id WHERE u.email = ?",
                (rs, row) -> mapearCliente(rs), email
        ).stream().findFirst();
    }
}

