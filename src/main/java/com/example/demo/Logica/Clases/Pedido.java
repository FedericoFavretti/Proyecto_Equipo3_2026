package com.example.demo.Logica.Clases;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
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
public class Pedido {
    private Long id;
    private LocalDateTime fecha;
    private Duration tiempoEstEntrega;
    private Double total;
    private DtDireccion domicilioEntrega;
    private String medioDePago;
    private Boolean pagoSimulado;
    private EstadoPedido estado;
    private Local local;
    private Cliente cliente;
    private String mpPreferenciaId;
    private String mpInitPoint;
}

