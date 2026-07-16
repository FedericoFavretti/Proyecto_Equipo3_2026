package com.example.demo.Logica.Service;

import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Enums.CanalNotificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Logica.Enums.TipoNotificacion;
import com.example.demo.Persistencia.Repositorios.AdministradorRepositorio;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;

import java.time.LocalDateTime;

@Service
public class CorreoRegistroLocalNotificador implements RegistroLocalNotificador {

    private static final Logger logger = LoggerFactory.getLogger(CorreoRegistroLocalNotificador.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String correoAdministrador;
    private final String correoRemitente;
    private final AdministradorRepositorio administradorRepositorio;
    private final NotificacionRepositorio notificacionRepositorio;

    public CorreoRegistroLocalNotificador(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.registro-local.admin-email:admin@foodly.local}") String correoAdministrador,
            @Value("${app.email.from:no-reply@foodly.local}") String correoRemitente,
            AdministradorRepositorio administradorRepositorio,
            NotificacionRepositorio notificacionRepositorio) {
        this.mailSenderProvider = mailSenderProvider;
        this.correoAdministrador = correoAdministrador;
        this.correoRemitente = correoRemitente;
        this.administradorRepositorio = administradorRepositorio;
        this.notificacionRepositorio = notificacionRepositorio;
    }

    @Override
    public void notificarAdministradorSolicitudPendiente(Local local) {
        String mensaje = "El local " + local.getNombre() + " envió una solicitud de habilitación pendiente de revisión.";

        enviarCorreo(correoAdministrador, "Nueva solicitud de registro de local", mensaje);
        notificarAdministradoresPorWeb(mensaje);
    }

    @Override
    public void notificarLocalResolucionSolicitud(Local local) {
        String mensaje = "La solicitud del local " + local.getNombre() + " fue resuelta con estado " + local.getEstadoLocal() + ".";

        enviarCorreo(local.getEmail(), "Resolución de solicitud de registro de local", mensaje);
        guardarNotificacionWeb(mensaje, TipoDestinatario.Local, local.getId());
    }

    private void notificarAdministradoresPorWeb(String mensaje) {
        for (Administrador administrador : administradorRepositorio.listarTodos()) {
            guardarNotificacionWeb(mensaje, TipoDestinatario.Administrador, administrador.getId());
        }
    }

    private void guardarNotificacionWeb(String mensaje, TipoDestinatario destinatarioTipo, Long destinatarioId) {
        if (destinatarioId == null) {
            logger.warn("No se pudo determinar el destinatario para la notificación web de registro de local.");
            return;
        }

        notificacionRepositorio.guardar(Notificacion.builder()
                .tipo(TipoNotificacion.Local)
                .mensaje(mensaje)
                .canal(CanalNotificacion.Web)
                .leida(false)
                .fecha(LocalDateTime.now())
                .destinatarioTipo(destinatarioTipo)
                .destinatarioId(destinatarioId)
                .build());
    }

    private void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("No hay JavaMailSender configurado; no se pudo enviar correo '{}' a {}", asunto, destinatario);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(correoRemitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);

        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            logger.error("No se pudo enviar el correo '{}' a {}: {}", asunto, destinatario, e.getMessage());
        }
    }
}
