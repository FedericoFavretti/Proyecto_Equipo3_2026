package com.example.demo.Logica.Clases;

import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    private Long id;
    private String numero;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEmision;
    private Double montoTotal;
    private String archivoPdf;
    private EstadoFacturaPdf estadoPdf;
    private Integer intentosGeneracion;
    private String ultimoErrorPdf;
    private LocalDateTime fechaUltimoIntento;
    private LocalDateTime proximoReintento;
    private LocalDateTime fechaGeneracionPdf;
    private String localNombreSnapshot;
    private String localEmailSnapshot;
    private String clienteNombreSnapshot;
    private String clienteEmailSnapshot;
    private String direccionEntregaSnapshot;
    private String medioPagoSnapshot;
    private List<FacturaDetalle> detalles;
    private Pedido pedido;
}
