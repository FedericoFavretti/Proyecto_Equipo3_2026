package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Local;

import java.util.List;
import java.util.Optional;

public interface LocalRepositorio {
    List<Local> listarTodos();
    List<Local> listarPendientes();
    Optional<Local> buscarPorId(long id);
    Optional<Local> buscarPorNombre(String nombre);
    void guardar(Local local);
    void actualizar(Local local);
    void eliminar(long id);
}
