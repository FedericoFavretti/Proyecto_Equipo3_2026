package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Reclamo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificarReclamoService {
    @Value("${app.reclamo.url}")
    private String reclamoUrl;
    private final EmailService emailService;

    public NotificarReclamoService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notificarReclamo(Reclamo reclamo) {
        emailService.enviarCorreo(
                reclamo.getPedido().getLocal().getEmail(),
                "Se recibio un reclamo",
                "Se ah recibido un reclamo para el pedido "+reclamo.getPedido().getId()
                +"fue realizado por el cliente "+reclamo.getPedido().getCliente().getNombre() + reclamo.getPedido().getCliente().getApellido()
                        +"ingresa a la web para resolver el reclamo."+
                        reclamoUrl
        );
    }

    public void notificarReslucionReclamo(Reclamo reclamo) {
        emailService.enviarCorreo(
          reclamo.getPedido().getCliente().getEmail(),
          "Se ah recibido la resolución de un reclamo",
                "Se resolvio el reclamo asociado con el pedido " + reclamo.getPedido().getId()
                        + "fue resuelto por el local " + reclamo.getPedido().getLocal().getNombre()
        );
    }
}
