package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.DtResloverHabilitacionLocal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface iAdminController {
    ResponseEntity<Void> resolverSolicitud(@RequestBody DtResloverHabilitacionLocal dtResloverHabilitacionLocal);
}
