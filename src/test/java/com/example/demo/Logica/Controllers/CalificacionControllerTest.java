package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.CalificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
                .setControllerAdvice(new RestExceptionHandler())
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
    void consultarCalificacionGlobalDelLocalDevPermiteProbarSinAutenticacion() throws Exception {
        when(calificacionService.consultarCalificacionGlobalDelLocalPorId(10L))
                .thenReturn(Map.of(
                        "calificacionGlobal", 4.0,
                        "totalValoraciones", 3,
                        "detallePorPuntuacion", Map.of("1", 0, "2", 1, "3", 0, "4", 1, "5", 1)
                ));

        mockMvc.perform(get("/api/v1/calificaciones/local/10/mi-calificacion-dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacionGlobal").value(4.0))
                .andExpect(jsonPath("$.totalValoraciones").value(3))
                .andExpect(jsonPath("$.detallePorPuntuacion.2").value(1));
    }
}
