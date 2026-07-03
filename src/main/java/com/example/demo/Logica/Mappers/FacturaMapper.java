package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.FacturaDetalle;
import com.example.demo.Logica.DataTypes.shared.DtFactura;
import com.example.demo.Logica.DataTypes.shared.DtFacturaDetalle;
import org.springframework.stereotype.Component;

import java.util.List;

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
                .fechaPedido(dtFactura.getFechaPedido())
                .fechaEmision(dtFactura.getFechaEmision())
                .montoTotal(dtFactura.getMontoTotal())
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
                .detalles(mapearDetallesDeDt(dtFactura.getDetalles()))
                .pedido(pedidoMapper.mapearPedidoDeDt(dtFactura.getDtPedido()))
                .build();
    }

    public DtFactura mapearDtFacturaDeClase(Factura factura) {
        return DtFactura.builder()
                .id(factura.getId())
                .numero(factura.getNumero())
                .fechaPedido(factura.getFechaPedido())
                .fechaEmision(factura.getFechaEmision())
                .montoTotal(factura.getMontoTotal())
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
                .detalles(mapearDetallesDeClase(factura.getDetalles()))
                .dtPedido(pedidoMapper.mapearDtPedidoDeClase(factura.getPedido()))
                .build();
    }

    private List<FacturaDetalle> mapearDetallesDeDt(List<DtFacturaDetalle> detalles) {
        if (detalles == null) {
            return List.of();
        }

        return detalles.stream()
                .map(detalle -> FacturaDetalle.builder()
                        .id(detalle.getId())
                        .nombreProductoSnapshot(detalle.getNombreProductoSnapshot())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .toList();
    }

    private List<DtFacturaDetalle> mapearDetallesDeClase(List<FacturaDetalle> detalles) {
        if (detalles == null) {
            return List.of();
        }

        return detalles.stream()
                .map(detalle -> DtFacturaDetalle.builder()
                        .id(detalle.getId() != null ? detalle.getId() : 0L)
                        .nombreProductoSnapshot(detalle.getNombreProductoSnapshot())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .toList();
    }
}

