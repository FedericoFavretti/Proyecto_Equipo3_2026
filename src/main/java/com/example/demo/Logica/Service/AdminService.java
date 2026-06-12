package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
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
        if (dtLocal == null || dtLocal.getEstadoLocal() == null) {
            return;
        }
        Local local = localRepositorio.buscarPorId(dtLocal.getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (local.getEstadoLocal() != EstadoLocal.Pendiente) {
            throw new IllegalStateException("Solo se pueden resolver solicitudes en estado Pendiente.");
        }
        local.setEstadoLocal(local.getEstadoLocal());
        local.setEstaAbierto(false);
        local.setEstado(EstadoCuenta.Activo);
        localRepositorio.actualizar(local);
        registroLocalNotificador.notificarLocalResolucionSolicitud(local);
    }

}
