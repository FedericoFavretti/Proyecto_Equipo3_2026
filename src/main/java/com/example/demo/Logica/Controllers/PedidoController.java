package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.*;
import com.example.demo.Logica.Interfaces.iPedidoController;
import com.example.demo.Logica.Mappers.PedidoResponseMapper;
import com.example.demo.Logica.Service.PedidoService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<DtPedidoResponse> confirmarPedido(
            @PathVariable Long idPedido,
            @RequestBody DtConfirmarPedidoRequest request) {
        Pedido pedido = pedidoService.confirmarPedido(idPedido, request.getTiempoEstimadoEntregaMinutos());
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PostMapping("/{idPedido}/rechazar")
    public ResponseEntity<Void> rechazarPedido(@PathVariable Long idPedido) {
        pedidoService.rechazarPedido(idPedido);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<DtPedidoResponse> realizarPedido(@RequestBody DtPedidoConDetalles dtPedidoConDetalles) {
        Pedido pedido = pedidoService.realizarPedido(dtPedidoConDetalles);
        return ResponseEntity.ok(pedidoResponseMapper.toResponse(pedido));
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido) {
        pedidoService.cancelarPedido(idPedido);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locales/{idLocal}")
    public ResponseEntity<List<DtPedido>> listarPedidos(@PathVariable Long idLocal) {
        List<DtPedido> pedidos = pedidoService.listarPedidos(idLocal);
        return ResponseEntity.ok(pedidos);
    }
}
