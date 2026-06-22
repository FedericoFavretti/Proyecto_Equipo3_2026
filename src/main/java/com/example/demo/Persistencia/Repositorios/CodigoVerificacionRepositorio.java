package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.CodigoVerificacion;

import java.util.Optional;

public interface CodigoVerificacionRepositorio {
    void guardar(CodigoVerificacion codigoVerificacion);
    Optional<CodigoVerificacion> buscarVigentePorUsuario(Long idUsuario);
    void actualizar(CodigoVerificacion codigoVerificacion);
}