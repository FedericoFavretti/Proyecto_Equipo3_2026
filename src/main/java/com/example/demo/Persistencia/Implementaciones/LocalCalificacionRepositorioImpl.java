package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Persistencia.Repositorios.LocalCalificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LocalCalificacionRepositorioImpl  implements LocalCalificacionRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public LocalCalificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void calificar(Long idLocal, Long idCalificacion) {
        jdbcTemplate.update("INSERT INTO local_calificacion (idlocal,idcalificacion) VALUES (?,?)", idLocal, idCalificacion);
    }

    @Override
    public Long obtenerLocal(Long idCalificacion) {
        return jdbcTemplate.queryForObject("SELECT idlocal FROM local_calificacion WHERE idcalificacion = ?", Long.class, idCalificacion);
    }
}
