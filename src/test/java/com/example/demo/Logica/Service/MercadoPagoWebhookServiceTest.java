package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.request.DtMercadoPagoWebhookData;
import com.example.demo.Logica.DataTypes.request.DtMercadoPagoWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookServiceTest {

    @Mock
    private PedidoService pedidoService;

    private MercadoPagoWebhookService mercadoPagoWebhookService;

    @BeforeEach
    void setUp() {
        mercadoPagoWebhookService = new MercadoPagoWebhookService(pedidoService);
    }

    @Test
    void procesarWebhookProcesaPagoCuandoLlegaPorQueryParams() {
        mercadoPagoWebhookService.procesarWebhook(null, "payment", null, "12345", null);

        verify(pedidoService).procesarPagoConfirmado("12345");
    }

    @Test
    void procesarWebhookProcesaPagoCuandoLlegaPorFormatoLegadoDeIpn() {
        mercadoPagoWebhookService.procesarWebhook(null, null, "payment", null, "12345");

        verify(pedidoService).procesarPagoConfirmado("12345");
    }

    @Test
    void procesarWebhookProcesaPagoCuandoLlegaEnElBodyJson() {
        DtMercadoPagoWebhookData data = new DtMercadoPagoWebhookData();
        data.setId("67890");
        DtMercadoPagoWebhookRequest body = new DtMercadoPagoWebhookRequest();
        body.setType("payment");
        body.setData(data);

        mercadoPagoWebhookService.procesarWebhook(body, null, null, null, null);

        verify(pedidoService).procesarPagoConfirmado("67890");
    }

    @Test
    void procesarWebhookPrefiereLosQueryParamsPorEncimaDelBody() {
        DtMercadoPagoWebhookData data = new DtMercadoPagoWebhookData();
        data.setId("del-body");
        DtMercadoPagoWebhookRequest body = new DtMercadoPagoWebhookRequest();
        body.setType("payment");
        body.setData(data);

        mercadoPagoWebhookService.procesarWebhook(body, "payment", null, "del-query-param", null);

        verify(pedidoService).procesarPagoConfirmado("del-query-param");
    }

    @Test
    void procesarWebhookIgnoraEventosQueNoSonDePago() {
        mercadoPagoWebhookService.procesarWebhook(null, "merchant_order", null, "12345", null);

        verifyNoInteractions(pedidoService);
    }

    @Test
    void procesarWebhookIgnoraEventoDePagoSinIdentificadorDePago() {
        mercadoPagoWebhookService.procesarWebhook(null, "payment", null, null, null);

        verifyNoInteractions(pedidoService);
    }

    @Test
    void procesarWebhookNoPropagaLaExcepcionSiFallaElProcesamientoDelPago() {

        org.mockito.Mockito.doThrow(new RuntimeException("Error de red"))
                .when(pedidoService).procesarPagoConfirmado("12345");

        mercadoPagoWebhookService.procesarWebhook(null, "payment", null, "12345", null);

        verify(pedidoService).procesarPagoConfirmado("12345");
    }

    @Test
    void procesarWebhookIgnoraCuandoNoHayNingunDatoEnQueryNiBody() {
        mercadoPagoWebhookService.procesarWebhook(null, null, null, null, null);

        verify(pedidoService, never()).procesarPagoConfirmado(org.mockito.ArgumentMatchers.anyString());
    }
}
