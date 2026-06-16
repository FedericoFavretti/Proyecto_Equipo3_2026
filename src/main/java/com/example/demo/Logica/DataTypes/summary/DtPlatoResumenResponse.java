package com.example.demo.Logica.DataTypes.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPlatoResumenResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private List<String> imagenes;
    private Boolean disponible;
}

