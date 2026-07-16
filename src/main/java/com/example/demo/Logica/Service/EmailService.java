package com.example.demo.Logica.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void enviarMailDeActivacion(String email, String link) {
        enviarCorreo(
                email,
                "Activá tu cuenta en Foodly",
                "Hacé clic en el siguiente enlace para activar tu cuenta (válido por 24hs): " + link
        );
    }

    public void recuperarPasswdPorCorreo(String correo, String link) {
        enviarCorreo(
                correo,
                "Recupera tu cuenta en Foodly",
                "Hacé clic en el siguiente enlace para recuperar tu cuenta (válido por 30 minutos): " + link
        );
    }

    public void enviarCodigoVerificacion(String correo, String codigo) {
        enviarCorreo(
                correo,
                "Código de verificación - Foodly",
                "Tu código de verificación para cambiar la contraseña es: " + codigo
                        + ". Este código vence en 10 minutos. Si no solicitaste este cambio, ignorá este mensaje."
        );
    }

    public void enviarConfirmacionCambioPasswd(String correo) {
        enviarCorreo(
                correo,
                "Tu contraseña fue actualizada - Foodly",
                "Te confirmamos que tu contraseña fue cambiada exitosamente. Si no realizaste este cambio, contactate con soporte de inmediato."
        );
    }

    public void solicitarCambioCorreo(String correoActual, String correoNuevo, String link) {
        enviarCorreo(
                correoActual,
                "Confirmá el cambio de correo de tu cuenta - Foodly",
                "Solicitaste cambiar el correo de tu cuenta de Foodly a " + correoNuevo
                        + ". Hacé clic en el siguiente enlace para confirmar el cambio (válido por 30 minutos): " + link
                        + ". Si no solicitaste este cambio, ignorá este mensaje; tu correo actual seguirá funcionando con normalidad."
        );
    }

    public void confirmarCambioCorreo(String correoAnterior, String correoNuevo) {
        enviarCorreo(
                correoAnterior,
                "Tu correo de cuenta fue actualizado - Foodly",
                "Te confirmamos que el correo de tu cuenta de Foodly fue cambiado a " + correoNuevo
                        + ". Si no realizaste este cambio, contactate con soporte de inmediato."
        );
        enviarCorreo(
                correoNuevo,
                "Tu correo de cuenta fue actualizado - Foodly",
                "Este correo ahora está asociado a tu cuenta de Foodly. A partir de ahora, usalo para iniciar sesión."
        );
    }

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("No hay JavaMailSender configurado; no se pudo enviar correo '{}' a {}", asunto, destinatario);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }

    public void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo, byte[] adjunto, String nombreArchivo) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("No hay JavaMailSender configurado; no se pudo enviar correo con adjunto '{}' a {}", asunto, destinatario);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo);
            helper.addAttachment(nombreArchivo, new ByteArrayResource(adjunto), "application/pdf");
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            throw new IllegalStateException("No se pudo enviar el correo con la factura adjunta.", ex);
        }
    }
}
