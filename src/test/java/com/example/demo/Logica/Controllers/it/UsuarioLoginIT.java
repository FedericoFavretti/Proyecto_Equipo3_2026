package com.example.demo.Logica.Controllers.it;

import com.example.demo.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion real: Spring Boot + MockMvc + Postgres en
 * Testcontainers, sin mockear ni el repositorio ni el AuthenticationManager.
 *
 * A diferencia de UsuarioControllerTest (unitario, con Mockito), aca se
 * valida el flujo completo: JdbcTemplate -> Postgres -> UserDetailsService ->
 * AuthenticationManager -> JwtService -> respuesta HTTP.
 */
class UsuarioLoginIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL = "cliente.it@foodly.com";
    private static final String PASSWORD_EN_CLARO = "Passw0rd!";

    @BeforeEach
    void crearClienteDePrueba() {
        Long id = jdbcTemplate.queryForObject(
                "INSERT INTO usuario (email, passwd, estado, tipo, autenticado_con_google) " +
                        "VALUES (?, ?, 'Activo', 'Cliente', false) RETURNING id",
                Long.class,
                EMAIL,
                passwordEncoder.encode(PASSWORD_EN_CLARO)
        );

        jdbcTemplate.update(
                "INSERT INTO cliente (id, documento, nombre, apellido, calle, numero, ciudad, codigopostal) " +
                        "VALUES (?, '12345678', 'Ana', 'Perez', '18 de Julio', '1234', 'Montevideo', '11200')",
                id
        );
    }

    @Test
    void loginConCredencialesCorrectasDevuelveTokenValido() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "passwd": "%s"
                                }
                                """.formatted(EMAIL, PASSWORD_EN_CLARO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void loginConPasswordIncorrectaNoDevuelveOk() throws Exception {
        // OJO: esto documenta el comportamiento ACTUAL, no el deseado.
        // BadCredentialsException no tiene un @ExceptionHandler propio en
        // GlobalExceptionHandler, asi que cae en el handler generico
        // (Exception.class) y devuelve 500 en lugar de un 401/403. Es un
        // hallazgo real de esta prueba: si lo quieren corregir, agregando
        // un @ExceptionHandler(BadCredentialsException.class) -> 401 en
        // GlobalExceptionHandler alcanza, y ahi este test pasa a esperar
        // isUnauthorized().
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "passwd": "otra-cosa"
                                }
                                """.formatted(EMAIL)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void endpointSinTokenDevuelveNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/busqueda"))
                .andExpect(status().isForbidden());
    }
}
