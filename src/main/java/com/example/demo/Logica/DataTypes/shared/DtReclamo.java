package com.example.demo.Logica.DataTypes.shared;

import com.example.demo.Logica.Enums.EstadoReclamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtReclamo {
    private Long id;
    private String motivo;
    private EstadoReclamo estado;
    private String tipoCompensacion;
    private Double montoReintegro;
    private LocalDateTime fecha;
    private DtPedido dtPedido;
}

