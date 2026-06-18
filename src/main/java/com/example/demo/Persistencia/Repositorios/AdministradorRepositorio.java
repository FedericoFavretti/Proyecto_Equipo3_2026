package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Administrador;

import java.util.List;
import java.util.Optional;

public interface AdministradorRepositorio {
    List<Administrador> listarTodos();
    Optional<Administrador> buscarPorId(Long id);
    void guardar(Administrador administrador);
    void actualizar(Administrador administrador);
    void eliminar(Long id);
}
