package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.DataTypes.summary.DtClienteResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;
import org.springframework.stereotype.Component;

@Component
public class PedidoListadoMapper {

    private static final String MEDIO_PAGO_EFECTIVO = "EFECTIVO";

    public DtPedidoListadoResponse toResponse(PedidoListadoView pedidoListadoView) {
        boolean pagoPendiente = esPagoPendiente(pedidoListadoView);

        return DtPedidoListadoResponse.builder()
                .id(pedidoListadoView.getId())
                .fecha(pedidoListadoView.getFecha())
                .estado(pedidoListadoView.getEstado())
                .estadoVisible(pagoPendiente ? "Pendiente de pago" : pedidoListadoView.getEstado().name())
                .total(pedidoListadoView.getTotal())
                .tiempoEstEntrega(pedidoListadoView.getTiempoEstEntrega())
                .cantidadItems(pedidoListadoView.getCantidadItems())
                .motivoRechazo(pedidoListadoView.getMotivoRechazo())
                .pagado(pedidoListadoView.getPagado())
                .pagoPendiente(pagoPendiente)
                .permiteReintentarPago(pagoPendiente)
                .mpInitPoint(pedidoListadoView.getMpInitPoint())
                .cliente(pedidoListadoView.getClienteId() != null
                        ? DtClienteResumenResponse.builder()
                        .id(pedidoListadoView.getClienteId())
                        .nombre(pedidoListadoView.getClienteNombre())
                        .apellido(pedidoListadoView.getClienteApellido())
                        .celular(pedidoListadoView.getClienteCelular())
                        .build()
                        : null)
                .local(pedidoListadoView.getLocalId() != null
                        ? DtLocalResumenResponse.builder()
                        .id(pedidoListadoView.getLocalId())
                        .nombre(pedidoListadoView.getLocalNombre())
                        .telefonoFijo(pedidoListadoView.getLocalTelefonoFijo())
                        .build()
                        : null)
                .build();
    }

    private boolean esPagoPendiente(PedidoListadoView pedidoListadoView) {
        return pedidoListadoView.getEstado() != null
                && "Pendiente".equals(pedidoListadoView.getEstado().name())
                && !Boolean.TRUE.equals(pedidoListadoView.getPagado())
                && !MEDIO_PAGO_EFECTIVO.equalsIgnoreCase(pedidoListadoView.getMedioDePago());
    }
}
