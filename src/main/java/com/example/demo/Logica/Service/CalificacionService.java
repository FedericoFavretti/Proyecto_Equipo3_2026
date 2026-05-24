package com.example.demo.Logica.Service;

import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalificacionService {
    @Autowired
    private CalificacionRepositorio calificacionRepositorio;

}
