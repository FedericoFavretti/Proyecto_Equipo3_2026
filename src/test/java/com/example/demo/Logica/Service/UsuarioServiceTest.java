package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.response.DtPerfilAdminResponse;
import com.example.demo.Logica.DataTypes.response.DtPerfilClienteResponse;
import com.example.demo.Logica.DataTypes.response.DtPerfilLocalResponse;
import com.example.demo.Logica.DataTypes.response.DtPerfilResponse;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import com.example.demo.Persistencia.Repositorios.TokenBlacklistRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private ClienteRepositorio clienteRepositorio;
    @Mock
    private PedidoRepositorio pedidoRepositorio;
    @Mock
    private ReclamoRepositorio reclamoRepositorio;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private TokenBlacklistRepositorio tokenBlacklistRepositorio;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CloudinaryService cloudinaryService;

    @Test
    void loginDevuelveTokenYUsuarioInfo() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();
        User userDetails = new User(
                "cliente@foodly.com",
                "hash-anterior",
                List.of(new SimpleGrantedAuthority("ROLE_cliente"))
        );

        when(userDetailsService.loadUserByUsername("cliente@foodly.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));

        AuthResponse response = usuarioService.login(new LoginRequest("cliente@foodly.com", "Clave123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.usuario()).isNotNull();
        assertThat(response.usuario().id()).isEqualTo(10L);
        assertThat(response.usuario().email()).isEqualTo("cliente@foodly.com");
        assertThat(response.usuario().tipo()).isEqualTo("cliente");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("cliente@foodly.com");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void obtenerPerfilClienteDevuelveSoloDatosSegurosYEspecificosDelCliente() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));

        DtPerfilResponse perfil = usuarioService.obtenerPerfil("cliente@foodly.com");

        assertThat(perfil.getId()).isEqualTo(10L);
        assertThat(perfil.getEmail()).isEqualTo("cliente@foodly.com");
        assertThat(perfil.getTipo()).isEqualTo("cliente");
        assertThat(perfil.getPerfil()).isInstanceOf(DtPerfilClienteResponse.class);

        DtPerfilClienteResponse detalle = (DtPerfilClienteResponse) perfil.getPerfil();
        assertThat(detalle.getNombre()).isEqualTo("Ana");
        assertThat(detalle.getApellido()).isEqualTo("Perez");
        assertThat(detalle.getDocumento()).isEqualTo("51234567");
        assertThat(detalle.getDireccion()).isEqualTo(new DtDireccion("Colonia", "100", "Montevideo", "11100"));
        assertThat(detalle.getActivo()).isTrue();
    }

    @Test
    void obtenerPerfilLocalDevuelveCamposEspecificosDelLocal() {
        UsuarioService usuarioService = crearServicio();
        Local local = localExistente();

        when(usuarioRepositorio.buscarPorEmail("local@foodly.com")).thenReturn(Optional.of(local));

        DtPerfilResponse perfil = usuarioService.obtenerPerfil("local@foodly.com");

        assertThat(perfil.getId()).isEqualTo(20L);
        assertThat(perfil.getEmail()).isEqualTo("local@foodly.com");
        assertThat(perfil.getTipo()).isEqualTo("local");
        assertThat(perfil.getPerfil()).isInstanceOf(DtPerfilLocalResponse.class);

        DtPerfilLocalResponse detalle = (DtPerfilLocalResponse) perfil.getPerfil();
        assertThat(detalle.getNombre()).isEqualTo("La Cocina");
        assertThat(detalle.getDescripcion()).isEqualTo("Comida casera");
        assertThat(detalle.getEstadoLocal()).isEqualTo(EstadoLocal.Habilitado);
        assertThat(detalle.getEstaAbierto()).isTrue();
        assertThat(detalle.getImagenes()).containsExactly("fachada.jpg");
    }

    @Test
    void obtenerPerfilAdministradorDevuelveNivelAcceso() {
        UsuarioService usuarioService = crearServicio();
        Administrador administrador = administradorExistente();

        when(usuarioRepositorio.buscarPorEmail("admin@foodly.com")).thenReturn(Optional.of(administrador));

        DtPerfilResponse perfil = usuarioService.obtenerPerfil("admin@foodly.com");

        assertThat(perfil.getId()).isEqualTo(30L);
        assertThat(perfil.getEmail()).isEqualTo("admin@foodly.com");
        assertThat(perfil.getTipo()).isEqualTo("admin");
        assertThat(perfil.getPerfil()).isInstanceOf(DtPerfilAdminResponse.class);

        DtPerfilAdminResponse detalle = (DtPerfilAdminResponse) perfil.getPerfil();
        assertThat(detalle.getNivelAcceso()).isEqualTo("super");
    }

    @Test
    void editarDatosDeCuentaClienteActualizaCamposPermitidosYRevocaTokenSiCambianCredenciales() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();
        MockMultipartFile foto = new MockMultipartFile("foto", "perfil.png", "image/png", new byte[]{1, 2, 3});
        LocalDateTime expiracion = LocalDateTime.of(2026, 6, 17, 12, 0);

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));
        when(usuarioRepositorio.existeCorreo("nuevo@foodly.com")).thenReturn(false);
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("hash-nuevo");
        when(cloudinaryService.subirImagen(foto)).thenReturn("https://cdn.foodly.com/perfil.png");
        when(jwtService.getExpiracion("token-actual")).thenReturn(expiracion);

        usuarioService.editarDatosDeCuentaDeUsuario(
                "cliente@foodly.com",
                "Bearer token-actual",
                Map.of(
                        "nombre", "Maria",
                        "apellido", "Gomez",
                        "direccion.calle", "18 de Julio",
                        "direccion.numero", "1234",
                        "direccion.ciudad", "Montevideo",
                        "direccion.codigoPostal", "11200",
                        "email", "nuevo@foodly.com",
                        "password", "NuevaClave123"
                ),
                foto
        );

        assertThat(cliente.getNombre()).isEqualTo("Maria");
        assertThat(cliente.getApellido()).isEqualTo("Gomez");
        assertThat(cliente.getDireccion().getCalle()).isEqualTo("18 de Julio");
        assertThat(cliente.getEmail()).isEqualTo("nuevo@foodly.com");
        assertThat(cliente.getPasswd()).isEqualTo("hash-nuevo");
        assertThat(cliente.getFoto()).isEqualTo("https://cdn.foodly.com/perfil.png");
        assertThat(cliente.getDocumento()).isEqualTo("51234567");
        assertThat(cliente.getActivo()).isTrue();
        assertThat(cliente.getCalificacionGlobal()).isEqualTo(4.7);

        verify(usuarioRepositorio).actualizar(cliente);
        verify(tokenBlacklistRepositorio).agregar("token-actual", expiracion);
    }

    @Test
    void editarDatosDeCuentaLocalRechazaCamposFueraDeWhitelist() {
        UsuarioService usuarioService = crearServicio();
        Local local = localExistente();

        when(usuarioRepositorio.buscarPorEmail("local@foodly.com")).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> usuarioService.editarDatosDeCuentaDeUsuario(
                "local@foodly.com",
                "Bearer token-local",
                Map.of("estadoLocal", "Bloqueado"),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El campo estadoLocal contiene un formato inválido. Por favor, revíselo e inténtelo de nuevo.");

        verify(usuarioRepositorio, never()).actualizar(local);
        verifyNoInteractions(passwordEncoder, cloudinaryService, tokenBlacklistRepositorio, jwtService);
    }

    @Test
    void editarDatosDeCuentaAdministradorActualizaCamposBaseYConservaNivelAcceso() {
        UsuarioService usuarioService = crearServicio();
        Administrador administrador = administradorExistente();
        LocalDateTime expiracion = LocalDateTime.of(2026, 6, 17, 13, 0);

        when(usuarioRepositorio.buscarPorEmail("admin@foodly.com")).thenReturn(Optional.of(administrador));
        when(usuarioRepositorio.existeCorreo("admin2@foodly.com")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash-admin");
        when(jwtService.getExpiracion("token-admin")).thenReturn(expiracion);

        usuarioService.editarDatosDeCuentaDeUsuario(
                "admin@foodly.com",
                "Bearer token-admin",
                Map.of(
                        "email", "admin2@foodly.com",
                        "password", "ClaveSegura123"
                ),
                null
        );

        assertThat(administrador.getEmail()).isEqualTo("admin2@foodly.com");
        assertThat(administrador.getPasswd()).isEqualTo("hash-admin");
        assertThat(administrador.getNivelAcceso()).isEqualTo("super");

        verify(usuarioRepositorio).actualizar(administrador);
        verify(tokenBlacklistRepositorio).agregar("token-admin", expiracion);
    }

    @Test
    void editarDatosDeCuentaRechazaFotoInvalida() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();
        MockMultipartFile foto = new MockMultipartFile("foto", "perfil.bmp", "image/bmp", new byte[]{1, 2, 3});

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> usuarioService.editarDatosDeCuentaDeUsuario(
                "cliente@foodly.com",
                "Bearer token-actual",
                Map.of("nombre", "Maria"),
                foto
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El formato de imagen no es compatible. Se aceptan archivos JPG, PNG o GIF de hasta 5 MB.");

        verify(usuarioRepositorio, never()).actualizar(cliente);
        verifyNoInteractions(cloudinaryService, tokenBlacklistRepositorio, jwtService);
    }

    @Test
    void eliminarCuentaPropiaAnonimizaClienteYLoBloqueaSiNoTienePedidosActivosNiReclamosPendientes() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(clienteRepositorio.buscarPorId(10L)).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(false);
        when(reclamoRepositorio.existeReclamoPendientePorCliente(10L)).thenReturn(false);
        when(passwordEncoder.encode("cuenta-eliminada-10")).thenReturn("hash-eliminada");

        usuarioService.eliminarCuentaDeUsuarioPropia(10L);

        assertThat(cliente.getEstado()).isEqualTo(EstadoCuenta.Bloqueado);
        assertThat(cliente.getActivo()).isFalse();
        assertThat(cliente.getEmail()).isEqualTo("anon-10@deleted.local");
        assertThat(cliente.getPasswd()).isEqualTo("hash-eliminada");
        assertThat(cliente.getFoto()).isEqualTo("anonimizado");
        assertThat(cliente.getNombre()).isEqualTo("Cliente eliminado");
        assertThat(cliente.getApellido()).isEmpty();
        assertThat(cliente.getDocumento()).isEqualTo("ANON-10");
        assertThat(cliente.getDireccion()).isEqualTo(new DtDireccion("Anonimizada", "S/N", "N/D", "00000"));
        assertThat(cliente.getCalificacionGlobal()).isEqualTo(4.7);

        verify(usuarioRepositorio).actualizar(cliente);
        verifyNoInteractions(jwtService, tokenBlacklistRepositorio, cloudinaryService);
    }

    @Test
    void eliminarCuentaPropiaRechazaSiTienePedidosActivos() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(clienteRepositorio.buscarPorId(10L)).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarCuentaDeUsuarioPropia(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No es posible eliminar la cuenta mientras tenga pedidos en curso. Espere a que todos sus pedidos sean resueltos.");

        verify(reclamoRepositorio, never()).existeReclamoPendientePorCliente(10L);
        verify(usuarioRepositorio, never()).actualizar(cliente);
    }

    @Test
    void eliminarCuentaPropiaRechazaSiTieneReclamosPendientes() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(clienteRepositorio.buscarPorId(10L)).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(false);
        when(reclamoRepositorio.existeReclamoPendientePorCliente(10L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarCuentaDeUsuarioPropia(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No es posible eliminar la cuenta mientras tenga reclamos pendientes de resolución.");

        verify(usuarioRepositorio, never()).actualizar(cliente);
    }

    @Test
    void eliminarCuentaPropiaFallaSiElClienteNoExiste() {
        UsuarioService usuarioService = crearServicio();

        when(clienteRepositorio.buscarPorId(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.eliminarCuentaDeUsuarioPropia(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente no encontrado.");

        verifyNoInteractions(pedidoRepositorio, reclamoRepositorio, passwordEncoder, usuarioRepositorio);
    }

    private UsuarioService crearServicio() {
        return new UsuarioService(
                usuarioRepositorio,
                clienteRepositorio,
                pedidoRepositorio,
                reclamoRepositorio,
                emailService,
                authenticationManager,
                jwtService,
                userDetailsService,
                tokenBlacklistRepositorio,
                passwordEncoder,
                cloudinaryService
        );
    }

    private Cliente clienteExistente() {
        return Cliente.builder()
                .id(10L)
                .email("cliente@foodly.com")
                .passwd("hash-anterior")
                .foto("https://cdn.foodly.com/vieja.png")
                .estado(EstadoCuenta.Activo)
                .tipo("cliente")
                .documento("51234567")
                .nombre("Ana")
                .apellido("Perez")
                .direccion(new DtDireccion("Colonia", "100", "Montevideo", "11100"))
                .calificacionGlobal(4.7)
                .activo(true)
                .build();
    }

    private Local localExistente() {
        return Local.builder()
                .id(20L)
                .email("local@foodly.com")
                .passwd("hash-local")
                .foto("https://cdn.foodly.com/local.png")
                .estado(EstadoCuenta.Activo)
                .tipo("local")
                .nombre("La Cocina")
                .direccion(new DtDireccion("Rivera", "200", "Montevideo", "11200"))
                .descripcion("Comida casera")
                .estadoLocal(EstadoLocal.Habilitado)
                .calificacionGlobal(4.4)
                .estaAbierto(true)
                .imagenes(List.of("fachada.jpg"))
                .build();
    }

    private Administrador administradorExistente() {
        Administrador administrador = new Administrador();
        administrador.setId(30L);
        administrador.setEmail("admin@foodly.com");
        administrador.setPasswd("hash-admin-anterior");
        administrador.setFoto("https://cdn.foodly.com/admin.png");
        administrador.setEstado(EstadoCuenta.Activo);
        administrador.setTipo("admin");
        administrador.setNivelAcceso("super");
        return administrador;
    }
}
