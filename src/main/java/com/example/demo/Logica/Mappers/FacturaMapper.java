package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.DataTypes.shared.DtFactura;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    private final PedidoMapper pedidoMapper;

    public  FacturaMapper(PedidoMapper pedidoMapper){
        this.pedidoMapper = pedidoMapper;
    }

    public Factura mapearFacturaDeDt(DtFactura dtFactura){
        return Factura.builder()
                .id(dtFactura.getId())
                .numero(dtFactura.getNumero())
                .monto(dtFactura.getMonto())
                .archivoPdf(dtFactura.getArchivoPdf())
                .pedido(pedidoMapper.mapearPedidoDeDt(dtFactura.getDtPedido()))
                .build();
    }

    public DtFactura mapearDtFacturaDeClase(Factura factura){
        return DtFactura.builder()
                .id(factura.getId())
                .numero(factura.getNumero())
                .monto(factura.getMonto())
                .archivoPdf(factura.getArchivoPdf())
                .dtPedido(pedidoMapper.mapearDtPedidoDeClase(factura.getPedido()))
                .build();
    }
}

