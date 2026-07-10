package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.request.DtGoogleAuthRequest;
import com.example.demo.Logica.DataTypes.request.DtGoogleRegistroCompletarRequest;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.response.DtGoogleRegistroPendienteResponse;
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
import com.example.demo.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositorio clienteRepositorio;

    @Mock
    private PlatoRepositorio platoRepositorio;

    @Mock
    private PromocionRepositorio promocionRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private PlatoMapper platoMapper;

    @Mock
    private PromocionMapper promocionMapper;

    @Mock
    private LocalRepositorio localRepositorio;

    @Mock
    private LocalMapper localMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private GoogleIdentityService googleIdentityService;

    @Mock
    private TokenActivacionCuentaRepositorio tokenActivacionCuentaRepositorio;

    @Mock
    private UserDetails userDetails;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(
                clienteRepositorio,
                platoRepositorio,
                promocionRepositorio,
                usuarioRepositorio,
                emailService,
                passwordEncoder,
                clienteMapper,
                platoMapper,
                promocionMapper,
                localRepositorio,
                localMapper,
                jwtService,
                userDetailsService,
                googleIdentityService,
                tokenActivacionCuentaRepositorio
        );
        // "activationFrontendBaseUrl" es un campo @Value que Spring solo inyecta
        // en tiempo de ejecución real; en un test unitario hay que setearlo a mano.
        ReflectionTestUtils.setField(clienteService, "activationFrontendBaseUrl", "https://foodly.com.uy");
    }

    private DtCliente dtClienteConPasswd(String passwd) {
        return DtCliente.builder()
                .email("nuevo.cliente@foodly.com")
                .passwd(passwd)
                .documento("12345678")
                .nombre("Ana")
                .apellido("Pérez")
                .build();
    }

    @Test
    void registrarUsuarioRechazaContraseñaCortaSinMayusculaNiNumero() {
        DtCliente dtCliente = dtClienteConPasswd("123");

        assertThatThrownBy(() -> clienteService.registrarUsuario(dtCliente))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.");

        verify(usuarioRepositorio, never()).guardar(any());
        verify(clienteRepositorio, never()).guardar(any());
    }

    @Test
    void registrarUsuarioRechazaContraseñaSinMayuscula() {
        DtCliente dtCliente = dtClienteConPasswd("password1");

        assertThatThrownBy(() -> clienteService.registrarUsuario(dtCliente))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.");
    }

    @Test
    void registrarUsuarioRechazaContraseñaSinNumero() {
        DtCliente dtCliente = dtClienteConPasswd("Password");

        assertThatThrownBy(() -> clienteService.registrarUsuario(dtCliente))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.");
    }

    @Test
    void registrarUsuarioAceptaContraseñaQueCumpleRequisitos() {
        DtCliente dtCliente = dtClienteConPasswd("Password1");
        Cliente cliente = new Cliente();

        when(usuarioRepositorio.existeCorreo(dtCliente.getEmail())).thenReturn(false);
        when(clienteRepositorio.existeDocumento(dtCliente.getDocumento())).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hash-encriptado");
        when(clienteMapper.mapearClienteDeDt(dtCliente)).thenReturn(cliente);

        Cliente resultado = clienteService.registrarUsuario(dtCliente);

        assertThat(resultado).isSameAs(cliente);
        assertThat(resultado.getPasswd()).isEqualTo("hash-encriptado");
    }

    @Test
    void iniciarRegistroConGoogleRetornaTokenTemporalCuandoCorreoNoExiste() {
        DtGoogleAuthRequest request = new DtGoogleAuthRequest("token-google", null, null, true);
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("nuevo@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();

        when(googleIdentityService.obtenerDatosUsuario("token-google")).thenReturn(googleUserInfo);
        when(usuarioRepositorio.existeCorreo("nuevo@foodly.com")).thenReturn(false);
        when(jwtService.generarTokenRegistroGoogle(googleUserInfo)).thenReturn("registro-temporal");

        DtGoogleRegistroPendienteResponse response = clienteService.iniciarRegistroConGoogle(request);

        assertThat(response.getTokenRegistro()).isEqualTo("registro-temporal");
        assertThat(response.getEmail()).isEqualTo("nuevo@foodly.com");
        assertThat(response.getNombre()).isEqualTo("Ana");
        assertThat(response.getApellido()).isEqualTo("Pérez");
        assertThat(response.getFoto()).isEqualTo("https://googleusercontent.com/ana.png");
        verify(usuarioRepositorio, never()).guardar(any());
        verify(clienteRepositorio, never()).guardar(any());
    }

    @Test
    void iniciarRegistroConGoogleRechazaCorreoExistente() {
        DtGoogleAuthRequest request = new DtGoogleAuthRequest("token-google", null, null, true);
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("existente@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();

        when(googleIdentityService.obtenerDatosUsuario("token-google")).thenReturn(googleUserInfo);
        when(usuarioRepositorio.existeCorreo("existente@foodly.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.iniciarRegistroConGoogle(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("El correo existente@foodly.com ya está asociado a una cuenta existente. ¿Desea iniciar sesión en su lugar?");
    }

    @Test
    void completarRegistroConGoogleCreaCuentaActivaYDevuelveTokenFinal() {
        DtGoogleRegistroCompletarRequest request = DtGoogleRegistroCompletarRequest.builder()
                .tokenRegistro("registro-temporal")
                .documento("51234567")
                .direccion(new DtDireccion("18 de Julio", "1234", "Montevideo", "11200"))
                .aceptaTerminos(true)
                .foto("https://cdn.foodly.com/perfil-google.png")
                .build();
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("nuevo@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();

        when(jwtService.validarYObtenerDatosRegistroGoogle("registro-temporal")).thenReturn(googleUserInfo);
        when(usuarioRepositorio.existeCorreo("nuevo@foodly.com")).thenReturn(false);
        when(clienteRepositorio.existeDocumento("51234567")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash-google");
        when(userDetailsService.loadUserByUsername("nuevo@foodly.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-final");

        DtLoginResponseCliente response = clienteService.completarRegistroConGoogle(request);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(usuarioRepositorio).guardar(clienteCaptor.capture());
        verify(clienteRepositorio).guardar(clienteCaptor.getValue());

        Cliente clienteGuardado = clienteCaptor.getValue();
        assertThat(clienteGuardado.getEmail()).isEqualTo("nuevo@foodly.com");
        assertThat(clienteGuardado.getNombre()).isEqualTo("Ana");
        assertThat(clienteGuardado.getApellido()).isEqualTo("Pérez");
        assertThat(clienteGuardado.getDocumento()).isEqualTo("51234567");
        assertThat(clienteGuardado.getDireccion()).isEqualTo(request.getDireccion());
        assertThat(clienteGuardado.getFoto()).isEqualTo("https://cdn.foodly.com/perfil-google.png");
        assertThat(clienteGuardado.getEstado()).isEqualTo(EstadoCuenta.Activo);
        assertThat(clienteGuardado.getActivo()).isTrue();
        assertThat(clienteGuardado.getTipo()).isEqualTo("cliente");
        assertThat(clienteGuardado.getCalificacionGlobal()).isZero();

        assertThat(response.getToken()).isEqualTo("jwt-final");
        assertThat(response.getEmail()).isEqualTo("nuevo@foodly.com");
        assertThat(response.getNombre()).isEqualTo("Ana");
        assertThat(response.getApellido()).isEqualTo("Pérez");
        assertThat(response.getDireccion()).isEqualTo(request.getDireccion());
        assertThat(response.getFoto()).isEqualTo("https://cdn.foodly.com/perfil-google.png");
    }

    @Test
    void completarRegistroConGoogleRechazaCamposObligatoriosFaltantes() {
        DtGoogleRegistroCompletarRequest request = DtGoogleRegistroCompletarRequest.builder()
                .tokenRegistro("registro-temporal")
                .aceptaTerminos(true)
                .build();
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("nuevo@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .build();

        when(jwtService.validarYObtenerDatosRegistroGoogle("registro-temporal")).thenReturn(googleUserInfo);

        assertThatThrownBy(() -> clienteService.completarRegistroConGoogle(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Los siguientes campos son requeridos: documento, dirección, foto de perfil. Por favor, complételos para finalizar el registro.");

        verify(usuarioRepositorio, never()).guardar(any());
        verify(clienteRepositorio, never()).guardar(any());
    }

    @Test
    void loginConGoogleAutenticaClienteExistente() {
        DtGoogleAuthRequest request = new DtGoogleAuthRequest("token-google", null, null, false);
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("cliente@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();
        Cliente clienteExistente = Cliente.builder()
                .id(10L)
                .email("cliente@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .documento("51234567")
                .direccion(new DtDireccion("18 de Julio", "1234", "Montevideo", "11200"))
                .foto("https://cdn.foodly.com/ana.png")
                .estado(EstadoCuenta.Activo)
                .tipo("cliente")
                .calificacionGlobal(4.8)
                .activo(true)
                .build();

        when(googleIdentityService.obtenerDatosUsuario("token-google")).thenReturn(googleUserInfo);
        when(clienteRepositorio.buscarPorEmail("cliente@foodly.com")).thenReturn(Optional.of(clienteExistente));
        when(userDetailsService.loadUserByUsername("cliente@foodly.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-login");

        DtLoginResponseCliente response = clienteService.loginConGoogle(request);

        assertThat(response.getToken()).isEqualTo("jwt-login");
        assertThat(response.getEmail()).isEqualTo("cliente@foodly.com");
        assertThat(response.getApellido()).isEqualTo("Pérez");
        assertThat(response.getDireccion()).isEqualTo(clienteExistente.getDireccion());
    }

    @Test
    void loginConGoogleRechazaSiNoExisteCuenta() {
        DtGoogleAuthRequest request = new DtGoogleAuthRequest("token-google", null, null, false);
        DtGoogleUserInfo googleUserInfo = DtGoogleUserInfo.builder()
                .email("desconocido@foodly.com")
                .nombre("Ana")
                .apellido("Pérez")
                .foto("https://googleusercontent.com/ana.png")
                .build();

        when(googleIdentityService.obtenerDatosUsuario("token-google")).thenReturn(googleUserInfo);
        when(clienteRepositorio.buscarPorEmail("desconocido@foodly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.loginConGoogle(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No existe una cuenta de cliente asociada al correo desconocido@foodly.com. Regístrese con Google para continuar.");
    }

    @Test
    void buscarPlatosYPromocionesRetornaAmbosResultados() {
        DtFiltro filtro = DtFiltro.builder()
                .nombre("Mil")
                .promocionActiva(true)
                .build();

        Plato plato = Plato.builder()
                .id(10L)
                .nombre("Milanesa")
                .precio(15.0)
                .local(Local.builder().id(5L).build())
                .build();
        Promocion promocion = Promocion.builder()
                .id(20L)
                .descripcion("2x1")
                .fechaInicio(LocalDateTime.now())
                .fechaFin(LocalDateTime.now().plusDays(1))
                .plato(plato)
                .build();

        DtPlato dtPlato = DtPlato.builder()
                .id(10L)
                .nombre("Milanesa")
                .precio(15.0)
                .build();
        DtPromocion dtPromocion = DtPromocion.builder()
                .id(20L)
                .descripcion("2x1")
                .descuento(25.0)
                .dtPlato(dtPlato)
                .build();

        when(platoRepositorio.buscarConFiltros(filtro)).thenReturn(List.of(plato));
        when(promocionRepositorio.buscarActivasConFiltros(filtro)).thenReturn(List.of(promocion));
        when(platoMapper.mapearDtPlatoDeClase(plato)).thenReturn(dtPlato);
        when(promocionMapper.mapearDtPromocionDeClase(promocion)).thenReturn(dtPromocion);

        DtBusquedaPlatosPromocionesResponse response = clienteService.buscarPlatosYPromociones(filtro, null, null);

        assertThat(response.getPlatos()).containsExactly(dtPlato);
        assertThat(response.getPromociones()).containsExactly(dtPromocion);
    }

    @Test
    void buscarPlatosYPromocionesRechazaCuandoNoHayResultados() {
        DtFiltro filtro = DtFiltro.builder().nombre("Inexistente").build();

        when(platoRepositorio.buscarConFiltros(filtro)).thenReturn(List.of());
        when(promocionRepositorio.buscarActivasConFiltros(filtro)).thenReturn(List.of());

        assertThatThrownBy(() -> clienteService.buscarPlatosYPromociones(filtro, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontraron platos o promociones que coincidan con su búsqueda.");
    }
}
