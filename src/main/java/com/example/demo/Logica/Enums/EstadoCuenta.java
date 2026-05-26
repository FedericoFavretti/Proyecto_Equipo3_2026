package com.example.demo.Logica.Enums;

public enum EstadoCuenta {
    PendienteAprobacion,
    Activo,
    Rechazado,
    Bloqueado
    ;

    public boolean habilitaAutenticacion() {
        return this == Activo;
    }

    public boolean estaBloqueada() {
        return this == Bloqueado;
    }

    public static EstadoCuenta desdeValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return PendienteAprobacion;
        }

        return switch (valor.trim().toUpperCase()) {
            case "PENDING_APPROVAL", "PENDIENTE", "PENDIENTE_APROBACION", "PENDIENTEAPROBACION" -> PendienteAprobacion;
            case "ACTIVE", "ACTIVO" -> Activo;
            case "REJECTED", "RECHAZADO" -> Rechazado;
            case "BLOCKED", "BLOQUEADO" -> Bloqueado;
            default -> EstadoCuenta.valueOf(valor.trim());
        };
    }
}
