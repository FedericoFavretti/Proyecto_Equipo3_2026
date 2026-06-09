package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtConfirmarPedidoRequest;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.DataTypes.DtPedidoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iPedidoController {
    ResponseEntity<DtPedidoResponse> confirmarPedido(
            @PathVariable Long idPedido,
            @RequestBody DtConfirmarPedidoRequest request
    );
    ResponseEntity<Void> rechazarPedido(@PathVariable Long idPedido);
    ResponseEntity<DtPedidoResponse> realizarPedido(@RequestBody DtPedido dtPedido);
    ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido);
    ResponseEntity<List<Pedido>> listarPedidos(@PathVariable Long idLocal);
}
