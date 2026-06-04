package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PedidoService PedidoService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataId) {

        if ("payment".equals(type) && dataId != null) {
            PedidoService.procesarPagoConfirmado(dataId);
        }
        return ResponseEntity.ok().build();
    }
}