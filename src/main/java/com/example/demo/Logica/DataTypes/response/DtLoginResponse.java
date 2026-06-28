package com.example.demo.Logica.DataTypes.response;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DtLoginResponse {
    private Long id;
    private String token;
    private String tipo;
    private String email;
    private String nombre;
    private DtDireccion direccion;
    private String foto;
    private Double calificacionGlobal;
}
