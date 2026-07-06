package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.TokenActivacionCuenta;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.request.DtFiltroLocal;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.request.DtGoogleRegistroCompletarRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtGoogleRegistroPendienteResponse;
import com.example.demo.Logica.DataTypes.response.DtLocalBusquedaResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.DataTypes.shared.DtGoogleUserInfo;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Mappers.PromocionMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import com.example.demo.Persistencia.Repositorios.TokenActivacionCuentaRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Utils.TokenSeguroUtils;
import com.example.demo.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private static final String TIPO_USUARIO_CLIENTE = "cliente";
    private static final String MENSAJE_PASSWORD_OBLIGATORIA = "La contraseña es obligatoria.";
    private static final String MENSAJE_PASSWD_INVALIDA =
            "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.";
    private static final String MENSAJE_CORREO_DUPLICADO =
            "El correo ya está asociado a una cuenta. ¿Desea iniciar sesión?";
    private static final String MENSAJE_DOCUMENTO_DUPLICADO =
            "El documento ya está asociado a una cuenta.";
    private static final String MENSAJE_FILTRO_NULO = "El filtro no puede ser nulo.";
    private static final String MENSAJE_SIN_RESULTADOS =
            "No se encontraron platos o promociones que coincidan con su búsqueda.";
    private static final String MENSAJE_GOOGLE_TOKEN_REQUERIDO =
            "La autenticación con Google requiere un token válido.";
    private static final String MENSAJE_TOKEN_REGISTRO_REQUERIDO =
            "El token de registro Google es obligatorio.";
    private static final String MENSAJE_TERMINOS_REQUERIDOS =
            "Debe aceptar términos y condiciones para finalizar el registro.";
    private static final String MENSAJE_CORREO_GOOGLE_EXISTENTE =
            "El correo %s ya está asociado a una cuenta existente. ¿Desea iniciar sesión en su lugar?";
    private static final String MENSAJE_LOGIN_GOOGLE_SIN_CUENTA =
            "No existe una cuenta de cliente asociada al correo %s. Regístrese con Google para continuar.";
    private static final String MENSAJE_DATOS_FALTANTES =
            "Los siguientes campos son requeridos: %s. Por favor, complételos para finalizar el registro.";
    private static final String RUTA_ACTIVACION_CUENTA = "/activar-cuenta";

    @Value("${app.account.activation-frontend-base-url}")
    private String activationFrontendBaseUrl;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final GoogleIdentityService googleIdentityService;
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
    private final TokenActivacionCuentaRepositorio tokenActivacionCuentaRepositorio;

    public ClienteService(ClienteRepositorio clienteRepositorio, PlatoRepositorio platoRepositorio,
                          PromocionRepositorio promocionRepositorio, UsuarioRepositorio usuarioRepositorio,
                          EmailService emailService, PasswordEncoder passwordEncoder, ClienteMapper clienteMapper,
                          PlatoMapper platoMapper, PromocionMapper promocionMapper, LocalRepositorio localRepositorio,
                          LocalMapper localMapper, JwtService jwtService, UserDetailsService userDetailsService,
                          GoogleIdentityService googleIdentityService,
                          TokenActivacionCuentaRepositorio tokenActivacionCuentaRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.platoRepositorio = platoRepositorio;
        this.promocionRepositorio = promocionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.clienteMapper = clienteMapper;
        this.platoMapper = platoMapper;
        this.promocionMapper = promocionMapper;
        this.localRepositorio = localRepositorio;
        this.localMapper = localMapper;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.googleIdentityService = googleIdentityService;
        this.tokenActivacionCuentaRepositorio = tokenActivacionCuentaRepositorio;
    }

    @Transactional
    public Cliente registrarUsuario(DtCliente dtCliente) {
        if (dtCliente == null || dtCliente.getPasswd() == null || dtCliente.getPasswd().isBlank()) {
            throw new BusinessRuleException(MENSAJE_PASSWORD_OBLIGATORIA);
        }
        if (!cumpleRequisitosPasswd(dtCliente.getPasswd())) {
            throw new BusinessRuleException(MENSAJE_PASSWD_INVALIDA);
        }
        dtCliente.setActivo(false);
        dtCliente.setEstadoCuenta(EstadoCuenta.Pendiente);
        String passwdCodificada = passwordEncoder.encode(dtCliente.getPasswd());

        if (usuarioRepositorio.existeCorreo(dtCliente.getEmail())) {
            throw new ResourceConflictException(MENSAJE_CORREO_DUPLICADO);
        }
        if (clienteRepositorio.existeDocumento(dtCliente.getDocumento())) {
            throw new ResourceConflictException(MENSAJE_DOCUMENTO_DUPLICADO);
        }
        Cliente cliente = clienteMapper.mapearClienteDeDt(dtCliente);
        cliente.setTipo(TIPO_USUARIO_CLIENTE);
        cliente.setPasswd(passwdCodificada);
        usuarioRepositorio.guardar(cliente);
        clienteRepositorio.guardar(cliente);

        String tokenPlano = TokenSeguroUtils.generar();
        TokenActivacionCuenta tokenActivacion = TokenActivacionCuenta.builder()
                .idUsuario(cliente.getId())
                .tokenHash(TokenSeguroUtils.hashear(tokenPlano))
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusHours(24))
                .fechaConsumo(null)
                .usado(false)
                .build();
        tokenActivacionCuentaRepositorio.guardar(tokenActivacion);

        String linkActivacion = UriComponentsBuilder.fromUriString(activationFrontendBaseUrl)
                .replacePath(RUTA_ACTIVACION_CUENTA)
                .replaceQuery(null)
                .queryParam("token", tokenPlano)
                .build()
                .encode()
                .toUriString();

        emailService.enviarMailDeActivacion(cliente.getEmail(), linkActivacion);
        return cliente;
    }

    @Transactional
    public DtGoogleRegistroPendienteResponse iniciarRegistroConGoogle(DtGoogleAuthRequest request) {
        DtGoogleUserInfo datosGoogle = obtenerDatosGoogle(request);
        if (usuarioRepositorio.existeCorreo(datosGoogle.getEmail())) {
            throw new ResourceConflictException(
                    String.format(MENSAJE_CORREO_GOOGLE_EXISTENTE, datosGoogle.getEmail())
            );
        }

        return DtGoogleRegistroPendienteResponse.builder()
                .tokenRegistro(jwtService.generarTokenRegistroGoogle(datosGoogle))
                .email(datosGoogle.getEmail())
                .nombre(datosGoogle.getNombre())
                .apellido(datosGoogle.getApellido())
                .foto(datosGoogle.getFoto())
                .build();
    }

    @Transactional
    public DtLoginResponseCliente completarRegistroConGoogle(DtGoogleRegistroCompletarRequest request) {
        validarSolicitudCompletarRegistro(request);
        DtGoogleUserInfo datosGoogle = jwtService.validarYObtenerDatosRegistroGoogle(request.getTokenRegistro());
        validarDatosComplementarios(request);

        if (!Boolean.TRUE.equals(request.getAceptaTerminos())) {
            throw new BusinessRuleException(MENSAJE_TERMINOS_REQUERIDOS);
        }
        if (usuarioRepositorio.existeCorreo(datosGoogle.getEmail())) {
            throw new ResourceConflictException(
                    String.format(MENSAJE_CORREO_GOOGLE_EXISTENTE, datosGoogle.getEmail())
            );
        }
        if (clienteRepositorio.existeDocumento(request.getDocumento().trim())) {
            throw new ResourceConflictException(MENSAJE_DOCUMENTO_DUPLICADO);
        }

        Cliente cliente = Cliente.builder()
                .email(datosGoogle.getEmail())
                .passwd(passwordEncoder.encode("GOOGLEAUTH-" + UUID.randomUUID()))
                .foto(request.getFoto())
                .estado(EstadoCuenta.Activo)
                .tipo(TIPO_USUARIO_CLIENTE)
                .documento(request.getDocumento().trim())
                .nombre(datosGoogle.getNombre())
                .apellido(datosGoogle.getApellido())
                .direccion(request.getDireccion())
                .calificacionGlobal(0.0)
                .activo(true)
                .build();

        usuarioRepositorio.guardar(cliente);
        clienteRepositorio.guardar(cliente);

        return construirRespuestaLogin(cliente);
    }

    @Transactional
    public DtLoginResponseCliente loginConGoogle(DtGoogleAuthRequest request) {
        DtGoogleUserInfo datosGoogle = obtenerDatosGoogle(request);
        Cliente cliente = clienteRepositorio.buscarPorEmail(datosGoogle.getEmail())
                .orElseThrow(() -> new BusinessRuleException(
                        String.format(MENSAJE_LOGIN_GOOGLE_SIN_CUENTA, datosGoogle.getEmail())));
        return construirRespuestaLogin(cliente);
    }

    @Transactional
    public DtBusquedaPlatosPromocionesResponse buscarPlatosYPromociones(DtFiltro dtFiltro) {
        if (dtFiltro == null) {
            throw new BusinessRuleException(MENSAJE_FILTRO_NULO);
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
            throw new IllegalArgumentException(MENSAJE_SIN_RESULTADOS);
        }

        Map<Long, DtPromocion> promoPorPlato = promociones.stream()
                .collect(Collectors.toMap(
                        p -> p.getDtPlato().getId(),
                        p -> p,
                        (a, b) -> a
                ));

        platos.forEach(plato -> {
            DtPromocion promo = promoPorPlato.get(plato.getId());
            if (promo != null) {
                double precioFinal = plato.getPrecio() * (1 - promo.getDescuento() / 100);
                plato.setPrecioFinal(precioFinal);
                plato.setTienePromocion(true);
            } else {
                plato.setPrecioFinal(plato.getPrecio());
                plato.setTienePromocion(false);
            }
        });

        return DtBusquedaPlatosPromocionesResponse.builder()
                .platos(platos)
                .promociones(promociones)
                .build();
    }

    @Transactional
    public List<DtLocalBusquedaResponse> buscarYListarLocales(DtFiltroLocal filtro) {
        validarFiltroLocal(filtro);
        return localRepositorio.buscarHabilitadosConFiltros(filtro).stream()
                .map(localMapper::mapearDtLocalBusquedaDeClase)
                .toList();
    }

    private DtGoogleUserInfo obtenerDatosGoogle(DtGoogleAuthRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            throw new BusinessRuleException(MENSAJE_GOOGLE_TOKEN_REQUERIDO);
        }
        return googleIdentityService.obtenerDatosUsuario(request.getIdToken());
    }

    private void validarSolicitudCompletarRegistro(DtGoogleRegistroCompletarRequest request) {
        if (request == null || request.getTokenRegistro() == null || request.getTokenRegistro().isBlank()) {
            throw new BusinessRuleException(MENSAJE_TOKEN_REGISTRO_REQUERIDO);
        }
    }

    private void validarDatosComplementarios(DtGoogleRegistroCompletarRequest request) {
        List<String> faltantes = new ArrayList<>();

        if (request.getDocumento() == null || request.getDocumento().isBlank()) {
            faltantes.add("documento");
        }

        DtDireccion direccion = request.getDireccion();
        if (direccion == null) {
            faltantes.add("dirección");
        } else {
            if (direccion.getCalle() == null || direccion.getCalle().isBlank()) {
                faltantes.add("dirección.calle");
            }
            if (direccion.getNumero() == null || direccion.getNumero().isBlank()) {
                faltantes.add("dirección.numero");
            }
            if (direccion.getCiudad() == null || direccion.getCiudad().isBlank()) {
                faltantes.add("dirección.ciudad");
            }
            if (direccion.getCodigoPostal() == null || direccion.getCodigoPostal().isBlank()) {
                faltantes.add("dirección.codigoPostal");
            }
        }

        if (request.getFoto() == null || request.getFoto().isBlank()) {
            faltantes.add("foto de perfil");
        }

        if (!faltantes.isEmpty()) {
            throw new BusinessRuleException(String.format(MENSAJE_DATOS_FALTANTES, String.join(", ", faltantes)));
        }
    }

    private DtLoginResponseCliente construirRespuestaLogin(Cliente cliente) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(cliente.getEmail());
        String token = jwtService.generateToken(userDetails);
        if (cliente.getCalificacionGlobal() == null) {
            cliente.setCalificacionGlobal(0.0);
        }
        return DtLoginResponseCliente.builder()
                .id(cliente.getId())
                .token(token)
                .tipo(cliente.getTipo())
                .email(cliente.getEmail())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .direccion(cliente.getDireccion())
                .foto(cliente.getFoto())
                .calificacionGlobal(cliente.getCalificacionGlobal())
                .build();
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

    private boolean cumpleRequisitosPasswd(String passwd) {
        if (passwd == null || passwd.length() < 8) {
            return false;
        }
        boolean tieneMayuscula = passwd.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = passwd.chars().anyMatch(Character::isDigit);
        return tieneMayuscula && tieneNumero;
    }
}
