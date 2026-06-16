package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins/solicitudes-locales")
public class AdminController implements iAdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<DtSolicitudLocalPendienteResponse>> listarSolicitudesPendientes() {
        return ResponseEntity.ok(adminService.listarSolicitudesPendientes());
    }

    @PutMapping("/{idLocal}")
    public ResponseEntity<Void> resolverSolicitud(
            @PathVariable Long idLocal,
            @RequestBody DtResolverSolicitudLocalRequest request) {
        if (request == null || request.getEstadoObjetivo() == null) {
            throw new IllegalArgumentException("Debe indicar el estado objetivo de la solicitud.");
        }
        adminService.resolverSolicitud(idLocal, request);
        return ResponseEntity.noContent().build();
    }
}

