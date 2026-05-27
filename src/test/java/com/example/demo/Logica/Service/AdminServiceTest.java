package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private LocalRepositorio localRepositorio;

    @Mock
    private RegistroLocalNotificador registroLocalNotificador;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(localRepositorio, registroLocalNotificador);
    }

    @Test
    void aprobarSolicitudRegistroLocalHabilitaLocalPendienteYNotificaResolucion() {
        Local local = localPendiente();
        when(localRepositorio.buscarPorId(7L)).thenReturn(Optional.of(local));

        adminService.aprobarSolicitudRegistroLocal(7L);

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.HABILITADO);
        assertThat(local.getEstaAbierto()).isFalse();
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void rechazarSolicitudRegistroLocalRechazaLocalPendienteYNotificaResolucion() {
        Local local = localPendiente();
        when(localRepositorio.buscarPorId(7L)).thenReturn(Optional.of(local));

        adminService.rechazarSolicitudRegistroLocal(7L);

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.RECHAZADO);
        assertThat(local.getEstaAbierto()).isFalse();
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void resolverSolicitudApruebaCuandoDtLocalIndicaHabilitado() {
        Local local = localPendiente();
        when(localRepositorio.buscarPorId(7L)).thenReturn(Optional.of(local));
        DtLocal resolucion = DtLocal.builder().id(7L).estado(EstadoLocal.HABILITADO).build();

        adminService.resolverSolicitud(resolucion);

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.HABILITADO);
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void resolverSolicitudRechazaCuandoDtLocalIndicaRechazado() {
        Local local = localPendiente();
        when(localRepositorio.buscarPorId(7L)).thenReturn(Optional.of(local));
        DtLocal resolucion = DtLocal.builder().id(7L).estado(EstadoLocal.RECHAZADO).build();

        adminService.resolverSolicitud(resolucion);

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.RECHAZADO);
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void resolverSolicitudNoCambiaNadaCuandoAdministradorCancelaAntesDeConfirmar() {
        DtLocal resolucionCancelada = DtLocal.builder().id(7L).build();

        adminService.resolverSolicitud(resolucionCancelada);

        verifyNoInteractions(localRepositorio, registroLocalNotificador);
    }

    @Test
    void aprobarSolicitudRegistroLocalRechazaSolicitudQueNoEstaPendiente() {
        Local local = localPendiente();
        local.setEstadoLocal(EstadoLocal.HABILITADO);
        when(localRepositorio.buscarPorId(7L)).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> adminService.aprobarSolicitudRegistroLocal(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Solo se pueden resolver solicitudes en estado Pendiente.");
    }

    private Local localPendiente() {
        Local local = new Local(7L, "La Cocina", new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"),
                "Comida casera", EstadoLocal.PENDIENTE, 0.0, false, List.of("fachada.jpg"));
        local.setEmail("local@foodly.com");
        return local;
    }
}
