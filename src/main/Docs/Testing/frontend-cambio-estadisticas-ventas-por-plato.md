# Cambio para Frontend — estadísticas del local con ventas por plato

**Fecha:** 2026-07-01  
**Backend:** Foodly API  
**Pantalla impactada:** panel de estadísticas del local

---

## Resumen

Se extendió el contrato del endpoint de estadísticas del local para que frontend pueda mostrar:

- cuántas unidades se vendieron por plato
- cuánto dinero vendió cada plato

NO cambió la ruta del endpoint.  
SÍ cambió el payload de respuesta.

---

## Endpoint

```http
GET /api/v1/locales/estadisticas/{idLocal}
```

Sigue aceptando los mismos filtros:

### Preset

```http
GET /api/v1/locales/estadisticas/10?preset=ULTIMOS_7_DIAS
```

### Rango libre

```http
GET /api/v1/locales/estadisticas/10?fechaDesde=2026-06-01&fechaHasta=2026-06-15
```

---

## Qué cambió en la respuesta

Antes, frontend recibía:

```json
{
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-15",
  "platosMasPedido": [...],
  "ventasConfirmadas": 2450.0
}
```

Ahora recibe:

```json
{
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-15",
  "platosMasPedido": [
    {
      "id": 20,
      "nombre": "Milanesa al pan",
      "imagenes": [
        "https://.../milanesa.jpg"
      ],
      "cantidadVendida": 7,
      "montoVendido": 2450.0
    }
  ],
  "ventasPorPlato": [
    {
      "id": 20,
      "nombre": "Milanesa al pan",
      "imagenes": [
        "https://.../milanesa.jpg"
      ],
      "cantidadVendida": 7,
      "montoVendido": 2450.0
    }
  ],
  "ventasConfirmadas": 2450.0
}
```

---

## Significado de los campos nuevos

### `platosMasPedido[].cantidadVendida`
Cantidad total de unidades vendidas de ese plato dentro del período.

### `platosMasPedido[].montoVendido`
Monto total vendido de ese plato dentro del período.

Importante:

- se calcula a partir de los importes históricos guardados en `detallepedido.subtotal`
- NO depende del precio actual del catálogo
- por eso backend ya no expone `precio` ni `precioFinal` en este DTO analítico

Reglas de `platosMasPedido`:

- viene ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`
- devuelve un top fijo de 5 platos

### `ventasPorPlato`
Desglose completo de platos vendidos en el período.

Reglas del contrato:

- incluye solo platos con ventas en el período
- viene ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`

Importante:

- `platosMasPedido` sigue siendo el **top**
- `ventasPorPlato` es el **detalle completo**

Si frontend quiere enriquecer el ranking actual, puede usar `platosMasPedido`.

Si frontend quiere mostrar una tabla o lista analítica completa de platos vendidos, debe usar `ventasPorPlato`.

---

## Qué debería cambiar frontend

## Opción A — cambio mínimo

Si solo quieren mejorar la sección actual de “platos más pedidos”, alcanza con:

1. actualizar el mapper/modelo
2. leer:
   - `platosMasPedido[].cantidadVendida`
   - `platosMasPedido[].montoVendido`
   - opcionalmente `imagenes`
3. renderizar esos campos en la UI

### Ejemplo visual posible

- Milanesa al pan
  - 7 unidades vendidas
  - $2450 vendidos

---

## Opción B — mejora completa

Si quieren mostrar el detalle real por plato:

1. actualizar el mapper/modelo
2. consumir `ventasPorPlato`
3. renderizar una tabla o lista con:
   - nombre
   - cantidadVendida
   - montoVendido

### Ejemplo visual posible

| Plato | Unidades vendidas | Monto vendido |
|------|------------------:|--------------:|
| Milanesa al pan | 7 | 2450 |

---

## Recomendación técnica

Si el objetivo de negocio es solo enriquecer la sección actual, usen `platosMasPedido`.

Si el objetivo es mostrar un panel más serio de estadísticas, usen `ventasPorPlato`.

La diferencia IMPORTA:

- `platosMasPedido` = ranking
- `ventasPorPlato` = análisis completo

No mezclen ambas ideas como si fueran lo mismo.

---

## Tipado sugerido para frontend

Ejemplo conceptual:

```ts
type PlatoEstadistica = {
  id: number;
  nombre: string;
  imagenes: string[];
  cantidadVendida: number;
  montoVendido: number;
};

type EstadisticasLocal = {
  fechaDesde: string;
  fechaHasta: string;
  ventasConfirmadas: number;
  platosMasPedido: PlatoEstadistica[];
  ventasPorPlato: PlatoEstadistica[];
};
```

---

## Compatibilidad

- la ruta NO cambió
- los filtros NO cambiaron
- `ventasConfirmadas` se mantiene
- `platosMasPedido` se mantiene, pero ahora viene enriquecido
- se agrega `ventasPorPlato`
- el DTO estadístico ya no expone precios de catálogo para evitar ambigüedad con montos históricos

Esto significa que el cambio para frontend es de **contrato ampliado**, no de endpoint nuevo.

---

## Comportamiento cuando no hay ventas en el período

Backend mantiene la regla actual:

- si no hay pedidos válidos para estadísticas en el período
- responde error de negocio

Hoy NO devuelve `200` con arrays vacíos.

---

## Mensaje corto para el equipo frontend

> Backend extendió el response de `GET /api/v1/locales/estadisticas/{idLocal}`.  
> `platosMasPedido` ahora incluye `cantidadVendida` y `montoVendido`, y además se agregó `ventasPorPlato` con el detalle completo por plato vendido en el período.  
> En el DTO estadístico ya no se envían precios de catálogo para no mezclar valores actuales con montos históricos.  
> Si quieren un cambio mínimo, lean los nuevos campos de `platosMasPedido`.  
> Si quieren mostrar el detalle analítico completo, consuman `ventasPorPlato`.
