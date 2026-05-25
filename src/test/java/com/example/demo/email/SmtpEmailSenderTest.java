package com.example.demo.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpEmailSenderTest {

    @Test
    void shouldComposePasswordResetEmailWithConfiguredSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpEmailSender sender = new SmtpEmailSender(mailSender, new EmailProperties("no-reply@foodly.local"));

        sender.sendPasswordResetEmail(
                "user@example.com",
                "http://localhost:4200/reset-password?token=abc",
                Duration.ofMinutes(30));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@foodly.local");
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getSubject()).isEqualTo("Recuperación de contraseña");
        assertThat(message.getText())
                .contains("http://localhost:4200/reset-password?token=abc")
                .contains("30 minutos");
    }
}
