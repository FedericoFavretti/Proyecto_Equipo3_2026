package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
@Service
public class ClienteService {
    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Transactional
    public Cliente registrarUsuario(DtCliente dtCliente) {
        return null;
    }

    @Transactional
    public Cliente registrarUsuarioGoogle(DtCliente dtCliente){
        return null;
    }

    @Transactional
    public List<Plato> buscarPlatos(DtFiltro dtFiltro) {
        return null;
    }

    @Transactional
    public List<Local> listarLocales() {
        return null;
    }
}
