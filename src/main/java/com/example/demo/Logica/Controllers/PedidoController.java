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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController implements iPedidoController {
    private final PedidoService pedidoService;
    private final PedidoResponseMapper pedidoResponseMapper;

    public PedidoController(PedidoService pedidoService, PedidoResponseMapper pedidoResponseMapper) {
        this.pedidoService = pedidoService;
        this.pedidoResponseMapper = pedidoResponseMapper;
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<DtPedidoResponse> confirmarPedido(
            @PathVariable Long idPedido,
            @RequestBody DtConfirmarPedidoRequest request) {
        Pedido pedido = pedidoService.confirmarPedido(idPedido, request.getTiempoEstimadoEntregaMinutos());
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/{idPedido}/rechazar")
    public ResponseEntity<Void> rechazarPedido(
            @PathVariable Long idPedido,
            @RequestBody DtRechazarPedidoRequest request) {
        pedidoService.rechazarPedido(idPedido, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping
    public ResponseEntity<DtPedidoResponse> realizarPedido(@RequestBody DtPedidoConDetalles dtPedidoConDetalles) {
        Pedido pedido = pedidoService.realizarPedido(dtPedidoConDetalles);
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido) {
        pedidoService.cancelarPedido(idPedido);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/listar-pedido-local/{idLocal}")
    public ResponseEntity<List<DtPedidoListadoResponse>> listarPedidos(@PathVariable Long idLocal, DtPedidoListadoFiltro dtPedidoListadoFiltro) {
        List<DtPedidoListadoResponse> pedidos = pedidoService.listarPedidos(idLocal, dtPedidoListadoFiltro);
        return ResponseEntity.ok(pedidos);
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/listar-pedido-cliente/{idCliente}")
    public ResponseEntity<List<DtPedidoListadoResponse>> buscarYListarHistorialPedidosPropios(@PathVariable Long idCliente, DtPedidoListadoFiltro dtPedidoListadoFiltro) {
        List<DtPedidoListadoResponse> pedidos = pedidoService.buscarYListarHistorialPedidosPropios(idCliente, dtPedidoListadoFiltro);
        return ResponseEntity.ok(pedidos);
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/mi-historial")
    public ResponseEntity<List<DtPedidoListadoResponse>> buscarYListarHistorialPedidosPropios(
            Authentication authentication,
            DtPedidoListadoFiltro dtPedidoListadoFiltro) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<DtPedidoListadoResponse> pedidos = pedidoService.buscarYListarHistorialPedidosPropios(
                authentication.getName(), dtPedidoListadoFiltro);
        return ResponseEntity.ok(pedidos);
    }
}