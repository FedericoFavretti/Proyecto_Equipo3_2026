package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepositorio {
    List<Categoria> listarPorLocal(Long idLocal);
    Optional<Categoria> buscarPorId(Long id);
    Optional<Categoria> buscarPorNombreYLocal(String nombre, Long idLocal);
    Categoria guardar(Categoria categoria);
    void eliminar(Long id);
}