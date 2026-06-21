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
import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Logica.Clases.Calificacion;
import com.example.demo.Logica.DataTypes.response.DtCalificacionGlobalResponse;
import com.example.demo.Persistencia.Repositorios.ClienteCalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

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
    private final LocalRepositorio localRepositorio;
    private final LocalMapper localMapper;
    private final ClienteCalificacionRepositorio clienteCalificacionRepositorio;
    private final CalificacionRepositorio calificacionRepositorio;

    public ClienteService (ClienteRepositorio clienteRepositorio, PlatoRepositorio platoRepositorio,
                           PromocionRepositorio promocionRepositorio, UsuarioRepositorio usuarioRepositorio,
                           EmailService emailService, PasswordEncoder passwordEncode,
                           ClienteMapper clienteMapper, PlatoMapper platoMapper,
                           PromocionMapper promocionMapper, LocalRepositorio localRepositorio,
                           LocalMapper localMapper, ClienteCalificacionRepositorio clienteCalificacionRepositorio,
                           CalificacionRepositorio calificacionRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncode;
        this.clienteMapper = clienteMapper;
        this.platoMapper = platoMapper;
        this.promocionMapper = promocionMapper;
        this.localRepositorio = localRepositorio;
        this.localMapper = localMapper;
        this.clienteCalificacionRepositorio = clienteCalificacionRepositorio;
        this.calificacionRepositorio = calificacionRepositorio;
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
    public List<DtLocalBusquedaResponse> buscarYListarLocales(DtFiltroLocal filtro) {
        validarFiltroLocal(filtro);

        List<DtLocalBusquedaResponse> locales = localRepositorio.buscarHabilitadosConFiltros(filtro).stream()
                .map(localMapper::mapearDtLocalBusquedaDeClase)
                .toList();

        if (locales.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron locales que coincidan con su búsqueda. Intente con otros criterios.");
        }

        return locales;
    }

    private void validarFiltroLocal(DtFiltroLocal filtro) {
        if (filtro == null) {
            return;
        }

        if (filtro.getCalificacionMinima() != null
                && (filtro.getCalificacionMinima() < 0 || filtro.getCalificacionMinima() > 5)) {
            throw new IllegalArgumentException("La calificación mínima debe estar entre 0 y 5.");
        }

        if (filtro.getOrdenarPor() != null) {
            List<String> camposValidos = List.of("nombre", "calificacion");
            if (!camposValidos.contains(filtro.getOrdenarPor().toLowerCase())) {
                throw new IllegalArgumentException("El campo de orden no es válido.");
            }
        }

        if (filtro.getDireccion() != null) {
            List<String> direccionesValidas = List.of("asc", "desc");
            if (!direccionesValidas.contains(filtro.getDireccion().toLowerCase())) {
                throw new IllegalArgumentException("La dirección de orden no es válida.");
            }
        }
    }

    @Transactional(readOnly = true)
    public DtCalificacionGlobalResponse consultarCalificacionGlobal(Long idCliente) {
        clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<Long> idsCalificaciones = clienteCalificacionRepositorio.obtenerCalificacionesDeCliente(idCliente);
        List<Calificacion> calificaciones = calificacionRepositorio.buscarPorIds(idsCalificaciones);

        if (calificaciones.isEmpty()) {
            throw new IllegalArgumentException("Aún no ha recibido calificaciones de ningún local.");
        }

        double promedio = calificaciones.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);

        Map<Integer, Integer> detalle = new HashMap<>();
        for (int puntaje = 1; puntaje <= 5; puntaje++) {
            detalle.put(puntaje, 0);
        }
        for (Calificacion calificacion : calificaciones) {
            detalle.merge(calificacion.getPuntaje(), 1, Integer::sum);
        }

        return DtCalificacionGlobalResponse.builder()
                .promedio(promedio)
                .totalCalificaciones(calificaciones.size())
                .detallePorPuntuacion(detalle)
                .build();
    }
}



