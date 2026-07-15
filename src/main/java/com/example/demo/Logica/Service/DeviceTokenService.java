package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.DeviceToken;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Persistencia.Repositorios.DeviceTokenRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeviceTokenService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTokenService.class);

    private final DeviceTokenRepositorio deviceTokenRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public DeviceTokenService(DeviceTokenRepositorio deviceTokenRepositorio,
                              UsuarioRepositorio usuarioRepositorio) {
        this.deviceTokenRepositorio = deviceTokenRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public void registrarToken(String emailAutenticado, String token, String plataforma) {
        if (token == null || token.isBlank()) return;

        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));

        DeviceToken deviceToken = DeviceToken.builder()
                .usuarioId(usuario.getId())
                .token(token.trim())
                .plataforma(plataforma != null ? plataforma.trim() : "ANDROID")
                .activo(true)
                .build();

        deviceTokenRepositorio.guardarOActualizar(deviceToken);
        logger.info("Token registrado para usuario {} ({})", emailAutenticado, plataforma);
    }

    public void eliminarToken(String token) {
        if (token == null || token.isBlank()) return;
        deviceTokenRepositorio.desactivarPorToken(token.trim());
        logger.info("Token desactivado: {}...", token.substring(0, Math.min(token.length(), 10)));
    }
}
