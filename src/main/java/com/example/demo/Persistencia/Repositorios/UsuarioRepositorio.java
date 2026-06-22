package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Enums.EstadoCuenta;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void actualizarEstado(Long id, EstadoCuenta estado);
    void eliminar(Long id);
    void activarCuenta(Long id);
    boolean existeCorreo(String email);
    void actualizarPasswd(Long id, String passwdCodificada);

}
