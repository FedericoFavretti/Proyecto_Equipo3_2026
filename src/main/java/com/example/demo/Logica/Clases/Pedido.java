package com.example.demo.Logica.Clases;
import com.example.demo.Logica.DataTypes.DtDireccion;
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
public class Pedido {
    private Long id;
    private Date fecha;
    private Duration tiempoEstEntrega;
    private Double total;
    private DtDireccion domicilioEntrega;
    private String medioDePago;
    private Boolean pagoSimulado;
    private Local local;
    private Cliente cliente;
}
