package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtCalificacionGlobalResponse {
    private Double promedio;
    private Integer totalCalificaciones;
    private Map<Integer, Integer> detallePorPuntuacion;
}