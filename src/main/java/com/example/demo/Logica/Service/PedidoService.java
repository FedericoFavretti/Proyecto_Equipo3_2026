package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepositorio pedidoRepositorio;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private LocalRepositorio localRepositorio;

    @Transactional
    public Pedido realizarPedido(DtPedido dtPedido) {
        Local local = localRepositorio.buscarPorId(dtPedido.getDtLocal().getId())
            .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        if (!local.isAbierto()) {
            throw new RuntimeException(
                "Lo sentimos, el local seleccionado cerró y no acepta más pedidos por el momento."
            );
        }

        Cliente cliente = clienteRepositorio.buscarPorId(dtPedido.getDtCliente().getId())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Pedido pedido = Pedido.builder()
            .fecha(new Date())
            .total(dtPedido.getTotal())
            .domicilioEntrega(dtPedido.getDomicilioEntrega())
            .medioDePago(dtPedido.getMedioDePago())
            .pagoSimulado(false)
            .estado(EstadoPedido.Pendiente)
            .local(local)
            .cliente(cliente)
            .build();

        pedidoRepositorio.guardar(pedido);
        return pedido;
    }

    @Transactional
    public Pedido confirmarPedido(long idPedido) {
        Pedido pedido = pedidoRepositorio.buscarPorId(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getEstado().equals(EstadoPedido.Pendiente)) {
            throw new RuntimeException(
                "Solo se pueden confirmar pedidos en estado Pendiente."
            );
        }

        if (pedido.getTiempoEstEntrega() == null) {
            throw new RuntimeException(
                "Debe ingresar el tiempo estimado de entrega para confirmar el pedido."
            );
        }

        pedido.setPagoSimulado(true);
        pedido.setEstado(EstadoPedido.Confirmado);

        pedidoRepositorio.actualizar(pedido);
        return pedido;
    }

    @Transactional
    public List<Pedido> listarPedidos(long idLocal) {
        localRepositorio.buscarPorId(idLocal)
            .orElseThrow(() -> new RuntimeException("Local no encontrado"));

        return pedidoRepositorio.listarPorLocal(idLocal);
    }

    @Transactional
    public void rechazarPedido(long idPedido) {
       
    }

    @Transactional
    public void cancelarPedido(long idPedido) {
        
    }
}