package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Factura;
import com.example.demo.Logica.Clases.Pedido;
import com.example.demo.Persistencia.Repositorios.FacturaRepositorio;
import org.springframework.stereotype.Service;

@Service
public class FacturaService {

    private final FacturaRepositorio facturaRepositorio;

    public FacturaService(FacturaRepositorio facturaRepositorio) {
        this.facturaRepositorio = facturaRepositorio;
    }

    public Factura generarYGuardarFactura(Pedido pedido) {
        Factura factura = Factura.builder()
                .numero("FAC-" + pedido.getId())
                .monto(pedido.getTotal())
                .archivoPdf("facturas/pedido-" + pedido.getId() + ".pdf")
                .pedido(pedido)
                .build();

        facturaRepositorio.guardar(factura);
        return factura;
    }
}
