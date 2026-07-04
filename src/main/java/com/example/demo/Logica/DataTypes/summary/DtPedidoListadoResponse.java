package com.example.demo.Logica.DataTypes.summary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class DtPedidoListadoResponse {
    private Long id;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private Double total;
    private Duration tiempoEstEntrega;
    private DtClienteResumenResponse cliente;
    private DtLocalResumenResponse local;
    private Integer cantidadItems;
    private String motivoRechazo;
}

