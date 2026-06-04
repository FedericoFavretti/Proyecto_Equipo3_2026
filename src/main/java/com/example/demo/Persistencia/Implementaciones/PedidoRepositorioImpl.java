package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtDireccion;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Optional;



@Repository
public class PedidoRepositorioImpl implements PedidoRepositorio {

    private final LocalRepositorio localRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final JdbcTemplate jdbcTemplate;

    public PedidoRepositorioImpl(JdbcTemplate jdbcTemplate,
                                 LocalRepositorio localRepo,
                                 ClienteRepositorio clienteRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.localRepositorio = localRepo;
        this.clienteRepositorio = clienteRepo;
    }



    private Pedido mapearPedido(ResultSet rs, int row) throws SQLException {
        return Pedido.builder()
                .id(rs.getLong("id"))
                .fecha(rs.getDate("fecha"))
                .tiempoEstEntrega(Duration.ofMinutes(rs.getLong("tiempoEstEntrega")))
                .total(rs.getDouble("total"))
                .domicilioEntrega(new DtDireccion(
                        rs.getString("calle"),
                        rs.getString("numero"),
                        rs.getString("ciudad"),
                        rs.getString("codigoPostal")
                ))
                .medioDePago(rs.getString("medioDePago"))
                .pagoSimulado(rs.getBoolean("pagoSimulado"))
                .estado(EstadoPedido.valueOf(rs.getString("estado")))
                .mpPreferenciaId(rs.getString("mp_preferencia_id"))
                .mpInitPoint(rs.getString("mp_init_point"))
                .local(localRepositorio.buscarPorId(rs.getLong("idLocal"))
                        .orElseThrow(() -> new RuntimeException("Local no encontrado")))
                .cliente(clienteRepositorio.buscarPorId(rs.getLong("idCliente"))
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado")))
                .build();
    }



    @Override
    public List<Pedido> listarTodos() {
        return jdbcTemplate.query(
                "SELECT * FROM pedidos",
                this::mapearPedido
        );
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM pedidos WHERE id = ?",
                this::mapearPedido, id
        ).stream().findFirst();
    }

    @Override
    public List<Pedido> listarPorLocal(Long idLocal) {
        return jdbcTemplate.query(
                "SELECT * FROM pedidos WHERE idLocal = ?",
                this::mapearPedido, idLocal
        );
    }



    @Override
    public void guardar(Pedido pedido) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO pedidos
                        (fecha, tiempoEstEntrega, total, calle, numero, ciudad, codigoPostal,
                         medioDePago, pagoSimulado, estado, mp_preferencia_id, mp_init_point,
                         idLocal, idCliente)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setDate(1, new java.sql.Date(pedido.getFecha().getTime()));
            ps.setLong(2, pedido.getTiempoEstEntrega() != null ? pedido.getTiempoEstEntrega().toMinutes() : 0);
            ps.setDouble(3, pedido.getTotal());
            ps.setString(4, pedido.getDomicilioEntrega().getCalle());
            ps.setString(5, pedido.getDomicilioEntrega().getNumero());
            ps.setString(6, pedido.getDomicilioEntrega().getCiudad());
            ps.setString(7, pedido.getDomicilioEntrega().getCodigoPostal());
            ps.setString(8, pedido.getMedioDePago());
            ps.setBoolean(9, pedido.getPagoSimulado());
            ps.setString(10, pedido.getEstado().name());
            ps.setString(11, pedido.getMpPreferenciaId());
            ps.setString(12, pedido.getMpInitPoint());
            ps.setLong(13, pedido.getLocal().getId());
            ps.setLong(14, pedido.getCliente().getId());
            return ps;
        }, keyHolder);

        pedido.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void actualizar(Pedido pedido) {
        jdbcTemplate.update(
                """
                UPDATE pedidos SET
                    fecha = ?, tiempoEstEntrega = ?, total = ?, calle = ?, numero = ?,
                    ciudad = ?, codigoPostal = ?, medioDePago = ?, pagoSimulado = ?,
                    estado = ?, mp_preferencia_id = ?, mp_init_point = ?,
                    idLocal = ?, idCliente = ?
                WHERE id = ?
                """,
                new java.sql.Date(pedido.getFecha().getTime()),
                pedido.getTiempoEstEntrega() != null ? pedido.getTiempoEstEntrega().toMinutes() : 0,
                pedido.getTotal(),
                pedido.getDomicilioEntrega().getCalle(),
                pedido.getDomicilioEntrega().getNumero(),
                pedido.getDomicilioEntrega().getCiudad(),
                pedido.getDomicilioEntrega().getCodigoPostal(),
                pedido.getMedioDePago(),
                pedido.getPagoSimulado(),
                pedido.getEstado().name(),
                pedido.getMpPreferenciaId(),
                pedido.getMpInitPoint(),
                pedido.getLocal().getId(),
                pedido.getCliente().getId(),
                pedido.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM pedidos WHERE id = ?", id);
    }


    @Override
    public void actualizarDatosMp(Long pedidoId, String mpPreferenciaId, String mpInitPoint) {
        jdbcTemplate.update(
                "UPDATE pedidos SET mp_preferencia_id = ?, mp_init_point = ? WHERE id = ?",
                mpPreferenciaId, mpInitPoint, pedidoId
        );
    }

    @Override
    public void actualizarPago(Long pedidoId, Boolean pagoSimulado, EstadoPedido estado) {
        jdbcTemplate.update(
                "UPDATE pedidos SET pagoSimulado = ?, estado = ? WHERE id = ?",
                pagoSimulado, estado.name(), pedidoId
        );
    }
}