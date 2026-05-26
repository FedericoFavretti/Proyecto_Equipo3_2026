package com.example.demo.Logica.DataTypes;
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
public class DtPedido {
    private Long id;
    private Date fecha;
    private Duration tiempoEstEntrega;
    private Double total;
    private DtDireccion domicilioEntrega;
    private String medioDePago;
    private boolean pagoSimulado;
    private DtLocal dtLocal;
    private DtCliente dtCliente;
}
