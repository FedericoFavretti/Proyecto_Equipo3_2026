package com.example.demo.Persistencia.Repositorios;

public interface LocalCalificacionRepositorio {
    public void calificar(Long idLocal, Long idCalificacion);
    public Long obtenerLocal(Long idCalificacion);
}
