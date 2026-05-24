package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Logica.Interfaces.iClienteController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController implements iClienteController {
    @PostMapping("")
    public ResponseEntity<Cliente> registrarUsuario(@RequestBody DtCliente dtCliente) {
        return null;
    }
    @PostMapping("")
    public ResponseEntity<Cliente> registrarUsuarioGoogle(@RequestBody DtCliente dtCliente){
        return null;
    }
    @GetMapping("/{filtro}")
    public ResponseEntity<List<Plato>> buscarPlatos(@PathVariable DtFiltro dtFiltro) {
        return null;
    }

    @GetMapping("")
    public ResponseEntity<List<Local>> listarLocales() {
        return null;
    }
}
