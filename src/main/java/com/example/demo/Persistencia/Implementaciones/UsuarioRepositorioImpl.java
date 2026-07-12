package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Persistencia.Repositorios.AdministradorRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Repository
public class UsuarioRepositorioImpl implements UsuarioRepositorio {
    private final AdministradorRepositorio administradorRepositorio;
    private final LocalRepositorio localRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final JdbcTemplate jdbcTemplate;


    public UsuarioRepositorioImpl(AdministradorRepositorio administradorRepositorio,
                                  LocalRepositorio localRepo,
                                  ClienteRepositorio clienteRepo, JdbcTemplate jdbcTemplate) {
        this.administradorRepositorio = administradorRepositorio;
        this.localRepositorio = localRepo;
        this.clienteRepositorio = clienteRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.addAll(administradorRepositorio.listarTodos());
        usuarios.addAll(localRepositorio.listarTodos());
        usuarios.addAll(clienteRepositorio.listarTodos());
        return usuarios;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        Optional<Usuario> local = localRepositorio.buscarPorId(id)
                .map(u -> u);
        if (local.isPresent()) {
            return local;
        }
        Optional<Usuario> administrador = administradorRepositorio.buscarPorId(id)
                .map(u -> u);
        if (administrador.isPresent()) {
            return administrador;
        }
        return clienteRepositorio.buscarPorId(id)
                .map(u -> u);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, tipo FROM usuario WHERE email = ?",
                email
        );

        if (rows.isEmpty()) return Optional.empty();

        Map<String, Object> row = rows.get(0);
        String tipo = (String) row.get("tipo");
        Number id = (Number) row.get("id");
        Long usuarioId = id.longValue();

        if ("local".equalsIgnoreCase(tipo)) {
            return localRepositorio.buscarPorId(usuarioId).map(u -> u);
        } else if ("admin".equalsIgnoreCase(tipo) || "administrador".equalsIgnoreCase(tipo)) {
            return administradorRepositorio.buscarPorId(usuarioId).map(u -> u);
        } else if ("cliente".equalsIgnoreCase(tipo)) {
            return clienteRepositorio.buscarPorId(usuarioId).map(u -> u);
        }
        return Optional.empty();
    }

    @Override
    public void guardar(Usuario usuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO usuario (email, passwd, foto, estado, tipo, autenticado_con_google, celular) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, usuario.getEmail());
            ps.setString(2, usuario.getPasswd());
            ps.setString(3, usuario.getFoto());
            ps.setString(4, usuario.getEstado().name());
            ps.setString(5, usuario.getTipo());
            ps.setBoolean(6, Boolean.TRUE.equals(usuario.getAutenticadoConGoogle()));
            ps.setString(7, usuario.getCelular());
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            usuario.setId(id.longValue());
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        jdbcTemplate.update("UPDATE usuario SET email = ?, passwd = ?, foto = ?, estado = ?, tipo = ?, autenticado_con_google = ?, sesiones_invalidadas_desde = ?, celular = ? WHERE id = ?",
                usuario.getEmail(),
                usuario.getPasswd(),
                usuario.getFoto(),
                usuario.getEstado().name(),
                usuario.getTipo(),
                Boolean.TRUE.equals(usuario.getAutenticadoConGoogle()),
                usuario.getSesionesInvalidadasDesde(),
                usuario.getCelular(),
                usuario.getId()
                );
        if (usuario instanceof Local) {
            localRepositorio.actualizar((Local) usuario);
        } else if (usuario instanceof Cliente) {
            clienteRepositorio.actualizar((Cliente) usuario);
        } else if (usuario instanceof Administrador) {
            administradorRepositorio.actualizar((Administrador) usuario);
        }
    }

    @Override
    public void actualizarEstado(Long id, EstadoCuenta estado) {
        jdbcTemplate.update(
                "UPDATE usuario SET estado = ? WHERE id = ?",
                estado.name(),
                id
        );
    }

    @Override
    public void eliminar(Long id) {
        Optional<Usuario> usuario = buscarPorId(id);
        jdbcTemplate.update("DELETE FROM Usuario WHERE id = ?", id);
        if (usuario.isPresent()) {
            if (usuario.get() instanceof Administrador) {
                administradorRepositorio.eliminar(id);
            } else if (usuario.get() instanceof Local) {
                localRepositorio.eliminar(id);
            } else if (usuario.get() instanceof Cliente) {
                clienteRepositorio.eliminar(id);
            }
        }
    }

    @Override
    public void activarCuenta(Long id) {
        jdbcTemplate.update(
                "UPDATE usuario SET  estado = ? WHERE id = ?",
                EstadoCuenta.Activo.name(), id
        );
    }

    @Override
    public void actualizarPasswd(Long id, String passwdCodificada) {
        jdbcTemplate.update(
                "UPDATE usuario SET passwd = ? WHERE id = ?",
                passwdCodificada, id
        );
    }

    @Override
    public boolean existeCorreo(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE email = ?",
                Integer.class, email
        );
        return count != null && count > 0;
    }
}
