package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPlatoEstadistica {
    private Long id;
    private String nombre;
    private String imagen;
    private Integer cantidadVendida;
    private Double montoVendido;
}
