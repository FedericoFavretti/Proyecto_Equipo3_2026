package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.SolicitudCambioCorreo;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SolicitudCambioCorreoRepositorio {
    void guardar(SolicitudCambioCorreo solicitudCambioCorreo);
    void invalidarActivasPorUsuario(Long idUsuario);
    Optional<SolicitudCambioCorreo> buscarVigentePorTokenHash(String tokenHash);
    void marcarComoUsada(Long id, LocalDateTime fechaConsumo);
}
