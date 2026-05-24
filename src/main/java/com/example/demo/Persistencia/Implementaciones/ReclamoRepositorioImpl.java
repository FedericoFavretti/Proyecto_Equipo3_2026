package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReclamoRepositorioImpl implements ReclamoRepositorio {
    private final PedidoRepositorio pedidoRepositorio;
    private final JdbcTemplate jdbcTemplate;


    public ReclamoRepositorioImpl(JdbcTemplate jdbcTemplate, PedidoRepositorio pedidoRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.pedidoRepositorio = pedidoRepo;
    }

    @Override
    public List<Reclamo> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Reclamo",
                (rs, row) -> new Reclamo(
                        rs.getLong("id"),
                        rs.getString("motivo"),
                        rs.getString("tipoCompensacion"),
                        rs.getDouble("montoReintegro"),
                        rs.getDate("fecha"),
                        pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(()->new RuntimeException("Pedido no encontrado"))
                )
        );
    }

    @Override
    public Optional<Reclamo> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Reclamo WHERE id = ?",
                (rs, row) -> new Reclamo(
                        rs.getLong("id"),
                        rs.getString("motivo"),
                        rs.getString("tipoCompensacion"),
                        rs.getDouble("montoReintegro"),
                        rs.getDate("fecha"),
                        pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(()->new RuntimeException("Pedido no encontrado"))
                ),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Reclamo reclamo) {
        jdbcTemplate.update("INSERT INTO Reclamo (motivo, tipoCompensacion, montoReintegro, fecha, idPedido) VALUES (?, ?, ?, ?, ?)",
                reclamo.getMotivo(),
                reclamo.getTipoCompensacion(),
                reclamo.getMontoReintegro(),
                reclamo.getFecha(),
                reclamo.getPedido().getId()
        );
    }

    @Override
    public void actualizar(Reclamo reclamo) {
        jdbcTemplate.update("UPDATE Reclamo SET motivo = ?, tipoCompensacion = ?, montoReintegro = ?, fecha = ?, idPedido = ? WHERE id = ?)",
                reclamo.getMotivo(),
                reclamo.getTipoCompensacion(),
                reclamo.getMontoReintegro(),
                reclamo.getFecha(),
                reclamo.getPedido().getId(),
                reclamo.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Reclamo WHERE id = ?", id);
    }
}
