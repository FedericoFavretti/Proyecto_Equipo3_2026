package com.example.demo;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para las pruebas de integracion (*IT).
 *
 * Levanta un Postgres real en un contenedor Docker e inicializa el esquema
 * con schema-it.sql antes de correr los tests.
 *
 * OJO: no usamos @ServiceConnection porque el proyecto arma su propio
 * DataSource a mano en ConexionBd.java (leyendo spring.datasource.url por
 * @Value), y @ServiceConnection solo conecta con el DataSource autoconfigurado
 * por Spring Boot. En cambio, con @DynamicPropertySource pisamos
 * directamente esas propiedades, que es justo lo que ConexionBd lee.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Transactional
public abstract class IntegrationTestBase {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withInitScript("schema-it.sql");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}