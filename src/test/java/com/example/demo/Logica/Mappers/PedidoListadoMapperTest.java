package com.example.demo.Logica.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Implementaciones.PedidoListadoView;

class PedidoListadoMapperTest {

    private final PedidoListadoMapper pedidoListadoMapper = new PedidoListadoMapper();

    @Test
    void toResponseMapeaContextoDeLocalConClienteYSinLocal() {
        PedidoListadoView view = PedidoListadoView.builder()
                .id(77L)
                .fecha(LocalDateTime.of(2026, 6, 16, 12, 0))
                .estado(EstadoPedido.Pendiente)
                .total(30.0)
                .tiempoEstEntrega(Duration.ofMinutes(25))
                .clienteId(7L)
                .clienteNombre("Ana")
                .clienteApellido("Perez")
                .cantidadItems(2)
                .build();

        DtPedidoListadoResponse response = pedidoListadoMapper.toResponse(view);

        assertThat(response.getCliente()).isNotNull();
        assertThat(response.getCliente().getId()).isEqualTo(7L);
        assertThat(response.getCliente().getNombre()).isEqualTo("Ana");
        assertThat(response.getCliente().getApellido()).isEqualTo("Perez");
        assertThat(response.getLocal()).isNull();
    }

    @Test
    void toResponseMapeaContextoDeClienteConLocalYSinCliente() {
        PedidoListadoView view = PedidoListadoView.builder()
                .id(88L)
                .fecha(LocalDateTime.of(2026, 6, 16, 13, 0))
                .estado(EstadoPedido.Confirmado)
                .total(45.0)
                .tiempoEstEntrega(Duration.ofMinutes(35))
                .localId(19L)
                .localNombre("La Cocina")
                .cantidadItems(3)
                .motivoRechazo("Sin stock de ingredientes")
                .build();

        DtPedidoListadoResponse response = pedidoListadoMapper.toResponse(view);

        assertThat(response.getLocal()).isNotNull();
        assertThat(response.getLocal().getId()).isEqualTo(19L);
        assertThat(response.getLocal().getNombre()).isEqualTo("La Cocina");
        assertThat(response.getCliente()).isNull();
        assertThat(response.getMotivoRechazo()).isEqualTo("Sin stock de ingredientes");
    }
}
