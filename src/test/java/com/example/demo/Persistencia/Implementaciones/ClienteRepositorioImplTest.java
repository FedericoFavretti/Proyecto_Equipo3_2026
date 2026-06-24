package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteRepositorioImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ResultSet resultSet;

    @Test
    void buscarPorIdMapeaTipoDesdeUsuario() throws Exception {
        ClienteRepositorioImpl clienteRepositorio = new ClienteRepositorioImpl(jdbcTemplate);
        LocalDateTime sesionesInvalidadasDesde = LocalDateTime.of(2026, 6, 22, 20, 15);

        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("email")).thenReturn("cliente@foodly.com");
        when(resultSet.getString("passwd")).thenReturn("hash");
        when(resultSet.getString("foto")).thenReturn("foto.png");
        when(resultSet.getString("estado")).thenReturn("Activo");
        when(resultSet.getString("tipo")).thenReturn("CLIENTE");
        when(resultSet.getTimestamp("sesiones_invalidadas_desde"))
                .thenReturn(Timestamp.valueOf(sesionesInvalidadasDesde));
        when(resultSet.getString("documento")).thenReturn("51234567");
        when(resultSet.getString("nombre")).thenReturn("Ana");
        when(resultSet.getString("apellido")).thenReturn("Perez");
        when(resultSet.getString("calle")).thenReturn("Colonia");
        when(resultSet.getString("ciudad")).thenReturn("Montevideo");
        when(resultSet.getString("numero")).thenReturn("100");
        when(resultSet.getString("codigoPostal")).thenReturn("11100");
        when(resultSet.getDouble("calificacionGlobal")).thenReturn(4.7);
        when(resultSet.getBoolean("activo")).thenReturn(true);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong()))
                .thenAnswer(invocation -> {
                    RowMapper<Cliente> rowMapper = invocation.getArgument(1);
                    return List.of(rowMapper.mapRow(resultSet, 0));
                });

        Optional<Cliente> cliente = clienteRepositorio.buscarPorId(10L);

        assertThat(cliente).isPresent();
        assertThat(cliente.get().getTipo()).isEqualTo("CLIENTE");
        assertThat(cliente.get().getSesionesInvalidadasDesde()).isEqualTo(sesionesInvalidadasDesde);
    }
}
