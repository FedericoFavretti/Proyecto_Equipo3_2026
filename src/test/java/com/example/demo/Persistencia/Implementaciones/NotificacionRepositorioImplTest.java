package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Persistencia.Repositorios.PedidoNotificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoNotificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionRepositorioImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReclamoNotificacionRepositorio reclamoNotificacionRepositorio;

    @Mock
    private PedidoNotificacionRepositorio pedidoNotificacionRepositorio;

    @Mock
    private ReclamoRepositorio reclamoRepositorio;

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @InjectMocks
    private NotificacionRepositorioImpl notificacionRepositorio;

    @Test
    void listarPorDestinatarioNoDebeFallarSiLaRelacionPedidoNotificacionNoExiste() throws SQLException {
        ResultSet resultSet = crearResultSetPedidoWeb();

        when(pedidoNotificacionRepositorio.buscarPedido(7L)).thenReturn(null);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any()))
                .thenAnswer(invocation -> {
                    RowMapper<Notificacion> rowMapper = invocation.getArgument(1);
                    return List.of(rowMapper.mapRow(resultSet, 0));
                });

        List<Notificacion> resultado = notificacionRepositorio.listarPorDestinatario(TipoDestinatario.Cliente, 20L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getPedido()).isNull();
        verifyNoInteractions(pedidoRepositorio);
    }

    private ResultSet crearResultSetPedidoWeb() throws SQLException {
        ResultSet resultSet = org.mockito.Mockito.mock(ResultSet.class);
        LocalDateTime fecha = LocalDateTime.of(2026, 7, 11, 22, 44, 59);

        when(resultSet.getLong("id")).thenReturn(7L);
        when(resultSet.getString("tipo")).thenReturn("Pedido");
        when(resultSet.getString("mensaje")).thenReturn("Tu pedido fue actualizado");
        when(resultSet.getString("canal")).thenReturn("Web");
        when(resultSet.getBoolean("leida")).thenReturn(false);
        when(resultSet.getTimestamp("fecha")).thenReturn(Timestamp.valueOf(fecha));
        when(resultSet.getString("destinatarioTipo")).thenReturn("Cliente");
        when(resultSet.getObject("destinatarioId")).thenReturn(20L);

        return resultSet;
    }
}
