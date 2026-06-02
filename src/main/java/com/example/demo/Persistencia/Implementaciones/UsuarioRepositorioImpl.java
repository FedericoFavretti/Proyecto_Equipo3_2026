package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;
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
import java.util.Map;
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
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, tipo FROM usuario WHERE email = ?",
                email
        );

        if (rows.isEmpty()) return Optional.empty();

        Map<String, Object> row = rows.get(0);
        String tipo = (String) row.get("tipo");
        Long id = (Long) row.get("id");

        if (tipo.equals("local")) {
            return localRepositorio.buscarPorId(id).map(u -> u);
        } else if (tipo.equals("cliente")) {
            return clienteRepositorio.buscarPorId(id).map(u -> u);
        }
        return Optional.empty();
    }

    @Override
    public void guardar(Usuario usuario) {
        jdbcTemplate.update(
                "INSERT INTO usuario (email, passwd, foto, estado, tipo) VALUES (?, ?, ?, ?, ?)",
                usuario.getEmail(),
                usuario.getPasswd(),
                usuario.getFoto(),
                usuario.getEstado() != null ? usuario.getEstado().name() : null,
                usuario.getTipo()
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
}
