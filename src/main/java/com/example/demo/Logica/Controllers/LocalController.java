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

    @PostMapping("/platos")
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestBody DtPlato dtPlato) {
        Plato plato = localService.altaPlato(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PutMapping("/platos/{idPlato}")
    public ResponseEntity<Plato> gestionarPlatoModificacion(
            @PathVariable("idPlato") long idPlato,
            @RequestBody DtPlato dtPlato) {
        Plato plato = localService.gestionarPlatoModificacion(idPlato, dtPlato);
        return ResponseEntity.ok(plato);
    }

    @DeleteMapping("/platos/{idPlato}")
    public ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") long idPlato) {
        localService.gestionarPlatoBaja(idPlato);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitudes-habilitacion")
    public ResponseEntity<Void> solicitarHabilitacion(@RequestBody DtLocal dtLocal) {
        localService.solicitarHabilitacion(dtLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idLocal}/apertura")
    public ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") long idLocal) {
        localService.registrarApertura(idLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idLocal}/cierre")
    public ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") long idLocal) {
        localService.regitrarCierre(idLocal);
        return ResponseEntity.ok().build();
    }
}
