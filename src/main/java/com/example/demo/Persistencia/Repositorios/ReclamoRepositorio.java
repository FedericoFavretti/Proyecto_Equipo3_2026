package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Reclamo;

import java.util.List;
import java.util.Optional;

public interface ReclamoRepositorio {
    List<Reclamo> listarTodos();
    Optional<Reclamo> buscarPorId(long id);
    void guardar(Reclamo reclamo);
    void actualizar(Reclamo reclamo);
    void eliminar(long id);
}
