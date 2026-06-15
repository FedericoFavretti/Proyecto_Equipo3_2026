package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.request.DtLoginRequest;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Optional;
import com.example.demo.Logica.Clases.Usuario;


@Service
public class UsuarioService {

    private UsuarioRepositorio usuarioRepositorio;
    private EmailService emailService;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, EmailService emailService){
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
    }

    @Transactional
    public DtLoginResponse login(DtLoginRequest dtLogin) {
        return null;
    }

    @Transactional
    public void activarCuenta(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        usuarioRepositorio.activarCuenta(usuarioOpt.get().getId());
    }
}

