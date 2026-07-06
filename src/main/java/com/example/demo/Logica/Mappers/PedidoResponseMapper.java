package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.Clases.DetallePedido;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.response.DtDetallePedidoResponse;
import com.example.demo.Logica.DataTypes.response.DtPedidoResponse;
import com.example.demo.Logica.DataTypes.summary.DtClienteResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPlatoResumenResponse;
import org.springframework.stereotype.Component;

@Component
public class PedidoResponseMapper {

    private static final String MEDIO_PAGO_EFECTIVO = "EFECTIVO";

    public DtPedidoResponse toResponse(Pedido pedido) {
        boolean pagoPendiente = esPagoPendiente(pedido);

        return DtPedidoResponse.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .tiempoEstEntrega(pedido.getTiempoEstEntrega())
                .total(pedido.getTotal())
                .domicilioEntrega(pedido.getDomicilioEntrega())
                .medioDePago(pedido.getMedioDePago())
                .pagoSimulado(pedido.getPagoSimulado())
                .pagado(pedido.getPagado())
                .estado(pedido.getEstado())
                .estadoVisible(pagoPendiente ? "Pendiente de pago" : pedido.getEstado().name())
                .pagoPendiente(pagoPendiente)
                .permiteReintentarPago(pagoPendiente)
                .mpInitPoint(pedido.getMpInitPoint())
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
                .build();
    }

    private boolean esPagoPendiente(Pedido pedido) {
        return pedido.getEstado() != null
                && "Pendiente".equals(pedido.getEstado().name())
                && !Boolean.TRUE.equals(pedido.getPagado())
                && !MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedido.getMedioDePago());
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
