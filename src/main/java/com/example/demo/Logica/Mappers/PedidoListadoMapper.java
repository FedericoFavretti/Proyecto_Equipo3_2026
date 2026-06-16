package com.example.demo.Logica.Mappers;

import com.example.demo.Logica.DataTypes.summary.DtClienteResumenResponse;
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
                .cliente(DtClienteResumenResponse.builder()
                        .id(pedidoListadoView.getClienteId())
                        .nombre(pedidoListadoView.getClienteNombre())
                        .apellido(pedidoListadoView.getClienteApellido())
                        .build())
                .build();
    }
}

