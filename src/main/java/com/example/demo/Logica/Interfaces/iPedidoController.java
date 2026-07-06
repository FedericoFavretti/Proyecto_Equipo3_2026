package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtConfirmarPedidoRequest;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.request.DtRechazarPedidoRequest;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.DataTypes.response.DtPedidoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iPedidoController {
    ResponseEntity<DtPedidoResponse> confirmarPedido(@PathVariable Long idPedido, @RequestBody DtConfirmarPedidoRequest request);
    ResponseEntity<Void> rechazarPedido(@PathVariable Long idPedido, @RequestBody DtRechazarPedidoRequest request);
    ResponseEntity<DtPedidoResponse> realizarPedido(@RequestBody DtPedidoConDetalles dtPedidoConDetalles);
    ResponseEntity<Void> cancelarPedido(@PathVariable Long idPedido, Authentication authentication);
    ResponseEntity<DtPedidoResponse> reintentarPago(@PathVariable Long idPedido, Authentication authentication);
    ResponseEntity<List<DtPedidoListadoResponse>> listarPedidos(@PathVariable Long idLocal, DtPedidoListadoFiltro dtPedidoListadoFiltro);
    ResponseEntity<List<DtPedidoListadoResponse>> buscarYListarHistorialPedidosPropios(Authentication authentication, DtPedidoListadoFiltro dtPedidoListadoFiltro);
}

