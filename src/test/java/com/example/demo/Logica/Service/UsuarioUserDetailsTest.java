package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Enums.EstadoCuenta;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/*class UsuarioUserDetailsTest {

    @Test
    void shouldExposeHorizontalUserCredentialsToSpringSecurity() {
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@foodly.test");
        cliente.setPasswd("encoded-password");
        cliente.setTipo(RolUsuario.CUSTOMER);
        cliente.setEstado(EstadoCuenta.Activo);

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.getUsername()).isEqualTo("cliente@foodly.test");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldDisableUsersThatAreNotActive() {
        Cliente cliente = new Cliente();
        cliente.setTipo(RolUsuario.CUSTOMER);
        cliente.setEstado(EstadoCuenta.PendienteAprobacion);

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void shouldLockBlockedUsers() {
        Cliente cliente = new Cliente();
        cliente.setTipo(RolUsuario.CUSTOMER);
        cliente.setEstado(EstadoCuenta.Bloqueado);

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.isAccountNonLocked()).isFalse();
    }
}
*/