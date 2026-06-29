package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.Enums.PeriodoEstadisticasPreset;
import com.example.demo.Logica.Service.CloudinaryService;
import com.example.demo.Logica.Service.LocalService;
import com.example.demo.Logica.DataTypes.response.DtEstadisticasLocal;
import com.example.demo.Logica.DataTypes.request.DtEstadisticasLocalFiltro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
                                DtPlato.builder()
                                        .id(20L)
                                        .nombre("Milanesa al pan")
                                        .precio(350.0)
                                        .disponible(true)
                                        .imagenes(List.of("milanesa.jpg"))
                                        .build()))
                        .build());

        mockMvc.perform(get("/api/v1/locales/estadisticas/10")
                        .param("preset", "ULTIMOS_7_DIAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasConfirmadas").value(1500.0))
                .andExpect(jsonPath("$.fechaDesde").value("2026-06-23"))
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-29"))
                .andExpect(jsonPath("$.platosMasPedido[0].id").value(20))
                .andExpect(jsonPath("$.platosMasPedido[0].nombre").value("Milanesa al pan"));

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
                        .build());

        mockMvc.perform(get("/api/v1/locales/estadisticas/10")
                        .param("fechaDesde", "2026-06-01")
                        .param("fechaHasta", "2026-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasConfirmadas").value(850.0))
                .andExpect(jsonPath("$.fechaDesde").value("2026-06-01"))
                .andExpect(jsonPath("$.fechaHasta").value("2026-06-15"));

        ArgumentCaptor<DtEstadisticasLocalFiltro> captor = ArgumentCaptor.forClass(DtEstadisticasLocalFiltro.class);
        verify(localService).obtenerEstadisticasLocal(eq(10L), captor.capture());
        assertThat(captor.getValue().getFechaDesde()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(captor.getValue().getFechaHasta()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(captor.getValue().getPreset()).isNull();
    }
}
