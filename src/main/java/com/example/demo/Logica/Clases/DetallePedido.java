package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {
    private Long id;
    private int cantidad;
    private Double precioUnitario;
    private Double subtotal;
    private Plato plato;
    private Pedido pedido;
}