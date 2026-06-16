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
public class DtFiltroReclamo {
    private Long idCliente;
    private EstadoPedido estadoPedido;
    private LocalDate fechaReclamo;
}
