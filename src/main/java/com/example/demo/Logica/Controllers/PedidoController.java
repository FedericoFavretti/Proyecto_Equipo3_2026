package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.Interfaces.iPedidoController;
import com.example.demo.Logica.Service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController implements iPedidoController {
    @Autowired
    private PedidoService pedidoService;

    @PostMapping("/{idPedido}/confirmar")
    public ResponseEntity<Pedido> confirmarPedido(@PathVariable Long idPedido) {
        Pedido pedido = pedidoService.confirmarPedido(idPedido);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/{idPedido}/rechazar")
    public ResponseEntity<Void> rechazarPedido(@PathVariable Long idPedido) {
        pedidoService.rechazarPedido(idPedido);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Pedido> realizarPedido(@RequestBody DtPedido dtPedido) {
        Pedido pedido = pedidoService.realizarPedido(dtPedido);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("/{idPedido}/cancelar")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido) {
        pedidoService.cancelarPedido(idPedido);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locales/{idLocal}")
    public ResponseEntity<List<Pedido>> listarPedidos(@PathVariable Long idLocal) {
        List<Pedido> pedidos = pedidoService.listarPedidos(idLocal);
        return ResponseEntity.ok(pedidos);
    }
}
