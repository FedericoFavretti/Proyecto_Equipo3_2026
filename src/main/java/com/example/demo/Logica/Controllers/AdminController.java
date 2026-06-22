package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtResCuentaUsuario;
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

    @PutMapping("/resolver_solicitud")
    public ResponseEntity<Void> resolverSolicitud(
            @RequestBody DtResolverSolicitudLocalRequest request) {
        if (request == null || request.getEstadoObjetivo() == null) {
            throw new IllegalArgumentException("Debe indicar el estado objetivo de la solicitud.");
        }
        adminService.resolverSolicitud(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> resolverCuentaUsuario(@RequestBody DtResCuentaUsuario dtResCuentaUsuario) {
        adminService.resolverCuentaUsuario(dtResCuentaUsuario);
        return ResponseEntity.ok().build();
    }
}

