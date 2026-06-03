package com.example.demo.Logica.DataTypes;
import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class DtUsuario {
    private Long id;
    private String email;
    private String passwd;
    private String foto;
    private EstadoCuenta estadoCuenta;
    private String tipo;
}
