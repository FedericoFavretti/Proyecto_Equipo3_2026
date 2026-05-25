package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    @Autowired
    private LocalRepositorio localRepositorio;

    @Transactional
    public void resolverSolicitud(DtLocal dtLocal) {

    }
}
