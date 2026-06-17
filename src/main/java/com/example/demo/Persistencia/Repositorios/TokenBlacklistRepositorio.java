package com.example.demo.Persistencia.Repositorios;

import java.time.LocalDateTime;

public interface TokenBlacklistRepositorio {
    public void agregar(String token, LocalDateTime expiracion);
    public boolean estaEnBlacklist(String token);
    public void limpiarExpirados();
}
