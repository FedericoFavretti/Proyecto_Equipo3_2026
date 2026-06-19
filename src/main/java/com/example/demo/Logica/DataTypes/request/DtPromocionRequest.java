package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPromocionRequest {
    private Long idPlato;
    private Double descuento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String descripcion;
}