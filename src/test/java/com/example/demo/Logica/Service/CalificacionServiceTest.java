package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Mappers.CalificacionMapper;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private CalificacionRepositorio calificacionRepositorio;
    @Mock
    private LocalRepositorio localRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private CalificacionMapper calificacionMapper;

    private CalificacionService calificacionService;

    @BeforeEach
    void setUp() {
        calificacionService = new CalificacionService(
                calificacionRepositorio,
                localRepositorio,
                usuarioRepositorio,
                calificacionMapper
        );
    }

    @Test
    void consultarCalificacionGlobalDelLocalRetornaResumenYActualizaCache() {
        Local local = local();
        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(local));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of(
                calificacion(5),
                calificacion(4),
                calificacion(4),
                calificacion(2)
        ));

        Map<String, Object> respuesta = calificacionService.consultarCalificacionGlobalDelLocal("local@test.com");

        assertThat(respuesta.get("calificacionGlobal")).isEqualTo(3.75);
        assertThat(respuesta.get("totalValoraciones")).isEqualTo(4);
        assertThat(respuesta.get("detallePorPuntuacion"))
                .isEqualTo(Map.of("1", 0L, "2", 1L, "3", 0L, "4", 2L, "5", 1L));
        verify(localRepositorio).actualizar(local);
        assertThat(local.getCalificacionGlobal()).isEqualTo(3.75);
    }

    @Test
    void consultarCalificacionGlobalDelLocalPorIdRetornaResumenYActualizaCache() {
        Local local = local();
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of(
                calificacion(5),
                calificacion(3)
        ));

        Map<String, Object> respuesta = calificacionService.consultarCalificacionGlobalDelLocalPorId(10L);

        assertThat(respuesta.get("calificacionGlobal")).isEqualTo(4.0);
        assertThat(respuesta.get("totalValoraciones")).isEqualTo(2);
        verify(localRepositorio).actualizar(local);
    }

    @Test
    void consultarCalificacionGlobalDelLocalInformaCuandoNoHayCalificaciones() {
        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(local()));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> calificacionService.consultarCalificacionGlobalDelLocal("local@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Su local todavía no ha recibido calificaciones de los clientes.");
    }

    @Test
    void calificarSincronizaCalificacionGlobalDelLocal() {
        DtCalificacion dtCalificacion = DtCalificacion.builder()
                .puntaje(5)
                .tipo(TipoCalificacion.Cliente_a_local)
                .dtCliente(DtCliente.builder().id(20L).build())
                .dtLocal(DtLocal.builder().id(10L).build())
                .build();

        Calificacion calificacion = Calificacion.builder()
                .puntaje(5)
                .tipo(TipoCalificacion.Cliente_a_local)
                .cliente(com.example.demo.Logica.Clases.Cliente.builder().id(20L).build())
                .local(Local.builder().id(10L).nombre("La Cocina").build())
                .build();

        Local localPersistido = local();

        when(calificacionMapper.mapearCalificacionDeDt(dtCalificacion)).thenReturn(calificacion);
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localPersistido));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of(calificacion(5), calificacion(3)));

        calificacionService.calificar(dtCalificacion);

        verify(calificacionRepositorio).guardar(calificacion);
        ArgumentCaptor<Local> localCaptor = ArgumentCaptor.forClass(Local.class);
        verify(localRepositorio).actualizar(localCaptor.capture());
        assertThat(localCaptor.getValue().getCalificacionGlobal()).isEqualTo(4.0);
    }

    @Test
    void calificarRechazaSiFaltaClienteOLocal() {
        DtCalificacion dtCalificacion = DtCalificacion.builder()
                .puntaje(4)
                .dtLocal(DtLocal.builder().id(10L).build())
                .build();

        assertThatThrownBy(() -> calificacionService.calificar(dtCalificacion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe indicarse tanto el cliente como el local asociados a la calificación.");
    }

    private Local local() {
        return Local.builder()
                .id(10L)
                .email("local@test.com")
                .nombre("La Cocina")
                .calificacionGlobal(0.0)
                .build();
    }

    private Calificacion calificacion(int puntaje) {
        return Calificacion.builder()
                .puntaje(puntaje)
                .local(Local.builder().id(10L).build())
                .cliente(com.example.demo.Logica.Clases.Cliente.builder().id(20L).build())
                .build();
    }
}
