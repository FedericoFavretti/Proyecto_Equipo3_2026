package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.Service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping("")
    public ResponseEntity<Pedido> realizarPedido(@RequestBody DtPedido dtPedido) {
        Pedido pedido = pedidoService.realizarPedido(dtPedido);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("")
    public ResponseEntity<Pedido> confirmarPedido(@PathVariable long id) {
        Pedido pedido = pedidoService.confirmarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping("")
    public ResponseEntity<Void> rechazarPedido(@PathVariable long id) {
        pedidoService.rechazarPedido(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("")
    public ResponseEntity<Void> cancelarPedido(@PathVariable long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<Pedido>> listarPedidos(@PathVariable long idLocal) {
        List<Pedido> pedidos = pedidoService.listarPedidos(idLocal);
        return ResponseEntity.ok(pedidos);
    }
}