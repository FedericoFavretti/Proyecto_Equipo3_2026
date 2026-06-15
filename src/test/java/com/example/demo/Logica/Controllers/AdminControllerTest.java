package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.request.DtResolverSolicitudLocalRequest;
import com.example.demo.Logica.DataTypes.response.DtSolicitudLocalPendienteResponse;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {

    private AdminService adminService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adminService = Mockito.mock(AdminService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void listarPendientesDevuelveResumen() throws Exception {
        when(adminService.listarSolicitudesPendientes()).thenReturn(List.of(
                new DtSolicitudLocalPendienteResponse(
                        10L,
                        "local@foodly.com",
                        "La Cocina",
                        new DtDireccion("Av. Italia", "1234", "Montevideo", "11600"),
                        "Comida casera",
                        List.of("fachada.jpg", "cocina.png")
                )
        ));

        mockMvc.perform(get("/api/v1/admins/solicitudes-locales/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].email").value("local@foodly.com"))
                .andExpect(jsonPath("$[0].nombre").value("La Cocina"))
                .andExpect(jsonPath("$[0].direccion.calle").value("Av. Italia"))
                .andExpect(jsonPath("$[0].imagenes[0]").value("fachada.jpg"));
    }

    @Test
    void resolverSolicitudApruebaRequestValido() throws Exception {
        mockMvc.perform(put("/api/v1/admins/solicitudes-locales/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoObjetivo": "Habilitado"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(adminService).resolverSolicitud(10L, new DtResolverSolicitudLocalRequest(EstadoLocal.Habilitado));
    }

    @Test
    void resolverSolicitudRespondeBadRequestSiFaltaEstadoObjetivo() throws Exception {
        mockMvc.perform(put("/api/v1/admins/solicitudes-locales/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debe indicar el estado objetivo de la solicitud."));
    }

    @Test
    void resolverSolicitudRespondeNotFoundSiLocalNoExiste() throws Exception {
        doThrow(new ResourceNotFoundException("Local no encontrado"))
                .when(adminService).resolverSolicitud(10L, new DtResolverSolicitudLocalRequest(EstadoLocal.Habilitado));

        mockMvc.perform(put("/api/v1/admins/solicitudes-locales/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoObjetivo": "Habilitado"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Local no encontrado"));
    }

    @Test
    void resolverSolicitudRespondeConflictSiSolicitudYaFueResuelta() throws Exception {
        doThrow(new IllegalStateException("Solo se pueden resolver solicitudes en estado Pendiente."))
                .when(adminService).resolverSolicitud(10L, new DtResolverSolicitudLocalRequest(EstadoLocal.Rechazado));

        mockMvc.perform(put("/api/v1/admins/solicitudes-locales/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estadoObjetivo": "Rechazado"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Solo se pueden resolver solicitudes en estado Pendiente."));
    }
}

