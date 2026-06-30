package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Enums.PeriodoEstadisticasPreset;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Interfaces.RegistroLocalNotificador;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Mappers.PromocionMapper;
import com.example.demo.Logica.Record.PlatoMasPedidoProjection;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalStatisticsServiceTest {

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

    @BeforeEach
    void setUp() {
        LocalMapper localMapper = new LocalMapper();
        PlatoMapper platoMapper = new PlatoMapper(localMapper);
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
    void obtenerEstadisticasLocalUsaRangoLibreYDevuelveMetricas() {
        Local local = localHabilitado();
        Plato plato = plato(local);
        DtEstadisticasLocalFiltro filtro = DtEstadisticasLocalFiltro.builder()
                .fechaDesde(LocalDate.of(2026, 6, 1))
                .fechaHasta(LocalDate.of(2026, 6, 15))
                .build();

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(pedidoRepositorio.existePedidoValidoParaEstadisticasEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(pedidoRepositorio.obtenerPlatosMasPedidosEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of(new PlatoMasPedidoProjection(20L, 7)));
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(plato));
        when(pedidoRepositorio.obtenerVentasParaEstadisticasEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2450.0);

        DtEstadisticasLocal resultado = localService.obtenerEstadisticasLocal(10L, filtro);

        assertThat(resultado.getFechaDesde()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(resultado.getFechaHasta()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(resultado.getVentasConfirmadas()).isEqualTo(2450.0);
        assertThat(resultado.getPlatosMasPedido()).hasSize(1);
        assertThat(resultado.getPlatosMasPedido().get(0).getId()).isEqualTo(20L);
        assertThat(resultado.getPlatosMasPedido().get(0).getNombre()).isEqualTo("Milanesa al pan");

        ArgumentCaptor<LocalDateTime> desdeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(pedidoRepositorio).obtenerVentasParaEstadisticasEnPeriodo(eq(10L), desdeCaptor.capture(), hastaCaptor.capture());
        assertThat(desdeCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(hastaCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 6, 16, 0, 0));
    }

    @Test
    void obtenerEstadisticasLocalUsaMesActualPorDefecto() {
        Local local = localHabilitado();
        Plato plato = plato(local);

        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(local));
        when(pedidoRepositorio.existePedidoValidoParaEstadisticasEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(pedidoRepositorio.obtenerPlatosMasPedidosEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class), eq(5)))
                .thenReturn(List.of(new PlatoMasPedidoProjection(20L, 3)));
        when(platoRepositorio.buscarPorId(20L)).thenReturn(Optional.of(plato));
        when(pedidoRepositorio.obtenerVentasParaEstadisticasEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(600.0);

        DtEstadisticasLocal resultado = localService.obtenerEstadisticasLocal(10L, null);

        LocalDate hoy = LocalDate.now();
        assertThat(resultado.getFechaDesde()).isEqualTo(hoy.withDayOfMonth(1));
        assertThat(resultado.getFechaHasta()).isEqualTo(hoy);
    }

    @Test
    void obtenerEstadisticasLocalRechazaPresetYRangoLibreAlMismoTiempo() {
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localHabilitado()));

        DtEstadisticasLocalFiltro filtro = DtEstadisticasLocalFiltro.builder()
                .preset(PeriodoEstadisticasPreset.MES_ACTUAL)
                .fechaDesde(LocalDate.of(2026, 6, 1))
                .fechaHasta(LocalDate.of(2026, 6, 30))
                .build();

        assertThatThrownBy(() -> localService.obtenerEstadisticasLocal(10L, filtro))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe enviar un preset o un rango libre, pero no ambos.");

        verify(pedidoRepositorio, never()).existePedidoValidoParaEstadisticasEnPeriodo(any(), any(), any());
    }

    @Test
    void obtenerEstadisticasLocalInformaCuandoNoHayPedidosValidosEnPeriodo() {
        when(localRepositorio.buscarPorId(10L)).thenReturn(Optional.of(localHabilitado()));
        when(pedidoRepositorio.existePedidoValidoParaEstadisticasEnPeriodo(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        DtEstadisticasLocalFiltro filtro = DtEstadisticasLocalFiltro.builder()
                .preset(PeriodoEstadisticasPreset.HOY)
                .build();

        assertThatThrownBy(() -> localService.obtenerEstadisticasLocal(10L, filtro))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No hay informacion disponible para el periodo seleccionado. Intente con un rango de fechas diferente.");
    }

    private Local localHabilitado() {
        return Local.builder()
                .id(10L)
                .email("local@foodly.com")
                .nombre("La Cocina")
                .direccion(new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"))
                .descripcion("Comida casera")
                .estadoLocal(EstadoLocal.Habilitado)
                .estaAbierto(false)
                .calificacionGlobal(4.8)
                .imagenes(List.of("fachada.jpg"))
                .build();
    }

    private Plato plato(Local local) {
        return Plato.builder()
                .id(20L)
                .nombre("Milanesa al pan")
                .descripcion("Clasica")
                .precio(350.0)
                .imagenes(List.of("milanesa.jpg"))
                .disponible(true)
                .local(local)
                .build();
    }
}
