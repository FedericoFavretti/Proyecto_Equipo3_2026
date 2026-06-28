package com.example.demo.Logica.Clases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacionPasswd {
    private Long id;
    private Long idUsuario;
    private String tokenHash;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaConsumo;
    private Boolean usado;
}
