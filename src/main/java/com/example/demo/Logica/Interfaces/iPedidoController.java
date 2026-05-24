package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iPedidoController {
    ResponseEntity<Pedido> confirmarPedido(@RequestBody long idPedido);
    ResponseEntity<Void> rechazarPedido(@RequestBody long idPedido);
    ResponseEntity<Pedido> realizarPedido(@RequestBody DtPedido dtPedido);
    ResponseEntity<Void> cancelarPedido(@RequestBody long idPedido);
    ResponseEntity<List<Pedido>> listarPedidos(@RequestBody long idLocal);
}
