package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Categoria;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.response.DtLocalPerfilResponse;
import com.example.demo.Logica.DataTypes.shared.DtCategoria;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtPromocionRequest;
import com.example.demo.Logica.DataTypes.request.DtFiltroClienteLocal;
import com.example.demo.Logica.DataTypes.response.DtClienteLocalResponse;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.DataTypes.response.DtPromocionesLocalResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface iLocalController {
    ResponseEntity<Plato> gestionarPlatoAlta(@RequestPart("datos") DtPlato dtPlato, @RequestPart("imagen") MultipartFile imagen);
    ResponseEntity<Plato> gestionarPlatoModificacion(@PathVariable("idPlato") Long idPlato, @RequestPart("datos") DtPlato dtPlato, @RequestPart(value = "imagen", required = false) MultipartFile imagen);
    ResponseEntity<Void> gestionarPlatoBaja(@PathVariable("idPlato") Long idPlato);
    ResponseEntity<Promocion> gestionarPromocionAlta(@RequestBody DtPromocionRequest request);
    ResponseEntity<Promocion> gestionarPromocionModificacion(@PathVariable("idPromocion") Long idPromocion, @RequestBody DtPromocionRequest request);
    ResponseEntity<Void> gestionarPromocionBaja(@PathVariable("idPromocion") Long idPromocion);
    ResponseEntity<Void> solicitarHabilitacion(@RequestPart("datos") DtLocal dtLocal,@RequestPart("logo") MultipartFile logo , @RequestPart("imagenes") List<MultipartFile> imagenes);
    ResponseEntity<Void> registrarApertura(@PathVariable("idLocal") Long idLocal);
    ResponseEntity<Void> regitrarCierre(@PathVariable("idLocal") Long idLocal);
    ResponseEntity<DtEstadisticasLocal> obtenerEstadisticas(@PathVariable Long idLocal, DtEstadisticasLocalFiltro filtro);
    ResponseEntity<List<DtClienteLocalResponse>> buscarYListarClientesDelLocal(@PathVariable("idLocal") Long idLocal, @RequestBody DtFiltroClienteLocal  DtFiltroClienteLocal);
    ResponseEntity<List<DtPlato>> buscarPlatosDeLocal(@PathVariable("idLocal") Long idLocal);
    ResponseEntity<DtPromocionesLocalResponse> buscaPromocionesDeLocal(@PathVariable("idLocal") Long idLocal);
    ResponseEntity<DtLocalPerfilResponse> obtenerPerfilPublico(@PathVariable("idLocal") Long idLocal);
    ResponseEntity<List<DtCategoria>> listarCategorias(@PathVariable Long idLocal);
    ResponseEntity<Categoria> altaCategoria(@RequestBody DtCategoria dto);
    ResponseEntity<Void> eliminarCategoria(@PathVariable Long idLocal, @PathVariable Long idCategoria);
}

