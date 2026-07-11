package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.request.DtFiltroReclamo;
import com.example.demo.Logica.DataTypes.shared.DtReclamo;
import com.example.demo.Logica.Interfaces.iReclamoController;
import com.example.demo.Logica.Service.ReclamoService;
import com.example.demo.Logica.Enums.EstadoPedido;
import com.example.demo.Logica.Enums.EstadoReclamo;
import com.example.demo.Logica.DataTypes.response.DtPagina;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
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
    public ResponseEntity<Void>  reclamar(Authentication authentication, @RequestBody DtReclamo dtReclamo) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reclamoService.reclamar(authentication.getName(), dtReclamo);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('Local')")
    @GetMapping("/buscar_reclamo")
    public ResponseEntity<DtPagina<DtReclamo>> buscarReclamos(
            @RequestParam(required = false) Long idLocal,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) EstadoPedido estadoPedido,
            @RequestParam(required = false) EstadoReclamo estadoReclamo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReclamo,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanio) {
        DtFiltroReclamo dtFiltroReclamo = DtFiltroReclamo.builder()
                .idLocal(idLocal)
                .idCliente(idCliente)
                .estadoPedido(estadoPedido)
                .estadoReclamo(estadoReclamo)
                .fechaReclamo(fechaReclamo)
                .build();
        return ResponseEntity.ok(reclamoService.buscarReclamos(dtFiltroReclamo, pagina, tamanio));
    }

    @PreAuthorize("hasRole('Local')")
    @PostMapping("/resolver_reclamo")
    public ResponseEntity<Void> resolverReclamo(Authentication authentication, @RequestBody DtReclamo dtReclamo) {
        if (autenticacionInvalida(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reclamoService.resolverReclamo(authentication.getName(), dtReclamo);
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
