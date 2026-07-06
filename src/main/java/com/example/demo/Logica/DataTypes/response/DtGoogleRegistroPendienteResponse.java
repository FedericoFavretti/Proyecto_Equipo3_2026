package com.example.demo.Logica.DataTypes.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DtGoogleRegistroPendienteResponse {
    private String tokenRegistro;
    private String email;
    private String nombre;
    private String apellido;
    private String foto;
}
