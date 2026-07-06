package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtMercadoPagoWebhookRequest;
import com.example.demo.Logica.Service.MercadoPagoWebhookService;
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

    private final MercadoPagoWebhookService mercadoPagoWebhookService;

    public PagoController(MercadoPagoWebhookService mercadoPagoWebhookService) {
        this.mercadoPagoWebhookService = mercadoPagoWebhookService;
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
        mercadoPagoWebhookService.procesarWebhook(body, type, topic, dataId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacionGet(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) String dataId,
            @RequestParam(required = false) String id) {
        mercadoPagoWebhookService.procesarWebhook(null, type, topic, dataId, id);
        return ResponseEntity.ok().build();
    }
}
