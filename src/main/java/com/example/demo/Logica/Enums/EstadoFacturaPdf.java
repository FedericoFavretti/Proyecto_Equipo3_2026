package com.example.demo.Logica.Enums;

public enum EstadoFacturaPdf {
    PENDIENTE(true, false, "Factura pendiente de generación"),
    GENERANDO(true, false, "Factura en proceso de generación"),
    GENERADA(false, true, "Factura generada correctamente"),
    ERROR_REINTENTABLE(true, false, "Falló la generación, se reintentará"),
    ERROR_FINAL(false, true, "Falló la generación y no quedan más reintentos");

    private final boolean retryable;
    private final boolean terminal;
    private final String descripcion;

    EstadoFacturaPdf(boolean retryable, boolean terminal, String descripcion) {
        this.retryable = retryable;
        this.terminal = terminal;
        this.descripcion = descripcion;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
