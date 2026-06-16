package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtFiltro;

import java.util.List;
import java.util.Optional;

public interface PlatoRepositorio {
    List<Plato> listarTodos();
    Optional<Plato> buscarPorId(Long id);
    Plato guardar(Plato plato);
    Plato actualizar(Plato plato);
    void eliminar(Long id);
    Optional<Plato> buscarPorNombre(String nombre);
    List<Plato> buscarConFiltros(DtFiltro filtro);
}
