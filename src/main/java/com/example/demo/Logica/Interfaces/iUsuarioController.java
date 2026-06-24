package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.response.DtPerfilUsuarioResponse;
import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface iUsuarioController {
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
    ResponseEntity<DtPerfilUsuarioResponse> obtenerPerfil(Authentication authentication);
    ResponseEntity<Void> cerrarSesion(@RequestHeader("Authorization") String authHeader);
    ResponseEntity<Void> editarDatosDeCuentaDeUsuario(
            @RequestParam Map<String, String> datos,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication);
    ResponseEntity<Void> eliminarCuentaDeUsuarioPropiaDev(@PathVariable Long idCliente);
    public ResponseEntity<Void> recuperarPasswdPorCorreo(@RequestBody String correo);
    public ResponseEntity<Void> recuperarPasswd(@RequestBody DtRecuperarPasswd dtRecuperarPasswd);
}

