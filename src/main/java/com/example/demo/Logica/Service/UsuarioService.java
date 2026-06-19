package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.request.DtLoginRequest;
import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Persistencia.Repositorios.TokenBlacklistRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import com.example.demo.Logica.Clases.Usuario;
import org.springframework.web.bind.annotation.RequestBody;


@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistRepositorio tokenBlacklistRepositorio;
    private final PasswordEncoder  passwordEncoder;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, EmailService emailService, AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService, TokenBlacklistRepositorio tokenBlacklistRepositorio,  PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistRepositorio = tokenBlacklistRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.email());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    @Transactional
    public void activarCuenta(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        usuarioRepositorio.activarCuenta(usuarioOpt.get().getId());
    }

    @Transactional
    public void cerrarSesion(String token) {
        LocalDateTime expiracion = jwtService.getExpiracion(token);
        tokenBlacklistRepositorio.agregar(token, expiracion);
    }

    @Transactional
    public void recuperarPasswdPorCorreo(String correo) {
        if (usuarioRepositorio.buscarPorEmail(correo).isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        String token = jwtService.generarTokenRecuperacion(correo);
        String link = "http://localhost:8080/api/v1/usuarios/recuperar?token=" + token;

        emailService.recuperarPasswdPorCorreo(correo, link);
    }

    @Transactional
    public void recuperarPasswd(DtRecuperarPasswd dtRecuperarPasswd){
        String correo = jwtService.validarYObtenerCorreoRecuperacion(dtRecuperarPasswd.getToken());
        Usuario usuario = usuarioRepositorio.buscarPorEmail(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setPasswd(passwordEncoder.encode(dtRecuperarPasswd.getNuevaPasswd()));
        usuarioRepositorio.actualizar(usuario);
    }

    @Transactional
    public void cerrarTodasLasSesiones(Long idUsuario) {
        Usuario usuario = usuarioRepositorio.buscarPorId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuario.setSesionesInvalidadasDesde(LocalDateTime.now());
        usuarioRepositorio.actualizar(usuario);
    }

}

