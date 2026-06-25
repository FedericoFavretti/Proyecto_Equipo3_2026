package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.PedidoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {
    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;
    private final PedidoService pedidoService;

    public PagoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataId) {

        if ("payment".equals(type) && dataId != null) {
            pedidoService.procesarPagoConfirmado(dataId);
        }
        return ResponseEntity.ok().build();
    }
}