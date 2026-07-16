package com.example.demo.Logica.Service;

import com.example.demo.Persistencia.Repositorios.DeviceTokenRepositorio;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    private final DeviceTokenRepositorio deviceTokenRepositorio;

    public FcmService(DeviceTokenRepositorio deviceTokenRepositorio) {
        this.deviceTokenRepositorio = deviceTokenRepositorio;
    }


    public void enviarAUsuario(Long usuarioId, String titulo, String cuerpo, Map<String, String> data) {
        if (!firebaseDisponible()) {
            logger.warn("Firebase no inicializado. No se enviará push al usuario {}.", usuarioId);
            return;
        }

        List<String> tokens = deviceTokenRepositorio.buscarActivosPorUsuario(usuarioId)
                .stream()
                .map(dt -> dt.getToken())
                .toList();

        if (tokens.isEmpty()) {
            logger.info("Usuario {} no tiene tokens registrados.", usuarioId);
            return;
        }

        for (String token : tokens) {
            enviarAToken(token, titulo, cuerpo, data, usuarioId);
        }
    }

    private void enviarAToken(String token, String titulo, String cuerpo,
                               Map<String, String> data, Long usuarioId) {
        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(cuerpo)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String messageId = FirebaseMessaging.getInstance().send(builder.build());
            logger.info("Push enviado. usuarioId={}, token={}..., messageId={}",
                    usuarioId, token.substring(0, Math.min(token.length(), 10)), messageId);

        } catch (FirebaseMessagingException e) {
            logger.error("Error FCM para token {}...: {} ({})",
                    token.substring(0, Math.min(token.length(), 10)),
                    e.getMessage(), e.getMessagingErrorCode());


            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepositorio.desactivarPorToken(token);
                logger.info("Token desactivado por inválido.");
            }
        }
    }

    private boolean firebaseDisponible() {
        return !FirebaseApp.getApps().isEmpty();
    }
}
