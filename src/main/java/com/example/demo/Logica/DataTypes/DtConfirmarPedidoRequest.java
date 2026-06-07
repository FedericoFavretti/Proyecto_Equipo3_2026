package com.example.demo.Logica.DataTypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtConfirmarPedidoRequest {
    private Long tiempoEstimadoEntregaMinutos;
}
