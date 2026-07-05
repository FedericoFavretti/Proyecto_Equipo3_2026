package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagoControllerTest {

    private PedidoService pedidoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pedidoService = Mockito.mock(PedidoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PagoController(pedidoService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void recibirNotificacionProcesaPagoCuandoEsWebhookDePayment() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/webhook")
                        .param("type", "payment")
                        .param("data.id", "12345"))
                .andExpect(status().isOk());

        verify(pedidoService).procesarPagoConfirmado("12345");
    }

    @Test
    void recibirNotificacionIgnoraEventosQueNoSonPayment() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/webhook")
                        .param("type", "merchant_order")
                        .param("data.id", "12345"))
                .andExpect(status().isOk());

        verify(pedidoService, never()).procesarPagoConfirmado("12345");
    }
}
