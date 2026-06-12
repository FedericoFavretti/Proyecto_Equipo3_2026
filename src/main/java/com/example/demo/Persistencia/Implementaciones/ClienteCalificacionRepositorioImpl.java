package com.example.demo.Persistencia.Implementaciones;

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
        jdbcTemplate.update("INSERT INTO cliente_calificacion (id_cliente, id_calificacion) values (?, ?)",
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

}
