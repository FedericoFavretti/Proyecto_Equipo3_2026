package com.example.demo.Logica.Controllers;

import com.example.demo.Logica.Service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class UsuarioControllerTest {

    @Test
    void reenviarActivacionDevuelveOkYDelegaEnServicio() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);

        var response = controller.reenviarActivacion(new com.example.demo.Logica.DataTypes.request.DtReenviarActivacionRequest("pendiente@foodly.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(usuarioService).reenviarActivacion("pendiente@foodly.com");
    }

    @Test
    void editarDatosDeCuentaDevuelveOkSiElUsuarioEstaAutenticado() {
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
    void eliminarMiCuentaDevuelveNoContent() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("cliente@foodly.com", null, "ROLE_cliente");

        var response = controller.eliminarMiCuenta(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(usuarioService).eliminarMiCuenta("cliente@foodly.com");
    }

    @Test
    void eliminarMiCuentaDevuelveUnauthorizedSiNoHayAutenticacion() {
        UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);

        var response = controller.eliminarMiCuenta(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(usuarioService);
    }
}
