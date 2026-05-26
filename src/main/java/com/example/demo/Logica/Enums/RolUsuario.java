package com.example.demo.Logica.Enums;

public enum RolUsuario {
    ADMIN("ADMIN"),
    LOCAL("LOCAL"),
    CUSTOMER("CLIENTE");

    private final String tipoPersistido;

    RolUsuario(String tipoPersistido) {
        this.tipoPersistido = tipoPersistido;
    }

    public String getTipoPersistido() {
        return tipoPersistido;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public static RolUsuario desdeTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return CUSTOMER;
        }

        return switch (tipo.trim().toUpperCase()) {
            case "ADMIN", "ADMINISTRADOR" -> ADMIN;
            case "LOCAL" -> LOCAL;
            case "CUSTOMER", "CLIENTE" -> CUSTOMER;
            default -> RolUsuario.valueOf(tipo.trim().toUpperCase());
        };
    }
}
