package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.shared.DtLocal;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
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
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.jwt.JwtService;
import com.example.demo.Logica.Clases.TokenActivacionCuenta;
import com.example.demo.Persistencia.Repositorios.TokenActivacionCuentaRepositorio;
import com.example.demo.Utils.TokenSeguroUtils;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

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
    private static final String RUTA_ACTIVACION_CUENTA = "/activar-cuenta";

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${app.account.activation-frontend-base-url}")
    private String activationFrontendBaseUrl;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
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
    public Cliente registrarUsuarioGoogle(DtCliente dtCliente){
        return null;
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

    @Transactional
    public DtLoginResponse registrarOLoguearConGoogle(DtGoogleAuthRequest request) {
        String email, nombre, apellido, foto;
        try {
            String url = "https://www.googleapis.com/oauth2/v3/userinfo";
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + request.getIdToken())
                    .GET()
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new BusinessRuleException("Token de Google inválido.");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(httpResponse.body());
            email = json.get("email").asText();
            nombre = json.has("given_name") ? json.get("given_name").asText() : "Usuario";
            apellido = json.has("family_name") ? json.get("family_name").asText() : "";
            foto = json.has("picture") ? json.get("picture").asText() : null;
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Error al verificar el token de Google.");
        }

        if (usuarioRepositorio.existeCorreo(email)) {
            if (Boolean.TRUE.equals(request.getEsRegistro())) {
                throw new BusinessRuleException("Ya existe una cuenta registrada con este email. Iniciá sesión en su lugar.");
            }
            Cliente clienteExistente = clienteRepositorio.buscarPorEmail(email)
                    .orElseThrow(() -> new BusinessRuleException("La cuenta existe pero no es de tipo cliente."));
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String token = jwtService.generateToken(userDetails);
            return DtLoginResponseCliente.builder()
                    .id(clienteExistente.getId())
                    .token(token)
                    .tipo(clienteExistente.getTipo())
                    .email(clienteExistente.getEmail())
                    .nombre(clienteExistente.getNombre())
                    .apellido(clienteExistente.getApellido())
                    .direccion(clienteExistente.getDireccion())
                    .foto(clienteExistente.getFoto())
                    .calificacionGlobal(clienteExistente.getCalificacionGlobal())
                    .build();
        }

        Cliente cliente = new Cliente();
        cliente.setEmail(email);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setFoto(foto);
        cliente.setDocumento(request.getDocumento() != null ?
                request.getDocumento() : "GOOG" + System.currentTimeMillis());
        cliente.setDireccion(request.getDireccion());
        cliente.setEstado(EstadoCuenta.Activo);
        cliente.setActivo(true);
        cliente.setTipo(TIPO_USUARIO_CLIENTE);
        cliente.setPasswd(passwordEncoder.encode("GOOGLEAUTH" + System.currentTimeMillis()));
        usuarioRepositorio.guardar(cliente);
        clienteRepositorio.guardar(cliente);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);
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

    private boolean cumpleRequisitosPasswd(String passwd) {
        if (passwd == null || passwd.length() < 8) {
            return false;
        }
        boolean tieneMayuscula = passwd.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = passwd.chars().anyMatch(Character::isDigit);
        return tieneMayuscula && tieneNumero;
    }
}