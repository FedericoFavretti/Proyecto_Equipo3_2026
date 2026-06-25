package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificacionPedidoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionPedidoService.class);

    private final EmailService emailService;
    private final NotificacionRepositorio notificacionRepositorio;

    public NotificacionPedidoService(EmailService emailService, NotificacionRepositorio notificacionRepositorio) {
        this.emailService = emailService;
        this.notificacionRepositorio = notificacionRepositorio;
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

    public void notificarRechazo(Pedido pedido, String motivo) {
        String mensaje = "Tu pedido #" + pedido.getId() + " fue rechazado. Motivo: " + motivo;

        notificacionRepositorio.guardar(Notificacion.builder()
                .tipo(TipoNotificacion.Pedido)
                .mensaje(mensaje)
                .canal(CanalNotificacion.Email)
                .leida(false)
                .fecha(LocalDateTime.now())
                .pedido(pedido)
                .build());

        if (pedido.getCliente() != null && pedido.getCliente().getEmail() != null) {
            emailService.enviarCorreo(
                    pedido.getCliente().getEmail(),
                    "Pedido rechazado",
                    mensaje
            );
        }

        logger.info("Notificación web pendiente para rechazo de pedido {}", pedido.getId());
        logger.info("Notificación push pendiente para rechazo de pedido {}", pedido.getId());
    }
}
