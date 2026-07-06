package com.example.demo.Logica.Service;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Clases.Plato;
import com.example.demo.Logica.Clases.Promocion;
import com.example.demo.Logica.DataTypes.request.DtFiltro;
import com.example.demo.Logica.DataTypes.shared.DtCliente;
import com.example.demo.Logica.DataTypes.shared.DtPlato;
import com.example.demo.Logica.DataTypes.response.DtBusquedaPlatosPromocionesResponse;
import com.example.demo.Logica.DataTypes.shared.DtPromocion;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.example.demo.Logica.Mappers.ClienteMapper;
import com.example.demo.Logica.Mappers.LocalMapper;
import com.example.demo.Logica.Mappers.PlatoMapper;
import com.example.demo.Logica.Mappers.PromocionMapper;
import com.example.demo.Persistencia.Repositorios.ClienteRepositorio;
import com.example.demo.Persistencia.Repositorios.LocalRepositorio;
import com.example.demo.Persistencia.Repositorios.PlatoRepositorio;
import com.example.demo.Persistencia.Repositorios.PromocionRepositorio;
import com.example.demo.Persistencia.Repositorios.UsuarioRepositorio;
import com.example.demo.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

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
                userDetailsService
        );
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
                .dtPlato(dtPlato)
                .build();

        when(platoRepositorio.buscarConFiltros(filtro)).thenReturn(List.of(plato));
        when(promocionRepositorio.buscarActivasConFiltros(filtro)).thenReturn(List.of(promocion));
        when(platoMapper.mapearDtPlatoDeClase(plato)).thenReturn(dtPlato);
        when(promocionMapper.mapearDtPromocionDeClase(promocion)).thenReturn(dtPromocion);

        DtBusquedaPlatosPromocionesResponse response = clienteService.buscarPlatosYPromociones(filtro);

        assertThat(response.getPlatos()).containsExactly(dtPlato);
        assertThat(response.getPromociones()).containsExactly(dtPromocion);
    }

    @Test
    void buscarPlatosYPromocionesRechazaCuandoNoHayResultados() {
        DtFiltro filtro = DtFiltro.builder().nombre("Inexistente").build();

        when(platoRepositorio.buscarConFiltros(filtro)).thenReturn(List.of());
        when(promocionRepositorio.buscarActivasConFiltros(filtro)).thenReturn(List.of());

        assertThatThrownBy(() -> clienteService.buscarPlatosYPromociones(filtro))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se encontraron platos o promociones que coincidan con su búsqueda.");
    }
}