package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Service.ClienteService;
import com.example.demo.Logica.Service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerTest {

    private ClienteService clienteService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        clienteService = Mockito.mock(ClienteService.class);
        CloudinaryService cloudinaryService = Mockito.mock(CloudinaryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ClienteController(clienteService, cloudinaryService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void buscarPlatosYPromocionesDevuelveListadoCombinado() throws Exception {
        DtPlato plato = DtPlato.builder()
                .id(10L)
                .nombre("Milanesa")
                .precio(15.0)
                .build();
        DtPromocion promocion = DtPromocion.builder()
                .id(20L)
                .descripcion("2x1")
                .dtPlato(plato)
                .build();

        when(clienteService.buscarPlatosYPromociones(any())).thenReturn(
                DtBusquedaPlatosPromocionesResponse.builder()
                        .platos(List.of(plato))
                        .promociones(List.of(promocion))
                        .build()
        );

        mockMvc.perform(post("/api/v1/clientes/busqueda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Mil",
                                  "promocionActiva": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platos[0].id").value(10))
                .andExpect(jsonPath("$.platos[0].nombre").value("Milanesa"))
                .andExpect(jsonPath("$.promociones[0].id").value(20))
                .andExpect(jsonPath("$.promociones[0].descripcion").value("2x1"));
    }

    @Test
    void buscarPlatosYPromocionesRespondeBadRequestSinResultados() throws Exception {
        when(clienteService.buscarPlatosYPromociones(any()))
                .thenThrow(new IllegalArgumentException("No se encontraron platos o promociones que coincidan con su búsqueda."));

        mockMvc.perform(post("/api/v1/clientes/busqueda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Inexistente"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se encontraron platos o promociones que coincidan con su búsqueda."));
    }
}

