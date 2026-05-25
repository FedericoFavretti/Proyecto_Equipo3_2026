package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFiltro {
    private String precioMasBajo;
    private String precioMasAlto;
    private String promocionActiva;
    private String alfabetico;
    private String local;
}
