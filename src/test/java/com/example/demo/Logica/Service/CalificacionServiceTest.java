package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Logica.DataTypes.response.DtMiCalificacionLocalResponse;
import com.example.demo.Logica.DataTypes.shared.DtCalificacion;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Mappers.CalificacionMapper;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
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
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private ClienteMapper clienteMapper;
    @Mock
    private LocalMapper localMapper;
    @Mock
    private PedidoRepositorio pedidoRepositorio;

    private CalificacionService calificacionService;

    @BeforeEach
    void setUp() {
        calificacionService = new CalificacionService(
                calificacionRepositorio,
                localRepositorio,
                usuarioRepositorio,
                calificacionMapper,
                clienteRepositorio,
                clienteMapper,
                localMapper,
                pedidoRepositorio
        );
    }

    @Test
    void consultarCalificacionGlobalDelLocalRetornaResumenYActualizaCache() {
        Local local = local();
        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(local));
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of(
                calificacionClienteALocal(5),
                calificacionClienteALocal(4),
                calificacionClienteALocal(4),
                calificacionClienteALocal(2)
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
    void consultarCalificacionGlobalDelClienteUsaSoloCalificacionesRecibidas() {
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(cliente()));
        when(calificacionRepositorio.listarPorCliente(20L)).thenReturn(List.of(
                calificacionLocalACliente(5),
                calificacionLocalACliente(3)
        ));

        DtCalificacionGlobalResponse respuesta = calificacionService.consultarCalificacionGlobal(20L);

        assertThat(respuesta.getPromedio()).isEqualTo(4.0);
        assertThat(respuesta.getTotalCalificaciones()).isEqualTo(2);
        assertThat(respuesta.getDetallePorPuntuacion()).containsEntry(3, 1).containsEntry(5, 1);
    }

    @Test
    void calificarClienteALocalActualizaCalificacionExistente() {
        Cliente clienteAutenticado = cliente();
        Local local = local();
        DtCalificacion solicitud = DtCalificacion.builder()
                .puntaje(4)
                .comentario("Mejor�")
                .dtLocal(DtLocal.builder().id(10L).build())
                .build();
        Calificacion existente = Calificacion.builder()
                .id(99L)
                .puntaje(2)
                .comentario("Regular")
                .fecha(LocalDateTime.now().minusDays(2))
                .tipo(TipoCalificacion.Cliente_a_local)
                .cliente(clienteAutenticado)
                .local(local)
                .build();

        when(usuarioRepositorio.buscarPorEmail("cliente@test.com")).thenReturn(Optional.of(clienteAutenticado));
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(pedidoRepositorio.existePedidoDeClienteEnLocal(20L, 10L)).thenReturn(true);
        when(calificacionRepositorio.buscarCalificacionClienteALocal(20L, 10L)).thenReturn(Optional.of(existente));
        when(calificacionRepositorio.listarPorLocal(10L)).thenReturn(List.of(existente));

        calificacionService.calificar(solicitud, "cliente@test.com");

        verify(calificacionRepositorio).actualizar(existente);
        verify(calificacionRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
        assertThat(existente.getPuntaje()).isEqualTo(4);
        assertThat(existente.getComentario()).isEqualTo("Mejor�");
        verify(localRepositorio).actualizar(local);
    }

    @Test
    void calificarClienteALocalRechazaSinPedidosPrevios() {
        Cliente clienteAutenticado = cliente();
        DtCalificacion solicitud = DtCalificacion.builder()
                .puntaje(5)
                .dtLocal(DtLocal.builder().id(10L).build())
                .build();

        when(usuarioRepositorio.buscarPorEmail("cliente@test.com")).thenReturn(Optional.of(clienteAutenticado));
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local()));
        when(pedidoRepositorio.existePedidoDeClienteEnLocal(20L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> calificacionService.calificar(solicitud, "cliente@test.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo puede calificar locales en los que haya realizado al menos un pedido.");
    }

    @Test
    void consultarMiCalificacionDeLocalRetornaLaCalificacionExistente() {
        Cliente clienteAutenticado = cliente();
        Local local = local();
        Calificacion existente = Calificacion.builder()
                .id(99L)
                .puntaje(5)
                .comentario("Excelente")
                .fecha(LocalDateTime.now())
                .tipo(TipoCalificacion.Cliente_a_local)
                .cliente(clienteAutenticado)
                .local(local)
                .build();

        when(usuarioRepositorio.buscarPorEmail("cliente@test.com")).thenReturn(Optional.of(clienteAutenticado));
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(calificacionRepositorio.buscarCalificacionClienteALocal(20L, 10L)).thenReturn(Optional.of(existente));

        DtMiCalificacionLocalResponse respuesta = calificacionService.consultarMiCalificacionDeLocal(10L, "cliente@test.com");

        assertThat(respuesta.getId()).isEqualTo(99L);
        assertThat(respuesta.getPuntaje()).isEqualTo(5);
        assertThat(respuesta.getComentario()).isEqualTo("Excelente");
    }

    @Test
    void calificarLocalAClienteCreaNuevaCalificacionCuandoNoExiste() {
        Local localAutenticado = local();
        Cliente clienteACalificar = cliente();
        DtCalificacion solicitud = DtCalificacion.builder()
                .puntaje(5)
                .comentario("Cliente puntual y respetuoso")
                .dtCliente(DtCliente.builder().id(20L).build())
                .build();
        Calificacion calificacionMapeada = Calificacion.builder()
                .puntaje(5)
                .comentario("Cliente puntual y respetuoso")
                .tipo(TipoCalificacion.Local_a_cliente)
                .cliente(clienteACalificar)
                .local(localAutenticado)
                .build();

        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(localAutenticado));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(clienteACalificar));
        when(pedidoRepositorio.existePedidoDeClienteEnLocal(20L, 10L)).thenReturn(true);
        when(calificacionRepositorio.buscarCalificacionLocalACliente(20L, 10L)).thenReturn(Optional.empty());
        when(calificacionMapper.mapearCalificacionDeDt(solicitud)).thenReturn(calificacionMapeada);
        when(calificacionRepositorio.listarPorCliente(20L)).thenReturn(List.of(calificacionMapeada));

        calificacionService.calificar(solicitud, "local@test.com");

        verify(calificacionRepositorio).guardar(calificacionMapeada);
        verify(calificacionRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any());
        verify(clienteRepositorio).actualizar(clienteACalificar);
        assertThat(clienteACalificar.getCalificacionGlobal()).isEqualTo(5.0);
    }

    @Test
    void calificarLocalAClienteActualizaCalificacionExistente() {
        Local localAutenticado = local();
        Cliente clienteACalificar = cliente();
        DtCalificacion solicitud = DtCalificacion.builder()
                .puntaje(2)
                .comentario("No estaba en el domicilio indicado")
                .dtCliente(DtCliente.builder().id(20L).build())
                .build();
        Calificacion existente = Calificacion.builder()
                .id(77L)
                .puntaje(5)
                .comentario("Todo bien la primera vez")
                .fecha(LocalDateTime.now().minusDays(10))
                .tipo(TipoCalificacion.Local_a_cliente)
                .cliente(clienteACalificar)
                .local(localAutenticado)
                .build();

        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(localAutenticado));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(clienteACalificar));
        when(pedidoRepositorio.existePedidoDeClienteEnLocal(20L, 10L)).thenReturn(true);
        when(calificacionRepositorio.buscarCalificacionLocalACliente(20L, 10L)).thenReturn(Optional.of(existente));
        when(calificacionRepositorio.listarPorCliente(20L)).thenReturn(List.of(existente));


        calificacionService.calificar(solicitud, "local@test.com");

        verify(calificacionRepositorio).actualizar(existente);
        verify(calificacionRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
        assertThat(existente.getPuntaje()).isEqualTo(2);
        assertThat(existente.getComentario()).isEqualTo("No estaba en el domicilio indicado");
        verify(clienteRepositorio).actualizar(clienteACalificar);
    }

    @Test
    void calificarLocalAClienteRechazaSinPedidosPrevios() {
        Local localAutenticado = local();
        Cliente clienteACalificar = cliente();
        DtCalificacion solicitud = DtCalificacion.builder()
                .puntaje(3)
                .dtCliente(DtCliente.builder().id(20L).build())
                .build();

        when(usuarioRepositorio.buscarPorEmail("local@test.com")).thenReturn(Optional.of(localAutenticado));
        when(clienteRepositorio.buscarPorId(20L)).thenReturn(Optional.of(clienteACalificar));
        when(pedidoRepositorio.existePedidoDeClienteEnLocal(20L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> calificacionService.calificar(solicitud, "local@test.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Solo puede calificar a clientes que hayan realizado al menos un pedido en su local.");

        verify(calificacionRepositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
        verify(calificacionRepositorio, never()).actualizar(org.mockito.ArgumentMatchers.any());
    }

    private Local local() {
        return Local.builder()
                .id(10L)
                .email("local@test.com")
                .nombre("La Cocina")
                .calificacionGlobal(0.0)
                .build();
    }

    private Cliente cliente() {
        return Cliente.builder()
                .id(20L)
                .email("cliente@test.com")
                .nombre("Ana")
                .apellido("P�rez")
                .calificacionGlobal(0.0)
                .build();
    }

    private Calificacion calificacionClienteALocal(int puntaje) {
        return Calificacion.builder()
                .puntaje(puntaje)
                .tipo(TipoCalificacion.Cliente_a_local)
                .local(local())
                .cliente(cliente())
                .build();
    }

    private Calificacion calificacionLocalACliente(int puntaje) {
        return Calificacion.builder()
                .puntaje(puntaje)
                .tipo(TipoCalificacion.Local_a_cliente)
                .local(local())
                .cliente(cliente())
                .build();
    }
}
