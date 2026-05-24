package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(long id);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void eliminar(long id);
}
