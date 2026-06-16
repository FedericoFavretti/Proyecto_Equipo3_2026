package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtResloverHabilitacionLocal;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final LocalRepositorio localRepositorio;
    private final RegistroLocalNotificador registroLocalNotificador;
    private final UsuarioRepositorio usuarioRepositorio;

    public AdminService(LocalRepositorio localRepositorio, RegistroLocalNotificador registroLocalNotificador,  UsuarioRepositorio usuarioRepositorio) {
        this.localRepositorio = localRepositorio;
        this.registroLocalNotificador = registroLocalNotificador;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public void resolverSolicitud(DtResloverHabilitacionLocal dtResloverHabilitacionLocal) {
        if (dtResloverHabilitacionLocal == null || dtResloverHabilitacionLocal.getEstado() == null) {
            return;
        }

        Local local = (Local) usuarioRepositorio.buscarPorId(dtResloverHabilitacionLocal.getId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (local.getEstadoLocal() != EstadoLocal.Pendiente) {
            throw new IllegalStateException("Solo se pueden resolver solicitudes en estado Pendiente.");
        }

        local.setEstadoLocal(dtResloverHabilitacionLocal.getEstado());
        local.setEstaAbierto(false);
        local.setEstado(EstadoCuenta.Activo);
        localRepositorio.actualizar(local);
        registroLocalNotificador.notificarLocalResolucionSolicitud(local);
    }

}
