package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Pedido;
import org.springframework.stereotype.Service;

@Service
public class PagoSimuladoService {

    public boolean procesarPago(Pedido pedido) {
        return pedido != null;
    }
}
