package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.*;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface iUsuarioController {
    ResponseEntity<DtLoginResponse> login(@Valid @RequestBody DtLoginRequest dtLoginRequest);
    ResponseEntity<Void> cerrarSesion(@RequestHeader("Authorization") String authHeader);
    ResponseEntity<Void> editarDatosDeCuentaDeUsuario(@RequestParam Map<String, String> datos, @RequestPart(value = "foto", required = false) MultipartFile foto, @RequestHeader("Authorization") String authHeader, Authentication authentication);
    ResponseEntity<Void> eliminarCuentaDeUsuarioPropiaDev(@PathVariable Long idCliente);
    ResponseEntity<Void> recuperarPasswdPorCorreo(@RequestBody String correo);
    ResponseEntity<Void> recuperarPasswd(@RequestBody DtRecuperarPasswd dtRecuperarPasswd);
    ResponseEntity<Void> iniciarCambioPasswd(@RequestBody DtIniciarCambioPasswdRequest request);
    ResponseEntity<Void> verificarCodigoCambioPasswd(@RequestBody DtVerificarCodigoRequest request);
    ResponseEntity<Void> confirmarCambioPasswd(@RequestBody DtConfirmarCambioPasswdRequest request);

}

