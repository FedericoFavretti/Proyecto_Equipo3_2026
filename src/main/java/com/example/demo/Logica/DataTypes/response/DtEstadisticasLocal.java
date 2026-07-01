package com.example.demo.Logica.DataTypes.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtEstadisticasLocal {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private List<DtPlatoEstadistica> platosMasPedido;
    private List<DtPlatoEstadistica> ventasPorPlato;
    private Double ventasConfirmadas;
}
