package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;

import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface iClienteController {
    public ResponseEntity<Cliente> registrarUsuario(@RequestPart("datos") DtCliente dtCliente, @RequestPart("foto") MultipartFile foto);
    ResponseEntity<Cliente> registrarUsuarioGoogle(@RequestBody DtCliente dtCliente);
    ResponseEntity<DtBusquedaPlatosPromocionesResponse> buscarPlatosYPromociones(@RequestBody DtFiltro dtFiltro);
    ResponseEntity<List<DtLocalBusquedaResponse>> buscarYListarLocales(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double calificacionMinima,
            @RequestParam(required = false) Boolean estaAbierto,
            @RequestParam(required = false, defaultValue = "nombre") String ordenarPor,
            @RequestParam(required = false, defaultValue = "desc") String direccion
    );
    ResponseEntity<DtCalificacionGlobalResponse> consultarCalificacionGlobal(@PathVariable("idCliente") Long idCliente);
}

