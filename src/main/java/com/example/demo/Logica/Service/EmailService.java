package com.example.demo.Logica.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarMailDeActivacion(String email) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Activá tu cuenta en Foodly");
        mensaje.setText("Hacé clic en el siguiente enlace para activar tu cuenta: "
                + "http://localhost:8082/api/v1/usuarios/activar?email=" + email);
        mailSender.send(mensaje);
    }
}