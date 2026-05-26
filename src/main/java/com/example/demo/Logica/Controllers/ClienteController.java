package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Logica.Interfaces.iClienteController;
import com.example.demo.Logica.Service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController implements iClienteController {
    @Autowired
    private ClienteService  clienteService;

    @PostMapping("")
    public ResponseEntity<Cliente> registrarUsuario(@RequestBody DtCliente dtCliente) {
        Cliente cliente = clienteService.registrarUsuario(dtCliente);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping("/google")
    public ResponseEntity<Cliente> registrarUsuarioGoogle(@RequestBody DtCliente dtCliente){
        Cliente cliente = clienteService.registrarUsuarioGoogle(dtCliente);
        return ResponseEntity.ok(cliente);
    }
    @GetMapping("/{filtro}")
    public ResponseEntity<List<Plato>> buscarPlatos(@PathVariable DtFiltro dtFiltro) {
        List<Plato> platos = clienteService.buscarPlatos(dtFiltro);
        return ResponseEntity.ok(platos);
    }

    @GetMapping("")
    public ResponseEntity<List<Local>> listarLocales() {
        List<Local> locales = clienteService.listarLocales();
        return ResponseEntity.ok(locales);
    }
}
