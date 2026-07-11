BEGIN;

ALTER TABLE reclamo
    ADD COLUMN IF NOT EXISTS motivorechazo character varying(255);

UPDATE reclamo
SET estado = 'Rechazado'
WHERE estado = 'Recazado';

ALTER TABLE reclamo
    DROP CONSTRAINT IF EXISTS reclamo_estado_check;

ALTER TABLE reclamo
    ADD CONSTRAINT reclamo_estado_check
        CHECK (estado::text = ANY (
            ARRAY[
                'Pendiente'::character varying,
                'Atendido'::character varying,
                'Rechazado'::character varying
            ]::text[]
        ));

COMMIT;
