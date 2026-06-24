package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Calificacion;


import java.util.List;
import java.util.Optional;

public interface CalificacionRepositorio {
    List<Calificacion> listarTodos();
    Optional<Calificacion> buscarPorId(Long id);
    void guardar(Calificacion calificacion);
    void actualizar(Calificacion calificacion);
    void eliminar(Long id);
    List<Calificacion> buscarPorIds(List<Long> ids);
    List<Calificacion> listarPorLocal(Long idLocal);
    List<Calificacion> listarPorCliente(Long idCliente);
}
