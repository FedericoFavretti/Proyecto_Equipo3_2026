package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Interfaces.iReclamoController;
import com.example.demo.Logica.Service.ReclamoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reclamos")
public class ReclamoController implements iReclamoController {
    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping("/realizar_reclamo")
    public ResponseEntity<Void>  reclamar(@RequestBody DtReclamo dtReclamo) {
        reclamoService.reclamar(dtReclamo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar_reclamo")
    public ResponseEntity<List<DtReclamo>> buscarReclamos(@RequestBody DtFiltroReclamo dtFiltroReclamo) {
        reclamoService.buscarReclamos(dtFiltroReclamo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resolver_reclamo")
    public ResponseEntity<Void> resolverReclamo(@RequestBody DtReclamo dtReclamo) {

        return  ResponseEntity.ok().build();
    }

}
