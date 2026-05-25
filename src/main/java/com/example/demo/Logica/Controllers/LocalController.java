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
        Plato plato = localService.gestionarPlatoAlta(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PutMapping("")
    public ResponseEntity<Plato> gestionarPlatoModificacion(@RequestBody DtPlato dtPlato) {
        Plato plato = localService.gestionarPlatoModificacion(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @DeleteMapping("/{idPlato}")
    public ResponseEntity<Void> gestionarPlatoBaja(@RequestBody long idPlato) {
        localService.gestionarPlatoBaja(idPlato);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("")
    public ResponseEntity<Void> solicitarHabilitacion(@RequestBody DtLocal dtLocal){
        localService.solicitarHabilitacion(dtLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("")
    public ResponseEntity<Void> registrarApertura(@RequestBody long idLocal){
        localService.registrarApertura(idLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("")
    public ResponseEntity<Void> regitrarCierre(@RequestBody long idLocal){
        localService.regitrarCierre(idLocal);
        return ResponseEntity.ok().build();
    }
}
