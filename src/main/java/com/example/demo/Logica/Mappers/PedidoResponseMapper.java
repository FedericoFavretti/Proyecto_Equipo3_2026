package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtClienteResumenResponse;
import com.example.demo.Logica.DataTypes.DtDetallePedidoResponse;
import com.example.demo.Logica.DataTypes.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.DtPedidoResponse;
import com.example.demo.Logica.DataTypes.DtPlatoResumenResponse;
import org.springframework.stereotype.Component;

@Component
public class PedidoResponseMapper {

    public DtPedidoResponse toResponse(Pedido pedido) {
        return DtPedidoResponse.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .tiempoEstEntrega(pedido.getTiempoEstEntrega())
                .total(pedido.getTotal())
                .domicilioEntrega(pedido.getDomicilioEntrega())
                .medioDePago(pedido.getMedioDePago())
                .pagoSimulado(pedido.getPagoSimulado())
                .estado(pedido.getEstado())
                .local(pedido.getLocal() != null
                        ? DtLocalResumenResponse.builder()
                        .id(pedido.getLocal().getId())
                        .nombre(pedido.getLocal().getNombre())
                        .build()
                        : null)
                .cliente(pedido.getCliente() != null
                        ? DtClienteResumenResponse.builder()
                        .id(pedido.getCliente().getId())
                        .nombre(pedido.getCliente().getNombre())
                        .apellido(pedido.getCliente().getApellido())
                        .build()
                        : null)
                .detalles(pedido.getDetalles() != null
                        ? pedido.getDetalles().stream().map(this::toDetalleResponse).toList()
                        : null)
                .build();
    }

    private DtDetallePedidoResponse toDetalleResponse(DetallePedido detalle) {
        return DtDetallePedidoResponse.builder()
                .id(detalle.getId())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .plato(toPlatoResumen(detalle.getPlato()))
                .build();
    }

    private DtPlatoResumenResponse toPlatoResumen(Plato plato) {
        if (plato == null) {
            return null;
        }

        return DtPlatoResumenResponse.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .descripcion(plato.getDescripcion())
                .precio(plato.getPrecio())
                .imagenes(plato.getImagenes())
                .disponible(plato.getDisponible())
                .build();
    }
}
