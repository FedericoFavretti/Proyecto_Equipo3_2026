package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Exceptions.AccessDeniedException;
import com.example.demo.Logica.Service.ReclamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReclamoControllerTest {

    private ReclamoService reclamoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reclamoService = Mockito.mock(ReclamoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReclamoController(reclamoService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void reclamarRespondeUnauthorizedCuandoLaAutenticacionEsInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/reclamos/realizar_reclamo")
                        .contentType("application/json")
                        .content("""
                                {
                                  "motivo": "Llegó frío",
                                  "dtPedido": {
                                    "id": 44
                                  }
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reclamarPasaEmailAutenticadoAlServicio() throws Exception {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("cliente@foodly.com", "secret");
        authentication.setAuthenticated(true);

        mockMvc.perform(post("/api/v1/reclamos/realizar_reclamo")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                  "motivo": "Llegó frío",
                                  "tipoCompensacion": "Reintegro",
                                  "dtPedido": {
                                    "id": 44
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        Mockito.verify(reclamoService).reclamar(eq("cliente@foodly.com"), any(DtReclamo.class));
    }

    @Test
    void reclamarRespondeForbiddenCuandoElPedidoEsAjeno() throws Exception {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("cliente@foodly.com", "secret");
        authentication.setAuthenticated(true);

        Mockito.doThrow(new AccessDeniedException("No puede realizar reclamos sobre pedidos que no le pertenecen."))
                .when(reclamoService)
                .reclamar(eq("cliente@foodly.com"), any(DtReclamo.class));

        mockMvc.perform(post("/api/v1/reclamos/realizar_reclamo")
                        .principal(authentication)
                        .contentType("application/json")
                        .content("""
                                {
                                  "motivo": "Llegó frío",
                                  "dtPedido": {
                                    "id": 44
                                  }
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No puede realizar reclamos sobre pedidos que no le pertenecen."));
    }
}
