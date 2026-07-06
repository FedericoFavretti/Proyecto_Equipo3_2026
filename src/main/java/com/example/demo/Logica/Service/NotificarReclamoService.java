package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificarReclamoService {
    @Value("${app.reclamo.url}")
    private String reclamoUrl;
    private static final Logger logger = LoggerFactory.getLogger(NotificarReclamoService.class);

    private final EmailService emailService;
    private final NotificacionRepositorio notificacionRepositorio;

    public NotificarReclamoService(EmailService emailService, NotificacionRepositorio notificacionRepositorio) {
        this.emailService = emailService;
        this.notificacionRepositorio = notificacionRepositorio;
    }

    public void notificarReclamo(Reclamo reclamo) {
        emailService.enviarCorreo(
                reclamo.getPedido().getLocal().getEmail(),
                "Se recibio un reclamo",
                "Se ah recibido un reclamo para el pedido "+reclamo.getPedido().getId()
                        +" fue realizado por el cliente "+reclamo.getPedido().getCliente().getNombre() +" "+  reclamo.getPedido().getCliente().getApellido()
                        +" ingresa a la web para resolver el reclamo. "+
                        reclamoUrl
        );

        var local = reclamo.getPedido() != null ? reclamo.getPedido().getLocal() : null;
        guardarNotificacionWeb(
                "Recibiste un nuevo reclamo sobre el pedido #" + reclamo.getPedido().getId() + ".",
                reclamo,
                TipoDestinatario.Local,
                local != null ? local.getId() : null
        );
    }

    public void notificarReslucionReclamo(Reclamo reclamo) {
        emailService.enviarCorreo(
                reclamo.getPedido().getCliente().getEmail(),
                "Se ah recibido la resolución de un reclamo",
                "Se resolvio el reclamo asociado con el pedido " + reclamo.getPedido().getId()
                        + "fue resuelto por el local " + reclamo.getPedido().getLocal().getNombre()
        );

        var cliente = reclamo.getPedido() != null ? reclamo.getPedido().getCliente() : null;
        guardarNotificacionWeb(
                "Tu reclamo sobre el pedido #" + reclamo.getPedido().getId() + " fue atendido.",
                reclamo,
                TipoDestinatario.Cliente,
                cliente != null ? cliente.getId() : null
        );
    }

    private void guardarNotificacionWeb(String mensaje, Reclamo reclamo,
                                        TipoDestinatario destinatarioTipo, Long destinatarioId) {
        if (destinatarioId == null) {
            logger.warn("No se pudo determinar el destinatario para la notificación web del reclamo {}.",
                    reclamo.getId());
            return;
        }

        notificacionRepositorio.guardar(Notificacion.builder()
                .tipo(TipoNotificacion.Reclamo)
                .mensaje(mensaje)
                .canal(CanalNotificacion.Web)
                .leida(false)
                .fecha(LocalDateTime.now())
                .reclamo(reclamo)
                .destinatarioTipo(destinatarioTipo)
                .destinatarioId(destinatarioId)
                .build());
    }
}