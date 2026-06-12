package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.DataTypes.DtCliente;
import com.example.demo.Logica.DataTypes.DtFiltro;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.Enums.EstadoCuenta;




import java.util.List;
@Service
public class ClienteService {
    private final ClienteRepositorio clienteRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ClienteService (ClienteRepositorio clienteRepositorio, PlatoRepositorio platoRepositorio, UsuarioRepositorio usuarioRepositorio, EmailService emailService, PasswordEncoder passwordEncode) {
        this.clienteRepositorio = clienteRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncode;
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

        Cliente cliente = Cliente.builder()
                .documento(dtCliente.getDocumento())
                .nombre(dtCliente.getNombre())
                .apellido(dtCliente.getApellido())
                .direccion(dtCliente.getDireccion())
                .calificacionGlobal(0.0)
                .activo(false)
                .build();
        cliente.setEmail(dtCliente.getEmail());
        cliente.setPasswd(passwordEncoder.encode(dtCliente.getPasswd()));
        cliente.setFoto(dtCliente.getFoto());
        cliente.setEstado(EstadoCuenta.Pendiente);
        cliente.setTipo("Cliente");
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
