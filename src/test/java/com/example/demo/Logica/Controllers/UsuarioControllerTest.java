package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.DataTypes.response.DtPerfilUsuarioResponse;
import com.example.demo.Logica.DataTypes.response.DtUsuarioInfo;
import com.example.demo.Logica.Service.UsuarioService;
import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    @Test
    void loginDevuelveTokenYUsuario() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        LoginRequest request = new LoginRequest("cliente@foodly.com", "Clave123");
        AuthResponse authResponse = new AuthResponse(
                "jwt-token",
                new DtUsuarioInfo(10L, "cliente@foodly.com", "cliente")
        );

        when(usuarioService.login(request)).thenReturn(authResponse);

        var response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(authResponse);
        verify(usuarioService).login(request);
    }

    @Test
    void obtenerPerfilDevuelveOkSiElUsuarioEstaAutenticado() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("cliente@foodly.com", null, "ROLE_cliente");
        DtPerfilUsuarioResponse perfil = DtPerfilUsuarioResponse.builder()
                .id(10L)
                .email("cliente@foodly.com")
                .tipo("cliente")
                .nombre("Ana")
                .apellido("Perez")
                .build();

        when(usuarioService.obtenerPerfil("cliente@foodly.com")).thenReturn(perfil);

        var response = controller.obtenerPerfil(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(perfil);
        verify(usuarioService).obtenerPerfil("cliente@foodly.com");
    }

    @Test
    void obtenerPerfilDevuelveUnauthorizedSiNoHayAutenticacion() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);

        var response = controller.obtenerPerfil(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(usuarioService);
    }

    @Test
    void obtenerPerfilDevuelveUnauthorizedSiLaAutenticacionEsAnonima() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("anonymousUser", null);

        var response = controller.obtenerPerfil(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(usuarioService);
    }

    @Test
    void editarDatosDeCuentaDevuelveNoContentSiElUsuarioEstaAutenticado() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        MockMultipartFile foto = new MockMultipartFile("foto", "perfil.png", "image/png", new byte[]{1});
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("cliente@foodly.com", null, "ROLE_cliente");

        var response = controller.editarDatosDeCuentaDeUsuario(
                Map.of("nombre", "Maria"),
                foto,
                "Bearer token",
                authentication
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(usuarioService).editarDatosDeCuentaDeUsuario("cliente@foodly.com", "Bearer token", Map.of("nombre", "Maria"), foto);
    }

    @Test
    void editarDatosDeCuentaDevuelveUnauthorizedSiNoHayAutenticacion() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);

        var response = controller.editarDatosDeCuentaDeUsuario(
                Map.of("nombre", "Maria"),
                null,
                "Bearer token",
                null
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(usuarioService);
    }

    @Test
    void eliminarCuentaPropiaDevuelveNoContent() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);

        var response = controller.eliminarCuentaDeUsuarioPropiaDev(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(usuarioService).eliminarCuentaDeUsuarioPropia(10L);
    }
}
