package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CalificacionSchemaInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CalificacionSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public CalificacionSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void asegurarColumnaArchivada() {
        jdbcTemplate.update("""
                ALTER TABLE calificacion
                ADD COLUMN IF NOT EXISTS archivada BOOLEAN NOT NULL DEFAULT FALSE
                """);
        LOGGER.info("Esquema de calificacion verificado: columna archivada disponible.");
    }
}
