package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.TokenRecuperacionPasswd;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionPasswdRepositorio {
    void guardar(TokenRecuperacionPasswd tokenRecuperacionPasswd);
    void invalidarActivosPorUsuario(Long idUsuario);
    Optional<TokenRecuperacionPasswd> buscarVigentePorTokenHash(String tokenHash);
    void marcarComoUsado(Long id, LocalDateTime fechaConsumo);
}
