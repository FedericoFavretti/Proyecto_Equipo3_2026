package com.example.demo;

import org.junit.jupiter.api.Test;

/**
 * Antes era DemoApplicationTests, un @SpringBootTest "pelado" generado por
 * Spring Initializr. Rompía apenas se ejecutaba de verdad (no en el build
 * de Railway, que salta los tests) porque intenta levantar el contexto
 * completo, incluida la conexion a la base, y application.properties
 * espera la variable de entorno DB_URL, que solo existe cuando corre la
 * app real (docker-compose / Railway).
 *
 * Ahora extiende IntegrationTestBase: el contexto se levanta contra el
 * Postgres real de Testcontainers, igual que el resto de los *IT. Al ser
 * *IT, corre con "mvnw verify" y no con "mvnw test", asi los tests rapidos
 * (los *Test.java, con Mockito) no empiezan a depender de Docker.
 */
class DemoApplicationIT extends IntegrationTestBase {

    @Test
    void contextLoads() {
    }

}
