package com.example.demo.Persistencia.Repositorios;

public interface LocalCalificacionRepositorio {
    void calificar(Long idLocal, Long idCalificacion);
    Long obtenerLocal(Long idCalificacion);
}
