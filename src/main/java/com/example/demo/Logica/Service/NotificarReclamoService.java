package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.EstadoReclamo;
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
                "Se ha recibido un reclamo para el pedido " + reclamo.getPedido().getId()
                        + " realizado por el cliente " + reclamo.getPedido().getCliente().getNombre() + " "
                        + reclamo.getPedido().getCliente().getApellido()
                        + ". Ingrese a la web para resolverlo: " + reclamoUrl
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
        String asunto;
        String cuerpoCorreo;
        String mensajeWeb;

        if (reclamo.getEstado() == EstadoReclamo.Rechazado) {
            asunto = "Tu reclamo fue rechazado";
            cuerpoCorreo = "El local " + reclamo.getPedido().getLocal().getNombre()
                    + " rechazó el reclamo del pedido " + reclamo.getPedido().getId()
                    + ". Motivo: " + reclamo.getMotivoRechazo();
            mensajeWeb = "Tu reclamo sobre el pedido #" + reclamo.getPedido().getId()
                    + " fue rechazado. Motivo: " + reclamo.getMotivoRechazo();
        } else {
            asunto = "Se recibió la resolución de tu reclamo";
            cuerpoCorreo = "El local " + reclamo.getPedido().getLocal().getNombre()
                    + " resolvió el reclamo del pedido " + reclamo.getPedido().getId()
                    + " con " + reclamo.getTipoCompensacion() + ".";
            mensajeWeb = "Tu reclamo sobre el pedido #" + reclamo.getPedido().getId()
                    + " fue atendido con " + reclamo.getTipoCompensacion() + ".";
        }

        emailService.enviarCorreo(
                reclamo.getPedido().getCliente().getEmail(),
                asunto,
                cuerpoCorreo
        );

        var cliente = reclamo.getPedido() != null ? reclamo.getPedido().getCliente() : null;
        guardarNotificacionWeb(
                mensajeWeb,
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
