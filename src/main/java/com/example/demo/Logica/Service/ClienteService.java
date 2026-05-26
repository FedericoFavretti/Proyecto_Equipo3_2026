package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.RolUsuario;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
@Service
public class ClienteService {
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private PlatoRepositorio platoRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Cliente registrarUsuario(DtCliente dtCliente) {
        if (clienteRepositorio.existeCorreo(dtCliente.getEmail())) {
            throw new IllegalArgumentException(
                    "El correo ya está asociado a una cuenta. ¿Desea iniciar sesión?");
        }
        if (clienteRepositorio.existeDocumento(dtCliente.getDocumento())) {
            throw new IllegalArgumentException(
                    "El documento ya está asociado a una cuenta.");
        }
        Cliente cliente = Cliente.builder()
                .documento(dtCliente.getDocumento())
                .nombre(dtCliente.getNombre())
                .apellido(dtCliente.getApellido())
                .direccion(dtCliente.getDireccion())
                .calificacionGlobal(0.0)
                .activo(true)
                .build();
        cliente.setEmail(dtCliente.getEmail());
        cliente.setPasswd(dtCliente.getPasswd());
        cliente.setFoto(dtCliente.getFoto());
        cliente.setEstado(EstadoCuenta.PendienteAprobacion);
        cliente.setTipo(RolUsuario.CUSTOMER);
        usuarioRepositorio.guardar(cliente);
        Long idGenerado = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE email = ?",
                Long.class, cliente.getEmail()
        );
        cliente.setId(idGenerado);
        clienteRepositorio.guardar(cliente);
        String tokenActivacion = java.util.UUID.randomUUID().toString();
        java.time.Instant expira = java.time.Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS);
        usuarioRepositorio.guardarTokenActivacion(cliente.getId(), tokenActivacion, expira);
        emailService.enviarMailDeActivacion(cliente.getEmail(), tokenActivacion);
        return cliente;
    }

    @Transactional
    public Cliente registrarUsuarioGoogle(DtCliente dtCliente){
        return null;
    }

    @Transactional
    public List<Plato> buscarPlatos(DtFiltro dtFiltro) {
        if(dtFiltro == null){
            throw  new IllegalArgumentException("El filtro no puede ser nulo.");
        }
        if (dtFiltro.getNombre() != null && dtFiltro.getPrecioMasBajo() != null && dtFiltro.getPromocionActiva() != null && dtFiltro.getAlfabetico() != null && dtFiltro.getDtLocal() != null){
            platoRepositorio.buscarPorNombre(dtFiltro.getNombre());
        }

        return null;
    }

    @Transactional
    public List<Local> listarLocales() {
        return null;
    }
}
