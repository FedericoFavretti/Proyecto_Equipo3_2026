package com.example.demo.Persistencia.Implementaciones;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Persistencia.Repositorios.FacturaRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class FacturaRepositorioImpl implements FacturaRepositorio {
    private final PedidoRepositorio pedidoRepositorio;
    private final JdbcTemplate jdbcTemplate;


    public FacturaRepositorioImpl(JdbcTemplate jdbcTemplate,  PedidoRepositorio pedidoRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.pedidoRepositorio = pedidoRepo;
    }

    @Override
    public List<Factura> listarTodos() {
        return jdbcTemplate.query("SELECT * FROM Factura",
                (rs, row) ->mapearFactura(rs)
        );
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return jdbcTemplate.query("SELECT * FROM Factura WHERE id = ?",
                (rs, row) -> mapearFactura(rs),id
        ).stream().findFirst();
    }

    @Override
    public Optional<Factura> buscarPorPedidoId(Long pedidoId) {
        return jdbcTemplate.query("SELECT * FROM Factura WHERE idPedido = ?",
                (rs, row) -> mapearFactura(rs), pedidoId
        ).stream().findFirst();
    }

    @Override
    public void guardar(Factura factura) {
        jdbcTemplate.update("INSERT INTO Factura (numero, monto, archivoPdf, idPedido) VALUES (?, ?, ?, ?)",
                factura.getNumero(),
                factura.getMonto(),
                factura.getArchivoPdf(),
                factura.getPedido().getId()
        );
    }

    @Override
    public void actualizar(Factura factura) {
        jdbcTemplate.update("UPDATE Factura SET numero = ?, monto = ?, archivoPdf = ?, idPedido = ? WHERE id = ?",
                factura.getNumero(),
                factura.getMonto(),
                factura.getArchivoPdf(),
                factura.getPedido().getId(),
                factura.getId()
        );
    }

    @Override
    public void eliminar(Long id) {
        jdbcTemplate.update("DELETE FROM Factura WHERE id = ?", id);
    }

    private Factura mapearFactura(ResultSet rs) throws SQLException {
        return new Factura(
                rs.getLong("id"),
                rs.getString("numero"),
                rs.getDouble("monto"),
                rs.getString("archivoPdf"),
                pedidoRepositorio.buscarPorId(rs.getLong("idPedido")).orElseThrow(()-> new RuntimeException("Pedido no encontrado"))
        );
    }
}
