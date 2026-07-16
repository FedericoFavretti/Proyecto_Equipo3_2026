package com.example.demo.Persistencia.Repositorios;

import java.util.List;

public interface ClienteCalificacionRepositorio {
    public void calificar(Long idCliente, Long idCalificacion);
    public Long obtenerCliente(Long idCalificacion);
    public List<Long> obtenerCalificacionesDeCliente(Long idCliente);
}