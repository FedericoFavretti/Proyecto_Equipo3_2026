package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.Interfaces.iPedidoController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController implements iPedidoController {
    @PostMapping("")
    public ResponseEntity<Pedido> confirmarPedido(@RequestBody long idPedido) {
        return null;
    }

    @PostMapping("")
    public ResponseEntity<Void> rechazarPedido(@RequestBody long idPedido) {
        return null;
    }

    @PostMapping("")
    public ResponseEntity<Pedido> realizarPedido(@RequestBody DtPedido dtPedido) {
        return null;
    }

    @PostMapping("")
    public ResponseEntity<Void> cancelarPedido(@RequestBody long idPedido) {
        return null;
    }

    @GetMapping("")
    public ResponseEntity<List<Pedido>> listarPedidos(@RequestBody long idLocal) {
        return null;
    }
}
