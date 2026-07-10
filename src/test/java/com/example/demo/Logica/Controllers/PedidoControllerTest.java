package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.summary.DtLocalResumenResponse;
import com.example.demo.Logica.DataTypes.summary.DtPedidoListadoResponse;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Mappers.PedidoResponseMapper;
import com.example.demo.Logica.Service.PedidoService;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .estado(EstadoPedido.Rechazado)
                .total(45.0)
                .cantidadItems(3)
                .motivoRechazo("Se cortó la energía en el local")
                .local(DtLocalResumenResponse.builder()
                        .id(10L)
                        .nombre("La Cocina")
                        .build())
                .build();

        when(pedidoService.buscarYListarHistorialPedidosPropios(any(), any()))
                .thenReturn(DtPagina.<DtPedidoListadoResponse>builder()
                        .contenido(List.of(response))
                        .paginaActual(0)
                        .tamanioPagina(10)
                        .totalPaginas(1)
                        .totalElementos(1)
                        .build());

        mockMvc.perform(get("/api/v1/pedidos/mi-historial")
                        .principal(new UsernamePasswordAuthenticationToken("ana@test.com", "token", List.of()))
                        .param("estado", "Confirmado")
                        .param("idLocal", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(88))
                .andExpect(jsonPath("$.contenido[0].local.id").value(10))
                .andExpect(jsonPath("$.contenido[0].local.nombre").value("La Cocina"))
                .andExpect(jsonPath("$.contenido[0].motivoRechazo").value("Se cortó la energía en el local"));
    }

    @Test
    void buscarYListarHistorialPedidosPropiosRespondeBadRequestSinPedidos() throws Exception {
        when(pedidoService.buscarYListarHistorialPedidosPropios(any(), any()))
                .thenThrow(new BusinessRuleException("Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!"));

        mockMvc.perform(get("/api/v1/pedidos/mi-historial")
                        .principal(new UsernamePasswordAuthenticationToken("ana@test.com", "token", List.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!"));
    }
    @Test
    void rechazarPedidoDevuelveOkCuandoMotivoEsValido() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos/44/rechazar")
                        .contentType("application/json")
                        .content("""
                                {
                                  "motivo": "No contamos con disponibilidad para prepararlo"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void rechazarPedidoRespondeBadRequestCuandoFaltaMotivo() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Debe seleccionar o escribir un motivo de rechazo antes de continuar."))
                .when(pedidoService)
                .rechazarPedido(anyLong(), anyString());

        mockMvc.perform(post("/api/v1/pedidos/44/rechazar")
                        .contentType("application/json")
                        .content("""
                                {
                                  "motivo": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debe seleccionar o escribir un motivo de rechazo antes de continuar."));
    }
}
