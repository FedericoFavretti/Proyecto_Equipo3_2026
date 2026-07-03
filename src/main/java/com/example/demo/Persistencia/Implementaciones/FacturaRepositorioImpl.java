package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.FacturaDetalle;
import com.example.demo.Logica.Enums.EstadoFacturaPdf;
import com.example.demo.Persistencia.Repositorios.FacturaRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FacturaRepositorioImpl implements FacturaRepositorio {
    private final PedidoRepositorio pedidoRepositorio;
    private final JdbcTemplate jdbcTemplate;

    public FacturaRepositorioImpl(JdbcTemplate jdbcTemplate, PedidoRepositorio pedidoRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.pedidoRepositorio = pedidoRepo;
    }

    @Override
    public List<Factura> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Factura",
                (rs, row) -> mapearFactura(rs)
        );
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM Factura WHERE id = ?",
                (rs, row) -> mapearFactura(rs), id
        ).stream().findFirst();
    }

    @Override
    public Optional<Factura> buscarPorPedidoId(Long pedidoId) {
        return jdbcTemplate.query("SELECT * FROM Factura WHERE id_pedido = ?",
                (rs, row) -> mapearFactura(rs), pedidoId
        ).stream().findFirst();
    }

    @Override
    public List<Factura> buscarPendientesDeProcesamiento(LocalDateTime fechaCorte) {
        return jdbcTemplate.query("""
                        SELECT f.*
                        FROM Factura f
                        JOIN FacturaPdfProceso fpp ON fpp.id_factura = f.id
                        WHERE fpp.estado_pdf = 'PENDIENTE'
                           OR (fpp.estado_pdf = 'ERROR_REINTENTABLE' AND fpp.proximo_reintento IS NOT NULL AND fpp.proximo_reintento <= ?)
                        ORDER BY COALESCE(fpp.proximo_reintento, fpp.fecha_ultimo_intento, NOW()), f.id
                        """,
                (rs, row) -> mapearFactura(rs),
                Timestamp.valueOf(fechaCorte)
        );
    }

    @Override
    public void guardar(Factura factura) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO Factura
                    (numero, id_pedido, fecha_pedido, fecha_emision, monto_total,
                     local_nombre_snapshot, local_email_snapshot, cliente_nombre_snapshot,
                     cliente_email_snapshot, direccion_entrega_snapshot, medio_pago_snapshot)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, factura.getNumero());
            ps.setLong(2, factura.getPedido().getId());
            ps.setTimestamp(3, toTimestamp(factura.getFechaPedido()));
            ps.setTimestamp(4, toTimestamp(factura.getFechaEmision()));
            ps.setDouble(5, factura.getMontoTotal());
            ps.setString(6, factura.getLocalNombreSnapshot());
            ps.setString(7, factura.getLocalEmailSnapshot());
            ps.setString(8, factura.getClienteNombreSnapshot());
            ps.setString(9, factura.getClienteEmailSnapshot());
            ps.setString(10, factura.getDireccionEntregaSnapshot());
            ps.setString(11, factura.getMedioPagoSnapshot());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            factura.setId(keyHolder.getKey().longValue());
        }

        guardarDetalles(factura);
        guardarProcesoPdf(factura);
    }

    @Override
    public void actualizar(Factura factura) {
        jdbcTemplate.update("""
                        UPDATE Factura
                        SET numero = ?, id_pedido = ?, fecha_pedido = ?, fecha_emision = ?, monto_total = ?,
                            local_nombre_snapshot = ?, local_email_snapshot = ?, cliente_nombre_snapshot = ?,
                            cliente_email_snapshot = ?, direccion_entrega_snapshot = ?, medio_pago_snapshot = ?
                        WHERE id = ?
                        """,
                factura.getNumero(),
                factura.getPedido().getId(),
                toTimestamp(factura.getFechaPedido()),
                toTimestamp(factura.getFechaEmision()),
                factura.getMontoTotal(),
                factura.getLocalNombreSnapshot(),
                factura.getLocalEmailSnapshot(),
                factura.getClienteNombreSnapshot(),
                factura.getClienteEmailSnapshot(),
                factura.getDireccionEntregaSnapshot(),
                factura.getMedioPagoSnapshot(),
                factura.getId()
        );

        reemplazarDetalles(factura);
        guardarProcesoPdf(factura);
    }

    @Override
    public void actualizarProcesoPdf(Factura factura) {
        guardarProcesoPdf(factura);
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Factura WHERE id = ?", id);
    }

    private void guardarDetalles(Factura factura) {
        if (factura.getDetalles() == null || factura.getDetalles().isEmpty()) {
            return;
        }

        for (FacturaDetalle detalle : factura.getDetalles()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO FacturaDetalle
                        (id_factura, nombre_producto_snapshot, cantidad, precio_unitario, subtotal)
                        VALUES (?, ?, ?, ?, ?)
                        """, new String[]{"id"});
                ps.setLong(1, factura.getId());
                ps.setString(2, detalle.getNombreProductoSnapshot());
                ps.setInt(3, detalle.getCantidad());
                ps.setDouble(4, detalle.getPrecioUnitario());
                ps.setDouble(5, detalle.getSubtotal());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                detalle.setId(keyHolder.getKey().longValue());
            }
        }
    }

    private void reemplazarDetalles(Factura factura) {
        jdbcTemplate.update("DELETE FROM FacturaDetalle WHERE id_factura = ?", factura.getId());
        guardarDetalles(factura);
    }

    private void guardarProcesoPdf(Factura factura) {
        jdbcTemplate.update("""
                        INSERT INTO FacturaPdfProceso
                        (id_factura, archivo_pdf, estado_pdf, intentos_generacion, ultimo_error_pdf,
                         fecha_ultimo_intento, proximo_reintento, fecha_generacion_pdf)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (id_factura)
                        DO UPDATE SET archivo_pdf = EXCLUDED.archivo_pdf,
                                      estado_pdf = EXCLUDED.estado_pdf,
                                      intentos_generacion = EXCLUDED.intentos_generacion,
                                      ultimo_error_pdf = EXCLUDED.ultimo_error_pdf,
                                      fecha_ultimo_intento = EXCLUDED.fecha_ultimo_intento,
                                      proximo_reintento = EXCLUDED.proximo_reintento,
                                      fecha_generacion_pdf = EXCLUDED.fecha_generacion_pdf
                        """,
                factura.getId(),
                factura.getArchivoPdf(),
                factura.getEstadoPdf() != null ? factura.getEstadoPdf().name() : EstadoFacturaPdf.PENDIENTE.name(),
                factura.getIntentosGeneracion() != null ? factura.getIntentosGeneracion() : 0,
                factura.getUltimoErrorPdf(),
                toTimestamp(factura.getFechaUltimoIntento()),
                toTimestamp(factura.getProximoReintento()),
                toTimestamp(factura.getFechaGeneracionPdf())
        );
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        Long facturaId = rs.getLong("id");
        Factura factura = Factura.builder()
                .id(facturaId)
                .numero(rs.getString("numero"))
                .fechaPedido(toLocalDateTime(rs.getTimestamp("fecha_pedido")))
                .fechaEmision(toLocalDateTime(rs.getTimestamp("fecha_emision")))
                .montoTotal(rs.getDouble("monto_total"))
                .localNombreSnapshot(rs.getString("local_nombre_snapshot"))
                .localEmailSnapshot(rs.getString("local_email_snapshot"))
                .clienteNombreSnapshot(rs.getString("cliente_nombre_snapshot"))
                .clienteEmailSnapshot(rs.getString("cliente_email_snapshot"))
                .direccionEntregaSnapshot(rs.getString("direccion_entrega_snapshot"))
                .medioPagoSnapshot(rs.getString("medio_pago_snapshot"))
                .detalles(buscarDetallesPorFacturaId(facturaId))
                .pedido(pedidoRepositorio.buscarPorId(rs.getLong("id_pedido")).orElseThrow(() -> new RuntimeException("Pedido no encontrado")))
                .build();

        cargarProcesoPdf(factura);
        return factura;
    }

    private List<FacturaDetalle> buscarDetallesPorFacturaId(Long facturaId) {
        return jdbcTemplate.query("""
                        SELECT id, nombre_producto_snapshot, cantidad, precio_unitario, subtotal
                        FROM FacturaDetalle
                        WHERE id_factura = ?
                        ORDER BY id
                        """,
                (rs, row) -> FacturaDetalle.builder()
                        .id(rs.getLong("id"))
                        .nombreProductoSnapshot(rs.getString("nombre_producto_snapshot"))
                        .cantidad(rs.getInt("cantidad"))
                        .precioUnitario(rs.getDouble("precio_unitario"))
                        .subtotal(rs.getDouble("subtotal"))
                        .build(),
                facturaId
        );
    }

    private void cargarProcesoPdf(Factura factura) {
        jdbcTemplate.query("""
                        SELECT archivo_pdf, estado_pdf, intentos_generacion, ultimo_error_pdf,
                               fecha_ultimo_intento, proximo_reintento, fecha_generacion_pdf
                        FROM FacturaPdfProceso
                        WHERE id_factura = ?
                        """,
                rs -> {
                    if (!rs.next()) {
                        return;
                    }
                    String estadoPdf = rs.getString("estado_pdf");
                    factura.setArchivoPdf(rs.getString("archivo_pdf"));
                    factura.setEstadoPdf(estadoPdf != null ? EstadoFacturaPdf.valueOf(estadoPdf) : null);
                    factura.setIntentosGeneracion(rs.getInt("intentos_generacion"));
                    factura.setUltimoErrorPdf(rs.getString("ultimo_error_pdf"));
                    factura.setFechaUltimoIntento(toLocalDateTime(rs.getTimestamp("fecha_ultimo_intento")));
                    factura.setProximoReintento(toLocalDateTime(rs.getTimestamp("proximo_reintento")));
                    factura.setFechaGeneracionPdf(toLocalDateTime(rs.getTimestamp("fecha_generacion_pdf")));
                },
                factura.getId()
        );
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
