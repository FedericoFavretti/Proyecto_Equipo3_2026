package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFiltroUsuario {
    private String texto;
    private String tipoUsuario;
    private EstadoCuenta estado;
    private String ordenarPor;
    private String direccion;
}