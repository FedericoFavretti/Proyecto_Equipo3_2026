package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Interfaces.iLocalController;
import com.example.demo.Logica.Service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locales")
public class LocalController implements iLocalController {
    @Autowired
    private LocalService localService;

    @PostMapping("")
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestBody DtPlato dtPlato) {

            return null;
    }

    @PutMapping("")
    public ResponseEntity<Plato> gestionarPlatoModificacion(@RequestBody DtPlato dtPlato) {

        return null;
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> gestionarPlatoBaja(@RequestBody String nombre) {

        return null;
    }

    @PostMapping("")
    public ResponseEntity<Void> solicitarHabilitacion(@RequestBody DtLocal dtLocal){
        return null;
    }

    @PutMapping("")
    public ResponseEntity<Void> registrarApertura(@RequestBody long idLocal){
        return null;
    }

    @PutMapping("")
    public ResponseEntity<Void> regitrarCierre(@RequestBody long idLocal){
        return null;
    }
}
