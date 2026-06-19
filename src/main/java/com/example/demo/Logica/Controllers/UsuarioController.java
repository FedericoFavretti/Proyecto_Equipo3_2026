package com.example.demo.Logica.Controllers;
import com.example.demo.Logica.DataTypes.request.DtLoginRequest;
import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Logica.Interfaces.iUsuarioController;
import com.example.demo.Logica.Service.UsuarioService;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

