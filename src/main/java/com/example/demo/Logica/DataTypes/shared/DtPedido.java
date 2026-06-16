package com.example.demo.Logica.DataTypes.shared;

import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPedido {
    private Long id;
    private LocalDateTime fecha;
    private Duration tiempoEstEntrega;
    private Double total;
    private DtDireccion domicilioEntrega;
    private String medioDePago;
    private Boolean pagoSimulado;
    private EstadoPedido estado;
    private DtLocal dtLocal;
    private DtCliente dtCliente;
    private String mpPreferenciaId;
    private String mpInitPoint;
}