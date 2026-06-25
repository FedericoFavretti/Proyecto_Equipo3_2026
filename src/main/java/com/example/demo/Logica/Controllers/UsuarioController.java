package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.*;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Logica.Interfaces.iUsuarioController;
import com.example.demo.Logica.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<DtLoginResponse> login(@Valid @RequestBody DtLoginRequest dtLoginRequest) {
        return ResponseEntity.ok(usuarioService.login(dtLoginRequest));
    }
    @GetMapping("/activar")
    public ResponseEntity<String> activarCuenta(@RequestParam String email) {
        usuarioService.activarCuenta(email);
        return ResponseEntity.ok("Cuenta activada correctamente.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        usuarioService.cerrarSesion(token);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> editarDatosDeCuentaDeUsuario(
            @RequestParam Map<String, String> datos,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuarioService.editarDatosDeCuentaDeUsuario(authentication.getName(), authHeader, datos, foto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/mi-cuenta")
    public ResponseEntity<Void> eliminarMiCuenta(Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuarioService.eliminarMiCuenta(authentication.getName());
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/recuperar_contra_correo")
    public ResponseEntity<Void> recuperarPasswdPorCorreo(@RequestBody String correo) {
        usuarioService.recuperarPasswdPorCorreo(correo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperar")
    public ResponseEntity<Void> recuperarPasswd(@RequestBody DtRecuperarPasswd dtRecuperarPasswd) {
        usuarioService.recuperarPasswd(dtRecuperarPasswd);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cambiar-passwd/iniciar")
    public ResponseEntity<Void> iniciarCambioPasswd(@RequestBody DtIniciarCambioPasswdRequest request) {
        usuarioService.iniciarCambioPasswd(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cambiar-passwd/verificar-codigo")
    public ResponseEntity<Void> verificarCodigoCambioPasswd(@RequestBody DtVerificarCodigoRequest request) {
        usuarioService.verificarCodigoCambioPasswd(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cambiar-passwd/confirmar")
    public ResponseEntity<Void> confirmarCambioPasswd(@RequestBody DtConfirmarCambioPasswdRequest request) {
        usuarioService.confirmarCambioPasswd(request);
        return ResponseEntity.ok().build();
    }
}

