package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Factura;

import java.util.List;
import java.util.Optional;

public interface FacturaRepositorio {
    List<Factura> listarTodos();
    Optional<Factura> buscarPorId(long id);
    void guardar(Factura factura);
    void actualizar(Factura factura);
    void eliminar(long id);
}
