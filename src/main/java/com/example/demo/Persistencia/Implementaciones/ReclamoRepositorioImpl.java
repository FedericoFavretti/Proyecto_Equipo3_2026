package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Reclamo;
import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                (rs, row) -> mapearRecalamo(rs)
        );
    }

    @Override
    public Optional<Reclamo> buscarPorId(long id) {
        return jdbcTemplate.query("SELECT * FROM Reclamo WHERE id = ?",
                (rs, row) -> mapearRecalamo(rs),id
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
        jdbcTemplate.update("UPDATE Reclamo SET motivo = ?, tipoCompensacion = ?, montoReintegro = ?, fecha = ?, idPedido = ? WHERE id = ?",
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

    @Override
    public List<Reclamo> buscarReclamosPorFiltro(DtFiltroReclamo filtro) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, p.* FROM reclamo r JOIN pedido p ON r.idpedido = p.idpedido WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (filtro.getIdCliente() != null) {
            sql.append(" AND p.id = ?");
            params.add(filtro.getIdCliente());
        }

        if (filtro.getEstadoPedido() != null) {
            sql.append(" AND p.estado = ?");
            params.add(filtro.getEstadoPedido().name());
        }

        if (filtro.getFechaReclamo() != null) {
            sql.append(" AND r.fecha = ?");
            params.add(filtro.getFechaReclamo());
        }

        return jdbcTemplate.query(sql.toString(), (rs, row) -> mapearRecalamo(rs), params.toArray());
    }

    @Override
    public boolean existeReclamoPendientePorCliente(Long idCliente) {
        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM reclamo r
                JOIN pedido p ON p.id = r.idpedido
                WHERE p.idcliente = ?
                """,
                Integer.class,
                idCliente
        );
        return cantidad != null && cantidad > 0;
    }

    private Reclamo mapearRecalamo(ResultSet rs) throws SQLException {
        return new Reclamo(
                rs.getLong("id"),
                rs.getString("motivo"),
                rs.getString("tipoCompensacion"),
                rs.getDouble("montoReintegro"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(()->new RuntimeException("Pedido no encontrado"))
        );
    }
}
