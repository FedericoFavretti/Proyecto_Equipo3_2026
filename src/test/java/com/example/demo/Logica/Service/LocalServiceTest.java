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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;

@ExtendWith(MockitoExtension.class)
class LocalServiceTest {

    @Mock
    private LocalRepositorio localRepositorio;

    @Mock
    private PlatoRepositorio platoRepositorio;

    @Mock
    private RegistroLocalNotificador registroLocalNotificador;

    private LocalService localService;

    @BeforeEach
    void setUp() {
        localService = new LocalService(localRepositorio, platoRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRegistraSolicitudPendienteYNotificaAdministrador() {
        DtLocal solicitud = solicitudValida();
        when(localRepositorio.buscarPorNombre("La Cocina")).thenReturn(Optional.empty());

        localService.solicitarRegistroComoLocalHabilitado(solicitud);

        ArgumentCaptor<Local> localCaptor = ArgumentCaptor.forClass(Local.class);
        verify(localRepositorio).guardar(localCaptor.capture());

        Local localGuardado = localCaptor.getValue();
        assertThat(localGuardado.getEmail()).isEqualTo("local@foodly.com");
        assertThat(localGuardado.getNombre()).isEqualTo("La Cocina");
        assertThat(localGuardado.getDireccion().getCalle()).isEqualTo("Av. Italia");
        assertThat(localGuardado.getEstadoLocal()).isEqualTo(EstadoLocal.PENDIENTE);
        assertThat(localGuardado.getEstaAbierto()).isFalse();
        assertThat(localGuardado.getCalificacionGlobal()).isZero();
        assertThat(localGuardado.getImagenes()).containsExactly("fachada.jpg", "producto.png");
        verify(registroLocalNotificador).notificarAdministradorSolicitudPendiente(localGuardado);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaCamposFaltantesConMensajeDocumentado() {
        DtLocal solicitud = DtLocal.builder()
                .email(" ")
                .direccion(new DtDireccion("", null, " ", ""))
                .imagenes(List.of())
                .build();

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Los siguientes campos son requeridos: email, nombre, calle, numero, ciudad, codigoPostal, descripcion, imagenes. Por favor, complételos antes de enviar.");

        verifyNoInteractions(localRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaCorreoInvalidoConMensajeDocumentado() {
        DtLocal solicitud = solicitudValida();
        solicitud.setEmail("correo-invalido");

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo electrónico ingresado no tiene un formato válido.");

        verifyNoInteractions(localRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaImagenInvalidaConMensajeDocumentado() {
        DtLocal solicitud = solicitudValida();
        solicitud.setImagenes(List.of("fachada.gif"));

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se aceptan imágenes en formato JPG o PNG de hasta 10 MB cada una.");

        verifyNoInteractions(localRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaNombreDeLocalRegistrado() {
        DtLocal solicitud = solicitudValida();
        when(localRepositorio.buscarPorNombre("La Cocina")).thenReturn(Optional.of(localExistente()));

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre del local ya se encuentra registrado.");

        verify(localRepositorio).buscarPorNombre("La Cocina");
        verifyNoInteractions(registroLocalNotificador);
    }

    private DtLocal solicitudValida() {
        return DtLocal.builder()
                .email("local@foodly.com")
                .nombre("La Cocina")
                .direccion(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .descripcion("Comida casera")
                .imagenes(List.of("fachada.jpg", "producto.png"))
                .build();
    }

    private Local localExistente() {
        Local local = new Local(99L, "La Cocina", new DtDireccion("A", "1", "Montevideo", "11000"),
                "Comida", EstadoLocal.PENDIENTE, 0.0, false, List.of("local.jpg"));
        local.setEmail("otro@foodly.com");
        return local;
    }
}
