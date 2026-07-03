package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Factura;
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
        return jdbcTemplate.query("SELECT * FROM Factura WHERE idPedido = ?",
                (rs, row) -> mapearFactura(rs), pedidoId
        ).stream().findFirst();
    }

    @Override
    public List<Factura> buscarPendientesDeProcesamiento(LocalDateTime fechaCorte) {
        return jdbcTemplate.query("""
                        SELECT * FROM Factura
                        WHERE estado_pdf = 'PENDIENTE'
                           OR (estado_pdf = 'ERROR_REINTENTABLE' AND proximo_reintento IS NOT NULL AND proximo_reintento <= ?)
                        ORDER BY COALESCE(proximo_reintento, fecha_ultimo_intento, NOW()), id
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
                    (numero, monto, archivoPdf, estado_pdf, intentos_generacion, ultimo_error_pdf,
                     fecha_ultimo_intento, proximo_reintento, fecha_generacion_pdf,
                     local_nombre_snapshot, local_email_snapshot, cliente_nombre_snapshot,
                     cliente_email_snapshot, direccion_entrega_snapshot, medio_pago_snapshot,
                     detalle_items_json, idPedido)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setString(1, factura.getNumero());
            ps.setDouble(2, factura.getMonto());
            ps.setString(3, factura.getArchivoPdf());
            ps.setString(4, factura.getEstadoPdf() != null ? factura.getEstadoPdf().name() : null);
            ps.setObject(5, factura.getIntentosGeneracion());
            ps.setString(6, factura.getUltimoErrorPdf());
            ps.setTimestamp(7, toTimestamp(factura.getFechaUltimoIntento()));
            ps.setTimestamp(8, toTimestamp(factura.getProximoReintento()));
            ps.setTimestamp(9, toTimestamp(factura.getFechaGeneracionPdf()));
            ps.setString(10, factura.getLocalNombreSnapshot());
            ps.setString(11, factura.getLocalEmailSnapshot());
            ps.setString(12, factura.getClienteNombreSnapshot());
            ps.setString(13, factura.getClienteEmailSnapshot());
            ps.setString(14, factura.getDireccionEntregaSnapshot());
            ps.setString(15, factura.getMedioPagoSnapshot());
            ps.setString(16, factura.getDetalleItemsJson());
            ps.setLong(17, factura.getPedido().getId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            factura.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void actualizar(Factura factura) {
        jdbcTemplate.update("""
                        UPDATE Factura
                        SET numero = ?, monto = ?, archivoPdf = ?, estado_pdf = ?, intentos_generacion = ?,
                            ultimo_error_pdf = ?, fecha_ultimo_intento = ?, proximo_reintento = ?,
                            fecha_generacion_pdf = ?, local_nombre_snapshot = ?, local_email_snapshot = ?,
                            cliente_nombre_snapshot = ?, cliente_email_snapshot = ?, direccion_entrega_snapshot = ?,
                            medio_pago_snapshot = ?, detalle_items_json = ?, idPedido = ?
                        WHERE id = ?
                        """,
                factura.getNumero(),
                factura.getMonto(),
                factura.getArchivoPdf(),
                factura.getEstadoPdf() != null ? factura.getEstadoPdf().name() : null,
                factura.getIntentosGeneracion(),
                factura.getUltimoErrorPdf(),
                toTimestamp(factura.getFechaUltimoIntento()),
                toTimestamp(factura.getProximoReintento()),
                toTimestamp(factura.getFechaGeneracionPdf()),
                factura.getLocalNombreSnapshot(),
                factura.getLocalEmailSnapshot(),
                factura.getClienteNombreSnapshot(),
                factura.getClienteEmailSnapshot(),
                factura.getDireccionEntregaSnapshot(),
                factura.getMedioPagoSnapshot(),
                factura.getDetalleItemsJson(),
                factura.getPedido().getId(),
                factura.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Factura WHERE id = ?", id);
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        String estadoPdf = rs.getString("estado_pdf");
        return Factura.builder()
                .id(rs.getLong("id"))
                .numero(rs.getString("numero"))
                .monto(rs.getDouble("monto"))
                .archivoPdf(rs.getString("archivoPdf"))
                .estadoPdf(estadoPdf != null ? EstadoFacturaPdf.valueOf(estadoPdf) : null)
                .intentosGeneracion(rs.getObject("intentos_generacion") != null ? rs.getInt("intentos_generacion") : null)
                .ultimoErrorPdf(rs.getString("ultimo_error_pdf"))
                .fechaUltimoIntento(toLocalDateTime(rs.getTimestamp("fecha_ultimo_intento")))
                .proximoReintento(toLocalDateTime(rs.getTimestamp("proximo_reintento")))
                .fechaGeneracionPdf(toLocalDateTime(rs.getTimestamp("fecha_generacion_pdf")))
                .localNombreSnapshot(rs.getString("local_nombre_snapshot"))
                .localEmailSnapshot(rs.getString("local_email_snapshot"))
                .clienteNombreSnapshot(rs.getString("cliente_nombre_snapshot"))
                .clienteEmailSnapshot(rs.getString("cliente_email_snapshot"))
                .direccionEntregaSnapshot(rs.getString("direccion_entrega_snapshot"))
                .medioPagoSnapshot(rs.getString("medio_pago_snapshot"))
                .detalleItemsJson(rs.getString("detalle_items_json"))
                .pedido(pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(() -> new RuntimeException("Pedido no encontrado")))
                .build();
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }
}
