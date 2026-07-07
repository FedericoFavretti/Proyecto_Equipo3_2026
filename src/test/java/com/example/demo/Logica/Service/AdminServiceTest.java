package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private LocalRepositorio localRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private RegistroLocalNotificador registroLocalNotificador;

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @Mock
    private UsuarioService usuarioService;

    @Test
    void listarSolicitudesPendientesDevuelveResumenSolicitudes() {
        AdminService adminService = crearServicio();
        when(localRepositorio.listarPendientes()).thenReturn(List.of(localPendiente(10L), localPendiente(11L)));

        List<DtSolicitudLocalPendienteResponse> pendientes = adminService.listarSolicitudesPendientes();

        assertThat(pendientes).hasSize(2);
        assertThat(pendientes.getFirst().getId()).isEqualTo(10L);
        assertThat(pendientes.getFirst().getEmail()).isEqualTo("local10@foodly.com");
        assertThat(pendientes.getFirst().getNombre()).isEqualTo("Local 10");
        assertThat(pendientes.getFirst().getDescripcion()).isEqualTo("Comida casera");
        assertThat(pendientes.getFirst().getImagenes()).containsExactly("fachada.jpg", "cocina.png");
    }

    @Test
    void resolverSolicitudApruebaLocalPendienteYActivaCuenta() {
        AdminService adminService = crearServicio();
        Local local = localPendiente(10L);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        adminService.resolverSolicitud(new DtResolverSolicitudLocalRequest(10L, EstadoLocal.Habilitado));

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.Habilitado);
        assertThat(local.getEstado()).isEqualTo(EstadoCuenta.Activo);
        assertThat(local.getEstaAbierto()).isFalse();
        verify(usuarioRepositorio).actualizarEstado(10L, EstadoCuenta.Activo);
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void resolverSolicitudRechazaLocalPendienteYBloqueaCuenta() {
        AdminService adminService = crearServicio();
        Local local = localPendiente(10L);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        adminService.resolverSolicitud(new DtResolverSolicitudLocalRequest(10L, EstadoLocal.Rechazado));

        assertThat(local.getEstadoLocal()).isEqualTo(EstadoLocal.Rechazado);
        assertThat(local.getEstado()).isEqualTo(EstadoCuenta.Bloqueado);
        verify(usuarioRepositorio).actualizarEstado(10L, EstadoCuenta.Bloqueado);
        verify(localRepositorio).actualizar(local);
        verify(registroLocalNotificador).notificarLocalResolucionSolicitud(local);
    }

    @Test
    void resolverSolicitudRechazaEstadoObjetivoInvalido() {
        AdminService adminService = crearServicio();

        assertThatThrownBy(() -> adminService.resolverSolicitud(new DtResolverSolicitudLocalRequest(10L, EstadoLocal.Pendiente)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El estado objetivo debe ser Habilitado o Rechazado.");

        verifyNoInteractions(localRepositorio, usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void resolverSolicitudRechazaRequestNulo() {
        AdminService adminService = crearServicio();

        assertThatThrownBy(() -> adminService.resolverSolicitud(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe indicar el estado objetivo de la solicitud.");

        verifyNoInteractions(localRepositorio, usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void resolverSolicitudFallaSiLocalNoExiste() {
        AdminService adminService = crearServicio();
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.resolverSolicitud(new DtResolverSolicitudLocalRequest(10L, EstadoLocal.Habilitado)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Local no encontrado con id: 10");

        verify(localRepositorio).buscarPorId(10L);
        verify(usuarioRepositorio, never()).actualizarEstado(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        verify(registroLocalNotificador, never()).notificarLocalResolucionSolicitud(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolverSolicitudFallaSiLocalNoEstaPendiente() {
        AdminService adminService = crearServicio();
        Local local = localPendiente(10L);
        local.setEstadoLocal(EstadoLocal.Habilitado);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> adminService.resolverSolicitud(new DtResolverSolicitudLocalRequest(10L, EstadoLocal.Rechazado)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo se pueden resolver solicitudes en estado Pendiente.");

        verify(localRepositorio).buscarPorId(10L);
        verify(usuarioRepositorio, never()).actualizarEstado(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        verify(localRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any());
    }

    private AdminService crearServicio() {
        return new AdminService(localRepositorio, usuarioRepositorio, registroLocalNotificador,
                clienteRepositorio, usuarioService);
    }

    private Local localPendiente(Long id) {
        return Local.builder()
                .id(id)
                .email("local" + id + "@foodly.com")
                .estado(EstadoCuenta.Pendiente)
                .tipo("local")
                .nombre("Local " + id)
                .direccion(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .descripcion("Comida casera")
                .estadoLocal(EstadoLocal.Pendiente)
                .calificacionGlobal(0.0)
                .estaAbierto(false)
                .imagenes(List.of("fachada.jpg", "cocina.png"))
                .build();
    }
}

