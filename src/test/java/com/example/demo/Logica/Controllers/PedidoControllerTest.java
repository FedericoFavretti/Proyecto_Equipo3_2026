package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Mappers.PedidoResponseMapper;
import com.example.demo.Logica.Service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoControllerTest {

    private PedidoService pedidoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pedidoService = Mockito.mock(PedidoService.class);
        PedidoResponseMapper pedidoResponseMapper = Mockito.mock(PedidoResponseMapper.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PedidoController(pedidoService, pedidoResponseMapper))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void buscarYListarHistorialPedidosPropiosDevuelveListado() throws Exception {
        DtPedidoListadoResponse response = DtPedidoListadoResponse.builder()
                .id(88L)
                .estado(EstadoPedido.Confirmado)
                .total(45.0)
                .cantidadItems(3)
                .local(DtLocalResumenResponse.builder()
                        .id(10L)
                        .nombre("La Cocina")
                        .build())
                .build();

        when(pedidoService.buscarYListarHistorialPedidosPropios(any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pedidos/clientes/20")
                        .param("estado", "Confirmado")
                        .param("idLocal", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(88))
                .andExpect(jsonPath("$[0].local.id").value(10))
                .andExpect(jsonPath("$[0].local.nombre").value("La Cocina"));
    }

    @Test
    void buscarYListarHistorialPedidosPropiosRespondeBadRequestSinPedidos() throws Exception {
        when(pedidoService.buscarYListarHistorialPedidosPropios(any(), any()))
                .thenThrow(new IllegalArgumentException("Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!"));

        mockMvc.perform(get("/api/v1/pedidos/clientes/20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!"));
    }
}
