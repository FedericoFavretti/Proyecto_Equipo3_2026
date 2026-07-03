CREATE TABLE IF NOT EXISTS Factura (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    id_pedido BIGINT NOT NULL,
    fecha_pedido TIMESTAMP NOT NULL,
    fecha_emision TIMESTAMP NOT NULL,
    monto_total NUMERIC(12,2) NOT NULL,
    local_nombre_snapshot VARCHAR(255) NOT NULL,
    local_email_snapshot VARCHAR(255),
    cliente_nombre_snapshot VARCHAR(255) NOT NULL,
    cliente_email_snapshot VARCHAR(255),
    direccion_entrega_snapshot TEXT,
    medio_pago_snapshot VARCHAR(100),
    CONSTRAINT fk_factura_pedido
        FOREIGN KEY (id_pedido) REFERENCES Pedido(id)
);

CREATE TABLE IF NOT EXISTS FacturaDetalle (
    id BIGSERIAL PRIMARY KEY,
    id_factura BIGINT NOT NULL,
    nombre_producto_snapshot VARCHAR(255) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_factura_detalle_factura
        FOREIGN KEY (id_factura) REFERENCES Factura(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FacturaPdfProceso (
    id_factura BIGINT PRIMARY KEY,
    archivo_pdf TEXT,
    estado_pdf VARCHAR(30) NOT NULL,
    intentos_generacion INTEGER NOT NULL DEFAULT 0,
    ultimo_error_pdf TEXT,
    fecha_ultimo_intento TIMESTAMP,
    proximo_reintento TIMESTAMP,
    fecha_generacion_pdf TIMESTAMP,
    CONSTRAINT fk_factura_pdf_proceso_factura
        FOREIGN KEY (id_factura) REFERENCES Factura(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_factura_pdf_estado
        CHECK (estado_pdf IN (
            'PENDIENTE',
            'GENERANDO',
            'GENERADA',
            'ERROR_REINTENTABLE',
            'ERROR_FINAL'
        ))
);

CREATE INDEX IF NOT EXISTS idx_factura_id_pedido
    ON Factura(id_pedido);

CREATE INDEX IF NOT EXISTS idx_factura_detalle_id_factura
    ON FacturaDetalle(id_factura);

CREATE INDEX IF NOT EXISTS idx_factura_pdf_proceso_estado_reintento
    ON FacturaPdfProceso(estado_pdf, proximo_reintento);
