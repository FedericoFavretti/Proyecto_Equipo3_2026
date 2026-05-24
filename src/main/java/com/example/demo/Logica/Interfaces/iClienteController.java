package com.example.demo.Logica.Interfaces;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface iClienteController {
    ResponseEntity<Cliente> registrarUsuario(@RequestBody DtCliente dtCliente);
    ResponseEntity<Cliente> registrarUsuarioGoogle(@RequestBody DtCliente dtCliente);
    ResponseEntity<List<Plato>> buscarPlatos(@PathVariable DtFiltro dtFiltro);
    ResponseEntity<List<Local>> listarLocales();
}
