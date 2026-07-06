package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Pedido;
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
public class NotificacionPedidoService {
    @Value("${app.pedido.url}")
    private String pedidiosUrl;
    private static final Logger logger = LoggerFactory.getLogger(NotificacionPedidoService.class);

    private final EmailService emailService;
    private final NotificacionRepositorio notificacionRepositorio;

    public NotificacionPedidoService(EmailService emailService, NotificacionRepositorio notificacionRepositorio) {
        this.emailService = emailService;
        this.notificacionRepositorio = notificacionRepositorio;
    }

    public void notificarPedido(Pedido pedido){
        if(pedido != null){
            emailService.enviarCorreo(
                    pedido.getLocal().getEmail(),
                    "Se ah recibido un pedido",
                    "Se recibio el pedido #"+ pedido.getId()
                            +" por el cliente "+pedido.getCliente().getNombre() +" "+  pedido.getCliente().getApellido()
                            +"ingresa a la web para confirmar o rechazar el pedido. "
                            +pedidiosUrl

            );

            guardarNotificacionWeb(
                    "Recibiste un nuevo pedido #" + pedido.getId() + ".",
                    pedido,
                    TipoDestinatario.Local,
                    pedido.getLocal() != null ? pedido.getLocal().getId() : null
            );
        }
    }

    public void notificarPedidoCancelado(Pedido pedido){
        if(pedido != null){
            emailService.enviarCorreo(
                    pedido.getLocal().getEmail(),
                    "Se ah cancelado un pedido",
                    "Se cancelado el pedido #"+ pedido.getId()
                            +" por el cliente "+pedido.getCliente().getNombre() +" "+ pedido.getCliente().getApellido()
            );

            guardarNotificacionWeb(
                    "El cliente canceló el pedido #" + pedido.getId() + ".",
                    pedido,
                    TipoDestinatario.Local,
                    pedido.getLocal() != null ? pedido.getLocal().getId() : null
            );
        }
    }

    public void notificarConfirmacion(Pedido pedido, Factura factura) {
        String mensaje = "Tu pedido #" + pedido.getId()
                + " fue confirmado. Tiempo estimado: "
                + pedido.getTiempoEstEntrega().toMinutes()
                + " minutos. La factura "
                + factura.getNumero()
                + " se está preparando y te la enviaremos por correo apenas quede generada.";

        if (pedido.getCliente() != null && pedido.getCliente().getEmail() != null) {
            emailService.enviarCorreo(
                    pedido.getCliente().getEmail(),
                    "Pedido confirmado",
                    mensaje
            );
        }

        guardarNotificacionWeb(
                mensaje,
                pedido,
                TipoDestinatario.Cliente,
                pedido.getCliente() != null ? pedido.getCliente().getId() : null
        );

        logger.info("Notificación push pendiente para pedido {}", pedido.getId());
    }

    public void notificarFacturaGenerada(Factura factura, byte[] pdf) {
        String mensaje = "Tu factura " + factura.getNumero() + " ya fue generada y se adjunta en este correo.";

        notificacionRepositorio.guardar(Notificacion.builder()
                .tipo(TipoNotificacion.Pedido)
                .mensaje(mensaje)
                .canal(CanalNotificacion.Email)
                .leida(false)
                .fecha(LocalDateTime.now())
                .pedido(factura.getPedido())
                .destinatarioTipo(TipoDestinatario.Cliente)
                .destinatarioId(factura.getPedido() != null && factura.getPedido().getCliente() != null
                        ? factura.getPedido().getCliente().getId() : null)
                .build());

        if (factura.getClienteEmailSnapshot() != null && !factura.getClienteEmailSnapshot().isBlank()) {
            emailService.enviarCorreoConAdjunto(
                    factura.getClienteEmailSnapshot(),
                    "Factura de tu pedido",
                    mensaje,
                    pdf,
                    factura.getNumero() + ".pdf"
            );
        }

        logger.info("Notificación push pendiente para factura {}", factura.getNumero());
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
                .destinatarioTipo(TipoDestinatario.Cliente)
                .destinatarioId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                .build());

        if (pedido.getCliente() != null && pedido.getCliente().getEmail() != null) {
            emailService.enviarCorreo(
                    pedido.getCliente().getEmail(),
                    "Pedido rechazado",
                    mensaje
            );
        }

        guardarNotificacionWeb(
                mensaje,
                pedido,
                TipoDestinatario.Cliente,
                pedido.getCliente() != null ? pedido.getCliente().getId() : null
        );

        logger.info("Notificación push pendiente para rechazo de pedido {}", pedido.getId());
    }

    private void guardarNotificacionWeb(String mensaje, Pedido pedido,
                                        TipoDestinatario destinatarioTipo, Long destinatarioId) {
        if (destinatarioId == null) {
            logger.warn("No se pudo determinar el destinatario para la notificación web del pedido {}.",
                    pedido.getId());
            return;
        }

        notificacionRepositorio.guardar(Notificacion.builder()
                .tipo(TipoNotificacion.Pedido)
                .mensaje(mensaje)
                .canal(CanalNotificacion.Web)
                .leida(false)
                .fecha(LocalDateTime.now())
                .pedido(pedido)
                .destinatarioTipo(destinatarioTipo)
                .destinatarioId(destinatarioId)
                .build());
    }
}