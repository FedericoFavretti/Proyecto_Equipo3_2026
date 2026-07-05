package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagoController.class);

    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;
    private final PedidoService pedidoService;

    public PagoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Mercado Pago llama por POST en producción, pero al simular notificaciones
    // desde el panel a veces se dispara un GET de verificación. Aceptamos ambos.
    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacionPost(
            @RequestBody(required = false) DtMercadoPagoWebhookRequest body,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id) {
        return procesar(body, type, topic, dataId, id);
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacionGet(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id) {
        return procesar(null, type, topic, dataId, id);
    }

    private ResponseEntity<Void> procesar(
            DtMercadoPagoWebhookRequest body,
            String type,
            String topic,
            String dataId,
            String id) {
        // "type"/"data.id" es el formato actual de Webhooks; "topic"/"id" es el formato legado de IPN.
        String tipoEvento = primerValorNoVacio(type, body != null ? body.getType() : null, topic, body != null ? body.getTopic() : null);
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
        } else {
            LOGGER.warn("Webhook MP ignorado: tipoEvento={}, paymentId={} (no es un evento de pago válido)", tipoEvento, paymentId);
        }

        return ResponseEntity.ok().build();
    }

    private String primerValorNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }

    public static class DtMercadoPagoWebhookRequest {
        private String id;
        private String type;
        private String topic;
        private DtMercadoPagoWebhookData data;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public DtMercadoPagoWebhookData getData() {
            return data;
        }

        public void setData(DtMercadoPagoWebhookData data) {
            this.data = data;
        }
    }

    public static class DtMercadoPagoWebhookData {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
