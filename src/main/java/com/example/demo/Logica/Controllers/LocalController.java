package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Interfaces.iLocalController;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.Service.LocalService;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtPromocionRequest;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;
import com.example.demo.Logica.DataTypes.response.DtClienteLocalResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PreAuthorize("hasRole('Local')")
    @PostMapping(value = "/platos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestPart("datos") DtPlato dtPlato, @RequestPart("imagenes") List<MultipartFile> imagenes) {
        List<String> urls = cloudinaryService.subirImagenes(imagenes);
        dtPlato.setImagenes(urls);
        Plato plato = localService.altaPlato(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PreAuthorize("hasRole('Local')")
    @PutMapping(value = "/platos/{idPlato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Plato> gestionarPlatoModificacion(@PathVariable("idPlato") Long idPlato, @RequestPart("datos") DtPlato dtPlato, @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        if (imagenes != null && !imagenes.isEmpty()) {
            List<String> urls = cloudinaryService.subirImagenes(imagenes);
            dtPlato.setImagenes(urls);
        }
        Plato plato = localService.gestionarPlatoModificacion(idPlato, dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PreAuthorize("hasRole('Local')")
    @DeleteMapping("/platos/{idPlato}")
    public ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") Long idPlato) {
        localService.gestionarPlatoBaja(idPlato);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/promociones")
    public ResponseEntity<Promocion> gestionarPromocionAlta(@RequestBody DtPromocionRequest request) {
        Promocion promocion = localService.altaPromocion(request);
        return ResponseEntity.ok(promocion);
    }

    @PreAuthorize("hasRole('Local')")
    @PutMapping("/promociones/{idPromocion}")
    public ResponseEntity<Promocion> gestionarPromocionModificacion(@PathVariable("idPromocion") Long idPromocion, @RequestBody DtPromocionRequest request) {
        Promocion promocion = localService.gestionarPromocionModificacion(idPromocion, request);
        return ResponseEntity.ok(promocion);
    }

    @PostMapping(value = "/solicitudes-habilitacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> solicitarHabilitacion(@RequestPart("datos") DtLocal dtLocal, @RequestPart("imagenes") List<MultipartFile> imagenes){
        List<String> urls = cloudinaryService.subirImagenes(imagenes);
        dtLocal.setImagenes(urls);
        localService.solicitarRegistroComoLocalHabilitado(dtLocal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @PutMapping("/{idLocal}/apertura")
    public ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") Long idLocal) {
        localService.registrarApertura(idLocal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @PutMapping("/{idLocal}/cierre")
    public ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") Long idLocal) {
        localService.regitrarCierre(idLocal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/estadisticas/{idLocal}")
    public ResponseEntity<DtEstadisticasLocal> obtenerEstadisticas(@PathVariable Long idLocal) {
        DtEstadisticasLocal dtEstadisticasLocal = localService.obtenerEstadisticasLocal(idLocal);
        return ResponseEntity.ok(dtEstadisticasLocal);
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/{idLocal}/clientes")
    public ResponseEntity<List<DtClienteLocalResponse>> buscarYListarClientesDelLocal(@PathVariable("idLocal") Long idLocal, @RequestBody DtFiltroClienteLocal  DtFiltroClienteLocal) {
        List<DtClienteLocalResponse> clientes = localService.buscarYListarClientesDelLocal(idLocal, DtFiltroClienteLocal);
        return ResponseEntity.ok(clientes);
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/busqueda_plato_local/{idLocal}")
    public ResponseEntity<List<DtPlato>> buscarPlatosDeLocal(@PathVariable("idLocal") Long idLocal) {
        return ResponseEntity.ok(localService.buscarPlatosDelocal(idLocal));
    }

}

