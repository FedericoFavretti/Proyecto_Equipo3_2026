package com.example.demo.Logica.Clases;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {
    private long id;
    private String email;
    private String passwd;
    private String foto;
    private EstadoCuenta estado;
    private RolUsuario tipo;
    private Instant createdAt;
    private Instant updatedAt;
}
