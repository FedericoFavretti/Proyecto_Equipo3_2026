package com.example.demo.Logica.Clases;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    private long id;
    private String numero;
    private Double monto;
    private String archivoPdf;
    private Pedido pedido;
}
