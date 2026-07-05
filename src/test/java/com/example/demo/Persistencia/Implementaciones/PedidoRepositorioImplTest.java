package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PedidoRepositorioImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private LocalRepositorio localRepositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;

    private PedidoRepositorioImpl pedidoRepositorio;

    @BeforeEach
    void setUp() {
        pedidoRepositorio = new PedidoRepositorioImpl(jdbcTemplate, localRepositorio, clienteRepositorio);
    }

    @Test
    void actualizarIncluyePagadoEnElUpdate() {
        Pedido pedido = Pedido.builder()
                .id(44L)
                .fecha(LocalDateTime.of(2026, 7, 4, 21, 30))
                .tiempoEstEntrega(Duration.ofMinutes(25))
                .total(450.0)
                .domicilioEntrega(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .medioDePago("EFECTIVO")
                .pagoSimulado(true)
                .pagado(true)
                .estado(EstadoPedido.Confirmado)
                .motivoRechazo(null)
                .build();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);

        pedidoRepositorio.actualizar(pedido);

        verify(jdbcTemplate).update(sqlCaptor.capture(), argsCaptor.capture());

        assertThat(sqlCaptor.getValue()).contains("pagado = ?");
        assertThat(argsCaptor.getValue()[9]).isEqualTo(true);
        assertThat(argsCaptor.getValue()[10]).isEqualTo(EstadoPedido.Confirmado.name());
        assertThat(argsCaptor.getValue()[12]).isEqualTo(44L);
    }
}
