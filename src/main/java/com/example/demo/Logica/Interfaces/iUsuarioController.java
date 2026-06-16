package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtLoginRequest;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface iUsuarioController {
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
}

