package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    private final LocalMapper localMapper;
    private final ClienteMapper clienteMapper;
    private final DetallePedidoMapper detallePedidoMapper;

    public PedidoMapper(LocalMapper localMapper, ClienteMapper clienteMapper,  DetallePedidoMapper detallePedidoMapper) {
        this.localMapper = localMapper;
        this.clienteMapper = clienteMapper;
        this.detallePedidoMapper = detallePedidoMapper;
    }

    public Pedido mapearPedidoDeDt(DtPedido dtPedido) {
        return Pedido.builder()
                .id(dtPedido.getId())
                .fecha(dtPedido.getFecha())
                .total(dtPedido.getTotal())
                .domicilioEntrega(dtPedido.getDomicilioEntrega())
                .medioDePago(dtPedido.getMedioDePago())
                .pagoSimulado(dtPedido.getPagoSimulado())
                .estado(dtPedido.getEstado())
                .detalles(detallePedidoMapper.mapearDetallesPedidoDeDt(dtPedido.getDetalles()))
                .cliente(clienteMapper.mapearClienteDeDt(dtPedido.getDtCliente()))
                .mpPreferenciaId(dtPedido.getMpPreferenciaId())
                .mpInitPoint(dtPedido.getMpInitPoint())
                .build();
    }

    public DtPedido mapearDtPedidoDeClase(Pedido pedido) {
        return DtPedido.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .total(pedido.getTotal())
                .domicilioEntrega(pedido.getDomicilioEntrega())
                .medioDePago(pedido.getMedioDePago())
                .pagoSimulado(pedido.getPagoSimulado())
                .estado(pedido.getEstado())
                .detalles(detallePedidoMapper.mapearDetallesPedidoDeClase(pedido.getDetalles()))
                .dtCliente(clienteMapper.mapearDtClienteDeClase(pedido.getCliente()))
                .mpPreferenciaId(pedido.getMpPreferenciaId())
                .mpInitPoint(pedido.getMpInitPoint())
                .build();
    }
}
