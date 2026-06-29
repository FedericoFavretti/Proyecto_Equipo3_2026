package com.example.demo.Persistencia.Repositorios;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio {
    List<Cliente> listarTodos();
    Optional<Cliente> buscarPorId(Long id);
    void guardar(Cliente cliente);
    void actualizar(Cliente cliente);
    void eliminar(Long id);
    boolean existeDocumento(String documento);
    List<Cliente> buscarConFiltros(DtFiltroUsuario filtro);
    List<Cliente> buscarClientesDelLocal(Long idLocal, DtFiltroClienteLocal filtro);
    Optional<Cliente> buscarPorEmail(String email);
}
