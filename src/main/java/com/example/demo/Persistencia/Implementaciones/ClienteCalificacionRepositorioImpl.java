package com.example.demo.Persistencia.Implementaciones;

import java.util.List;

import com.example.demo.Persistencia.Repositorios.ClienteCalificacionRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteCalificacionRepositorioImpl implements ClienteCalificacionRepositorio {

    private final JdbcTemplate jdbcTemplate;

    public ClienteCalificacionRepositorioImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void calificar(Long idCliente, Long idCalificacion) {
        jdbcTemplate.update("INSERT INTO cliente_calificacion (idcliente, idcalificacion) values (?, ?)",
                idCliente,
                idCalificacion
        );
    }

    @Override
    public Long obtenerCliente(Long idCalificacion) {
        return jdbcTemplate.queryForObject(
                "SELECT idcliente FROM cliente_calificacion WHERE idcalificacion = ?",
                (rs, row) -> rs.getLong("idcliente"),
                idCalificacion
        );
    }

    @Override
    public List<Long> obtenerCalificacionesDeCliente(Long idCliente) {
        return jdbcTemplate.query(
                "SELECT idcalificacion FROM cliente_calificacion WHERE idcliente = ?",
                (rs, row) -> rs.getLong("idcalificacion"),
                idCliente
        );
    }

}
