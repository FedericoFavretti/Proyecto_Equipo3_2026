package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
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
import java.sql.Time;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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

    @Override
    public List<Pedido> listarTodos() {
        return jdbcTemplate.query(
                "SELECT * FROM pedido",
                this::mapearPedido
        );
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return jdbcTemplate.query(
                "SELECT * FROM pedido WHERE id = ?",
                this::mapearPedido, id
        ).stream().findFirst();
    }

    @Override
    public List<Pedido> listarPorLocal(Long idLocal) {
        return jdbcTemplate.query(
                "SELECT * FROM pedido WHERE idlocal = ?",
                this::mapearPedido, idLocal
        );
    }

    @Override
    public List<PedidoListadoView> listarRecibidosPorLocal(Long idLocal, DtPedidoListadoFiltro filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id,
                    p.fecha,
                    p.estado,
                    p.total,
                    p.tiempoestentrega,
                    c.id AS cliente_id,
                    c.nombre AS cliente_nombre,
                    c.apellido AS cliente_apellido,
                    COALESCE(SUM(dp.cantidad), 0) AS cantidad_items
                FROM pedido p
                JOIN cliente c ON c.id = p.idcliente
                LEFT JOIN detallepedido dp ON dp.idpedido = p.id
                WHERE p.idlocal = ?
                """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idLocal);

        if (filtro != null && filtro.getEstado() != null) {
            sql.append(" AND p.estado = ?");
            parametros.add(filtro.getEstado().name());
        }

        if (filtro != null && filtro.getFechaDesde() != null) {
            sql.append(" AND p.fecha >= ?");
            parametros.add(java.sql.Date.valueOf(filtro.getFechaDesde()));
        }

        if (filtro != null && filtro.getFechaHasta() != null) {
            sql.append(" AND p.fecha <= ?");
            parametros.add(java.sql.Date.valueOf(filtro.getFechaHasta()));
        }

        sql.append("""
                
                GROUP BY p.id, p.fecha, p.estado, p.total, p.tiempoestentrega,
                         c.id, c.nombre, c.apellido
                ORDER BY 
                """);
        sql.append(resolverCampoOrden(filtro));
        sql.append(" ");
        sql.append(resolverDireccionOrden(filtro));

        return jdbcTemplate.query(
                sql.toString(),
                this::mapearPedidoListadoView,
                parametros.toArray()
        );
    }

    @Override
    public boolean existePedidoPendientePorLocal(Long idLocal) {
        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedido WHERE idlocal = ? AND estado = ?",
                Integer.class,
                idLocal,
                EstadoPedido.Pendiente.name()
        );
        return cantidad != null && cantidad > 0;
    }



    @Override
    public void guardar(Pedido pedido) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO pedido
                        (fecha, tiempoestentrega, total, calle, numero, ciudad, codigopostal,
                         mediopago, pagosimulado, estado, idlocal, idcliente)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setDate(1, new java.sql.Date(pedido.getFecha().getTime()));
            if (pedido.getTiempoEstEntrega() != null) {
                ps.setTime(2, Time.valueOf(LocalTime.MIDNIGHT.plusSeconds(pedido.getTiempoEstEntrega().getSeconds())));
            } else {
                ps.setNull(2, Types.TIME);
            }
            ps.setDouble(3, pedido.getTotal());
            ps.setString(4, pedido.getDomicilioEntrega().getCalle());
            ps.setString(5, pedido.getDomicilioEntrega().getNumero());
            ps.setString(6, pedido.getDomicilioEntrega().getCiudad());
            ps.setString(7, pedido.getDomicilioEntrega().getCodigoPostal());
            ps.setString(8, pedido.getMedioDePago());
            ps.setBoolean(9, pedido.getPagoSimulado());
            ps.setString(10, pedido.getEstado().name());
            ps.setLong(11, pedido.getLocal().getId());
            ps.setLong(12, pedido.getCliente().getId());
            return ps;
        }, keyHolder);

        pedido.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void actualizar(Pedido pedido) {
        jdbcTemplate.update(
                "UPDATE pedido SET fecha = ?, tiempoestentrega = ?, total = ?, calle = ?, numero = ?,ciudad = ?, codigopostal = ?, mediopago = ?, pagosimulado = ?,estado = ?, idlocal = ?, idcliente = ? WHERE id = ?"

                ,
                new java.sql.Date(pedido.getFecha().getTime()),
                pedido.getTiempoEstEntrega() != null
                        ? Time.valueOf(LocalTime.MIDNIGHT.plusSeconds(pedido.getTiempoEstEntrega().getSeconds()))
                        : null,
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
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM pedido WHERE id = ?", id);
    }


    @Override
    public void actualizarDatosMp(Long pedidoId, String mpPreferenciaId, String mpInitPoint) {
        throw new UnsupportedOperationException(
                "La tabla pedido no tiene columnas de Mercado Pago en el esquema actual."
        );
    }

    @Override
    public void actualizarPago(Long pedidoId, Boolean pagoSimulado, EstadoPedido estado) {
        jdbcTemplate.update(
                "UPDATE pedido SET pagosimulado = ?, estado = ? WHERE id = ?",
                pagoSimulado, estado.name(), pedidoId
        );
    }

    private Pedido mapearPedido(ResultSet rs, int row) throws SQLException {
        Time tiempoEstEntrega = rs.getTime("tiempoestentrega");

        return Pedido.builder()
                .id(rs.getLong("id"))
                .fecha(rs.getDate("fecha"))
                .tiempoEstEntrega(tiempoEstEntrega != null
                        ? Duration.ofSeconds(tiempoEstEntrega.toLocalTime().toSecondOfDay())
                        : null)
                .total(rs.getDouble("total"))
                .domicilioEntrega(new DtDireccion(
                        rs.getString("calle"),
                        rs.getString("numero"),
                        rs.getString("ciudad"),
                        rs.getString("codigopostal")
                ))
                .medioDePago(rs.getString("mediopago"))
                .pagoSimulado(rs.getBoolean("pagosimulado"))
                .estado(EstadoPedido.valueOf(rs.getString("estado")))
                .local(localRepositorio.buscarPorId(rs.getLong("idlocal"))
                        .orElseThrow(() -> new RuntimeException("Local no encontrado")))
                .cliente(clienteRepositorio.buscarPorId(rs.getLong("idcliente"))
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado")))
                .build();
    }

    private PedidoListadoView mapearPedidoListadoView(ResultSet rs, int row) throws SQLException {
        Time tiempoEstEntrega = rs.getTime("tiempoestentrega");

        return PedidoListadoView.builder()
                .id(rs.getLong("id"))
                .fecha(rs.getDate("fecha"))
                .estado(EstadoPedido.valueOf(rs.getString("estado")))
                .total(rs.getDouble("total"))
                .tiempoEstEntrega(tiempoEstEntrega != null
                        ? Duration.ofSeconds(tiempoEstEntrega.toLocalTime().toSecondOfDay())
                        : null)
                .clienteId(rs.getLong("cliente_id"))
                .clienteNombre(rs.getString("cliente_nombre"))
                .clienteApellido(rs.getString("cliente_apellido"))
                .cantidadItems(rs.getInt("cantidad_items"))
                .build();
    }

    private String resolverCampoOrden(DtPedidoListadoFiltro filtro) {
        if (filtro == null || filtro.getOrdenarPor() == null) {
            return "p.fecha";
        }

        return switch (filtro.getOrdenarPor().toLowerCase()) {
            case "total" -> "p.total";
            case "estado" -> "p.estado";
            default -> "p.fecha";
        };
    }

    private String resolverDireccionOrden(DtPedidoListadoFiltro filtro) {
        if (filtro == null || filtro.getDireccion() == null) {
            return "DESC";
        }

        return "asc".equalsIgnoreCase(filtro.getDireccion()) ? "ASC" : "DESC";
    }

}

