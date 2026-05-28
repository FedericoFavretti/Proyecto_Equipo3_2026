package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;

public interface RegistroLocalNotificador {

    void notificarAdministradorSolicitudPendiente(Local local);

    void notificarLocalResolucionSolicitud(Local local);
}
