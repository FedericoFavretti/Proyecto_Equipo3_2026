package com.example.demo.Logica.DataTypes.request;

import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.EstadoReclamo;
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
    private Long idLocal;
    private Long idCliente;
    private EstadoPedido estadoPedido;
    private EstadoReclamo estadoReclamo;
    private LocalDate fechaReclamo;
}
