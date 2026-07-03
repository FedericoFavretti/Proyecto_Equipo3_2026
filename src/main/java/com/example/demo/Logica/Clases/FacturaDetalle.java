package com.example.demo.Logica.Clases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDetalle {
    private Long id;
    private String nombreProductoSnapshot;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
