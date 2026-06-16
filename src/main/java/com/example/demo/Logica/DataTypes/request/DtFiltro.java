package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.DataTypes.shared.DtLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFiltro {
    private String nombre;
    private Boolean precioMasBajo;
    private Boolean precioMasAlto;
    private Boolean promocionActiva;
    private Boolean alfabetico;
    private DtLocal dtLocal;
}
