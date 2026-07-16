package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtMercadoPagoWebhookRequest;
import com.example.demo.Logica.Service.MercadoPagoWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagoControllerTest {

    private MercadoPagoWebhookService mercadoPagoWebhookService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mercadoPagoWebhookService = Mockito.mock(MercadoPagoWebhookService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PagoController(mercadoPagoWebhookService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void recibirNotificacionPostDelegaAlServicioDeWebhookConLosQueryParams() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/webhook")
                        .param("type", "payment")
                        .param("data.id", "12345"))
                .andExpect(status().isOk());

        verify(mercadoPagoWebhookService).procesarWebhook(
                isNull(),
                eq("payment"),
                isNull(),
                eq("12345"),
                isNull()
        );
    }

    @Test
    void recibirNotificacionPostConBodyJsonDelegaElBodyCompleto() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "payment",
                                  "data": {
                                    "id": "67890"
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        verify(mercadoPagoWebhookService).procesarWebhook(
                any(DtMercadoPagoWebhookRequest.class),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void recibirNotificacionGetDelegaAlServicioDeWebhookConBodyNulo() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/webhook")
                        .param("type", "payment")
                        .param("data.id", "12345"))
                .andExpect(status().isOk());


        verify(mercadoPagoWebhookService).procesarWebhook(
                isNull(),
                eq("payment"),
                isNull(),
                eq("12345"),
                isNull()
        );
    }

    @Test
    void recibirNotificacionGetSinDatosIgualDelegaAlServicioConTodoNulo() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/webhook"))
                .andExpect(status().isOk());

        verify(mercadoPagoWebhookService).procesarWebhook(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }
}
