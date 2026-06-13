package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Logica.DataTypes.DtLocal;
import com.example.demo.Logica.DataTypes.DtPlato;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.Enums.EstadoCuenta;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepositorio clienteRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ClienteMapper clienteMapper;
    private final PlatoMapper platoMapper;

    public ClienteService (ClienteRepositorio clienteRepositorio, PlatoRepositorio platoRepositorio, UsuarioRepositorio usuarioRepositorio, EmailService emailService, PasswordEncoder passwordEncode,  ClienteMapper clienteMapper, PlatoMapper platoMapper) {
        this.clienteRepositorio = clienteRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncode;
        this.clienteMapper = clienteMapper;
        this.platoMapper = platoMapper;
    }


    @Transactional
    public Cliente registrarUsuario(DtCliente dtCliente) {
        if (usuarioRepositorio.existeCorreo(dtCliente.getEmail())) {
            throw new IllegalArgumentException(
                    "El correo ya está asociado a una cuenta. ¿Desea iniciar sesión?");
        }
        if (clienteRepositorio.existeDocumento(dtCliente.getDocumento())) {
            throw new IllegalArgumentException(
                    "El documento ya está asociado a una cuenta.");
        }

        Cliente cliente = clienteMapper.mapearClienteDeDt(dtCliente);
        cliente.setPasswd(passwordEncoder.encode(cliente.getPasswd()));
        usuarioRepositorio.guardar(cliente);
        clienteRepositorio.guardar(cliente);
        emailService.enviarMailDeActivacion(cliente.getEmail());
        return cliente;
    }

    @Transactional
    public Cliente registrarUsuarioGoogle(DtCliente dtCliente){
        return null;
    }

    @Transactional
    public List<DtPlato> buscarPlatos(DtFiltro dtFiltro) {
        if (dtFiltro == null) {
            throw new IllegalArgumentException("El filtro no puede ser nulo.");
        }

        return platoRepositorio.buscarConFiltros(dtFiltro)
                .stream()
                .map(platoMapper::mapearDtPlatoDeClase)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DtLocal> listarLocales() {
        return null;
    }
}
