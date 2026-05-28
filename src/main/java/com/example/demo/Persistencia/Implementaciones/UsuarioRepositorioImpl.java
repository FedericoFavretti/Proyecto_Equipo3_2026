package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.RolUsuario;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
public class UsuarioRepositorioImpl implements UsuarioRepositorio {
    private final LocalRepositorio localRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final JdbcTemplate jdbcTemplate;


    public UsuarioRepositorioImpl(LocalRepositorio localRepo,
                                  ClienteRepositorio clienteRepo, JdbcTemplate jdbcTemplate) {
        this.localRepositorio = localRepo;
        this.clienteRepositorio = clienteRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.addAll(localRepositorio.listarTodos());
        usuarios.addAll(clienteRepositorio.listarTodos());
        return usuarios;
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        Optional<Usuario> local = localRepositorio.buscarPorId(id)
                .map(u -> u);
        if (local.isPresent()) {
            return local;
        }
        return clienteRepositorio.buscarPorId(id)
                .map(u -> u);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jdbcTemplate.query(
                "SELECT * FROM usuarios WHERE email = ?",
                (rs, row) -> mapUsuarioParaAutenticacion(rs),
                email
        ).stream().findFirst();
    }

    @Override
    public void guardar(Usuario usuario) {
        jdbcTemplate.update(
                "INSERT INTO usuarios (email, passwd, foto, estado, tipo) VALUES (?, ?, ?, ?, ?)",
                usuario.getEmail(),
                usuario.getPasswd(),
                usuario.getFoto(),
                usuario.getEstado() != null ? usuario.getEstado().name() : null,
                tipoPersistido(usuario)
        );
    }

    @Override
    public void actualizar(Usuario usuario) {
        if (usuario instanceof Local) {
            localRepositorio.actualizar((Local) usuario);
        } else if (usuario instanceof Cliente) {
            clienteRepositorio.actualizar((Cliente) usuario);
        }
    }

    @Override
    public void eliminar(long id) {
        Optional<Usuario> usuario = buscarPorId(id);
        if (usuario.isPresent()) {
            if (usuario.get() instanceof Local) {
                localRepositorio.eliminar(id);
            } else if (usuario.get() instanceof Cliente) {
                clienteRepositorio.eliminar(id);
            }
        }
    }

    private Usuario mapUsuarioParaAutenticacion(ResultSet rs) throws SQLException {
        RolUsuario tipo = RolUsuario.desdeTipo(getNullableString(rs, "tipo"));
        Usuario usuario = switch (tipo) {
            case ADMIN -> new Administrador();
            case LOCAL -> new Local();
            case CUSTOMER -> new Cliente();
        };

        usuario.setId(rs.getLong("id"));
        usuario.setEmail(getNullableString(rs, "email"));
        usuario.setPasswd(getNullableString(rs, "passwd"));
        usuario.setFoto(getNullableString(rs, "foto"));
        usuario.setEstado(EstadoCuenta.desdeValor(getNullableString(rs, "estado", "esado")));
        usuario.setTipo(tipo);
        usuario.setCreatedAt(getNullableInstant(rs, "created_at"));
        usuario.setUpdatedAt(getNullableInstant(rs, "updated_at"));

        return usuario;
    }

    private String tipoPersistido(Usuario usuario) {
        if (usuario.getTipo() != null) {
            return usuario.getTipo().getTipoPersistido();
        }
        if (usuario instanceof Administrador) {
            return RolUsuario.ADMIN.getTipoPersistido();
        }
        if (usuario instanceof Local) {
            return RolUsuario.LOCAL.getTipoPersistido();
        }
        return RolUsuario.CUSTOMER.getTipoPersistido();
    }

    private String getNullableString(ResultSet rs, String... columnNames) {
        for (String columnName : columnNames) {
            try {
                return rs.getString(columnName);
            } catch (SQLException ignored) {
                // Compatibilidad con esquemas anteriores mientras se consolida la persistencia horizontal.
            }
        }
        return null;
    }

    private Instant getNullableInstant(ResultSet rs, String columnName) {
        try {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toInstant() : null;
        } catch (SQLException ignored) {
            return null;
        }
    }

    @Override
    public void guardarTokenActivacion(long id, String token, java.time.Instant expira) {
        jdbcTemplate.update(
                "UPDATE usuarios SET token_activacion = ?, token_activacion_expira = ? WHERE id = ?",
                token, java.sql.Timestamp.from(expira), id
        );
    }

    @Override
    public void activarCuenta(long id) {
        jdbcTemplate.update(
                "UPDATE usuarios SET estado = ?, token_activacion = NULL, token_activacion_expira = NULL WHERE id = ?",
                EstadoCuenta.Activo.name(), id
        );
    }

    @Override
    public Optional<Usuario> buscarPorTokenActivacion(String token) {
        return jdbcTemplate.query(
                "SELECT * FROM usuarios WHERE token_activacion = ?",
                (rs, row) -> mapUsuarioParaAutenticacion(rs),
                token
        ).stream().findFirst();
    }
}
