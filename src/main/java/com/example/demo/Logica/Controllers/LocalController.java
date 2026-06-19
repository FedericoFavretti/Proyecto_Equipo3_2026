package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Interfaces.iLocalController;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.Service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locales")
public class LocalController implements iLocalController {
    private final LocalService localService;
    private final CloudinaryService cloudinaryService;

    public LocalController(LocalService localService, CloudinaryService cloudinaryService) {
        this.localService = localService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/platos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestPart("datos") DtPlato dtPlato, @RequestPart("imagenes") List<MultipartFile> imagenes) {
        List<String> urls = cloudinaryService.subirImagenes(imagenes);
        dtPlato.setImagenes(urls);
        Plato plato = localService.altaPlato(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PutMapping(value = "/platos/{idPlato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Plato> gestionarPlatoModificacion(@PathVariable("idPlato") Long idPlato, @RequestPart("datos") DtPlato dtPlato, @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        if (imagenes != null && !imagenes.isEmpty()) {
            List<String> urls = cloudinaryService.subirImagenes(imagenes);
            dtPlato.setImagenes(urls);
        }
        Plato plato = localService.gestionarPlatoModificacion(idPlato, dtPlato);
        return ResponseEntity.ok(plato);
    }

    @DeleteMapping("/platos/{idPlato}")
    public ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") Long idPlato) {
        localService.gestionarPlatoBaja(idPlato);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/solicitudes-habilitacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> solicitarHabilitacion(@RequestPart("datos") DtLocal dtLocal, @RequestPart("imagenes") List<MultipartFile> imagenes){
        List<String> urls = cloudinaryService.subirImagenes(imagenes);
        dtLocal.setImagenes(urls);
        localService.solicitarRegistroComoLocalHabilitado(dtLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idLocal}/apertura")
    public ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") Long idLocal) {
        localService.registrarApertura(idLocal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idLocal}/cierre")
    public ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") Long idLocal) {
        localService.regitrarCierre(idLocal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<DtEstadisticasLocal> obtenerEstadisticas(@PathVariable Long idLocal) {
        DtEstadisticasLocal dtEstadisticasLocal = localService.obtenerEstadisticasLocal(idLocal);
        return ResponseEntity.ok(dtEstadisticasLocal);
    }
}

