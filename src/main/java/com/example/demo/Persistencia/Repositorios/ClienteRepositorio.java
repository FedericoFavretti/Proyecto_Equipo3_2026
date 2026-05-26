package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Cliente;


import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio {
    List<Cliente> listarTodos();
    Optional<Cliente> buscarPorId(long id);
    void guardar(Cliente cliente);
    void actualizar(Cliente cliente);
    void eliminar(long id);
    Optional<Cliente> buscarPorEmail(String email);
    boolean existeCorreo(String email);
    boolean existeDocumento(String documento);
}
