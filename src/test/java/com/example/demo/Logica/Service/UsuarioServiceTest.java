package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.DataTypes.request.DtActualizarPerfilRequest;
import com.example.demo.Logica.DataTypes.shared.DtDireccion;
import com.example.demo.Logica.Enums.EstadoCuenta;
import com.example.demo.Logica.Enums.EstadoLocal;
import com.example.demo.Logica.Exceptions.ResourceNotFoundException;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Persistencia.Repositorios.AdministradorRepositorio;
import com.example.demo.Persistencia.Repositorios.CalificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.CodigoVerificacionRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PedidoRepositorio;
import com.example.demo.Persistencia.Repositorios.ReclamoRepositorio;
import com.example.demo.Persistencia.Repositorios.SolicitudCambioCorreoRepositorio;
import com.example.demo.Persistencia.Repositorios.TokenActivacionCuentaRepositorio;
import com.example.demo.Persistencia.Repositorios.TokenBlacklistRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private CodigoVerificacionRepositorio codigoVerificacionRepositorio;
    @Mock
    private LocalRepositorio localRepositorio;
    @Mock
    private AdministradorRepositorio administradorRepositorio;
    @Mock
    private CalificacionRepositorio calificacionRepositorio;
    @Mock
    private SolicitudCambioCorreoRepositorio solicitudCambioCorreoRepositorio;
    @Mock
    private ClienteMapper clienteMapper;
    @Mock
    private LocalMapper localMapper;
    @Mock
    private TokenActivacionCuentaRepositorio tokenActivacionCuentaRepositorio;

    @Test
    void editarDatosDeCuentaClienteActualizaCamposPermitidosYRevocaTokenSiCambianCredenciales() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();
        MockMultipartFile foto = new MockMultipartFile("foto", "perfil.png", "image/png", new byte[]{1, 2, 3});

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("hash-nuevo");
        when(cloudinaryService.subirImagen(foto)).thenReturn("https://cdn.foodly.com/perfil.png");

        usuarioService.editarDatosDeCuentaDeUsuario(
                "cliente@foodly.com",
                "Bearer token-actual",
                DtActualizarPerfilRequest.builder()
                        .nombre("Maria")
                        .apellido("Gomez")
                        .direccion(new DtDireccion("18 de Julio", "1234", "Montevideo", "11200"))
                        .password("NuevaClave123")
                        .build(),
                foto
        );

        assertThat(cliente.getNombre()).isEqualTo("Maria");
        assertThat(cliente.getApellido()).isEqualTo("Gomez");
        assertThat(cliente.getDireccion().getCalle()).isEqualTo("18 de Julio");
        assertThat(cliente.getPasswd()).isEqualTo("hash-nuevo");
        assertThat(cliente.getFoto()).isEqualTo("https://cdn.foodly.com/perfil.png");
        assertThat(cliente.getDocumento()).isEqualTo("51234567");
        assertThat(cliente.getActivo()).isTrue();
        assertThat(cliente.getCalificacionGlobal()).isEqualTo(4.7);

        verify(usuarioRepositorio).actualizar(cliente);
        assertThat(cliente.getSesionesInvalidadasDesde()).isNotNull();
        verifyNoInteractions(tokenBlacklistRepositorio, jwtService);
    }

    @Test
    void editarDatosDeCuentaLocalRechazaCelularConFormatoInvalido() {
        UsuarioService usuarioService = crearServicio();
        Local local = localExistente();

        when(usuarioRepositorio.buscarPorEmail("local@foodly.com")).thenReturn(Optional.of(local));

        assertThatThrownBy(() -> usuarioService.editarDatosDeCuentaDeUsuario(
                "local@foodly.com",
                "Bearer token-local",
                DtActualizarPerfilRequest.builder().celular("no-es-un-celular").build(),
                null
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessage("El campo celular contiene un formato inválido. Por favor, revíselo e inténtelo de nuevo.");

        verify(usuarioRepositorio, never()).actualizar(local);
        verifyNoInteractions(passwordEncoder, cloudinaryService, tokenBlacklistRepositorio, jwtService);
    }

    @Test
    void editarDatosDeCuentaAdministradorNoEstaSoportado() {
        UsuarioService usuarioService = crearServicio();
        Administrador administrador = administradorExistente();

        when(usuarioRepositorio.buscarPorEmail("admin@foodly.com")).thenReturn(Optional.of(administrador));
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash-admin");


        assertThatThrownBy(() -> usuarioService.editarDatosDeCuentaDeUsuario(
                "admin@foodly.com",
                "Bearer token-admin",
                DtActualizarPerfilRequest.builder().password("ClaveSegura123").build(),
                null
        )).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El tipo de usario es incorrecto");
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
                DtActualizarPerfilRequest.builder().nombre("Maria").build(),
                foto
        )).isInstanceOf(BusinessRuleException.class)
                .hasMessage("El formato de imagen no es compatible. Se aceptan archivos JPG, PNG o GIF de hasta 5 MB.");

        verify(usuarioRepositorio, never()).actualizar(cliente);
        verifyNoInteractions(cloudinaryService, tokenBlacklistRepositorio, jwtService);
    }

    @Test
    void eliminarMiCuentaAnonimizaClienteArchivaCalificacionesYRecalculaLocales() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();
        Local local = localExistente();

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(false);
        when(reclamoRepositorio.existeReclamoPendientePorCliente(10L)).thenReturn(false);
        when(calificacionRepositorio.obtenerLocalesAfectadosPorArchivoDeCliente(10L)).thenReturn(List.of(20L));
        when(localRepositorio.buscarPorId(20L)).thenReturn(Optional.of(local));
        when(calificacionRepositorio.listarPorLocal(20L)).thenReturn(List.of());
        when(passwordEncoder.encode("cuenta-eliminada-10")).thenReturn("hash-eliminada");

        usuarioService.eliminarMiCuenta("cliente@foodly.com");

        assertThat(cliente.getEstado()).isEqualTo(EstadoCuenta.Bloqueado);
        assertThat(cliente.getActivo()).isFalse();
        assertThat(cliente.getEmail()).isEqualTo("anon-10@deleted.local");
        assertThat(cliente.getPasswd()).isEqualTo("hash-eliminada");
        assertThat(cliente.getFoto()).isEqualTo("anonimizado");
        assertThat(cliente.getNombre()).isEqualTo("Cliente eliminado");
        assertThat(cliente.getApellido()).isEmpty();
        assertThat(cliente.getDocumento()).isEqualTo("ANON-10");
        assertThat(cliente.getDireccion()).isEqualTo(new DtDireccion("Anonimizada", "S/N", "N/D", "00000"));
        assertThat(cliente.getCalificacionGlobal()).isZero();
        assertThat(cliente.getSesionesInvalidadasDesde()).isNotNull();
        assertThat(local.getCalificacionGlobal()).isZero();

        verify(calificacionRepositorio).archivarPorCliente(10L);
        verify(localRepositorio).actualizar(local);
        verify(usuarioRepositorio).actualizar(cliente);
        verifyNoInteractions(jwtService, tokenBlacklistRepositorio, cloudinaryService);
    }

    @Test
    void eliminarMiCuentaRechazaSiTienePedidosActivos() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarMiCuenta("cliente@foodly.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No es posible eliminar la cuenta mientras tenga pedidos en curso. Espere a que todos sus pedidos sean resueltos.");

        verify(reclamoRepositorio, never()).existeReclamoPendientePorCliente(10L);
        verify(calificacionRepositorio, never()).archivarPorCliente(10L);
        verify(usuarioRepositorio, never()).actualizar(cliente);
    }

    @Test
    void eliminarMiCuentaRechazaSiTieneReclamosPendientes() {
        UsuarioService usuarioService = crearServicio();
        Cliente cliente = clienteExistente();

        when(usuarioRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(cliente));
        when(pedidoRepositorio.existePedidoActivoPorCliente(10L)).thenReturn(false);
        when(reclamoRepositorio.existeReclamoPendientePorCliente(10L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.eliminarMiCuenta("cliente@foodly.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No es posible eliminar la cuenta mientras tenga reclamos pendientes de resolución.");

        verify(calificacionRepositorio, never()).archivarPorCliente(10L);
        verify(usuarioRepositorio, never()).actualizar(cliente);
    }

    @Test
    void eliminarMiCuentaFallaSiElUsuarioNoExiste() {
        UsuarioService usuarioService = crearServicio();

        when(usuarioRepositorio.buscarPorEmail("desconocido@foodly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.eliminarMiCuenta("desconocido@foodly.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado.");

        verifyNoInteractions(pedidoRepositorio, reclamoRepositorio, passwordEncoder, calificacionRepositorio);
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
                cloudinaryService,
                codigoVerificacionRepositorio,
                localRepositorio,
                administradorRepositorio,
                calificacionRepositorio,
                solicitudCambioCorreoRepositorio,
                clienteMapper,
                localMapper,
                tokenActivacionCuentaRepositorio
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
