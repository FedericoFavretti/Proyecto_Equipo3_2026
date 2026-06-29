package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.response.DtMiCalificacionLocalResponse;
import com.example.demo.Logica.Exceptions.GlobalExceptionHandler;
import com.example.demo.Logica.Service.CalificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalificacionControllerTest {

    private CalificacionService calificacionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        calificacionService = Mockito.mock(CalificacionService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CalificacionController(calificacionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void consultarCalificacionGlobalDelLocalDevuelveResumen() throws Exception {
        when(calificacionService.consultarCalificacionGlobalDelLocal("local@test.com"))
                .thenReturn(Map.of(
                        "calificacionGlobal", 4.5,
                        "totalValoraciones", 2,
                        "detallePorPuntuacion", Map.of("1", 0, "2", 0, "3", 0, "4", 1, "5", 1)
                ));

        mockMvc.perform(get("/api/v1/calificaciones/local/mi-calificacion")
                        .principal(new UsernamePasswordAuthenticationToken("local@test.com", "token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacionGlobal").value(4.5))
                .andExpect(jsonPath("$.totalValoraciones").value(2))
                .andExpect(jsonPath("$.detallePorPuntuacion.4").value(1))
                .andExpect(jsonPath("$.detallePorPuntuacion.5").value(1));
    }

    @Test
    void consultarCalificacionGlobalDelLocalRespondeUnauthorizedSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/calificaciones/local/mi-calificacion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultarMiCalificacionDeLocalDevuelveNoContentCuandoNoExiste() throws Exception {
        when(calificacionService.consultarMiCalificacionDeLocal(10L, "cliente@test.com"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/calificaciones/locales/10/mi-calificacion")
                        .principal(new UsernamePasswordAuthenticationToken("cliente@test.com", "token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void consultarMiCalificacionDeLocalDevuelveCalificacionExistente() throws Exception {
        when(calificacionService.consultarMiCalificacionDeLocal(10L, "cliente@test.com"))
                .thenReturn(DtMiCalificacionLocalResponse.builder()
                        .id(8L)
                        .puntaje(5)
                        .comentario("Excelente")
                        .fecha(LocalDateTime.of(2026, 6, 28, 12, 0))
                        .build());

        mockMvc.perform(get("/api/v1/calificaciones/locales/10/mi-calificacion")
                        .principal(new UsernamePasswordAuthenticationToken("cliente@test.com", "token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.puntaje").value(5))
                .andExpect(jsonPath("$.comentario").value("Excelente"));
    }
}
