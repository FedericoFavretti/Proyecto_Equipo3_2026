package com.example.demo.Utils;

import org.springframework.security.core.Authentication;

public final class AuthUtils {
    private AuthUtils() {}

    public static boolean autenticacionInvalida(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication.getName() == null;
    }
}