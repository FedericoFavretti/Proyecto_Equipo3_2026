package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;

import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface iClienteController {
    ResponseEntity<Cliente> registrarUsuario(@RequestPart("datos") DtCliente dtCliente, @RequestPart("foto") MultipartFile foto);
    ResponseEntity<DtLoginResponse> registrarUsuarioGoogle(DtGoogleAuthRequest request);
    ResponseEntity<DtBusquedaPlatosPromocionesResponse> buscarPlatosYPromociones(@RequestBody DtFiltro dtFiltro);
    ResponseEntity<List<DtLocalBusquedaResponse>> buscarYListarLocales(@RequestBody DtFiltroLocal dtFiltroLocal);
}

