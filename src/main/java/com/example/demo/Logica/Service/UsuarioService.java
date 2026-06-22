package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Usuario;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Exceptions.ResourceConflictException;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import com.example.demo.Logica.DataTypes.request.DtRecuperarPasswd;
import com.example.demo.Persistencia.Repositorios.TokenBlacklistRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.jwt.JwtService;
import com.example.demo.Logica.Clases.CodigoVerificacion;
import com.example.demo.Logica.DataTypes.request.DtIniciarCambioPasswdRequest;
import com.example.demo.Logica.DataTypes.request.DtVerificarCodigoRequest;
import com.example.demo.Logica.DataTypes.request.DtConfirmarCambioPasswdRequest;
import com.example.demo.Persistencia.Repositorios.CodigoVerificacionRepositorio;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
public class UsuarioService {
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
    private static final String MENSAJE_NO_SE_PUDO_INVALIDAR_SESION =
            "No se pudo invalidar la sesión actual.";
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

    public UsuarioService(UsuarioRepositorio usuarioRepositorio,
                          ClienteRepositorio clienteRepositorio,
                          PedidoRepositorio pedidoRepositorio,
                          ReclamoRepositorio reclamoRepositorio,
                          EmailService emailService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserDetailsService userDetailsService,
                          TokenBlacklistRepositorio tokenBlacklistRepositorio,
                          PasswordEncoder passwordEncoder,
                          CloudinaryService cloudinaryService,
                          CodigoVerificacionRepositorio codigoVerificacionRepositorio) {
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
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.email());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    @Transactional
    public void activarCuenta(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.buscarPorEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario", email);
        }
        usuarioRepositorio.activarCuenta(usuarioOpt.get().getId());
    }

    @Transactional
    public void cerrarSesion(String token) {
        LocalDateTime expiracion = jwtService.getExpiracion(token);
        tokenBlacklistRepositorio.agregar(token, expiracion);
    }

    @Transactional
    public void recuperarPasswdPorCorreo(String correo) {
        if (usuarioRepositorio.buscarPorEmail(correo).isEmpty()) {
            throw new ResourceNotFoundException("Usuario", correo);
        }

        String token = jwtService.generarTokenRecuperacion(correo);
        String link = "http://localhost:8080/api/v1/usuarios/recuperar?token=" + token;

        emailService.recuperarPasswdPorCorreo(correo, link);
    }

    @Transactional
    public void recuperarPasswd(DtRecuperarPasswd dtRecuperarPasswd){
        String correo = jwtService.validarYObtenerCorreoRecuperacion(dtRecuperarPasswd.getToken());
        Usuario usuario = usuarioRepositorio.buscarPorEmail(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", correo));
        usuario.setPasswd(passwordEncoder.encode(dtRecuperarPasswd.getNuevaPasswd()));
        usuarioRepositorio.actualizar(usuario);
    }

    @Transactional
    public void cerrarTodasLasSesiones(Long idUsuario) {
        Usuario usuario = usuarioRepositorio.buscarPorId(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario));
        usuario.setSesionesInvalidadasDesde(LocalDateTime.now());
        usuarioRepositorio.actualizar(usuario);
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
        } else if (usuario instanceof Administrador administrador) {
            aplicarCambiosAdministrador(administrador, datosActualizacion);
        }

        if (foto != null && !foto.isEmpty()) {
            validarFoto(foto);
            usuario.setFoto(cloudinaryService.subirImagen(foto));
        }

        usuarioRepositorio.actualizar(usuario);

        if (credencialesActualizadas) {
            invalidarSesionActual(authHeader);
        }
    }

    @Transactional
    public void eliminarCuentaDeUsuarioPropia(Long idCliente) {
        Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", idCliente));

        if (pedidoRepositorio.existePedidoActivoPorCliente(idCliente)) {
            throw new BusinessRuleException(MENSAJE_PEDIDOS_ACTIVOS);
        }

        if (reclamoRepositorio.existeReclamoPendientePorCliente(idCliente)) {
            throw new BusinessRuleException(MENSAJE_RECLAMOS_PENDIENTES);
        }

        anonimizarCliente(cliente);
        usuarioRepositorio.actualizar(cliente);
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

    private void aplicarCambiosAdministrador(Administrador administrador, Map<String, String> datosActualizacion) {

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

    private void invalidarSesionActual(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessRuleException(MENSAJE_NO_SE_PUDO_INVALIDAR_SESION);
        }
        cerrarSesion(authHeader.substring("Bearer ".length()));
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

    @Transactional
    public void iniciarCambioPasswd(DtIniciarCambioPasswdRequest request) {
        if (request == null || request.getIdUsuario() == null || request.getPasswdActual() == null) {
            throw new IllegalArgumentException("Debe indicar el usuario y la contraseña actual.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorId(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswdActual(), usuario.getPasswd())) {
            throw new IllegalArgumentException("La contraseña actual ingresada es incorrecta.");
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
            throw new IllegalArgumentException("Debe completar la nueva contraseña y su confirmación.");
        }

        CodigoVerificacion codigoVerificacion = codigoVerificacionRepositorio
                .buscarVigentePorUsuario(request.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("No hay ninguna verificación pendiente. Inicie el proceso nuevamente."));

        if (codigoVerificacion.getIntentosFallidos() >= 3) {
            throw new IllegalArgumentException("No se puede continuar: se superó el número de intentos permitidos.");
        }

        if (LocalDateTime.now().isAfter(codigoVerificacion.getFechaExpiracion())) {
            throw new IllegalArgumentException("El código de verificación ha expirado. Solicite uno nuevo.");
        }

        if (!request.getPasswdNueva().equals(request.getPasswdConfirmacion())) {
            throw new IllegalArgumentException("Las contraseñas ingresadas no coinciden.");
        }

        if (!cumpleRequisitosPasswd(request.getPasswdNueva())) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.");
        }

        Usuario usuario = usuarioRepositorio.buscarPorId(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String passwdCodificada = passwordEncoder.encode(request.getPasswdNueva());
        usuarioRepositorio.actualizarPasswd(usuario.getId(), passwdCodificada);

        codigoVerificacion.setUsado(true);
        codigoVerificacionRepositorio.actualizar(codigoVerificacion);

        emailService.enviarConfirmacionCambioPasswd(usuario.getEmail());
    }

    private String generarCodigoNumerico() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private boolean cumpleRequisitosPasswd(String passwd) {
        if (passwd.length() < 8) {
            return false;
        }
        boolean tieneMayuscula = passwd.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = passwd.chars().anyMatch(Character::isDigit);
        return tieneMayuscula && tieneNumero;
    }
}