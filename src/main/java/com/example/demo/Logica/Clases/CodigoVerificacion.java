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
public class CodigoVerificacion {
    private Long id;
    private Long idUsuario;
    private String codigo;
    private LocalDateTime fechaExpiracion;
    private Integer intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private Boolean usado;
}