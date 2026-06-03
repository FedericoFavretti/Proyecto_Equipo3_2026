package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    @Override
    public List<Pedido> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Pedido",
                (rs, row)-> new Pedido(
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
                        rs.getString("medioDePago"),
                        rs.getBoolean("pagoSimulado"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal")).orElseThrow(() -> new RuntimeException("Local no encontrado")),
                        clienteRepositorio.buscarPorId(rs.getLong("idCliente")).orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
                )
        );
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM Pedido WHERE id = ?",
                (rs, row)-> new Pedido(
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
                        rs.getString("medioDePago"),
                        rs.getBoolean("pagoSimulado"),
                        localRepositorio.buscarPorId(rs.getLong("idLocal")).orElseThrow(() -> new RuntimeException("Local no encontrado")),
                        clienteRepositorio.buscarPorId(rs.getLong("idCliente")).orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
                ),id
        ).stream().findFirst();
    }

    @Override
    public void guardar(Pedido pedido) {
        jdbcTemplate.update("INSERT INTO Pedido (fecha, tiempoEstEntrega, total, calle, numero, ciudad, codigoPostal, medioPago, idLocal, idCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                pedido.getFecha(),
                pedido.getTiempoEstEntrega(),
                pedido.getTotal(),
                pedido.getTotal(),
                pedido.getDomicilioEntrega().getCalle(),
                pedido.getDomicilioEntrega().getNumero(),
                pedido.getDomicilioEntrega().getCiudad(),
                pedido.getDomicilioEntrega().getCodigoPostal(),
                pedido.getMedioDePago(),
                pedido.getPagoSimulado(),
                pedido.getLocal().getId(),
                pedido.getCliente().getId()
        );
    }

    @Override
    public void actualizar(Pedido pedido) {
        jdbcTemplate.update("UPDATE Pedido SET fecha = ?, tiempoEstEntrega = ?, total = ?, calle = ?, numero = ?, ciudad = ?, codigoPostal = ?, medioPago = ?, idLocal = ?, idCliente = ? WHERE id = ?",
                pedido.getFecha(),
                pedido.getTiempoEstEntrega(),
                pedido.getTotal(),
                pedido.getTotal(),
                pedido.getDomicilioEntrega().getCalle(),
                pedido.getDomicilioEntrega().getNumero(),
                pedido.getDomicilioEntrega().getCiudad(),
                pedido.getDomicilioEntrega().getCodigoPostal(),
                pedido.getMedioDePago(),
                pedido.getPagoSimulado(),
                pedido.getLocal().getId(),
                pedido.getCliente().getId(),
                pedido.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Pedido WHERE id = ?", id);
    }
}
