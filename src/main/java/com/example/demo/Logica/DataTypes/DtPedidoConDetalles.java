package com.example.demo.Logica.DataTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPedidoConDetalles {
    private DtPedido dtPedido;
    private List<DtDetallePedido> detalles;
}
