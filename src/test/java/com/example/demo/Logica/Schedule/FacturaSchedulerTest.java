package com.example.demo.Logica.Schedule;

import com.example.demo.Logica.Service.FacturaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FacturaSchedulerTest {

    @Mock
    private FacturaService facturaService;

    @Test
    void procesarFacturasPendientesDelegatesEnService() {
        FacturaScheduler scheduler = new FacturaScheduler(facturaService);

        scheduler.procesarFacturasPendientes();

        verify(facturaService).procesarFacturasPendientes();
    }
}
