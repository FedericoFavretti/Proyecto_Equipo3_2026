package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.DeviceToken;

import java.util.List;

public interface DeviceTokenRepositorio {
    void guardarOActualizar(DeviceToken token);
    List<DeviceToken> buscarActivosPorUsuario(Long usuarioId);
    void desactivarPorToken(String token);
    void desactivarPorUsuario(Long usuarioId);
}
