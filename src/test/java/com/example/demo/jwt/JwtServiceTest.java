package com.example.demo.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceUnitTest {

    private static final String SECRET = "change-this-secret-key-change-this-secret-key-123456";

    private final JwtService jwtService = new JwtService(SECRET, 900_000L);

    @Test
    void shouldGenerateAndValidateTokenForSameUser() {
        UserDetails user = new User(
                "user@example.com",
                "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }
}
