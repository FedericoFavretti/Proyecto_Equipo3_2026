package com.example.demo.Logica.DataTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFactura {
    private long id;
    private String numero;
    private Double monto;
    private String archivoPdf;
    private DtPedido dtPedido;
}
