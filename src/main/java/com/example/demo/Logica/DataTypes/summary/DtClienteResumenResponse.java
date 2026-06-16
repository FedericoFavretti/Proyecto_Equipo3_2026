package com.example.demo.Logica.DataTypes.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtClienteResumenResponse {
    private Long id;
    private String nombre;
    private String apellido;
}

