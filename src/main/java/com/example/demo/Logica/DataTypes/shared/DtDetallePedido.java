package com.example.demo.Logica.DataTypes.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtDetallePedido {
    private Long id;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private DtPlato dtPlato;
}

