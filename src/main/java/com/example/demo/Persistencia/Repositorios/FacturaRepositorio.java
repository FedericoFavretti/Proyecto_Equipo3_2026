package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Factura;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaRepositorio {
    List<Factura> listarTodos();
    Optional<Factura> buscarPorId(Long id);
    Optional<Factura> buscarPorPedidoId(Long pedidoId);
    List<Factura> buscarPendientesDeProcesamiento(LocalDateTime fechaCorte);
    void guardar(Factura factura);
    void actualizar(Factura factura);
    void actualizarProcesoPdf(Factura factura);
    void eliminar(Long id);
}
