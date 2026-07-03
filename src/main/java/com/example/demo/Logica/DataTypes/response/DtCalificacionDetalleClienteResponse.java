package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtCalificacionDetalleClienteResponse {
    private Long idCliente;
    private String nombreCliente;
    private Integer puntaje;
    private String comentario;
    private LocalDateTime fecha;
}