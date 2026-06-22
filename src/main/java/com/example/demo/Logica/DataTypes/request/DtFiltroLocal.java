package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFiltroLocal {
    private String nombre;
    private Double calificacionMinima;
    private Boolean estaAbierto;
    private String ordenarPor;
    private String direccion;
}