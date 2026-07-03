package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.DataTypes.shared.DtFactura;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    private final PedidoMapper pedidoMapper;

    public FacturaMapper(PedidoMapper pedidoMapper) {
        this.pedidoMapper = pedidoMapper;
    }

    public Factura mapearFacturaDeDt(DtFactura dtFactura) {
        return Factura.builder()
                .id(dtFactura.getId())
                .numero(dtFactura.getNumero())
                .monto(dtFactura.getMonto())
                .archivoPdf(dtFactura.getArchivoPdf())
                .estadoPdf(dtFactura.getEstadoPdf())
                .intentosGeneracion(dtFactura.getIntentosGeneracion())
                .ultimoErrorPdf(dtFactura.getUltimoErrorPdf())
                .fechaUltimoIntento(dtFactura.getFechaUltimoIntento())
                .proximoReintento(dtFactura.getProximoReintento())
                .fechaGeneracionPdf(dtFactura.getFechaGeneracionPdf())
                .localNombreSnapshot(dtFactura.getLocalNombreSnapshot())
                .localEmailSnapshot(dtFactura.getLocalEmailSnapshot())
                .clienteNombreSnapshot(dtFactura.getClienteNombreSnapshot())
                .clienteEmailSnapshot(dtFactura.getClienteEmailSnapshot())
                .direccionEntregaSnapshot(dtFactura.getDireccionEntregaSnapshot())
                .medioPagoSnapshot(dtFactura.getMedioPagoSnapshot())
                .detalleItemsJson(dtFactura.getDetalleItemsJson())
                .pedido(pedidoMapper.mapearPedidoDeDt(dtFactura.getDtPedido()))
                .build();
    }

    public DtFactura mapearDtFacturaDeClase(Factura factura) {
        return DtFactura.builder()
                .id(factura.getId())
                .numero(factura.getNumero())
                .monto(factura.getMonto())
                .archivoPdf(factura.getArchivoPdf())
                .estadoPdf(factura.getEstadoPdf())
                .intentosGeneracion(factura.getIntentosGeneracion())
                .ultimoErrorPdf(factura.getUltimoErrorPdf())
                .fechaUltimoIntento(factura.getFechaUltimoIntento())
                .proximoReintento(factura.getProximoReintento())
                .fechaGeneracionPdf(factura.getFechaGeneracionPdf())
                .localNombreSnapshot(factura.getLocalNombreSnapshot())
                .localEmailSnapshot(factura.getLocalEmailSnapshot())
                .clienteNombreSnapshot(factura.getClienteNombreSnapshot())
                .clienteEmailSnapshot(factura.getClienteEmailSnapshot())
                .direccionEntregaSnapshot(factura.getDireccionEntregaSnapshot())
                .medioPagoSnapshot(factura.getMedioPagoSnapshot())
                .detalleItemsJson(factura.getDetalleItemsJson())
                .dtPedido(pedidoMapper.mapearDtPedidoDeClase(factura.getPedido()))
                .build();
    }
}

