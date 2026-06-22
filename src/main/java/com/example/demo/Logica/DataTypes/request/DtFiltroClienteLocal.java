package com.example.demo.Logica.DataTypes.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFiltroClienteLocal {
    private String nombre;
    private Double calificacionMinima;
    private String ordenarPor;
    private String direccion;
}