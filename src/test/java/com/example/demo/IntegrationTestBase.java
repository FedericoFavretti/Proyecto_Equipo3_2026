package com.example.demo;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para las pruebas de integracion (*IT).
 *
 * Levanta un Postgres real en un contenedor Docker (mismo motor que usamos
 * en docker-compose.yml) e inicializa el esquema con schema-it.sql antes de
 * correr los tests. Spring Boot conecta el datasource automaticamente al
 * contenedor gracias a @ServiceConnection, no hace falta tocar
 * application.properties a mano.
 *
 * El contenedor se declara "static" para que se levante una unica vez para
 * toda la corrida de tests (no una vez por clase), lo que hace que la suite
 * sea mucho mas rapida.
 *
 * @Transactional en la clase base hace que cada metodo de test corra dentro
 * de una transaccion que se revierte (rollback) al terminar, asi cada test
 * arranca con la base limpia sin necesidad de hacer TRUNCATE a mano. Como
 * el acceso a datos es via JdbcTemplate (no JPA), esto funciona sin ningun
 * cambio adicional.
 *
 * Requisito: Docker (Desktop o Engine) tiene que estar corriendo en la
 * maquina donde se ejecuten estos tests.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Transactional
public abstract class IntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withInitScript("schema-it.sql");

}
