package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Notificacion;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.shared.DtNotificacion;
import com.example.demo.Logica.Enums.TipoDestinatario;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Mappers.NotificacionMapper;
import com.example.demo.Persistencia.Repositorios.NotificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepositorio notificacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final NotificacionMapper notificacionMapper;

    public NotificacionService(NotificacionRepositorio notificacionRepositorio,
                               UsuarioRepositorio usuarioRepositorio,
                               NotificacionMapper notificacionMapper) {
        this.notificacionRepositorio = notificacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.notificacionMapper = notificacionMapper;
    }

    @Transactional(readOnly = true)
    public List<DtNotificacion> listarMisNotificaciones(String emailAutenticado) {
        Usuario usuario = resolverUsuarioAutenticado(emailAutenticado);
        TipoDestinatario destinatarioTipo = resolverTipoDestinatario(usuario);

        return notificacionRepositorio.listarPorDestinatario(destinatarioTipo, usuario.getId()).stream()
                .map(notificacionMapper::mapearNotificacionDeClase)
                .toList();
    }

    @Transactional
    public void marcarComoLeida(String emailAutenticado, Long idNotificacion) {
        Usuario usuario = resolverUsuarioAutenticado(emailAutenticado);
        TipoDestinatario destinatarioTipo = resolverTipoDestinatario(usuario);

        Notificacion notificacion = notificacionRepositorio.buscarPorId(idNotificacion)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", idNotificacion));

        boolean perteneceAlUsuario = notificacion.getDestinatarioTipo() == destinatarioTipo
                && notificacion.getDestinatarioId() != null
                && notificacion.getDestinatarioId().equals(usuario.getId());

        if (!perteneceAlUsuario) {
            throw new BusinessRuleException("La notificación no pertenece al usuario autenticado.");
        }

        notificacion.setLeida(true);
        notificacionRepositorio.actualizar(notificacion);
    }

    private Usuario resolverUsuarioAutenticado(String email) {
        return usuarioRepositorio.buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
    }

    private TipoDestinatario resolverTipoDestinatario(Usuario usuario) {
        if (usuario instanceof Cliente) {
            return TipoDestinatario.Cliente;
        }
        if (usuario instanceof Local) {
            return TipoDestinatario.Local;
        }
        throw new BusinessRuleException("Solo clientes y locales tienen notificaciones.");
    }
}