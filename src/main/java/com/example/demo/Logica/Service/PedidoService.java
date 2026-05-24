package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Logica.DataTypes.DtPedido;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @Transactional
    public Pedido confirmarPedido (long idPedido) {
        return null;
    }

    @Transactional
    public Void rechazarPedido(long idPedido) {
        return null;
    }

    @Transactional
    public Pedido realizarPedido(DtPedido dtPedido) {
        return null;
    }

    @Transactional
    public Void cancelarPedido(long idPedido) {
        return null;
    }

    @Transactional
    public List<Pedido> listarPedidos(long idLocal) {
        return null;
    }
}
