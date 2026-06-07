package com.example.demo.Logica.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void enviarMailDeActivacion(String email) {
        enviarCorreo(
                email,
                "Activá tu cuenta en Foodly",
                "Hacé clic en el siguiente enlace para activar tu cuenta: "
                        + "http://localhost:8082/api/v1/usuarios/activar?email=" + email
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
}
