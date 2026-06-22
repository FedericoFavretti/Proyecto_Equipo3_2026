package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtResCuentaUsuario;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Service.AdminService;
import com.example.demo.Logica.DataTypes.request.DtFiltroUsuario;
import com.example.demo.Logica.DataTypes.response.DtUsuarioListadoResponse;
import com.example.demo.Logica.Enums.EstadoCuenta;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/solicitudes-locales/pendientes")
    public ResponseEntity<List<DtSolicitudLocalPendienteResponse>> listarSolicitudesPendientes() {
        return ResponseEntity.ok(adminService.listarSolicitudesPendientes());
    }

    @PutMapping("/solicitudes-locales/resolver_solicitud")
    public ResponseEntity<Void> resolverSolicitud(
            @RequestBody DtResolverSolicitudLocalRequest request) {
        if (request == null || request.getEstadoObjetivo() == null) {
            throw new IllegalArgumentException("Debe indicar el estado objetivo de la solicitud.");
        }
        adminService.resolverSolicitud(request);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/solicitudes-locales")
    public ResponseEntity<Void> resolverCuentaUsuario(@RequestBody DtResCuentaUsuario dtResCuentaUsuario) {
        adminService.resolverCuentaUsuario(dtResCuentaUsuario);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<DtUsuarioListadoResponse>> buscarYListarUsuarios(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String tipoUsuario,
            @RequestParam(required = false) EstadoCuenta estado,
            @RequestParam(required = false, defaultValue = "calificacion") String ordenarPor,
            @RequestParam(required = false, defaultValue = "desc") String direccion) {
        DtFiltroUsuario filtro = DtFiltroUsuario.builder()
                .texto(texto)
                .tipoUsuario(tipoUsuario)
                .estado(estado)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();
        List<DtUsuarioListadoResponse> usuarios = adminService.buscarYListarUsuarios(filtro);
        return ResponseEntity.ok(usuarios);
    }
}

