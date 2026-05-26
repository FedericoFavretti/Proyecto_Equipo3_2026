package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Plato;

import java.util.List;
import java.util.Optional;

public interface PlatoRepositorio {
    List<Plato> listarTodos();
    Optional<Plato> buscarPorId(long id);
    Plato guardar(Plato plato);
    Plato actualizar(Plato plato);
    void eliminar(long id);
    Optional<Plato> buscarPorNombre(String nombre);
}