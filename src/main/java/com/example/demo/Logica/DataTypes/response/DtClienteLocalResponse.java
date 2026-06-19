package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtClienteLocalResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private Double calificacionGlobal;
}