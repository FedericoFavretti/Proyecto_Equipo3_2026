package com.example.demo.Logica.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.demo.Logica.Clases.Cliente;
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
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void shouldUseSpringSecurityDefaultAccountFlags() {
        Cliente cliente = Cliente.builder()
                .tipo("cliente")
                .estado(EstadoCuenta.Bloqueado)
                .build();

        UsuarioUserDetails details = new UsuarioUserDetails(cliente);

        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldRejectNullUser() {
        assertThatThrownBy(() -> new UsuarioUserDetails(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("usuario no puede ser null");
    }
}
