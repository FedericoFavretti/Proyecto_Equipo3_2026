package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.request.DtGoogleRegistroCompletarRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtGoogleRegistroPendienteResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.Interfaces.iClienteController;
import com.example.demo.Logica.Service.ClienteService;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController implements iClienteController {
    private final ClienteService clienteService;
    private final CloudinaryService cloudinaryService;

    public ClienteController(ClienteService clienteService, CloudinaryService cloudinaryService) {
        this.clienteService = clienteService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Cliente> registrarUsuario(@RequestPart("datos") DtCliente dtCliente, @RequestPart("foto") MultipartFile foto) {
        String url = cloudinaryService.subirImagen(foto);
        dtCliente.setFoto(url);
        Cliente cliente = clienteService.registrarUsuario(dtCliente);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping("/google")
    public ResponseEntity<DtLoginResponseCliente> loginConGoogle(@RequestBody DtGoogleAuthRequest request) {
        return ResponseEntity.ok(clienteService.loginConGoogle(request));
    }

    @PostMapping("/google/registro/iniciar")
    public ResponseEntity<DtGoogleRegistroPendienteResponse> iniciarRegistroConGoogle(@RequestBody DtGoogleAuthRequest request) {
        return ResponseEntity.ok(clienteService.iniciarRegistroConGoogle(request));
    }

    @PostMapping(value = "/google/registro/completar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DtLoginResponseCliente> completarRegistroConGoogle(@RequestPart("datos") DtGoogleRegistroCompletarRequest datos, @RequestPart("foto") MultipartFile foto) {
        datos.setFoto(cloudinaryService.subirImagen(foto));
        return ResponseEntity.ok(clienteService.completarRegistroConGoogle(datos));
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/busqueda")
    public ResponseEntity<DtBusquedaPlatosPromocionesResponse> buscarPlatosYPromociones(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean precioMasBajo,
            @RequestParam(required = false) Boolean precioMasAlto,
            @RequestParam(required = false) Boolean promocionActiva,
            @RequestParam(required = false) Boolean alfabetico,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanio) {
        DtFiltro dtFiltro = DtFiltro.builder()
                .nombre(nombre)
                .precioMasBajo(precioMasBajo)
                .precioMasAlto(precioMasAlto)
                .promocionActiva(promocionActiva)
                .alfabetico(alfabetico)
                .dtLocal(localId != null ? DtLocal.builder().id(localId).build() : null)
                .build();
        DtBusquedaPlatosPromocionesResponse response = clienteService.buscarPlatosYPromociones(dtFiltro, pagina, tamanio);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/listar_locales")
    public ResponseEntity<DtPagina<DtLocalBusquedaResponse>> buscarYListarLocales(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Double calificacionMinima,
            @RequestParam(required = false) Boolean estaAbierto,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanio) {
        DtFiltroLocal dtFiltroLocal = DtFiltroLocal.builder()
                .nombre(nombre)
                .calificacionMinima(calificacionMinima)
                .estaAbierto(estaAbierto)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();
        DtPagina<DtLocalBusquedaResponse> locales = clienteService.buscarYListarLocales(dtFiltroLocal, pagina, tamanio);
        return ResponseEntity.ok(locales);
    }
}
