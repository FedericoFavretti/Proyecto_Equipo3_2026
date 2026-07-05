package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id) {
        return procesar(type, topic, dataId, id);
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacionGet(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id) {
        return procesar(type, topic, dataId, id);
    }

    private ResponseEntity<Void> procesar(String type, String topic, String dataId, String id) {
        // "type"/"data.id" es el formato actual de Webhooks; "topic"/"id" es el formato legado de IPN.
        String tipoEvento = type != null ? type : topic;
        String paymentId = dataId != null ? dataId : id;

        LOGGER.info("Webhook MP recibido. type={}, topic={}, data.id={}, id={}", type, topic, dataId, id);

        if ("payment".equals(tipoEvento) && paymentId != null) {
            try {
                pedidoService.procesarPagoConfirmado(paymentId);
                LOGGER.info("Webhook MP procesado correctamente para paymentId={}", paymentId);
            } catch (Exception e) {
                // Logueamos explícitamente acá porque el manejador global de excepciones
                // no imprime el detalle de ExternalServiceException, y sin esto el fallo
                // queda invisible en los logs de Railway.
                LOGGER.error("Error procesando webhook de MP para paymentId={}: {}", paymentId, e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Webhook MP ignorado: tipoEvento={}, paymentId={} (no es un evento de pago válido)", tipoEvento, paymentId);
        }

        return ResponseEntity.ok().build();
    }
}