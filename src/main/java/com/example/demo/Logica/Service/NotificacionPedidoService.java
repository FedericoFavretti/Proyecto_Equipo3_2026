package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacionPedidoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionPedidoService.class);

    private final EmailService emailService;

    public NotificacionPedidoService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notificarConfirmacion(Pedido pedido, Factura factura) {
        if (pedido.getCliente() != null && pedido.getCliente().getEmail() != null) {
            emailService.enviarCorreo(
                    pedido.getCliente().getEmail(),
                    "Pedido confirmado",
                    "Tu pedido #" + pedido.getId()
                            + " fue confirmado. Tiempo estimado: "
                            + pedido.getTiempoEstEntrega().toMinutes()
                            + " minutos. Factura: "
                            + factura.getNumero()
                            + "."
            );
        }

        logger.info("Notificación web pendiente para pedido {}", pedido.getId());
        logger.info("Notificación push pendiente para pedido {}", pedido.getId());
    }
}
