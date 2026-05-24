package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtDetallePedido {
    private long id;
    private int cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private DtPlato dtPlato;
    private DtPedido dtPedido;
}
