package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtFiltro;

import java.util.List;
import java.util.Optional;

public interface PromocionRepositorio {
    List<Promocion> listarTodos();
    Optional<Promocion> buscarPorId(long id);
    void guardar(Promocion promocion);
    void actualizar(Promocion promocion);
    void eliminar(long id);
    List<Promocion> buscarActivasConFiltros(DtFiltro filtro);
}

