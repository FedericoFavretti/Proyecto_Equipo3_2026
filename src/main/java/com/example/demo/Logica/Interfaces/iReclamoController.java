package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.EstadoReclamo;
import java.time.LocalDate;

import java.util.List;

public interface iReclamoController {
    ResponseEntity<Void> reclamar(Authentication authentication, @RequestBody DtReclamo dtReclamo);
    ResponseEntity<List<DtReclamo>> buscarReclamos(
            Long idLocal, Long idCliente, EstadoPedido estadoPedido, EstadoReclamo estadoReclamo, LocalDate fechaReclamo);;
    ResponseEntity<Void> resolverReclamo(@RequestBody DtReclamo dtReclamo);
}
