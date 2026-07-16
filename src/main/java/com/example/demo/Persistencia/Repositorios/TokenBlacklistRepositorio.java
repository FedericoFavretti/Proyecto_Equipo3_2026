package com.example.demo.Persistencia.Repositorios;

import java.time.LocalDateTime;

public interface TokenBlacklistRepositorio {
    void agregar(String token, LocalDateTime expiracion);
    boolean estaEnBlacklist(String token);
    void limpiarExpirados();
}
