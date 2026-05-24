package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.DtLoginRequest;
import com.example.demo.Logica.DataTypes.DtLoginResponse;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public DtLoginResponse login(DtLoginRequest dtLogin) {
        return null;
    }
}
