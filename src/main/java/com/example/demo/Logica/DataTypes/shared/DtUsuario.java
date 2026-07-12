package com.example.demo.Logica.DataTypes.shared;

import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public  abstract class DtUsuario {
    private Long id;
    private String email;
    private String passwd;
    private String foto;
    private EstadoCuenta estadoCuenta;
    private String tipo;
    private LocalDateTime sesionesInvalidadasDesde;
    private String celular;
}

