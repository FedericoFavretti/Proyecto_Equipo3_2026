package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Notificacion;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepositorio {
    List<Notificacion> listarTodos();
    Optional<Notificacion> buscarPorId(Long id);
    void guardar(Notificacion notificacion);
    void actualizar(Notificacion notificacion);
    void eliminar(Long id);
}
