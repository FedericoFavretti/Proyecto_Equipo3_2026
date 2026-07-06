package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtNotificacion;
import com.example.demo.Logica.Service.NotificacionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.Utils.AuthUtils.autenticacionInvalida;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local')")
    @GetMapping("/mias")
    public ResponseEntity<List<DtNotificacion>> listarMisNotificaciones(Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(notificacionService.listarMisNotificaciones(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('Cliente', 'Local')")
    @PutMapping("/{idNotificacion}/leida")
    public ResponseEntity<Void> marcarComoLeida(Authentication authentication, @PathVariable Long idNotificacion) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificacionService.marcarComoLeida(authentication.getName(), idNotificacion);
        return ResponseEntity.ok().build();
    }
}