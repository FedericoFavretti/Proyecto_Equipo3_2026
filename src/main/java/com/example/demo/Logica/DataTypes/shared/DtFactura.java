package com.example.demo.Logica.DataTypes.shared;

import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtFactura {
    private long id;
    private String numero;
    private Double monto;
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
    private String detalleItemsJson;
    private DtPedido dtPedido;
}

