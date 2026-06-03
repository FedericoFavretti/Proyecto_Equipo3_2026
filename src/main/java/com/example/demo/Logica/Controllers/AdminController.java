package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController implements iAdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("")
    public ResponseEntity<Void> resolverSolicitud(@RequestBody DtLocal dtLocal) {
        adminService.resolverSolicitudRegistroLocal(dtLocal.getId(), EstadoLocal.Habilitado);
        return ResponseEntity.ok().build();
    }
}