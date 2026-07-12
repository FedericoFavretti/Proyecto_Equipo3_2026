package com.example.demo.Persistencia.Implementaciones;

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
public class PedidoListadoView {
    private Long id;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private Double total;
    private Duration tiempoEstEntrega;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteCelular;
    private Long localId;
    private String localNombre;
    private String localTelefonoFijo;
    private Integer cantidadItems;
    private String motivoRechazo;
    private Boolean pagado;
    private String medioDePago;
    private String mpInitPoint;
}
