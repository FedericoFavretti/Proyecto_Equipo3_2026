package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.DtLoginRequest;
import com.example.demo.Logica.DataTypes.DtLoginResponse;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Optional;
import com.example.demo.Logica.Clases.Usuario;


@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    @Transactional
    public DtLoginResponse login(DtLoginRequest dtLogin) {
        return null;
    }

    @Transactional
    public void activarCuenta(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorTokenActivacion(token);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "El enlace de activación ha expirado. Se ha enviado uno nuevo a su correo.");
        }
        Usuario usuario = usuarioOpt.get();
        java.time.Instant expira = jdbcTemplate.queryForObject(
                "SELECT token_activacion_expira FROM usuarios WHERE id = ?",
                java.sql.Timestamp.class, usuario.getId()
        ).toInstant();
        if (java.time.Instant.now().isAfter(expira)) {
            String nuevoToken = java.util.UUID.randomUUID().toString();
            java.time.Instant nuevaExpira = java.time.Instant.now()
                    .plus(24, java.time.temporal.ChronoUnit.HOURS);
            usuarioRepositorio.guardarTokenActivacion(usuario.getId(), nuevoToken, nuevaExpira);
            emailService.enviarMailDeActivacion(usuario.getEmail(), nuevoToken);
            throw new IllegalArgumentException(
                    "El enlace de activación ha expirado. Se ha enviado uno nuevo a su correo.");
        }
        usuarioRepositorio.activarCuenta(usuario.getId());
    }
}
