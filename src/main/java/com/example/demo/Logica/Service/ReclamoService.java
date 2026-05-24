package com.example.demo.Logica.Service;

import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReclamoService {
    @Autowired
    private ReclamoRepositorio reclamoRepositorio;
}
