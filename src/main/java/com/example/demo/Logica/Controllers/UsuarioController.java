package com.example.demo.Logica.Controllers;
import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.Logica.DataTypes.response.DtPerfilResponse;
import com.example.demo.Logica.Interfaces.iUsuarioController;
import com.example.demo.Logica.Service.UsuarioService;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController implements iUsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @GetMapping("/perfil")
    public ResponseEntity<DtPerfilResponse> obtenerPerfil(Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(usuarioService.obtenerPerfil(authentication.getName()));
    }

    @GetMapping("/activar")
    public ResponseEntity<String> activarCuenta(@RequestParam String email) {
        usuarioService.activarCuenta(email);
        return ResponseEntity.ok("Cuenta activada correctamente.");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        usuarioService.cerrarSesion(token);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> editarDatosDeCuentaDeUsuario(
            @RequestParam Map<String, String> datos,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuarioService.editarDatosDeCuentaDeUsuario(authentication.getName(), authHeader, datos, foto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clientes/{idCliente}/cuenta-dev")
    public ResponseEntity<Void> eliminarCuentaDeUsuarioPropiaDev(@PathVariable Long idCliente) {
        usuarioService.eliminarCuentaDeUsuarioPropia(idCliente);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperar_contra_correo")
    public ResponseEntity<Void> recuperarPasswdPorCorreo(@RequestBody String correo) {
        usuarioService.recuperarPasswdPorCorreo(correo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperar")
    public ResponseEntity<Void> recuperarPasswd(@RequestBody DtRecuperarPasswd dtRecuperarPasswd) {
        usuarioService.recuperarPasswd(dtRecuperarPasswd);
        return ResponseEntity.noContent().build();
    }

    private boolean autenticacionInvalida(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equalsIgnoreCase(authentication.getName());
    }
}

