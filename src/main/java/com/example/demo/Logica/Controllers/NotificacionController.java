package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtRegistroTokenRequest;
import com.example.demo.Logica.DataTypes.shared.DtNotificacion;
import com.example.demo.Logica.Service.DeviceTokenService;
import com.example.demo.Logica.Service.NotificacionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.demo.Utils.AuthUtils.autenticacionInvalida;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final DeviceTokenService deviceTokenService;

    public NotificacionController(NotificacionService notificacionService,
                                  DeviceTokenService deviceTokenService) {
        this.notificacionService = notificacionService;
        this.deviceTokenService = deviceTokenService;
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local', 'Admin')")
    @GetMapping("/mias")
    public ResponseEntity<List<DtNotificacion>> listarMisNotificaciones(Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificacionService.listarMisNotificaciones(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local', 'Admin')")
    @PutMapping("/{idNotificacion}/leida")
    public ResponseEntity<Void> marcarComoLeida(Authentication authentication, @PathVariable Long idNotificacion) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificacionService.marcarComoLeida(authentication.getName(), idNotificacion);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local', 'Admin')")
    @PostMapping("/token")
    public ResponseEntity<Void> registrarToken(Authentication authentication,
                                               @RequestBody DtRegistroTokenRequest request) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        deviceTokenService.registrarToken(authentication.getName(), request.getToken(), request.getPlataforma());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local', 'Admin')")
    @DeleteMapping("/token")
    public ResponseEntity<Void> eliminarToken(Authentication authentication,
                                              @RequestBody Map<String, String> body) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        deviceTokenService.eliminarToken(body.get("token"));
        return ResponseEntity.ok().build();
    }
}