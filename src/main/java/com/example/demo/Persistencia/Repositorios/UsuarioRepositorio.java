package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void eliminar(Long id);
    void activarCuenta(Long id);
    boolean existeCorreo(String email);

}
