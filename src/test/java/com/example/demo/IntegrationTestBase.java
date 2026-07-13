package com.example.demo;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Clase base para las pruebas de integracion (*IT).
 *
 * El contenedor de Postgres se arranca UNA sola vez, a mano, en el bloque
 * estatico de abajo ("patron singleton container" recomendado por
 * Testcontainers para cuando varias clases de test comparten el mismo
 * contenedor). A proposito NO usamos @Testcontainers/@Container: esas
 * anotaciones apagan el contenedor apenas termina la primera clase de test
 * que lo usa, lo que rompe a la segunda clase (justo lo que estaba pasando).
 * Sin ellas, el contenedor queda vivo para toda la corrida de mvn verify, y
 * Testcontainers lo limpia solo cuando termina el proceso.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Transactional
public abstract class IntegrationTestBase {

    protected static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withInitScript("schema-it.sql");
        postgres.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}