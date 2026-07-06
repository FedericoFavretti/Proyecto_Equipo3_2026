package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtGoogleRegistroPendienteResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Service.ClienteService;
import com.example.demo.Logica.Service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerTest {

    private ClienteService clienteService;
    private CloudinaryService cloudinaryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        clienteService = Mockito.mock(ClienteService.class);
        cloudinaryService = Mockito.mock(CloudinaryService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ClienteController(clienteService, cloudinaryService))
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void loginConGoogleDevuelveOk() throws Exception {
        DtLoginResponseCliente response = DtLoginResponseCliente.builder()
                .id(10L)
                .token("jwt-login")
                .tipo("cliente")
                .email("cliente@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .direccion(new DtDireccion("18 de Julio", "1234", "Montevideo", "11200"))
                .foto("https://cdn.foodly.com/ana.png")
                .calificacionGlobal(4.8)
                .build();

        when(clienteService.loginConGoogle(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "token-google"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-login"))
                .andExpect(jsonPath("$.email").value("cliente@foodly.com"))
                .andExpect(jsonPath("$.apellido").value("Pérez"));
    }

    @Test
    void iniciarRegistroConGoogleDevuelveOk() throws Exception {
        DtGoogleRegistroPendienteResponse response = DtGoogleRegistroPendienteResponse.builder()
                .tokenRegistro("registro-temporal")
                .email("nuevo@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();

        when(clienteService.iniciarRegistroConGoogle(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes/google/registro/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "token-google",
                                  "esRegistro": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenRegistro").value("registro-temporal"))
                .andExpect(jsonPath("$.email").value("nuevo@foodly.com"));
    }

    @Test
    void completarRegistroConGoogleSubeFotoYDelega() throws Exception {
        MockMultipartFile foto = new MockMultipartFile("foto", "perfil.png", "image/png", new byte[]{1, 2, 3});
        MockMultipartFile datos = new MockMultipartFile(
                "datos",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                        {
                          "tokenRegistro": "registro-temporal",
                          "documento": "51234567",
                          "aceptaTerminos": true,
                          "direccion": {
                            "calle": "18 de Julio",
                            "numero": "1234",
                            "ciudad": "Montevideo",
                            "codigoPostal": "11200"
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
        DtLoginResponseCliente response = DtLoginResponseCliente.builder()
                .id(10L)
                .token("jwt-final")
                .tipo("cliente")
                .email("nuevo@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .direccion(new DtDireccion("18 de Julio", "1234", "Montevideo", "11200"))
                .foto("https://cdn.foodly.com/perfil-google.png")
                .calificacionGlobal(0.0)
                .build();

        when(cloudinaryService.subirImagen(foto)).thenReturn("https://cdn.foodly.com/perfil-google.png");
        when(clienteService.completarRegistroConGoogle(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/clientes/google/registro/completar")
                        .file(foto)
                        .file(datos)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-final"))
                .andExpect(jsonPath("$.foto").value("https://cdn.foodly.com/perfil-google.png"));
    }

    @Test
    void buscarPlatosYPromocionesDevuelveListadoCombinado() throws Exception {
        DtPlato plato = DtPlato.builder()
                .id(10L)
                .nombre("Milanesa")
                .precio(15.0)
                .build();
        DtPromocion promocion = DtPromocion.builder()
                .id(20L)
                .descripcion("2x1")
                .dtPlato(plato)
                .build();

        when(clienteService.buscarPlatosYPromociones(any())).thenReturn(
                DtBusquedaPlatosPromocionesResponse.builder()
                        .platos(List.of(plato))
                        .promociones(List.of(promocion))
                        .build()
        );

        mockMvc.perform(post("/api/v1/clientes/busqueda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Mil",
                                  "promocionActiva": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platos[0].id").value(10))
                .andExpect(jsonPath("$.platos[0].nombre").value("Milanesa"))
                .andExpect(jsonPath("$.promociones[0].id").value(20))
                .andExpect(jsonPath("$.promociones[0].descripcion").value("2x1"));
    }

    @Test
    void buscarPlatosYPromocionesRespondeBadRequestSinResultados() throws Exception {
        when(clienteService.buscarPlatosYPromociones(any()))
                .thenThrow(new IllegalArgumentException("No se encontraron platos o promociones que coincidan con su búsqueda."));

        mockMvc.perform(post("/api/v1/clientes/busqueda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Inexistente"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se encontraron platos o promociones que coincidan con su búsqueda."));
    }
}
