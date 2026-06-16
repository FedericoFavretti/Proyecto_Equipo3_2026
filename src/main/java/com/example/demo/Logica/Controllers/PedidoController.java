package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtConfirmarPedidoRequest;
import com.example.demo.Logica.DataTypes.shared.DtPedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtPedidoConDetalles;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.DataTypes.response.DtPedidoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Interfaces.iPedidoController;
import com.example.demo.Logica.Mappers.PedidoResponseMapper;
import com.example.demo.Logica.Service.PedidoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<List<DtPedidoListadoResponse>> listarPedidos(
            @PathVariable Long idLocal,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false, defaultValue = "fecha") String ordenarPor,
            @RequestParam(required = false, defaultValue = "desc") String direccion) {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .estado(estado)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();
        List<DtPedidoListadoResponse> pedidos = pedidoService.listarPedidos(idLocal, filtro);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/clientes/{idCliente}")
    public ResponseEntity<List<DtPedidoListadoResponse>> buscarYListarHistorialPedidosPropios(
            @PathVariable Long idCliente,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long idLocal,
            @RequestParam(required = false, defaultValue = "fecha") String ordenarPor,
            @RequestParam(required = false, defaultValue = "desc") String direccion) {
        DtPedidoListadoFiltro filtro = DtPedidoListadoFiltro.builder()
                .estado(estado)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .idLocal(idLocal)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();
        List<DtPedidoListadoResponse> pedidos = pedidoService.buscarYListarHistorialPedidosPropios(idCliente, filtro);
        return ResponseEntity.ok(pedidos);
    }
}

