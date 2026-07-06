package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.TokenActivacionCuenta;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenActivacionCuentaRepositorio {
    void guardar(TokenActivacionCuenta tokenActivacionCuenta);
    void invalidarActivosPorUsuario(Long idUsuario);
    Optional<TokenActivacionCuenta> buscarVigentePorTokenHash(String tokenHash);
    void marcarComoUsado(Long id, LocalDateTime fechaConsumo);
}