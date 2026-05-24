package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plato {
    private long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private List<String> imagenes;
    private Boolean disponible;
    private Local local;
}
