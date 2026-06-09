package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtReclamo {
    private Long id;
    private String motivo;
    private String tipoCompensacion;
    private Double montoReintegro;
    private Date fecha;
    private DtPedido dtPedido;
}
