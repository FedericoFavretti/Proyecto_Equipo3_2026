package com.example.demo.Logica.Clases;
import com.example.demo.Logica.Enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {
    private Long id;
    private String email;
    private String passwd;
    private String foto;
    private EstadoCuenta estado;
    private String tipo;
    private LocalDateTime sesionesInvalidadasDesde;
}
