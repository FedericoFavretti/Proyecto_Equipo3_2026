-- Tabla para almacenar tokens FCM de dispositivos móviles.
-- Ejecutar en Railway (o en la base de datos Postgres de producción) antes de deployar.
CREATE TABLE IF NOT EXISTS device_tokens (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT        NOT NULL,
    token           TEXT          NOT NULL,
    plataforma      VARCHAR(20)   NOT NULL DEFAULT 'ANDROID',
    fecha_registro  TIMESTAMP     NOT NULL DEFAULT NOW(),
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_device_token UNIQUE (usuario_id, token)
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_usuario_activo
    ON device_tokens (usuario_id, activo);
