package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Enums.TipoCalificacion;
import com.example.demo.Persistencia.Repositorios.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CalificacionRepositorioImpl implements CalificacionRepositorio {
    private final JdbcTemplate jdbcTemplate;
    private final ClienteRepositorio clienteRepositorio;
    private final LocalRepositorio localRepositorio;
    private final ClienteCalificacionRepositorio clienteCalificacionRepositorio;
    private final LocalCalificacionRepositorio localCalificacionRepositorio;

    public CalificacionRepositorioImpl(JdbcTemplate jdbcTemplate, ClienteRepositorio clienteRepositorio, LocalRepositorio localRepositorio,  ClienteCalificacionRepositorio clienteCalificacionRepositorio,  LocalCalificacionRepositorio localCalificacionRepositorio) {
        this.jdbcTemplate = jdbcTemplate;
        this.clienteRepositorio = clienteRepositorio;
        this.localRepositorio = localRepositorio;
        this.clienteCalificacionRepositorio = clienteCalificacionRepositorio;
        this.localCalificacionRepositorio = localCalificacionRepositorio;
    }

    @Override
    public List<Calificacion> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Calificacion",
                (rs, row)-> calificacionMapper(rs, row)
        );
    }

    @Override
    public Optional<Calificacion> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM Calificacion WHERE id = ?",
                (rs, row) -> calificacionMapper(rs, row),
                id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Calificacion calificacion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO Calificacion (puntaje, comentario, fecha, tipo) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
                    );
                    ps.setDouble(1, calificacion.getPuntaje());
                    ps.setString(2, calificacion.getComentario());
                    ps.setDate(3, new java.sql.Date(calificacion.getFecha().getTime()));
                    ps.setString(4, calificacion.getTipo().toString());
                    return ps;
        }, keyHolder);
        Long idCalificacion = keyHolder.getKey().longValue();
        if(calificacion.getTipo().equals(TipoCalificacion.Cliente_a_local)){
            clienteCalificacionRepositorio.calificar(calificacion.getCliente().getId(), idCalificacion);
        } else if (calificacion.getTipo().equals(TipoCalificacion.Local_a_cliente)) {
            localCalificacionRepositorio.calificar(calificacion.getLocal().getId(), idCalificacion);
        }
    }

    @Override
    public void actualizar(Calificacion calificacion) {
       jdbcTemplate.update("UPDATE Calificacion SET  puntaje = ?, comentario = ?, fecha = ?, tipo = ? WHERE id = ?",
               calificacion.getPuntaje(),
               calificacion.getComentario(),
               calificacion.getFecha(),
               calificacion.getTipo(),
               calificacion.getId()
       );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Calificacion WHERE id = ?", id);
    }

    private void cM (Calificacion calificacion){
        calificacion.getPuntaje();
                calificacion.getComentario();
                calificacion.getFecha();
                calificacion.getTipo();
                calificacion.getId();
    }

    private Calificacion calificacionMapper(ResultSet rs,  int row) throws SQLException {
        if(rs.getString("tipo").equals(TipoCalificacion.Cliente_a_local)){
            Long idCliente = clienteCalificacionRepositorio.obtenerCliente(rs.getLong("id"));
            Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            return Calificacion.builder()
                    .id(rs.getLong("id"))
                    .puntaje(rs.getInt("puntaje"))
                    .comentario(rs.getString("comentario"))
                    .fecha(rs.getDate("fecha"))
                    .tipo(TipoCalificacion.valueOf(rs.getString("tipo")))
                    .cliente(cliente)
                    .local(null)
                    .build();
        } else if (rs.getString("tipo").equals(TipoCalificacion.Local_a_cliente)) {
            Long idLocal = localCalificacionRepositorio.obtenerLocal(rs.getLong("id"));
            Local local = localRepositorio.buscarPorId(idLocal).orElseThrow(()-> new RuntimeException("Local no encontrado"));
            return Calificacion.builder()
                    .id(rs.getLong("id"))
                    .puntaje(rs.getInt("puntaje"))
                    .comentario(rs.getString("comentario"))
                    .fecha(rs.getDate("fecha"))
                    .tipo(TipoCalificacion.valueOf(rs.getString("tipo")))
                    .cliente(null)
                    .local(local)
                    .build();
        }
        return null;
    }
}
