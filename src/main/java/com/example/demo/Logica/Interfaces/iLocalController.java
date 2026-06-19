package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface iLocalController {
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestPart("datos") DtPlato dtPlato, @RequestPart("imagenes") List<MultipartFile> imagenes);

    public ResponseEntity<Plato> gestionarPlatoModificacion(@PathVariable("idPlato") Long idPlato, @RequestPart("datos") DtPlato dtPlato, @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes);

    ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") Long idPlato);

    public ResponseEntity<Void> solicitarHabilitacion(@RequestPart("datos") DtLocal dtLocal, @RequestPart("imagenes") List<MultipartFile> imagenes);

    ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") Long idLocal);

    ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") Long idLocal);

   ResponseEntity<DtEstadisticasLocal> obtenerEstadisticas(@PathVariable Long idLocal);
}

