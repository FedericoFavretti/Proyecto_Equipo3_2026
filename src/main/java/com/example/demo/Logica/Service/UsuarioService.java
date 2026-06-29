package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.CodigoVerificacion;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.TokenRecuperacionPasswd;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.request.*;
import com.example.demo.Logica.DataTypes.response.DtLoginResponse;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseAdmin;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseCliente;
import com.example.demo.Logica.DataTypes.response.DtLoginResponseLocal;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Persistencia.Repositorios.*;
import com.example.demo.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private static final String RUTA_RESTABLECER_PASSWD = "/restablecer-contrasena";

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final long MAX_TAMANIO_FOTO_BYTES = 5L * 1024 * 1024;
    private static final Set<String> CAMPOS_EDITABLES_CLIENTE =
            Set.of("nombre", "apellido", "email", "password",
                    "direccion.calle", "direccion.numero", "direccion.ciudad", "direccion.codigoPostal");
    private static final Set<String> CAMPOS_EDITABLES_LOCAL =
            Set.of("nombre", "descripcion", "email", "password",
                    "direccion.calle", "direccion.numero", "direccion.ciudad", "direccion.codigoPostal");
    private static final Set<String> CAMPOS_EDITABLES_ADMIN =
            Set.of("email", "password");
    private static final String MENSAJE_FOTO_INVALIDA =
            "El formato de imagen no es compatible. Se aceptan archivos JPG, PNG o GIF de hasta 5 MB.";
    private static final String MENSAJE_EMAIL_DUPLICADO =
            "El correo ya está asociado a otra cuenta.";
    private static final String MENSAJE_PEDIDOS_ACTIVOS =
            "No es posible eliminar la cuenta mientras tenga pedidos en curso. Espere a que todos sus pedidos sean resueltos.";
    private static final String MENSAJE_RECLAMOS_PENDIENTES =
            "No es posible eliminar la cuenta mientras tenga reclamos pendientes de resolución.";
    private static final String MENSAJE_USUARIO_NO_AUTENTICADO = "Usuario no autenticado.";
    private static final String MENSAJE_TIPO_USUARIO_NO_COMPATIBLE =
            "El tipo de usuario no es compatible con la edición de cuenta.";
    private static final String MENSAJE_USUARIO_NO_ENCONTRADO =
            "El Usuario no fue encontrado.";
    private static final String MENSAJE_NO_SE_PUDO_INVALIDAR_SESION =
            "No se pudo invalidar la sesión actual.";
    private static final String MENSAJE_LINK_RECUPERACION_INVALIDO =
            "El enlace de recuperación ha expirado. Por favor, solicite uno nuevo.";
    private static final String MENSAJE_PASSWD_INVALIDA =
            "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.";
    private static final String MENSAJE_PASSWD_NO_COINCIDE =
            "Las contraseñas ingresadas no coinciden. Por favor, verifique e inténtelo de nuevo.";
    private static final DtDireccion DIRECCION_ANONIMIZADA =
            new DtDireccion("Anonimizada", "S/N", "N/D", "00000");

    private final UsuarioRepositorio usuarioRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final PedidoRepositorio pedidoRepositorio;
    private final ReclamoRepositorio reclamoRepositorio;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistRepositorio tokenBlacklistRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final CodigoVerificacionRepositorio codigoVerificacionRepositorio;
    private final LocalRepositorio localRepositorio;
    private final AdministradorRepositorio administradorRepositorio;

    @Autowired
    private TokenRecuperacionPasswdRepositorio tokenRecuperacionPasswdRepositorio;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, ClienteRepositorio clienteRepositorio, PedidoRepositorio pedidoRepositorio, ReclamoRepositorio reclamoRepositorio, EmailService emailService, AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService, TokenBlacklistRepositorio tokenBlacklistRepositorio, PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService, CodigoVerificacionRepositorio codigoVerificacionRepositorio, LocalRepositorio localRepositorio, AdministradorRepositorio administradorRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.reclamoRepositorio = reclamoRepositorio;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistRepositorio = tokenBlacklistRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.codigoVerificacionRepositorio = codigoVerificacionRepositorio;
        this.localRepositorio = localRepositorio;
        this.administradorRepositorio = administradorRepositorio;
    }

    @Transactional
    public DtLoginResponse login(DtLoginRequest dtLoginRequest) {
        UserDetails user = userDetailsService.loadUserByUsername(dtLoginRequest.getEmail());
        Usuario u = usuarioRepositorio.buscarPorEmail(dtLoginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dtLoginRequest.getEmail()));

        if (!u.getEstado().equals(EstadoCuenta.Activo)) {
            throw new ResourceNotFoundException("Usuario no activado o bloqueado.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dtLoginRequest.getEmail(), dtLoginRequest.getPasswd()));

        String token = jwtService.generateToken(user);

        if (u instanceof Cliente) {
            Cliente cliente = clienteRepositorio.buscarPorId(u.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", u.getId()));
            return DtLoginResponseCliente.builder()
                    .id(cliente.getId())
                    .token(token)
                    .tipo(cliente.getTipo())
                    .email(cliente.getEmail())
                    .nombre(cliente.getNombre())
                    .direccion(cliente.getDireccion())
                    .calificacionGlobal(cliente.getCalificacionGlobal())
                    .foto(cliente.getFoto())
                    .apellido(cliente.getApellido())
                    .build();
        } else if (u instanceof Local) {
            Local local = localRepositorio.buscarPorId(u.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Local", u.getId()));
            return DtLoginResponseLocal.builder()
                    .id(local.getId())
                    .token(token)
                    .tipo(local.getTipo())
                    .email(local.getEmail())
                    .nombre(local.getNombre())
                    .direccion(local.getDireccion())
                    .foto(local.getFoto())
                    .calificacionGlobal(local.getCalificacionGlobal())
                    .descripcion(local.getDescripcion())
                    .estaAbierto(local.getEstaAbierto())
                    .imagenes(local.getImagenes())
                    .build();
        } else if (u instanceof Administrador) {
            Administrador administrador = administradorRepositorio.buscarPorId(u.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin", u.getId()));
            return DtLoginResponseAdmin.builder()
                    .id(administrador.getId())
                    .token(token)
                    .tipo(administrador.getTipo())
                    .email(administrador.getEmail())
                    .foto(administrador.getFoto())
                    .nivelAcceso(administrador.getNivelAcceso())
                    .build();
        } else {
            throw new ResourceNotFoundException("Tipo de usuario no soportado para login.");
        }
    }

    @Transactional
    public void activarCuenta(String email) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
        usuarioRepositorio.activarCuenta(usuario.getId());
        clienteRepositorio.actualizar(
                clienteRepositorio.buscarPorId(usuario.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente", email))
        );
    }

    @Transactional
    public void cerrarSesion(String token) {
        String email = jwtService.extractUsername(token);
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
        invalidarSesiones(usuario);
        usuarioRepositorio.actualizar(usuario);
    }

    @Transactional
    public void recuperarPasswdPorCorreo(String correo) {
        String correoNormalizado = normalizarCorreo(correo);
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorEmail(correoNormalizado);
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();
        tokenRecuperacionPasswdRepositorio.invalidarActivosPorUsuario(usuario.getId());

        String tokenPlano = generarTokenRecuperacion();
        TokenRecuperacionPasswd tokenRecuperacion = TokenRecuperacionPasswd.builder()
                .idUsuario(usuario.getId())
                .tokenHash(hashToken(tokenPlano))
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(30))
                .fechaConsumo(null)
                .usado(false)
                .build();

        tokenRecuperacionPasswdRepositorio.guardar(tokenRecuperacion);

        try {
            emailService.recuperarPasswdPorCorreo(correoNormalizado, construirLinkRecuperacion(tokenPlano));
        } catch (Exception ex) {
            logger.error("No se pudo enviar el correo de recuperación para el usuario {}", usuario.getId(), ex);
        }
    }

    @Transactional
    public void recuperarPasswd(DtRecuperarPasswd dtRecuperarPasswd) {
        if (dtRecuperarPasswd == null) {
            throw new BusinessRuleException(MENSAJE_LINK_RECUPERACION_INVALIDO);
        }

        TokenRecuperacionPasswd tokenRecuperacion = tokenRecuperacionPasswdRepositorio
                .buscarVigentePorTokenHash(hashToken(dtRecuperarPasswd.getToken()))
                .orElseThrow(() -> new BusinessRuleException(MENSAJE_LINK_RECUPERACION_INVALIDO));

        if (Boolean.TRUE.equals(tokenRecuperacion.getUsado())
                || LocalDateTime.now().isAfter(tokenRecuperacion.getFechaExpiracion())) {
            throw new BusinessRuleException(MENSAJE_LINK_RECUPERACION_INVALIDO);
        }

        if (!cumpleRequisitosPasswd(dtRecuperarPasswd.getNuevaPasswd())) {
            throw new BusinessRuleException(MENSAJE_PASSWD_INVALIDA);
        }

        if (!dtRecuperarPasswd.getNuevaPasswd().equals(dtRecuperarPasswd.getConfirmacionPasswd())) {
            throw new BusinessRuleException(MENSAJE_PASSWD_NO_COINCIDE);
        }

        Usuario usuario = usuarioRepositorio.buscarPorId(tokenRecuperacion.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", tokenRecuperacion.getIdUsuario()));
        usuario.setPasswd(passwordEncoder.encode(dtRecuperarPasswd.getNuevaPasswd()));
        invalidarSesiones(usuario);
        usuarioRepositorio.actualizar(usuario);
        tokenRecuperacionPasswdRepositorio.marcarComoUsado(tokenRecuperacion.getId(), LocalDateTime.now());
    }

    @Transactional
    public void editarDatosDeCuentaDeUsuario(String emailAutenticado, String authHeader, Map<String, String> datos, MultipartFile foto) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException(MENSAJE_USUARIO_NO_AUTENTICADO);
        }

        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailAutenticado));

        Map<String, String> datosActualizacion = datos == null ? Map.of() : datos;
        validarCamposPermitidos(usuario, datosActualizacion);

        boolean credencialesActualizadas = aplicarCambiosComunes(usuario, datosActualizacion);

        if (usuario instanceof Cliente cliente) {
            aplicarCambiosCliente(cliente, datosActualizacion);
        } else if (usuario instanceof Local local) {
            aplicarCambiosLocal(local, datosActualizacion);
        }
        if (foto != null && !foto.isEmpty()) {
            validarFoto(foto);
            usuario.setFoto(cloudinaryService.subirImagen(foto));
        }

        if (credencialesActualizadas) {
            invalidarSesiones(usuario);
        }

        usuarioRepositorio.actualizar(usuario);
    }

    @Transactional
    public void eliminarMiCuenta(String emailAutenticado) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorEmail(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (!(usuario instanceof Cliente cliente)) {
            throw new IllegalStateException("Solo los clientes pueden eliminar su propia cuenta.");
        }

        Long idCliente = cliente.getId();

        if (pedidoRepositorio.existePedidoActivoPorCliente(idCliente)) {
            throw new IllegalStateException(MENSAJE_PEDIDOS_ACTIVOS);
        }

        if (reclamoRepositorio.existeReclamoPendientePorCliente(idCliente)) {
            throw new IllegalStateException(MENSAJE_RECLAMOS_PENDIENTES);
        }

        anonimizarCliente(cliente);
        usuarioRepositorio.actualizar(cliente);
    }

    @Transactional
    public void iniciarCambioPasswd(DtIniciarCambioPasswdRequest request) {
        if (request == null || request.getIdUsuario() == null || request.getPasswdActual() == null) {
            throw new BusinessRuleException("Debe indicar el usuario y la contraseña actual.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorId(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_USUARIO_NO_ENCONTRADO));

        if (!passwordEncoder.matches(request.getPasswdActual(), usuario.getPasswd())) {
            throw new BusinessRuleException("La contraseña actual ingresada es incorrecta.");
        }

        String codigo = generarCodigoNumerico();

        CodigoVerificacion codigoVerificacion = CodigoVerificacion.builder()
                .idUsuario(usuario.getId())
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .intentosFallidos(0)
                .bloqueadoHasta(null)
                .usado(false)
                .build();

        codigoVerificacionRepositorio.guardar(codigoVerificacion);
        emailService.enviarCodigoVerificacion(usuario.getEmail(), codigo);
    }

    @Transactional
    public void verificarCodigoCambioPasswd(DtVerificarCodigoRequest request) {
        if (request == null || request.getIdUsuario() == null || request.getCodigo() == null) {
            throw new IllegalArgumentException("Debe indicar el usuario y el código de verificación.");
        }

        CodigoVerificacion codigoVerificacion = codigoVerificacionRepositorio
                .buscarVigentePorUsuario(request.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("No hay ningún código de verificación pendiente. Solicite uno nuevo."));

        if (codigoVerificacion.getBloqueadoHasta() != null
                && LocalDateTime.now().isBefore(codigoVerificacion.getBloqueadoHasta())) {
            throw new IllegalArgumentException("Ha superado el número de intentos permitidos. Intente nuevamente en 15 minutos.");
        }

        if (LocalDateTime.now().isAfter(codigoVerificacion.getFechaExpiracion())) {
            throw new IllegalArgumentException("El código de verificación ha expirado. Solicite uno nuevo.");
        }

        if (!codigoVerificacion.getCodigo().equals(request.getCodigo())) {
            int intentos = codigoVerificacion.getIntentosFallidos() + 1;
            codigoVerificacion.setIntentosFallidos(intentos);

            if (intentos >= 3) {
                codigoVerificacion.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
                codigoVerificacionRepositorio.actualizar(codigoVerificacion);
                throw new IllegalArgumentException("Ha superado el número de intentos permitidos. Intente nuevamente en 15 minutos.");
            }

            codigoVerificacionRepositorio.actualizar(codigoVerificacion);
            throw new IllegalArgumentException("El código ingresado es incorrecto. Intentos restantes: " + (3 - intentos));
        }
    }

    @Transactional
    public void confirmarCambioPasswd(DtConfirmarCambioPasswdRequest request) {
        if (request == null || request.getIdUsuario() == null
                || request.getPasswdNueva() == null || request.getPasswdConfirmacion() == null) {
            throw new BusinessRuleException("Debe completar la nueva contraseña y su confirmación.");
        }

        CodigoVerificacion codigoVerificacion = codigoVerificacionRepositorio
                .buscarVigentePorUsuario(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("CodigoVerificacion", request.getIdUsuario()));

        if (codigoVerificacion.getIntentosFallidos() >= 3) {
            throw new BusinessRuleException("No se puede continuar: se superó el número de intentos permitidos.");
        }

        if (LocalDateTime.now().isAfter(codigoVerificacion.getFechaExpiracion())) {
            throw new BusinessRuleException("El código de verificación ha expirado. Solicite uno nuevo.");
        }

        if (!request.getPasswdNueva().equals(request.getPasswdConfirmacion())) {
            throw new BusinessRuleException("Las contraseñas ingresadas no coinciden.");
        }

        if (!cumpleRequisitosPasswd(request.getPasswdNueva())) {
            throw new BusinessRuleException("La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorId(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(MENSAJE_USUARIO_NO_ENCONTRADO));

        String passwdCodificada = passwordEncoder.encode(request.getPasswdNueva());
        usuarioRepositorio.actualizarPasswd(usuario.getId(), passwdCodificada);

        codigoVerificacion.setUsado(true);
        codigoVerificacionRepositorio.actualizar(codigoVerificacion);

        emailService.enviarConfirmacionCambioPasswd(usuario.getEmail());
    }

    private void validarCamposPermitidos(Usuario usuario, Map<String, String> datosActualizacion) {
        Set<String> camposPermitidos = obtenerCamposPermitidos(usuario);
        for (String field : datosActualizacion.keySet()) {
            if (!camposPermitidos.contains(field)) {
                throw formatoInvalido(field);
            }
        }
    }

    private Set<String> obtenerCamposPermitidos(Usuario usuario) {
        if (usuario instanceof Cliente) {
            return CAMPOS_EDITABLES_CLIENTE;
        }
        if (usuario instanceof Local) {
            return CAMPOS_EDITABLES_LOCAL;
        }
        if (usuario instanceof Administrador) {
            return CAMPOS_EDITABLES_ADMIN;
        }
        throw new BusinessRuleException(MENSAJE_TIPO_USUARIO_NO_COMPATIBLE);
    }

    private boolean aplicarCambiosComunes(Usuario usuario, Map<String, String> datosActualizacion) {
        boolean credencialesActualizadas = false;

        if (datosActualizacion.containsKey("email")) {
            String nuevoEmail = extraerTextoObligatorio(datosActualizacion, "email");
            if (!FORMATO_EMAIL.matcher(nuevoEmail).matches()) {
                throw formatoInvalido("email");
            }
            if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail()) && usuarioRepositorio.existeCorreo(nuevoEmail)) {
                throw new ResourceConflictException(MENSAJE_EMAIL_DUPLICADO);
            }
            if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
                usuario.setEmail(nuevoEmail);
                credencialesActualizadas = true;
            }
        }

        if (datosActualizacion.containsKey("password")) {
            String nuevaPassword = extraerTextoObligatorio(datosActualizacion, "password");
            usuario.setPasswd(passwordEncoder.encode(nuevaPassword));
            credencialesActualizadas = true;
        }

        return credencialesActualizadas;
    }

    private void aplicarCambiosCliente(Cliente cliente, Map<String, String> datosActualizacion) {
        if (datosActualizacion.containsKey("nombre")) {
            cliente.setNombre(extraerTextoObligatorio(datosActualizacion, "nombre"));
        }
        if (datosActualizacion.containsKey("apellido")) {
            cliente.setApellido(extraerTextoObligatorio(datosActualizacion, "apellido"));
        }
        if (tieneCambiosEnDireccion(datosActualizacion)) {
            cliente.setDireccion(mapearDireccion(datosActualizacion));
        }
    }

    private void aplicarCambiosLocal(Local local, Map<String, String> datosActualizacion) {
        if (datosActualizacion.containsKey("nombre")) {
            local.setNombre(extraerTextoObligatorio(datosActualizacion, "nombre"));
        }
        if (datosActualizacion.containsKey("descripcion")) {
            local.setDescripcion(extraerTextoObligatorio(datosActualizacion, "descripcion"));
        }
        if (tieneCambiosEnDireccion(datosActualizacion)) {
            local.setDireccion(mapearDireccion(datosActualizacion));
        }
    }

    private boolean tieneCambiosEnDireccion(Map<String, String> datosActualizacion) {
        return datosActualizacion.containsKey("direccion.calle")
                || datosActualizacion.containsKey("direccion.numero")
                || datosActualizacion.containsKey("direccion.ciudad")
                || datosActualizacion.containsKey("direccion.codigoPostal");
    }

    private DtDireccion mapearDireccion(Map<String, String> datosActualizacion) {
        String calle = extraerTextoObligatorio(datosActualizacion, "direccion.calle");
        String numero = extraerTextoObligatorio(datosActualizacion, "direccion.numero");
        String ciudad = extraerTextoObligatorio(datosActualizacion, "direccion.ciudad");
        String codigoPostal = extraerTextoObligatorio(datosActualizacion, "direccion.codigoPostal");
        return new DtDireccion(calle, numero, ciudad, codigoPostal);
    }

    private String extraerTextoObligatorio(Map<String, String> datosActualizacion, String campo) {
        String valor = datosActualizacion.get(campo);
        if (valor == null) {
            throw formatoInvalido(campo);
        }
        String texto = valor.trim();
        if (texto.isBlank()) {
            throw formatoInvalido(campo);
        }
        return texto;
    }

    private void validarFoto(MultipartFile foto) {
        String nombre = foto.getOriginalFilename();
        String contentType = foto.getContentType();
        if (foto.getSize() > MAX_TAMANIO_FOTO_BYTES
                || nombre == null
                || nombre.isBlank()
                || !extensionPermitida(nombre)
                || !contentTypePermitido(contentType)) {
            throw new BusinessRuleException(MENSAJE_FOTO_INVALIDA);
        }
    }

    private boolean extensionPermitida(String nombre) {
        String nombreNormalizado = nombre.trim().toLowerCase();
        return nombreNormalizado.endsWith(".jpg")
                || nombreNormalizado.endsWith(".jpeg")
                || nombreNormalizado.endsWith(".png")
                || nombreNormalizado.endsWith(".gif");
    }

    private boolean contentTypePermitido(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType)
                || "image/png".equalsIgnoreCase(contentType)
                || "image/gif".equalsIgnoreCase(contentType);
    }

    private void invalidarSesiones(Usuario usuario) {
        usuario.setSesionesInvalidadasDesde(LocalDateTime.now());
    }

    private BusinessRuleException formatoInvalido(String campo) {
        return new BusinessRuleException(
                "El campo " + campo + " contiene un formato inválido. Por favor, revíselo e inténtelo de nuevo.");
    }

    private void anonimizarCliente(Cliente cliente) {
        Long idCliente = cliente.getId();
        cliente.setEstado(EstadoCuenta.Bloqueado);
        cliente.setActivo(false);
        cliente.setEmail("anon-" + idCliente + "@deleted.local");
        cliente.setPasswd(passwordEncoder.encode("cuenta-eliminada-" + idCliente));
        cliente.setFoto("anonimizado");
        cliente.setNombre("Cliente eliminado");
        cliente.setApellido("");
        cliente.setDocumento("ANON-" + idCliente);
        cliente.setDireccion(DIRECCION_ANONIMIZADA);
    }

    private String normalizarCorreo(String correo) {
        if (correo == null) {
            return "";
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String generarTokenRecuperacion() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String construirLinkRecuperacion(String tokenPlano) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .replacePath(RUTA_RESTABLECER_PASSWD)
                .replaceQuery(null)
                .queryParam("token", tokenPlano)
                .build()
                .encode()
                .toUriString();
    }

    private String hashToken(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) {
            throw new BusinessRuleException(MENSAJE_LINK_RECUPERACION_INVALIDO);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenPlano.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar el hash del token de recuperación.", e);
        }
    }

    private String generarCodigoNumerico() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private boolean cumpleRequisitosPasswd(String passwd) {
        if (passwd == null) {
            return false;
        }
        if (passwd.length() < 8) {
            return false;
        }
        boolean tieneMayuscula = passwd.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = passwd.chars().anyMatch(Character::isDigit);
        return tieneMayuscula && tieneNumero;
    }
}
