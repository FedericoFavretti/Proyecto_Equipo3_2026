package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iReclamoController {
    public ResponseEntity<Void> reclamar(@RequestBody DtReclamo dtReclamo);
    public ResponseEntity<List<DtReclamo>> buscarReclamos(@RequestBody DtFiltroReclamo dtFiltroReclamo);
    public ResponseEntity<Void> resolverReclamo(@RequestBody DtReclamo dtReclamo);
}
