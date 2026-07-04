package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.DataTypes.summary.DtClienteResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;
import org.springframework.stereotype.Component;

@Component
public class PedidoListadoMapper {

    public DtPedidoListadoResponse toResponse(PedidoListadoView pedidoListadoView) {
        return DtPedidoListadoResponse.builder()
                .id(pedidoListadoView.getId())
                .fecha(pedidoListadoView.getFecha())
                .estado(pedidoListadoView.getEstado())
                .total(pedidoListadoView.getTotal())
                .tiempoEstEntrega(pedidoListadoView.getTiempoEstEntrega())
                .cantidadItems(pedidoListadoView.getCantidadItems())
                .motivoRechazo(pedidoListadoView.getMotivoRechazo())
                .cliente(pedidoListadoView.getClienteId() != null
                        ? DtClienteResumenResponse.builder()
                                .id(pedidoListadoView.getClienteId())
                                .nombre(pedidoListadoView.getClienteNombre())
                                .apellido(pedidoListadoView.getClienteApellido())
                                .build()
                        : null)
                .local(pedidoListadoView.getLocalId() != null
                        ? DtLocalResumenResponse.builder()
                                .id(pedidoListadoView.getLocalId())
                                .nombre(pedidoListadoView.getLocalNombre())
                                .build()
                        : null)
                .build();
    }
}

