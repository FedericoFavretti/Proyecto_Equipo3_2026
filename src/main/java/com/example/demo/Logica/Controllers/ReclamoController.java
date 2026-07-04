package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Interfaces.iReclamoController;
import com.example.demo.Logica.Service.ReclamoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.Utils.AuthUtils.autenticacionInvalida;

@RestController
@RequestMapping("/api/v1/reclamos")
public class ReclamoController implements iReclamoController {
    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PreAuthorize("hasRole('Cliente')")
    @PostMapping("/realizar_reclamo")
    public ResponseEntity<Void>  reclamar(@RequestBody DtReclamo dtReclamo) {
        reclamoService.reclamar(dtReclamo);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/buscar_reclamo")
    public ResponseEntity<List<DtReclamo>> buscarReclamos(@RequestBody DtFiltroReclamo dtFiltroReclamo) {
        return ResponseEntity.ok(reclamoService.buscarReclamos(dtFiltroReclamo));
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/resolver_reclamo")
    public ResponseEntity<Void> resolverReclamo(@RequestBody DtReclamo dtReclamo) {
        reclamoService.resolverReclamo(dtReclamo);
        return  ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Cliente')")
    @GetMapping("/mi-reclamo/{idPedido}")
    public ResponseEntity<DtReclamo> buscarMiReclamoDePedido(
            Authentication authentication, @PathVariable Long idPedido) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DtReclamo dtReclamo = reclamoService.buscarReclamoPropioDePedido(authentication.getName(), idPedido);
        if (dtReclamo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dtReclamo);
    }
}
