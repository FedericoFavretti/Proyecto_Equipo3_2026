package com.example.demo.Logica.DataTypes;

import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Date;
import java.util.List;

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
    private EstadoPedido estado;
    private List<DtDetallePedido> detalles;
    private DtLocal dtLocal;
    private DtCliente dtCliente;
    private String mpPreferenciaId;
    private String mpInitPoint;
}
