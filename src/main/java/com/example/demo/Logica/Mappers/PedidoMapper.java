package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import org.springframework.stereotype.Component;

import java.util.List;

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
        Pedido pedido = Pedido.builder()
                .id(dtPedido.getId())
                .fecha(dtPedido.getFecha())
                .tiempoEstEntrega(dtPedido.getTiempoEstEntrega())
                .total(dtPedido.getTotal())
                .domicilioEntrega(dtPedido.getDomicilioEntrega())
                .medioDePago(dtPedido.getMedioDePago())
                .pagoSimulado(dtPedido.getPagoSimulado())
                .estado(dtPedido.getEstado())
                .local(dtPedido.getDtLocal() != null ? localMapper.mapearLocalDeDt(dtPedido.getDtLocal()) : null)
                .cliente(clienteMapper.mapearClienteDeDt(dtPedido.getDtCliente()))
                .mpPreferenciaId(dtPedido.getMpPreferenciaId())
                .mpInitPoint(dtPedido.getMpInitPoint())
                .build();

        if (dtPedido.getDetalles() == null) {
            return pedido;
        }

        List<DetallePedido> detalles = detallePedidoMapper.mapearDetallesPedidoDeDt(dtPedido.getDetalles());
        detalles.forEach(detalle -> detalle.setPedido(pedido));
        pedido.setDetalles(detalles);

        return pedido;
    }

    public DtPedido mapearDtPedidoDeClase(Pedido pedido) {
        return DtPedido.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .tiempoEstEntrega(pedido.getTiempoEstEntrega())
                .total(pedido.getTotal())
                .domicilioEntrega(pedido.getDomicilioEntrega())
                .medioDePago(pedido.getMedioDePago())
                .pagoSimulado(pedido.getPagoSimulado())
                .estado(pedido.getEstado())
                .detalles(detallePedidoMapper.mapearDetallesPedidoDeClase(pedido.getDetalles()))
                .dtLocal(pedido.getLocal() != null ? localMapper.mapearDtLocalDeClase(pedido.getLocal()) : null)
                .dtCliente(clienteMapper.mapearDtClienteDeClase(pedido.getCliente()))
                .mpPreferenciaId(pedido.getMpPreferenciaId())
                .mpInitPoint(pedido.getMpInitPoint())
                .build();
    }
}
