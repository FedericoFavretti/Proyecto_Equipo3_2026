package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Cliente;

public interface ClienteCalificacionRepositorio {
    public void calificar(Long idCliente, Long idCalificacion);
    public Long obtenerCliente(Long idCalificacion);
}
