package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Interfaces.iCalificacionController;
import com.example.demo.Logica.Service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/calificar")
    public ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion){
        calificacionService.calificar(dtCalificacion);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/local/mi-calificacion")
    public ResponseEntity<Map<String, Object>> consultarCalificacionGlobalDelLocal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(calificacionService.consultarCalificacionGlobalDelLocal(authentication.getName()));
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/{idCliente}/calificacion")
    public ResponseEntity<DtCalificacionGlobalResponse> consultarCalificacionGlobal(@PathVariable("idCliente") Long idCliente) {
        DtCalificacionGlobalResponse response = calificacionService.consultarCalificacionGlobal(idCliente);
        return ResponseEntity.ok(response);
    }
}
