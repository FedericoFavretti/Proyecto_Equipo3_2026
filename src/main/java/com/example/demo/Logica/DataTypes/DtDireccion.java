package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtDireccion {
    private String calle;
    private String numero;
    private String apartamento;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    private String referencia;

    public DtDireccion(String calle, String numero, String ciudad, String codigoPostal) {
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
    }
}
