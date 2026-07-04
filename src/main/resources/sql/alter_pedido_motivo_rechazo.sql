BEGIN;

ALTER TABLE pedido
ADD COLUMN IF NOT EXISTS motivorechazo TEXT;

UPDATE pedido p
SET motivorechazo = src.motivo
FROM (
    SELECT DISTINCT ON (pn.idpedido)
        pn.idpedido,
        regexp_replace(
            n.mensaje,
            '^Tu pedido #[0-9]+ fue rechazado\. Motivo: ',
            ''
        ) AS motivo
    FROM notificacion n
    JOIN pedido_notificacion pn
      ON pn.idnotificacion = n.id
    WHERE n.tipo = 'Pedido'
      AND n.mensaje LIKE 'Tu pedido #% fue rechazado. Motivo: %'
    ORDER BY pn.idpedido, n.fecha DESC, n.id DESC
) src
WHERE p.id = src.idpedido
  AND (p.motivorechazo IS NULL OR btrim(p.motivorechazo) = '');

COMMIT;
