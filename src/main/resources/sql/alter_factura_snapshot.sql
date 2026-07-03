ALTER TABLE Factura
    ADD COLUMN local_nombre_snapshot VARCHAR(255),
    ADD COLUMN cliente_nombre_snapshot VARCHAR(255),
    ADD COLUMN direccion_entrega_snapshot TEXT,
    ADD COLUMN medio_pago_snapshot VARCHAR(100),
    ADD COLUMN detalle_items_json TEXT;
