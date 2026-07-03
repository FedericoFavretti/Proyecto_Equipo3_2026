package com.example.demo.Logica.DataTypes.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFacturaDetalle {
    private long id;
    private String nombreProductoSnapshot;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
