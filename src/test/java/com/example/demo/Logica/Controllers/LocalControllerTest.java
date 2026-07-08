package com.example.demo.Logica.Controllers;
import com.example.demo.Logica.DataTypes.response.DtPromocionesLocalResponse;
import com.example.demo.Logica.DataTypes.response.DtPlatoEstadistica;
import com.example.demo.Logica.DataTypes.response.DtVentaMensualEstadistica;
import com.example.demo.Logica.Enums.PeriodoEstadisticasPreset;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.Service.LocalService;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalControllerTest {

    private LocalService localService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        localService = Mockito.mock(LocalService.class);
        CloudinaryService cloudinaryService = Mockito.mock(CloudinaryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LocalController(localService, cloudinaryService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void obtenerEstadisticasAceptaPresetComoQueryParam() throws Exception {
        when(localService.obtenerEstadisticasLocal(eq(10L), any(DtEstadisticasLocalFiltro.class)))
                .thenReturn(DtEstadisticasLocal.builder()
                        .fechaDesde(LocalDate.of(2026, 6, 23))
                        .fechaHasta(LocalDate.of(2026, 6, 29))
                        .ventasConfirmadas(1500.0)
                        .platosMasPedido(List.of(
                                DtPlatoEstadistica.builder()
                                        .id(20L)
                                        .nombre("Milanesa al pan")
                                        .imagen("milanesa.jpg")
                                        .cantidadVendida(4)
                                        .montoVendido(1400.0)
                                        .build()))
                        .ventasPorPlato(List.of(
                                DtPlatoEstadistica.builder()
                                        .id(20L)
                                        .nombre("Milanesa al pan")
                                        .imagen("milanesa.jpg")
                                        .cantidadVendida(4)
                                        .montoVendido(1400.0)
                                        .build()))
                        .ventasMensuales(List.of(
                                DtVentaMensualEstadistica.builder()
                                        .anio(2026)
                                        .mes(6)
                                        .montoVendido(1500.0)
                                        .build()))
                        .build());

        mockMvc.perform(get("/api/v1/locales/estadisticas/10")
                        .param("preset", "ULTIMOS_7_DIAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasConfirmadas").value(1500.0))
                .andExpect(jsonPath("$.fechaDesde").value("2026-06-23"))
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-29"))
                .andExpect(jsonPath("$.platosMasPedido[0].id").value(20))
                .andExpect(jsonPath("$.platosMasPedido[0].nombre").value("Milanesa al pan"))
                .andExpect(jsonPath("$.platosMasPedido[0].cantidadVendida").value(4))
                .andExpect(jsonPath("$.ventasPorPlato[0].montoVendido").value(1400.0))
                .andExpect(jsonPath("$.ventasMensuales[0].anio").value(2026))
                .andExpect(jsonPath("$.ventasMensuales[0].mes").value(6))
                .andExpect(jsonPath("$.ventasMensuales[0].montoVendido").value(1500.0));

        ArgumentCaptor<DtEstadisticasLocalFiltro> captor = ArgumentCaptor.forClass(DtEstadisticasLocalFiltro.class);
        verify(localService).obtenerEstadisticasLocal(eq(10L), captor.capture());
        assertThat(captor.getValue().getPreset()).isEqualTo(PeriodoEstadisticasPreset.ULTIMOS_7_DIAS);
        assertThat(captor.getValue().getFechaDesde()).isNull();
        assertThat(captor.getValue().getFechaHasta()).isNull();
    }

    @Test
    void obtenerEstadisticasAceptaRangoLibreComoQueryParam() throws Exception {
        when(localService.obtenerEstadisticasLocal(eq(10L), any(DtEstadisticasLocalFiltro.class)))
                .thenReturn(DtEstadisticasLocal.builder()
                        .fechaDesde(LocalDate.of(2026, 6, 1))
                        .fechaHasta(LocalDate.of(2026, 6, 15))
                        .ventasConfirmadas(850.0)
                        .platosMasPedido(List.of())
                        .ventasPorPlato(List.of())
                        .ventasMensuales(List.of(
                                DtVentaMensualEstadistica.builder()
                                        .anio(2026)
                                        .mes(6)
                                        .montoVendido(850.0)
                                        .build()))
                        .build());

        mockMvc.perform(get("/api/v1/locales/estadisticas/10")
                        .param("fechaDesde", "2026-06-01")
                        .param("fechaHasta", "2026-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasConfirmadas").value(850.0))
                .andExpect(jsonPath("$.fechaDesde").value("2026-06-01"))
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-15"))
                .andExpect(jsonPath("$.ventasMensuales[0].anio").value(2026))
                .andExpect(jsonPath("$.ventasMensuales[0].mes").value(6))
                .andExpect(jsonPath("$.ventasMensuales[0].montoVendido").value(850.0));

        ArgumentCaptor<DtEstadisticasLocalFiltro> captor = ArgumentCaptor.forClass(DtEstadisticasLocalFiltro.class);
        verify(localService).obtenerEstadisticasLocal(eq(10L), captor.capture());
        assertThat(captor.getValue().getFechaDesde()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(captor.getValue().getFechaHasta()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(captor.getValue().getPreset()).isNull();
    }

    @Test
    void buscaPromocionesDeLocalRespondeSeparadasEnVigentesVencidasYProximas() throws Exception {
        DtPromocion vigente = DtPromocion.builder()
                .id(1L)
                .descripcion("Vigente")
                .fechaInicio(LocalDateTime.of(2026, 7, 1, 0, 0))
                .fechaFin(LocalDateTime.of(2026, 7, 10, 0, 0))
                .build();
        DtPromocion vencida = DtPromocion.builder()
                .id(2L)
                .descripcion("Vencida")
                .fechaInicio(LocalDateTime.of(2026, 6, 1, 0, 0))
                .fechaFin(LocalDateTime.of(2026, 6, 10, 0, 0))
                .build();
        DtPromocion proxima = DtPromocion.builder()
                .id(3L)
                .descripcion("Proxima")
                .fechaInicio(LocalDateTime.of(2026, 7, 20, 0, 0))
                .fechaFin(LocalDateTime.of(2026, 7, 25, 0, 0))
                .build();

        when(localService.buscaPromocionesDeLocal(10L))
                .thenReturn(DtPromocionesLocalResponse.builder()
                        .vigentes(List.of(vigente))
                        .vencidas(List.of(vencida))
                        .proximas(List.of(proxima))
                        .build());

        mockMvc.perform(get("/api/v1/locales/busqueda_promocion_local/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigentes[0].id").value(1))
                .andExpect(jsonPath("$.vigentes[0].descripcion").value("Vigente"))
                .andExpect(jsonPath("$.vencidas[0].id").value(2))
                .andExpect(jsonPath("$.vencidas[0].descripcion").value("Vencida"))
                .andExpect(jsonPath("$.proximas[0].id").value(3))
                .andExpect(jsonPath("$.proximas[0].descripcion").value("Proxima"));
    }
}
