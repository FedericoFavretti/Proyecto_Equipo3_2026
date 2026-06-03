package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promocion {
    private Long id;
    private Double descuento;
    private Date fechaInicio;
    private Date fechaFin;
    private String descripcion;
    private Plato plato;
}
