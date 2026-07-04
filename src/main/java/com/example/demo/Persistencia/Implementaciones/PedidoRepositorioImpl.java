package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.request.DtPedidoListadoFiltro;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Record.PlatoVendidoEstadisticaProjection;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                    p.motivorechazo,
                    c.id AS cliente_id,
                    c.nombre AS cliente_nombre,
                    c.apellido AS cliente_apellido,
                    NULL::bigint AS local_id,
                    NULL::varchar AS local_nombre,
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
                         p.motivorechazo, c.id, c.nombre, c.apellido
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
    public List<PedidoListadoView> listarHistorialPorCliente(Long idCliente, DtPedidoListadoFiltro filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id,
                    p.fecha,
                    p.estado,
                    p.total,
                    p.tiempoestentrega,
                    p.motivorechazo,
                    NULL::bigint AS cliente_id,
                    NULL::varchar AS cliente_nombre,
                    NULL::varchar AS cliente_apellido,
                    l.id AS local_id,
                    l.nombre AS local_nombre,
                    COALESCE(SUM(dp.cantidad), 0) AS cantidad_items
                FROM pedido p
                JOIN local l ON l.id = p.idlocal
                LEFT JOIN detallepedido dp ON dp.idpedido = p.id
                WHERE p.idcliente = ?
                """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idCliente);

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

        if (filtro != null && filtro.getIdLocal() != null) {
            sql.append(" AND p.idlocal = ?");
            parametros.add(filtro.getIdLocal());
        }

        sql.append("""

                GROUP BY p.id, p.fecha, p.estado, p.total, p.tiempoestentrega,
                         p.motivorechazo, l.id, l.nombre
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
    public boolean existePedidoPorCliente(Long idCliente) {
        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedido WHERE idcliente = ?",
                Integer.class,
                idCliente
        );
        return cantidad != null && cantidad > 0;
    }

    @Override
    public boolean existePedidoDeClienteEnLocal(Long idCliente, Long idLocal) {
        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedido WHERE idcliente = ? AND idlocal = ?",
                Integer.class,
                idCliente,
                idLocal
        );
        return cantidad != null && cantidad > 0;
    }

    @Override
    public boolean existePedidoActivoPorCliente(Long idCliente) {
        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedido WHERE idcliente = ? AND estado IN (?, ?)",
                Integer.class,
                idCliente,
                EstadoPedido.Pendiente.name(),
                EstadoPedido.Confirmado.name()
        );
        return cantidad != null && cantidad > 0;
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
    public boolean existePedidoValidoParaEstadisticasEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva) {
        String sql = """
        SELECT COUNT(*)
        FROM pedido p
        WHERE p.idlocal = ?
          AND p.estado IN (?, ?)
          AND p.fecha >= ?
          AND p.fecha < ?
        """;

        Integer cantidad = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idLocal,
                EstadoPedido.Confirmado.name(),
                EstadoPedido.Entregado.name(),
                Timestamp.valueOf(fechaDesdeInclusive),
                Timestamp.valueOf(fechaHastaExclusiva)
        );
        return cantidad != null && cantidad > 0;
    }

    @Override
    public List<PlatoVendidoEstadisticaProjection> obtenerPlatosMasPedidosEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva, int limite) {
        String sql = """
        SELECT dp.idplato AS id_plato,
               SUM(dp.cantidad) AS cantidad_total,
               COALESCE(SUM(dp.subtotal), 0) AS monto_total
        FROM pedido p
        JOIN detallepedido dp ON p.id = dp.idpedido
        WHERE p.idlocal = ?
          AND p.estado IN (?, ?)
          AND p.fecha >= ?
          AND p.fecha < ?
        GROUP BY dp.idplato
        ORDER BY cantidad_total DESC, monto_total DESC, dp.idplato ASC
        LIMIT ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlatoVendidoEstadisticaProjection(
                rs.getLong("id_plato"),
                rs.getInt("cantidad_total"),
                rs.getDouble("monto_total")
        ), idLocal,
                EstadoPedido.Confirmado.name(),
                EstadoPedido.Entregado.name(),
                Timestamp.valueOf(fechaDesdeInclusive),
                Timestamp.valueOf(fechaHastaExclusiva),
                limite);
    }

    @Override
    public List<PlatoVendidoEstadisticaProjection> obtenerVentasPorPlatoEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva) {
        String sql = """
        SELECT dp.idplato AS id_plato,
               SUM(dp.cantidad) AS cantidad_total,
               COALESCE(SUM(dp.subtotal), 0) AS monto_total
        FROM pedido p
        JOIN detallepedido dp ON p.id = dp.idpedido
        WHERE p.idlocal = ?
          AND p.estado IN (?, ?)
          AND p.fecha >= ?
          AND p.fecha < ?
        GROUP BY dp.idplato
        ORDER BY cantidad_total DESC, monto_total DESC, dp.idplato ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlatoVendidoEstadisticaProjection(
                rs.getLong("id_plato"),
                rs.getInt("cantidad_total"),
                rs.getDouble("monto_total")
        ), idLocal,
                EstadoPedido.Confirmado.name(),
                EstadoPedido.Entregado.name(),
                Timestamp.valueOf(fechaDesdeInclusive),
                Timestamp.valueOf(fechaHastaExclusiva));
    }

    @Override
    public Double obtenerVentasParaEstadisticasEnPeriodo(Long idLocal, LocalDateTime fechaDesdeInclusive, LocalDateTime fechaHastaExclusiva) {
        String sql = """
        SELECT COALESCE(SUM(p.total), 0) AS ventas
        FROM pedido p
        WHERE p.idlocal = ?
          AND p.estado IN (?, ?)
          AND p.fecha >= ?
          AND p.fecha < ?
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Double.class,
                idLocal,
                EstadoPedido.Confirmado.name(),
                EstadoPedido.Entregado.name(),
                Timestamp.valueOf(fechaDesdeInclusive),
                Timestamp.valueOf(fechaHastaExclusiva)
        );
    }

    @Override
    public void guardar(Pedido pedido) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO pedido
                        (fecha, tiempoestentrega, total, calle, numero, ciudad, codigopostal,
                         mediopago, pagosimulado, estado, motivorechazo, idlocal, idcliente)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(pedido.getFecha()));
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
            ps.setString(11, pedido.getMotivoRechazo());
            ps.setLong(12, pedido.getLocal().getId());
            ps.setLong(13, pedido.getCliente().getId());
            return ps;
        }, keyHolder);

        pedido.setId(keyHolder.getKey().longValue());
    }

    @Override
    public void actualizar(Pedido pedido) {
        jdbcTemplate.update(
                "UPDATE pedido SET fecha = ?, tiempoestentrega = ?, total = ?, calle = ?, numero = ?, ciudad = ?, codigopostal = ?, mediopago = ?, pagosimulado = ?, estado = ?, motivorechazo = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(pedido.getFecha()),
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
                pedido.getMotivoRechazo(),
                pedido.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM pedido WHERE id = ?", id);
    }


    @Override
    public void actualizarDatosMp(Long pedidoId, String mpPreferenciaId, String mpInitPoint) {
        jdbcTemplate.update(
                "UPDATE pedido SET mp_preferencia_id = ?, mp_init_point = ? WHERE id = ?",
                mpPreferenciaId, mpInitPoint, pedidoId
        );
    }

    @Override
    public void actualizarPago(Long pedidoId, Boolean pagoSimulado, EstadoPedido estado) {
        jdbcTemplate.update(
                "UPDATE pedido SET pagosimulado = ?, estado = ? WHERE id = ?",
                pagoSimulado, estado.name(), pedidoId
        );
    }

    @Override
    public void marcarPagoAprobado(Long pedidoId) {
        jdbcTemplate.update(
                "UPDATE pedido SET pagado = ?, pagosimulado = ? WHERE id = ?",
                true, false, pedidoId
        );
    }

    private Pedido mapearPedido(ResultSet rs, int row) throws SQLException {
        Time tiempoEstEntrega = rs.getTime("tiempoestentrega");

        return Pedido.builder()
                .id(rs.getLong("id"))
                .fecha(rs.getTimestamp("fecha").toLocalDateTime())
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
                .pagado(rs.getBoolean("pagado"))
                .mpPreferenciaId(rs.getString("mp_preferencia_id"))
                .mpInitPoint(rs.getString("mp_init_point"))
                .estado(EstadoPedido.valueOf(rs.getString("estado")))
                .motivoRechazo(rs.getString("motivorechazo"))
                .local(localRepositorio.buscarPorId(rs.getLong("idlocal"))
                        .orElseThrow(() -> new RuntimeException("Local no encontrado")))
                .cliente(clienteRepositorio.buscarPorId(rs.getLong("idcliente"))
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado")))
                .build();
    }

    @Override
    public List<Pedido> buscarEnCaminoVencidos(LocalDateTime ahora) {
        String sql = """
        SELECT * FROM pedido
        WHERE estado = 'Confirmado'
          AND (fecha + (tiempoestentrega)::interval) <= ?
        """;
        return jdbcTemplate.query(sql, new Object[]{Timestamp.valueOf(ahora)}, this::mapearPedido);
    }

    private PedidoListadoView mapearPedidoListadoView(ResultSet rs, int row) throws SQLException {
        Time tiempoEstEntrega = rs.getTime("tiempoestentrega");

        return PedidoListadoView.builder()
                .id(rs.getLong("id"))
                .fecha(rs.getTimestamp("fecha").toLocalDateTime())
                .estado(EstadoPedido.valueOf(rs.getString("estado")))
                .total(rs.getDouble("total"))
                .tiempoEstEntrega(tiempoEstEntrega != null
                        ? Duration.ofSeconds(tiempoEstEntrega.toLocalTime().toSecondOfDay())
                        : null)
                .clienteId(rs.getObject("cliente_id", Long.class))
                .clienteNombre(rs.getString("cliente_nombre"))
                .clienteApellido(rs.getString("cliente_apellido"))
                .localId(rs.getObject("local_id", Long.class))
                .localNombre(rs.getString("local_nombre"))
                .cantidadItems(rs.getInt("cantidad_items"))
                .motivoRechazo(rs.getString("motivorechazo"))
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

