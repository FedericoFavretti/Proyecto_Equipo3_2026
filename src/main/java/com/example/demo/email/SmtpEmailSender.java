package com.example.demo.email;

import java.time.Duration;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    public SmtpEmailSender(JavaMailSender mailSender, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink, Duration validFor) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.from());
        message.setTo(to);
        message.setSubject("Recuperación de contraseña");
        message.setText("""
                Recibimos una solicitud para restablecer tu contraseña.

                Usá el siguiente enlace para continuar:
                %s

                Este enlace es válido por %d minutos.
                Si no solicitaste este cambio, podés ignorar este correo.
                """.formatted(resetLink, validFor.toMinutes()));

        mailSender.send(message);
    }
}
