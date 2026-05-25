package com.example.demo.Logica.Service;

import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalificacionService {
    @Autowired
    private CalificacionRepositorio calificacionRepositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private LocalRepositorio  localRepositorio;

}
