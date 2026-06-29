package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.Interfaces.iClienteController;
import com.example.demo.Logica.Service.ClienteService;
import com.example.demo.Logica.Service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController implements iClienteController {
    private final ClienteService  clienteService;
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
    public ResponseEntity<DtLoginResponse> registrarUsuarioGoogle(@RequestBody DtGoogleAuthRequest request) {
        DtLoginResponse response = clienteService.registrarOLoguearConGoogle(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/busqueda")
    public ResponseEntity<DtBusquedaPlatosPromocionesResponse> buscarPlatosYPromociones(@RequestBody DtFiltro dtFiltro) {
        DtBusquedaPlatosPromocionesResponse response = clienteService.buscarPlatosYPromociones(dtFiltro);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/listar_locales")
    public ResponseEntity<List<DtLocalBusquedaResponse>> buscarYListarLocales(@RequestBody DtFiltroLocal dtFiltroLocal) {
        List<DtLocalBusquedaResponse> locales = clienteService.buscarYListarLocales(dtFiltroLocal);
        return ResponseEntity.ok(locales);
    }
}

