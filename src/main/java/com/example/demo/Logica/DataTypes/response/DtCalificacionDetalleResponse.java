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
public class DtCalificacionDetalleResponse {
    private Long idLocal;
    private String nombreLocal;
    private Integer puntaje;
    private String comentario;
    private LocalDateTime fecha;
}
