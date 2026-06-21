package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Mappers.PromocionMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private static final String TIPO_USUARIO_CLIENTE = "cliente";

    private final ClienteRepositorio clienteRepositorio;
    private final PlatoRepositorio platoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ClienteMapper clienteMapper;
    private final PlatoMapper platoMapper;
    private final PromocionRepositorio promocionRepositorio;
    private final PromocionMapper promocionMapper;

    public ClienteService (ClienteRepositorio clienteRepositorio, PlatoRepositorio platoRepositorio,
                           PromocionRepositorio promocionRepositorio, UsuarioRepositorio usuarioRepositorio,
                           EmailService emailService, PasswordEncoder passwordEncode,
                           ClienteMapper clienteMapper, PlatoMapper platoMapper,
                           PromocionMapper promocionMapper) {
        this.clienteRepositorio = clienteRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncode;
        this.clienteMapper = clienteMapper;
        this.platoMapper = platoMapper;
        this.promocionMapper = promocionMapper;
    }


    @Transactional
    public Cliente registrarUsuario(DtCliente dtCliente) {
        if (dtCliente == null || dtCliente.getPasswd() == null || dtCliente.getPasswd().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        String passwdCodificada = passwordEncoder.encode(dtCliente.getPasswd());

        if (usuarioRepositorio.existeCorreo(dtCliente.getEmail())) {
            throw new IllegalArgumentException(
                    "El correo ya está asociado a una cuenta. ¿Desea iniciar sesión?");
        }
        if (clienteRepositorio.existeDocumento(dtCliente.getDocumento())) {
            throw new IllegalArgumentException(
                    "El documento ya está asociado a una cuenta.");
        }

        Cliente cliente = clienteMapper.mapearClienteDeDt(dtCliente);
        cliente.setEstado(EstadoCuenta.Pendiente);
        cliente.setTipo(TIPO_USUARIO_CLIENTE);
        cliente.setPasswd(passwdCodificada);
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
    public DtBusquedaPlatosPromocionesResponse buscarPlatosYPromociones(DtFiltro dtFiltro) {
        if (dtFiltro == null) {
            throw new IllegalArgumentException("El filtro no puede ser nulo.");
        }

        List<DtPlato> platos = platoRepositorio.buscarConFiltros(dtFiltro)
                .stream()
                .map(platoMapper::mapearDtPlatoDeClase)
                .collect(Collectors.toList());

        List<DtPromocion> promociones = promocionRepositorio.buscarActivasConFiltros(dtFiltro)
                .stream()
                .map(promocionMapper::mapearDtPromocionDeClase)
                .collect(Collectors.toList());

        if (platos.isEmpty() && promociones.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron platos o promociones que coincidan con su búsqueda.");
        }

        return DtBusquedaPlatosPromocionesResponse.builder()
                .platos(platos)
                .promociones(promociones)
                .build();
    }

    @Transactional
    public List<DtLocal> listarLocales() {
        return null;
    }
}



