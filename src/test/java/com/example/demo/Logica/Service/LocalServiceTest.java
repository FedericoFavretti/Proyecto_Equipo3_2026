package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Mappers.PromocionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LocalServiceTest {

    @Mock
    private LocalRepositorio localRepositorio;

    @Mock
    private PlatoRepositorio platoRepositorio;

    @Mock
    private RegistroLocalNotificador registroLocalNotificador;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private PedidoRepositorio pedidoRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PromocionRepositorio promocionRepositorio;
    @Mock
    private PromocionMapper promocionMapper;
    @Mock
    private ClienteRepositorio clienteRepositorio;

    private LocalService localService;

    private LocalMapper localMapper;

    private PlatoMapper platoMapper;

    @BeforeEach
    void setUp() {
        localMapper = new LocalMapper();
        platoMapper = new PlatoMapper(localMapper);
        localService = new LocalService(
                localRepositorio,
                platoRepositorio,
                registroLocalNotificador,
                usuarioRepositorio,
                pedidoRepositorio,
                passwordEncoder,
                localMapper,
                platoMapper,
                promocionRepositorio,
                promocionMapper,
                clienteRepositorio
                );
    }

    @Test
    void altaPlatoGuardaPlatoParaLocalHabilitado() {
        DtPlato solicitud = platoValido();
        Local local = localHabilitado(false);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(platoRepositorio.buscarPorNombre("Milanesa al pan")).thenReturn(Optional.empty());
        when(platoRepositorio.guardar(org.mockito.ArgumentMatchers.any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plato platoGuardado = localService.altaPlato(solicitud);

        assertThat(platoGuardado.getNombre()).isEqualTo("Milanesa al pan");
        assertThat(platoGuardado.getDescripcion()).isEqualTo("Milanesa con lechuga y tomate");
        assertThat(platoGuardado.getPrecio()).isEqualTo(350.0);
        assertThat(platoGuardado.getDisponible()).isTrue();
        assertThat(platoGuardado.getLocal()).isSameAs(local);
        verify(platoRepositorio).guardar(platoGuardado);
    }

    @Test
    void altaPlatoRechazaNombreVacio() {
        DtPlato solicitud = platoValido();
        solicitud.setNombre(" ");

        assertThatThrownBy(() -> localService.altaPlato(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre del plato es obligatorio.");

        verifyNoInteractions(localRepositorio, platoRepositorio);
    }

    @Test
    void altaPlatoRechazaPrecioInvalido() {
        DtPlato solicitud = platoValido();
        solicitud.setPrecio(0.0);

        assertThatThrownBy(() -> localService.altaPlato(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser un valor numerico mayor a cero.");

        verifyNoInteractions(localRepositorio, platoRepositorio);
    }

    @Test
    void altaPlatoRechazaImagenInvalida() {
        DtPlato solicitud = platoValido();
        solicitud.setImagenes(List.of("milanesa.gif"));

        assertThatThrownBy(() -> localService.altaPlato(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se aceptan imagenes JPG o PNG.");

        verifyNoInteractions(localRepositorio, platoRepositorio);
    }

    @Test
    void altaPlatoRechazaLocalNoHabilitado() {
        DtPlato solicitud = platoValido();
        Local local = localPendiente();
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(platoRepositorio.buscarPorNombre("Milanesa al pan")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localService.altaPlato(solicitud))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El local debe estar habilitado para realizar esta operacion.");

        verify(platoRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any(Plato.class));
    }

    @Test
    void gestionarPlatoModificacionActualizaPlatoExistenteDelLocal() {
        DtPlato solicitud = platoValido();
        solicitud.setNombre("Milanesa completa");
        solicitud.setDescripcion("Milanesa con fritas");
        solicitud.setPrecio(420.0);
        solicitud.setImagenes(List.of("milanesa2.jpg"));

        Local local = localHabilitado(false);
        Plato existente = platoExistente(local);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(existente));
        when(platoRepositorio.buscarPorNombre("Milanesa completa")).thenReturn(Optional.empty());
        when(platoRepositorio.actualizar(org.mockito.ArgumentMatchers.any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plato actualizado = localService.gestionarPlatoModificacion(20L, solicitud);

        assertThat(actualizado.getId()).isEqualTo(20L);
        assertThat(actualizado.getNombre()).isEqualTo("Milanesa completa");
        assertThat(actualizado.getDescripcion()).isEqualTo("Milanesa con fritas");
        assertThat(actualizado.getPrecio()).isEqualTo(420.0);
        assertThat(actualizado.getImagenes()).containsExactly("milanesa2.jpg");
        assertThat(actualizado.getLocal()).isSameAs(local);
        verify(platoRepositorio).actualizar(actualizado);
    }

    @Test
    void gestionarPlatoModificacionRechazaPlatoInexistente() {
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localService.gestionarPlatoModificacion(20L, platoValido()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Plato no encontrado");

        verify(localRepositorio, never()).buscarPorId(10L);
        verify(platoRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any(Plato.class));
    }

    @Test
    void gestionarPlatoModificacionPermiteMantenerNombreDelMismoPlato() {
        DtPlato solicitud = platoValido();
        Local local = localHabilitado(false);
        Plato existente = platoExistente(local);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(existente));
        when(platoRepositorio.buscarPorNombre("Milanesa al pan")).thenReturn(Optional.of(existente));
        when(platoRepositorio.actualizar(org.mockito.ArgumentMatchers.any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Plato actualizado = localService.gestionarPlatoModificacion(20L, solicitud);

        assertThat(actualizado.getNombre()).isEqualTo("Milanesa al pan");
        verify(platoRepositorio).actualizar(actualizado);
    }

    @Test
    void gestionarPlatoModificacionRechazaCuandoPlatoPerteneceAOtroLocal() {
        DtPlato solicitud = platoValido();
        Local localSolicitante = localHabilitado(false);
        Plato existente = platoExistente(localHabilitado(false));
        existente.getLocal().setId(99L);

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localSolicitante));
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> localService.gestionarPlatoModificacion(20L, solicitud))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El plato no pertenece al local indicado.");

        verify(platoRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any(Plato.class));
    }

    @Test
    void gestionarPlatoBajaDesactivaPlatoEnLugarDeEliminarlo() {
        Local local = localHabilitado(false);
        Plato existente = platoExistente(local);
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(existente));
        when(platoRepositorio.actualizar(org.mockito.ArgumentMatchers.any(Plato.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        localService.gestionarPlatoBaja(20L);

        assertThat(existente.getDisponible()).isFalse();
        verify(platoRepositorio).actualizar(existente);
        verify(platoRepositorio, never()).eliminar(20L);
    }

    @Test
    void gestionarPlatoBajaRechazaPlatoInexistente() {
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localService.gestionarPlatoBaja(20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Plato no encontrado");

        verify(platoRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any(Plato.class));
        verify(platoRepositorio, never()).eliminar(20L);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRegistraSolicitudPendienteYNotificaAdministrador() {
        DtLocal solicitud = solicitudValida();
        when(localRepositorio.buscarPorNombre("La Cocina")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");

        localService.solicitarRegistroComoLocalHabilitado(solicitud);

        ArgumentCaptor<Local> localCaptor = ArgumentCaptor.forClass(Local.class);
        verify(usuarioRepositorio).guardar(localCaptor.capture());
        verify(localRepositorio).guardar(localCaptor.capture());

        Local localGuardado = localCaptor.getAllValues().getLast();
        assertThat(localGuardado.getEmail()).isEqualTo("local@foodly.com");
        assertThat(localGuardado.getPasswd()).isEqualTo("encoded-123456");
        assertThat(localGuardado.getEstado()).isEqualTo(EstadoCuenta.Pendiente);
        assertThat(localGuardado.getTipo()).isEqualTo("local");
        assertThat(localGuardado.getNombre()).isEqualTo("La Cocina");
        assertThat(localGuardado.getDireccion().getCalle()).isEqualTo("Av. Italia");
        assertThat(localGuardado.getEstadoLocal()).isEqualTo(EstadoLocal.Pendiente);
        assertThat(localGuardado.getEstaAbierto()).isFalse();
        assertThat(localGuardado.getCalificacionGlobal()).isZero();
        assertThat(localGuardado.getImagenes()).containsExactly("fachada.jpg", "producto.png");
        verify(registroLocalNotificador).notificarAdministradorSolicitudPendiente(localGuardado);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoIgnoraEstadosInternosEnviadosPorCliente() {
        DtLocal solicitud = solicitudValida();
        solicitud.setTipo("admin");
        solicitud.setEstadoCuenta(EstadoCuenta.Activo);
        solicitud.setEstadoLocal(EstadoLocal.Habilitado);
        solicitud.setCalificacionGlobal(99.0);
        solicitud.setEstaAbierto(true);
        when(localRepositorio.buscarPorNombre("La Cocina")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");

        localService.solicitarRegistroComoLocalHabilitado(solicitud);

        ArgumentCaptor<Local> localCaptor = ArgumentCaptor.forClass(Local.class);
        verify(localRepositorio).guardar(localCaptor.capture());
        Local localGuardado = localCaptor.getValue();

        assertThat(localGuardado.getEstado()).isEqualTo(EstadoCuenta.Pendiente);
        assertThat(localGuardado.getTipo()).isEqualTo("local");
        assertThat(localGuardado.getEstadoLocal()).isEqualTo(EstadoLocal.Pendiente);
        assertThat(localGuardado.getCalificacionGlobal()).isZero();
        assertThat(localGuardado.getEstaAbierto()).isFalse();
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaCamposFaltantesConMensajeDocumentado() {
        DtLocal solicitud = DtLocal.builder()
                .direccion(new DtDireccion("", null, " ", ""))
                .imagenes(List.of())
                .build();

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Los siguientes campos son requeridos: email, passwd, nombre, calle, numero, ciudad, codigoPostal, descripcion, imagenes. Por favor, completelos antes de enviar.");

        verifyNoInteractions(localRepositorio, usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaCorreoInvalidoConMensajeDocumentado() {
        DtLocal solicitud = solicitudValida();
        solicitud.setEmail("correo-invalido");

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo electronico ingresado no tiene un formato valido.");

        verifyNoInteractions(localRepositorio, usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaImagenInvalidaConMensajeDocumentado() {
        DtLocal solicitud = solicitudValida();
        solicitud.setImagenes(List.of("fachada.gif"));

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se aceptan imagenes en formato JPG o PNG de hasta 10 MB cada una.");

        verifyNoInteractions(localRepositorio, usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void solicitarRegistroComoLocalHabilitadoRechazaNombreDeLocalRegistrado() {
        DtLocal solicitud = solicitudValida();
        when(localRepositorio.buscarPorNombre("La Cocina")).thenReturn(Optional.of(localHabilitado(false)));

        assertThatThrownBy(() -> localService.solicitarRegistroComoLocalHabilitado(solicitud))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre del local ya se encuentra registrado.");

        verify(localRepositorio).buscarPorNombre("La Cocina");
        verifyNoInteractions(usuarioRepositorio, registroLocalNotificador);
    }

    @Test
    void registrarAperturaAbreLocalHabilitadoQueEstabaCerrado() {
        Local local = localHabilitado(false);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        localService.registrarApertura(10L);

        assertThat(local.getEstaAbierto()).isTrue();
        verify(localRepositorio).actualizar(local);
        verifyNoInteractions(pedidoRepositorio);
    }

    @Test
    void registrarAperturaRechazaCuandoLocalYaEstaAbierto() {
        Local local = localHabilitado(true);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> localService.registrarApertura(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El local ya se encuentra registrado como abierto para el dia de hoy.");

        verify(localRepositorio, never()).actualizar(local);
        verifyNoInteractions(pedidoRepositorio);
    }

    @Test
    void registrarCierreCierraLocalSinPedidosPendientes() {
        Local local = localHabilitado(true);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(pedidoRepositorio.existePedidoPendientePorLocal(10L)).thenReturn(false);

        localService.regitrarCierre(10L);

        assertThat(local.getEstaAbierto()).isFalse();
        verify(localRepositorio).actualizar(local);
        verify(pedidoRepositorio).existePedidoPendientePorLocal(10L);
    }

    @Test
    void registrarCierreRechazaCuandoLocalYaEstaCerrado() {
        Local local = localHabilitado(false);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> localService.regitrarCierre(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El local ya se encuentra registrado como cerrado.");

        verify(localRepositorio, never()).actualizar(local);
        verifyNoInteractions(pedidoRepositorio);
    }

    @Test
    void registrarCierreRechazaCuandoHayPedidosPendientes() {
        Local local = localHabilitado(true);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(pedidoRepositorio.existePedidoPendientePorLocal(10L)).thenReturn(true);

        assertThatThrownBy(() -> localService.regitrarCierre(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El local no puede cerrarse porque tiene pedidos pendientes de confirmacion.");

        verify(localRepositorio, never()).actualizar(local);
        verify(pedidoRepositorio).existePedidoPendientePorLocal(10L);
    }

    private DtLocal solicitudValida() {
        DtLocal solicitud = DtLocal.builder()
                .nombre("La Cocina")
                .direccion(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .descripcion("Comida casera")
                .imagenes(List.of("fachada.jpg", "producto.png"))
                .build();
        solicitud.setEmail("local@foodly.com");
        solicitud.setPasswd("123456");
        return solicitud;
    }

    private DtPlato platoValido() {
        DtLocal dtLocal = DtLocal.builder().build();
        dtLocal.setId(10L);

        return DtPlato.builder()
                .nombre("Milanesa al pan")
                .descripcion("Milanesa con lechuga y tomate")
                .precio(350.0)
                .imagenes(List.of("milanesa.jpg"))
                .disponible(true)
                .dtLocal(dtLocal)
                .build();
    }

    private Local localHabilitado(boolean estaAbierto) {
        return Local.builder()
                .id(10L)
                .email("local@foodly.com")
                .nombre("La Cocina")
                .direccion(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .descripcion("Comida casera")
                .estadoLocal(EstadoLocal.Habilitado)
                .calificacionGlobal(4.5)
                .estaAbierto(estaAbierto)
                .imagenes(List.of("fachada.jpg"))
                .build();
    }

    private Local localPendiente() {
        Local local = localHabilitado(false);
        local.setEstadoLocal(EstadoLocal.Pendiente);
        return local;
    }

    private Plato platoExistente(Local local) {
        return Plato.builder()
                .id(20L)
                .nombre("Milanesa al pan")
                .descripcion("Milanesa con lechuga y tomate")
                .precio(350.0)
                .imagenes(List.of("milanesa.jpg"))
                .disponible(true)
                .local(local)
                .build();
    }
}

