package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalService {
    @Autowired
    private LocalRepositorio localRepositorio;

    @Transactional
    public Plato gestionarPlatoAlta(DtPlato dtPlato) {

        return null;
    }

    @Transactional
    public Plato gestionarPlatoModificacion(DtPlato dtPlato) {

        return null;
    }

    @Transactional
    public Void gestionarPlatoBaja(String nombre) {

        return null;
    }

    @Transactional
    public Void solicitarHabilitacion(DtLocal dtLocal){
        return null;
    }

    @Transactional
    public Void registrarApertura(long idLocal){
        return null;
    }

    @Transactional
    public Void regitrarCierre(long idLocal){
        return null;
    }
}
