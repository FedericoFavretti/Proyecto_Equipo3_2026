package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.Enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtPedidoListadoFiltro {
    private EstadoPedido estado;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Long idLocal;
    private String ordenarPor;
    private String direccion;
}

