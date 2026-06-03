package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface iLocalController {
    ResponseEntity<Plato> gestionarPlatoAlta(@RequestBody DtPlato dtPlato);

    ResponseEntity<Plato> gestionarPlatoModificacion(
            @PathVariable("idPlato") Long idPlato,
            @RequestBody DtPlato dtPlato);

    ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") Long idPlato);

    ResponseEntity<Void> solicitarHabilitacion(@RequestBody DtLocal dtLocal);

    ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") Long idLocal);

    ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") Long idLocal);
}
