package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.DtResloverHabilitacionLocal;

import com.example.demo.Logica.Interfaces.iAdminController;
import com.example.demo.Logica.Service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController implements iAdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/resolver_solicitud")
    public ResponseEntity<Void> resolverSolicitud(@RequestBody DtResloverHabilitacionLocal dtResloverHabilitacionLocal) {
        adminService.resolverSolicitud(dtResloverHabilitacionLocal);
        return ResponseEntity.ok().build();
    }
}