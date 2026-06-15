package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private static final String MENSAJE_ID_LOCAL_INVALIDO = "El id del local es obligatorio.";
    private static final String MENSAJE_ESTADO_OBJETIVO_REQUERIDO = "Debe indicar el estado objetivo de la solicitud.";
    private static final String MENSAJE_ESTADO_OBJETIVO_INVALIDO = "El estado objetivo debe ser Habilitado o Rechazado.";
    private static final String MENSAJE_LOCAL_NO_ENCONTRADO = "Local no encontrado";
    private static final String MENSAJE_SOLICITUD_NO_PENDIENTE = "Solo se pueden resolver solicitudes en estado Pendiente.";
    private static final String MENSAJE_LOCAL_SIN_CORREO = "El local no tiene correo asociado para notificar la resolucion de la solicitud.";

    private final LocalRepositorio localRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;

    public AdminService(
            LocalRepositorio localRepositorio,
            UsuarioRepositorio usuarioRepositorio,
            RegistroLocalNotificador registroLocalNotificador) {
        this.localRepositorio = localRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
    }

    @Transactional(readOnly = true)
    public List<DtSolicitudLocalPendienteResponse> listarSolicitudesPendientes() {
        return localRepositorio.listarPendientes().stream()
                .map(this::mapearPendiente)
                .toList();
    }

    @Transactional
    public void resolverSolicitud(Long idLocal, DtResolverSolicitudLocalRequest request) {
        if (request == null || request.getEstadoObjetivo() == null) {
            throw new IllegalArgumentException(MENSAJE_ESTADO_OBJETIVO_REQUERIDO);
        }
        validarIdLocal(idLocal);

        EstadoLocal estadoObjetivo = request.getEstadoObjetivo();
        validarEstadoObjetivo(estadoObjetivo);

        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_LOCAL_NO_ENCONTRADO));

        if (local.getEstadoLocal() != EstadoLocal.Pendiente) {
            throw new IllegalStateException(MENSAJE_SOLICITUD_NO_PENDIENTE);
        }
        validarCorreoLocal(local);

        local.setEstadoLocal(estadoObjetivo);
        local.setEstaAbierto(false);
        EstadoCuenta estadoCuenta = estadoCuentaSegunResolucion(estadoObjetivo);
        local.setEstado(estadoCuenta);
        usuarioRepositorio.actualizarEstado(local.getId(), estadoCuenta);
        localRepositorio.actualizar(local);
        registroLocalNotificador.notificarLocalResolucionSolicitud(local);
    }

    private void validarIdLocal(Long idLocal) {
        if (idLocal == null || idLocal <= 0) {
            throw new IllegalArgumentException(MENSAJE_ID_LOCAL_INVALIDO);
        }
    }

    private void validarEstadoObjetivo(EstadoLocal estadoObjetivo) {
        if (estadoObjetivo != EstadoLocal.Habilitado && estadoObjetivo != EstadoLocal.Rechazado) {
            throw new IllegalArgumentException(MENSAJE_ESTADO_OBJETIVO_INVALIDO);
        }
    }

    private void validarCorreoLocal(Local local) {
        if (local.getEmail() == null || local.getEmail().isBlank()) {
            throw new IllegalStateException(MENSAJE_LOCAL_SIN_CORREO);
        }
    }

    private EstadoCuenta estadoCuentaSegunResolucion(EstadoLocal estadoObjetivo) {
        return estadoObjetivo == EstadoLocal.Habilitado
                ? EstadoCuenta.Activo
                : EstadoCuenta.Bloqueado;
    }

    private DtSolicitudLocalPendienteResponse mapearPendiente(Local local) {
        return new DtSolicitudLocalPendienteResponse(
                local.getId(),
                local.getEmail(),
                local.getNombre(),
                local.getDireccion(),
                local.getDescripcion(),
                local.getImagenes()
        );
    }
}

