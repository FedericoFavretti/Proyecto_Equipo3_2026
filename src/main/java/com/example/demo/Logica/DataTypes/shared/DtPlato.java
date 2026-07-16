package com.example.demo.Logica.DataTypes.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPlato {
    private Long id;
    private String nombre;
    private String descripcion;
    private DtCategoria dtCategoria;
    private Double precio;
    private Double precioFinal;
    private Boolean tienePromocion;
    private String imagen;
    private Boolean disponible;
    private DtLocal dtLocal;
}

