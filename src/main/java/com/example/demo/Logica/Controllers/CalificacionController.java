package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Interfaces.iCalificacionController;
import com.example.demo.Logica.Service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/calificaciones")
public class CalificacionController implements iCalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PutMapping("/calificar")
    public ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion){
        calificacionService.calificar(dtCalificacion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/local/mi-calificacion")
    public ResponseEntity<Map<String, Object>> consultarCalificacionGlobalDelLocal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(calificacionService.consultarCalificacionGlobalDelLocal(authentication.getName()));
    }

    @GetMapping("/local/{idLocal}/mi-calificacion-dev")
    public ResponseEntity<Map<String, Object>> consultarCalificacionGlobalDelLocalDev(@PathVariable Long idLocal) {
        return ResponseEntity.ok(calificacionService.consultarCalificacionGlobalDelLocalPorId(idLocal));
    }

}
