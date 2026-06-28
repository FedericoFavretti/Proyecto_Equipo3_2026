package com.example.demo.Logica.Schedule;

import com.example.demo.Logica.Service.PedidoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PedidoScheduler {

    private final PedidoService pedidoService;

    public PedidoScheduler(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedRate = 60000)
    public void actualizarPedidosEntregados() {
        pedidoService.marcarPedidosComoEntregados();
    }
}