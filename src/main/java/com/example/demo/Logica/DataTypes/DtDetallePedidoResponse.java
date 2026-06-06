package com.example.demo.Logica.DataTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtDetallePedidoResponse {
    private Long id;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private DtPlatoResumenResponse plato;
}
