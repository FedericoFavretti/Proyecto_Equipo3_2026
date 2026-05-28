package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepositorioImpl implements PedidoRepositorio {

    private final LocalRepositorio localRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final JdbcTemplate jdbcTemplate;

    public PedidoRepositorioImpl(JdbcTemplate jdbcTemplate, LocalRepositorio localRepo, ClienteRepositorio clienteRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.localRepositorio = localRepo;
        this.clienteRepositorio = clienteRepo;
    }

    private Pedido mapearPedido(ResultSet rs, int row) throws SQLException {
        return new Pedido(
            rs.getLong("id"),
            rs.getDate("fecha"),
            Duration.ofMinutes(rs.getLong("tiempoEstEntrega")),
            rs.getDouble("total"),
            new DtDireccion(
                rs.getString("calle"),
                rs.getString("numero"),
                rs.getString("ciudad"),
                rs.getString("codigoPostal")
            ),
            rs.getString("medioPago"),
            rs.getBoolean("pagoSimulado"),
            EstadoPedido.valueOf(rs.getString("estado")),
            null,
            localRepositorio.buscarPorId(rs.getLong("idLocal"))
                .orElseThrow(() -> new RuntimeException("Local no encontrado")),
            clienteRepositorio.buscarPorId(rs.getInt("idCliente"))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
        );
    }

    @Override
    public List<Pedido> listarTodos() {
        return jdbcTemplate.query(
            "SELECT * FROM Pedido",
            this::mapearPedido
        );
    }

    @Override
    public Optional<Pedido> buscarPorId(long id) {
        return jdbcTemplate.query(
            "SELECT * FROM Pedido WHERE id = ?",
            this::mapearPedido,
            id
        ).stream().findFirst();
    }

    @Override
    public List<Pedido> listarPorLocal(long idLocal) {
        return jdbcTemplate.query(
            "SELECT * FROM Pedido WHERE idLocal = ?",
            this::mapearPedido,
            idLocal
        );
    }

    @Override
    public void guardar(Pedido pedido) {
        jdbcTemplate.update(
            "INSERT INTO Pedido (fecha, tiempoEstEntrega, total, calle, numero, ciudad, codigoPostal, medioPago, pagoSimulado, estado, idLocal, idCliente) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            pedido.getFecha(),
            pedido.getTiempoEstEntrega().toMinutes(),
            pedido.getTotal(),
            pedido.getDomicilioEntrega().getCalle(),
            pedido.getDomicilioEntrega().getNumero(),
            pedido.getDomicilioEntrega().getCiudad(),
            pedido.getDomicilioEntrega().getCodigoPostal(),
            pedido.getMedioDePago(),
            pedido.getPagoSimulado(),
            pedido.getEstado().name(),
            pedido.getLocal().getId(),
            pedido.getCliente().getId()
        );
    }

    @Override
    public void actualizar(Pedido pedido) {
        jdbcTemplate.update(
            "UPDATE Pedido SET fecha = ?, tiempoEstEntrega = ?, total = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, medioPago = ?, pagoSimulado = ?, estado = ?, idLocal = ?, idCliente = ? " +
            "WHERE id = ?",
            pedido.getFecha(),
            pedido.getTiempoEstEntrega().toMinutes(),
            pedido.getTotal(),
            pedido.getDomicilioEntrega().getCalle(),
            pedido.getDomicilioEntrega().getNumero(),
            pedido.getDomicilioEntrega().getCiudad(),
            pedido.getDomicilioEntrega().getCodigoPostal(),
            pedido.getMedioDePago(),
            pedido.getPagoSimulado(),
            pedido.getEstado().name(),
            pedido.getLocal().getId(),
            pedido.getCliente().getId(),
            pedido.getId()
        );
    }

    @Override
    public void eliminar(long id) {
        jdbcTemplate.update("DELETE FROM Pedido WHERE id = ?", id);
    }
}