package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtResCuentaUsuario {
    private Long id;
    private EstadoCuenta estado;
}
