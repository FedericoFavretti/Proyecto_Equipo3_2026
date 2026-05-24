package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
    public void guardar(Usuario usuario) {
        jdbcTemplate.update(
                "INSERT INTO usuarios (email, passwd, foto, esado, tipo) VALUES (?, ?, ?, ?, ?)",
                usuario.getEmail(),
                usuario.getPasswd(),
                usuario.getFoto(),
                usuario.getEstado(),
                usuario instanceof Local ? "LOCAL" : "CLIENTE"
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
