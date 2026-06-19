package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface iUsuarioController {
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
    public ResponseEntity<Void> cerrarSesion(@RequestHeader("Authorization") String authHeader);
    public ResponseEntity<Void> recuperarPasswdPorCorreo(@RequestBody String correo);
    public ResponseEntity<Void> recuperarPasswd(@RequestBody DtRecuperarPasswd dtRecuperarPasswd);
}

