package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtResCuentaUsuario;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Service.AdminService;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.response.DtUsuarioListadoResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController implements iAdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/solicitudes-locales/pendientes")
    public ResponseEntity<List<DtSolicitudLocalPendienteResponse>> listarSolicitudesPendientes() {
        return ResponseEntity.ok(adminService.listarSolicitudesPendientes());
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/solicitudes-locales/resolver_solicitud")
    public ResponseEntity<Void> resolverSolicitud(@RequestBody DtResolverSolicitudLocalRequest request) {
        adminService.resolverSolicitud(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping("/cuentas-usuario/resolver")
    public ResponseEntity<Void> resolverCuentaUsuario(@RequestBody DtResCuentaUsuario dtResCuentaUsuario) {
        adminService.resolverCuentaUsuario(dtResCuentaUsuario);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Admin')")
    @PostMapping("/usuarios")
    public ResponseEntity<List<DtUsuarioListadoResponse>> buscarYListarUsuarios(@RequestBody DtFiltroUsuario  dtFiltroUsuario) {
        List<DtUsuarioListadoResponse> usuarios = adminService.buscarYListarUsuarios(dtFiltroUsuario);
        return ResponseEntity.ok(usuarios);
    }
}

