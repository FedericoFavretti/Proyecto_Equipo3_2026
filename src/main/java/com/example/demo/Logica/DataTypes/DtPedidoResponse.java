package com.example.demo.Logica.DataTypes;

import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPedidoResponse {
    private Long id;
    private LocalDateTime fecha;
    private Duration tiempoEstEntrega;
    private Double total;
    private DtDireccion domicilioEntrega;
    private String medioDePago;
    private Boolean pagoSimulado;
    private EstadoPedido estado;
    private DtLocalResumenResponse local;
    private DtClienteResumenResponse cliente;
    private List<DtDetallePedidoResponse> detalles;
}
