package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.request.DtMercadoPagoWebhookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MercadoPagoWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MercadoPagoWebhookService.class);

    private final PedidoService pedidoService;

    public MercadoPagoWebhookService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public void procesarWebhook(
            DtMercadoPagoWebhookRequest body,
            String type,
            String topic,
            String dataId,
            String id) {
        // "type"/"data.id" es el formato actual de Webhooks; "topic"/"id" es el formato legado de IPN.
        String tipoEvento = primerValorNoVacio(
                type,
                body != null ? body.getType() : null,
                topic,
                body != null ? body.getTopic() : null
        );
        String paymentId = primerValorNoVacio(
                dataId,
                body != null && body.getData() != null ? body.getData().getId() : null,
                id,
                body != null ? body.getId() : null
        );

        LOGGER.info("Webhook MP recibido. type={}, topic={}, data.id={}, id={}, bodyType={}, bodyDataId={}, bodyId={}",
                type,
                topic,
                dataId,
                id,
                body != null ? body.getType() : null,
                body != null && body.getData() != null ? body.getData().getId() : null,
                body != null ? body.getId() : null);

        if ("payment".equals(tipoEvento) && paymentId != null) {
            try {
                pedidoService.procesarPagoConfirmado(paymentId);
                LOGGER.info("Webhook MP procesado correctamente para paymentId={}", paymentId);
            } catch (Exception e) {
                LOGGER.error("Error procesando webhook de MP para paymentId={}: {}", paymentId, e.getMessage(), e);
            }
            return;
        }

        LOGGER.warn("Webhook MP ignorado: tipoEvento={}, paymentId={} (no es un evento de pago válido)", tipoEvento, paymentId);
    }

    private String primerValorNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }
}
