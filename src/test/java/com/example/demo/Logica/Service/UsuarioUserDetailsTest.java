package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.demo.Logica.Clases.Administrador;
import com.example.demo.Logica.Clases.Cliente;
import com.example.demo.Logica.Clases.Local;
import com.example.demo.Logica.Enums.EstadoCuenta;

class UsuarioUserDetailsTest {

    @Test
    void shouldExposeUserCredentialsToSpringSecurity() {
        Cliente cliente = Cliente.builder()
                .email("cliente@foodly.test")
                .passwd("encoded-password")
                .tipo("cliente")
                .estado(EstadoCuenta.Activo)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.getUsername()).isEqualTo("cliente@foodly.test");
        assertThat(details.getPassword()).isEqualTo("encoded-password");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_Cliente");
    }

    @Test
    void shouldExposeAdminRole() {
        Administrador administrador = new Administrador();
        administrador.setEmail("admin@foodly.test");
        administrador.setTipo("admin");
        administrador.setEstado(EstadoCuenta.Activo);

        UsuarioUserDetails details = new UsuarioUserDetails(administrador);

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_Admin");
    }

    @Test
    void shouldExposeLocalRole() {
        Local local = Local.builder()
                .email("local@foodly.test")
                .tipo("local")
                .estado(EstadoCuenta.Activo)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(local);

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_Local");
    }

    @Test
    void shouldReportAccountLockedWhenEstadoBloqueado() {
        Cliente cliente = Cliente.builder()
                .tipo("cliente")
                .estado(EstadoCuenta.Bloqueado)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void shouldReportAccountDisabledWhenEstadoPendiente() {
        Cliente cliente = Cliente.builder()
                .tipo("cliente")
                .estado(EstadoCuenta.Pendiente)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldReportAccountUnlockedAndEnabledWhenEstadoActivo() {
        Cliente cliente = Cliente.builder()
                .tipo("cliente")
                .estado(EstadoCuenta.Activo)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldRejectNullUser() {
        assertThatThrownBy(() -> new UsuarioUserDetails(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("usuario no puede ser null");
    }
}
