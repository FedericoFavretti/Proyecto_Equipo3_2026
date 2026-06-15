package com.example.demo.Logica.DataTypes.summary;

import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPedidoListadoResponse {
    private Long id;
    private Date fecha;
    private EstadoPedido estado;
    private Double total;
    private Duration tiempoEstEntrega;
    private DtClienteResumenResponse cliente;
    private Integer cantidadItems;
}

