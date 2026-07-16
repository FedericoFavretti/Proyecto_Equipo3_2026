package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Categoria;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtCategoria;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Interfaces.iLocalController;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.Service.LocalService;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtPromocionRequest;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;
import com.example.demo.Logica.DataTypes.response.DtClienteLocalResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalPerfilResponse;
import com.example.demo.Logica.DataTypes.response.DtPromocionesLocalResponse;
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
    public ResponseEntity<Plato> gestionarPlatoAlta(@RequestPart("datos") DtPlato dtPlato, @RequestPart("imagen") MultipartFile imagen) {
        dtPlato.setImagen(cloudinaryService.subirImagen(imagen));
        Plato plato = localService.gestionarPlatoAlta(dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PreAuthorize("hasRole('Local')")
    @PutMapping(value = "/platos/{idPlato}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Plato> gestionarPlatoModificacion(@PathVariable("idPlato") Long idPlato, @RequestPart("datos") DtPlato dtPlato, @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        if (imagen != null && !imagen.isEmpty()) {
          dtPlato.setImagen(cloudinaryService.subirImagen(imagen));
        }
        Plato plato = localService.gestionarPlatoModificacion(idPlato, dtPlato);
        return ResponseEntity.ok(plato);
    }

    @PreAuthorize("hasAnyRole('Local','Cliente')")
    @GetMapping("/{idLocal}/categorias")
    public ResponseEntity<List<DtCategoria>> listarCategorias(@PathVariable Long idLocal) {
        return ResponseEntity.ok(localService.listarCategoriasDeLocal(idLocal));
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/categorias")
    public ResponseEntity<Categoria> altaCategoria(@RequestBody DtCategoria dto) {
        return ResponseEntity.ok(localService.altaCategoria(dto));
    }

    @PreAuthorize("hasRole('Local')")
    @DeleteMapping("/{idLocal}/categorias/{idCategoria}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long idLocal, @PathVariable Long idCategoria) {
        localService.eliminarCategoria(idCategoria, idLocal);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @DeleteMapping("/platos/eliminar/{idPlato}")
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

    @PreAuthorize("hasRole('Local')")
    @DeleteMapping("/promociones_baja/{idPromocion}")
    public ResponseEntity<Void> gestionarPromocionBaja(@PathVariable("idPromocion") Long idPromocion) {
        localService.gestionarPromocionBaja(idPromocion);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/solicitudes-habilitacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> solicitarHabilitacion(@RequestPart("datos") DtLocal dtLocal,@RequestPart("logo") MultipartFile logo , @RequestPart("imagenes") List<MultipartFile> imagenes){
        localService.validarPartesMultimediaRegistroLocal(logo, imagenes);
        List<String> urls = cloudinaryService.subirImagenes(imagenes);
        String logoLocal = cloudinaryService.subirImagen(logo);
        dtLocal.setFoto(logoLocal);
        dtLocal.setImagenes(urls);
        localService.solicitarHabilitacion(dtLocal);
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
    public ResponseEntity<DtEstadisticasLocal> obtenerEstadisticas(@PathVariable Long idLocal, DtEstadisticasLocalFiltro filtro) {
        DtEstadisticasLocal dtEstadisticasLocal = localService.obtenerEstadisticasLocal(idLocal, filtro);
        return ResponseEntity.ok(dtEstadisticasLocal);
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/{idLocal}/clientes")
    public ResponseEntity<List<DtClienteLocalResponse>> buscarYListarClientesDelLocal(
            @PathVariable("idLocal") Long idLocal,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double calificacionMinima,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String direccion) {
        DtFiltroClienteLocal filtro = DtFiltroClienteLocal.builder()
                .nombre(nombre)
                .calificacionMinima(calificacionMinima)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();
        List<DtClienteLocalResponse> clientes = localService.buscarYListarClientesDelLocal(idLocal, filtro);
        return ResponseEntity.ok(clientes);
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/busqueda_plato_local/{idLocal}")
    public ResponseEntity<List<DtPlato>> buscarPlatosDeLocal(@PathVariable("idLocal") Long idLocal) {
        return ResponseEntity.ok(localService.buscarPlatosDelocal(idLocal));
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/busqueda_promocion_local/{idLocal}")
    public ResponseEntity<DtPromocionesLocalResponse> buscaPromocionesDeLocal(@PathVariable("idLocal") Long idLocal) {
        return ResponseEntity.ok(localService.buscaPromocionesDeLocal(idLocal));
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/{idLocal}/perfil")
    public ResponseEntity<DtLocalPerfilResponse> obtenerPerfilPublico(@PathVariable("idLocal") Long idLocal) {
        return ResponseEntity.ok(localService.obtenerPerfilPublico(idLocal));
    }
}

