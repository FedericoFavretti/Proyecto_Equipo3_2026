package com.example.demo.Logica.Service;

import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalificacionService {

    private final CalificacionRepositorio calificacionRepositorio;

    private final ClienteRepositorio clienteRepositorio;

    private final LocalRepositorio  localRepositorio;

    public CalificacionService(CalificacionRepositorio calificacionRepositorio, ClienteRepositorio clienteRepositorio, LocalRepositorio  localRepositorio) {
        this.calificacionRepositorio = calificacionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
    }
}
