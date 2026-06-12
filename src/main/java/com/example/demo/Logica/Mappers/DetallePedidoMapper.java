package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.DataTypes.DtDetallePedido;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DetallePedidoMapper {

    private final PlatoMapper platoMapper;
    private final PedidoMapper pedidoMapper;

    public DetallePedidoMapper(PlatoMapper platoMapper, PedidoMapper pedidoMapper) {
        this.platoMapper = platoMapper;
        this.pedidoMapper = pedidoMapper;
    }



    public DetallePedido mapearDetallePedidoDeDt(DtDetallePedido dtDetallePedido) {
        return DetallePedido.builder()
                .id(dtDetallePedido.getId())
                .cantidad(dtDetallePedido.getCantidad())
                .subtotal(dtDetallePedido.getSubtotal())
                .plato(platoMapper.mapearPlatoDeDt(dtDetallePedido.getDtPlato()))
                .pedido(pedidoMapper.mapearPedidoDeDt(dtDetallePedido.getDtPedido()))
                .build();
    }

    public List<DetallePedido> mapearDetallesPedidoDeDt(List<DtDetallePedido> detalles) {
        return detalles.stream()
                .map(this::mapearDetallePedidoDeDt)
                .collect(Collectors.toList());
    }

    public DtDetallePedido mapearDtDetallePedidoDeClase(DetallePedido detallePedido) {
        return DtDetallePedido.builder()
                .id(detallePedido.getId())
                .cantidad(detallePedido.getCantidad())
                .subtotal(detallePedido.getSubtotal())
                .dtPlato(platoMapper.mapearDtPlatoDeClase(detallePedido.getPlato()))
                .dtPedido(pedidoMapper.mapearDtPedidoDeClase(detallePedido.getPedido()))
                .build();
    }

    public List<DtDetallePedido> mapearDetallesPedidoDeClase(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(this::mapearDtDetallePedidoDeClase)
                .collect(Collectors.toList());
    }
}
