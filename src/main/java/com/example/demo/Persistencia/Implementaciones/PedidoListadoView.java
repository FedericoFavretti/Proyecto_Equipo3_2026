package com.example.demo.Persistencia.Implementaciones;

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
public class PedidoListadoView {
    private Long id;
    private Date fecha;
    private EstadoPedido estado;
    private Double total;
    private Duration tiempoEstEntrega;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private Integer cantidadItems;
}
