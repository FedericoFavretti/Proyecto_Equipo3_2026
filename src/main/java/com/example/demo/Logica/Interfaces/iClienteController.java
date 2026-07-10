package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.request.DtGoogleRegistroCompletarRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtGoogleRegistroPendienteResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface iClienteController {
    ResponseEntity<Cliente> registrarUsuario(@RequestPart("datos") DtCliente dtCliente, @RequestPart("foto") MultipartFile foto);
    ResponseEntity<DtLoginResponseCliente> loginConGoogle(DtGoogleAuthRequest request);
    ResponseEntity<DtGoogleRegistroPendienteResponse> iniciarRegistroConGoogle(DtGoogleAuthRequest request);
    ResponseEntity<DtLoginResponseCliente> completarRegistroConGoogle(@RequestPart("datos") DtGoogleRegistroCompletarRequest datos,
                                                                      @RequestPart("foto") MultipartFile foto);
    ResponseEntity<DtBusquedaPlatosPromocionesResponse> buscarPlatosYPromociones(
            String nombre, Boolean precioMasBajo, Boolean precioMasAlto,
            Boolean promocionActiva, Boolean alfabetico, Long localId, Integer pagina, Integer tamanio);
    ResponseEntity<DtPagina<DtLocalBusquedaResponse>> buscarYListarLocales(
            String nombre, Double calificacionMinima, Boolean estaAbierto, String ordenarPor, String direccion,
            Integer pagina, Integer tamanio);
}
