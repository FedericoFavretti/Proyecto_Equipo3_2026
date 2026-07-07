package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iReclamoController {
    ResponseEntity<Void> reclamar(Authentication authentication, @RequestBody DtReclamo dtReclamo);
    ResponseEntity<List<DtReclamo>> buscarReclamos(@RequestBody DtFiltroReclamo dtFiltroReclamo);
    ResponseEntity<Void> resolverReclamo(@RequestBody DtReclamo dtReclamo);
}
