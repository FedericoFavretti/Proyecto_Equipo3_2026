package com.example.demo.Logica.Schedule;

import com.example.demo.Logica.Service.FacturaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FacturaScheduler {

    private final FacturaService facturaService;

    public FacturaScheduler(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @Scheduled(fixedDelayString = "${app.facturas.retry-delay-ms:60000}")
    public void procesarFacturasPendientes() {
        facturaService.procesarFacturasPendientes();
    }
}
