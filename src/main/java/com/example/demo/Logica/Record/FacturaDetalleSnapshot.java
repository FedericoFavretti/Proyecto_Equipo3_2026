package com.example.demo.Logica.Record;

public record FacturaDetalleSnapshot(
        String nombreProducto,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) {
}
