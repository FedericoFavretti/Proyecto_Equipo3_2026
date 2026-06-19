package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;

import java.util.List;
import java.util.Optional;

public interface LocalRepositorio {
    List<Local> listarTodos();
    List<Local> listarPendientes();
    Optional<Local> buscarPorId(Long id);
    Optional<Local> buscarPorNombre(String nombre);
    void guardar(Local local);
    void actualizar(Local local);
    void eliminar(Long id);
    List<Local> buscarHabilitadosConFiltros(DtFiltroLocal filtro);
    List<Local> buscarUsuariosConFiltros(DtFiltroUsuario filtro);
}
