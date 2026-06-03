package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.DetallePedido;

import com.example.demo.Persistencia.Repositorios.DetallePedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DetallePedidoRepositorioImpl implements DetallePedidoRepositorio {
    private final PlatoRepositorio platoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final JdbcTemplate jdbcTemplate;

    public DetallePedidoRepositorioImpl(JdbcTemplate jdbcTemplate, PlatoRepositorio platoRepo,  PedidoRepositorio pedidoRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.platoRepositorio = platoRepo;
        this.pedidoRepositorio = pedidoRepo;
    }

    @Override
    public List<DetallePedido> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM DetallePedido",
                (rs, row)-> new DetallePedido(
                        rs.getLong("id"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precioUnitario"),
                        rs.getDouble("subtotal"),
                        platoRepositorio.buscarPorId(rs.getLong("idPlato")).orElseThrow(() -> new RuntimeException("Plato no encontrado")),
                        pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(() -> new RuntimeException("Pedido no encontrado"))
                )
        );
    }

    @Override
    public Optional<DetallePedido> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM DetallePedido WHERE id = ?",
                (rs, row)-> new DetallePedido(
                        rs.getLong("id"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precioUnitario"),
                        rs.getDouble("subtotal"),
                        platoRepositorio.buscarPorId(rs.getLong("idPlato")).orElseThrow(() -> new RuntimeException("Plato no encontrado")),
                        pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(() -> new RuntimeException("Pedido no encontrado"))
                ),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(DetallePedido detallePedido) {
        jdbcTemplate.update("INSERT INTO DetallePedido (cantidad, precioUnitario, subtotal, idPlato, idPedido) VALUES (?, ?, ?, ?, ?)",
                detallePedido.getCantidad(),
                detallePedido.getPrecioUnitario(),
                detallePedido.getSubtotal(),
                detallePedido.getPlato().getId(),
                detallePedido.getPedido().getId()
        );
    }

    @Override
    public void actualizar(DetallePedido detallePedido) {
        jdbcTemplate.update("UPDATE DetallePedido SET cantidad = ?, precioUnitario = ?, subtotal = ?, idPlato = ?, idPedido = ? WHERE id  = ?",
                detallePedido.getCantidad(),
                detallePedido.getPrecioUnitario(),
                detallePedido.getSubtotal(),
                detallePedido.getPlato().getId(),
                detallePedido.getPedido().getId(),
                detallePedido.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM DetallePedido WHERE id = ?", id);
    }
}
