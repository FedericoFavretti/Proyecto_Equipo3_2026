package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final LocalRepositorio localRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;

    public AdminService(LocalRepositorio localRepositorio, RegistroLocalNotificador registroLocalNotificador) {
        this.localRepositorio = localRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
    }

    @Transactional
    public void resolverSolicitud(DtLocal dtLocal) {
        if (dtLocal == null || dtLocal.getEstado() == null) {
            return;
        }

        if (dtLocal.getEstado() == EstadoLocal.HABILITADO) {
            aprobarSolicitudRegistroLocal(dtLocal.getId());
            return;
        }

        if (dtLocal.getEstado() == EstadoLocal.RECHAZADO) {
            rechazarSolicitudRegistroLocal(dtLocal.getId());
            return;
        }

        throw new IllegalArgumentException("La resolución de la solicitud debe ser HABILITADO o RECHAZADO.");
    }

    @Transactional
    public void aprobarSolicitudRegistroLocal(long idLocal) {
        resolverSolicitudRegistroLocal(idLocal, EstadoLocal.HABILITADO);
    }

    @Transactional
    public void rechazarSolicitudRegistroLocal(long idLocal) {
        resolverSolicitudRegistroLocal(idLocal, EstadoLocal.RECHAZADO);
    }

    private void resolverSolicitudRegistroLocal(long idLocal, EstadoLocal nuevoEstado) {
        Local local = localRepositorio.buscarPorId(idLocal)
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (local.getEstadoLocal() != EstadoLocal.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden resolver solicitudes en estado Pendiente.");
        }

        local.setEstadoLocal(nuevoEstado);
        local.setEstaAbierto(false);

        localRepositorio.actualizar(local);
        registroLocalNotificador.notificarLocalResolucionSolicitud(local);
    }
}
