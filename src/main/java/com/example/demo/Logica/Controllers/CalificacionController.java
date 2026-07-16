package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.response.DtCalificacionDetalleClienteResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionDetalleResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.response.DtMiCalificacionLocalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.Interfaces.iCalificacionController;
import com.example.demo.Logica.Service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.example.demo.Utils.AuthUtils.autenticacionInvalida;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calificaciones")
public class CalificacionController implements iCalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/calificar")
    public ResponseEntity<Void> calificar(@RequestBody DtCalificacion dtCalificacion, Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        calificacionService.calificar(dtCalificacion, authentication.getName());
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

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/{idCliente}/calificacion/detalle")
    public ResponseEntity<List<DtCalificacionDetalleResponse>> consultarCalificacionDetalle(@PathVariable("idCliente") Long idCliente) {
        return ResponseEntity.ok(calificacionService.consultarCalificacionesDetalladasDelCliente(idCliente));
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/local/mi-calificacion/detalle")
    public ResponseEntity<List<DtCalificacionDetalleClienteResponse>> consultarCalificacionDetalleDelLocal(Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(calificacionService.consultarCalificacionesDetalladasDelLocal(authentication.getName()));
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/locales/{idLocal}/detalle")
    public ResponseEntity<List<DtCalificacionDetalleClienteResponse>> consultarCalificacionDetalleDeLocalPorId(
            @PathVariable("idLocal") Long idLocal) {
        return ResponseEntity.ok(calificacionService.consultarCalificacionesDetalladasDelLocalPorId(idLocal));
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/locales/{idLocal}/mi-calificacion")
    public ResponseEntity<DtMiCalificacionLocalResponse> consultarMiCalificacionDeLocal(
            @PathVariable("idLocal") Long idLocal,
            Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DtMiCalificacionLocalResponse response =
                calificacionService.consultarMiCalificacionDeLocal(idLocal, authentication.getName());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}