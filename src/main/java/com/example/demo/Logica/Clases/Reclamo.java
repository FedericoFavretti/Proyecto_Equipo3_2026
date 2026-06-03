package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reclamo {
    private Long id;
    private String motivo;
    private String tipoCompensacion;
    private Double montoReintegro;
    private Date fecha;
    private Pedido pedido;
}
