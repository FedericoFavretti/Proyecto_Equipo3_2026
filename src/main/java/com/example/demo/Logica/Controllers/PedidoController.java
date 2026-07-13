package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtConfirmarPedidoRequest;
import com.example.demo.Logica.DataTypes.request.DtRechazarPedidoRequest;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.DataTypes.response.DtPedidoResponse;
import com.example.demo.Logica.Interfaces.iPedidoController;
import com.example.demo.Logica.Mappers.PedidoResponseMapper;
import com.example.demo.Logica.Service.PedidoService;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


import static com.example.demo.Utils.AuthUtils.autenticacionInvalida;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController implements iPedidoController {
    private static final String CLIENTE_MOBILE = "mobile";
    private final PedidoService pedidoService;
    private final PedidoResponseMapper pedidoResponseMapper;

    public PedidoController(PedidoService pedidoService, PedidoResponseMapper pedidoResponseMapper) {
        this.pedidoService = pedidoService;
        this.pedidoResponseMapper = pedidoResponseMapper;
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<DtPedidoResponse> confirmarPedido(@PathVariable Long idPedido, @RequestBody DtConfirmarPedidoRequest request) {
        Pedido pedido = pedidoService.confirmarPedido(idPedido, request.getTiempoEstimadoEntregaMinutos());
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/{idPedido}/rechazar")
    public ResponseEntity<Void> rechazarPedido(@PathVariable Long idPedido, @RequestBody DtRechazarPedidoRequest request) {
        pedidoService.rechazarPedido(idPedido, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping
    public ResponseEntity<DtPedidoResponse> realizarPedido(
            @RequestBody DtPedidoConDetalles dtPedidoConDetalles,
            @RequestHeader(value = "X-Foodly-Client", required = false) String clienteOrigen) {
        boolean esClienteMobile = CLIENTE_MOBILE.equalsIgnoreCase(clienteOrigen);
        Pedido pedido = pedidoService.realizarPedido(dtPedidoConDetalles, esClienteMobile);
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido, Authentication authentication) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        pedidoService.cancelarPedidoDeCliente(authentication.getName(), idPedido);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/{idPedido}/reintentar-pago")
    public ResponseEntity<DtPedidoResponse> reintentarPago(
            @PathVariable Long idPedido,
            Authentication authentication,
            @RequestHeader(value = "X-Foodly-Client", required = false) String clienteOrigen) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean esClienteMobile = CLIENTE_MOBILE.equalsIgnoreCase(clienteOrigen);
        Pedido pedido = pedidoService.reintentarPago(authentication.getName(), idPedido, esClienteMobile);
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/listar-pedido-local/{idLocal}")
    public ResponseEntity<DtPagina<DtPedidoListadoResponse>> listarPedidos(@PathVariable Long idLocal, DtPedidoListadoFiltro dtPedidoListadoFiltro) {
        DtPagina<DtPedidoListadoResponse> pedidos = pedidoService.listarPedidos(idLocal, dtPedidoListadoFiltro);
        return ResponseEntity.ok(pedidos);
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/mi-historial")
    public ResponseEntity<DtPagina<DtPedidoListadoResponse>> buscarYListarHistorialPedidosPropios(
            Authentication authentication,
            DtPedidoListadoFiltro dtPedidoListadoFiltro) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DtPagina<DtPedidoListadoResponse> pedidos = pedidoService.buscarYListarHistorialPedidosPropios(
                authentication.getName(), dtPedidoListadoFiltro);
        return ResponseEntity.ok(pedidos);
    }
}
