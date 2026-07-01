# Alineación Backend / Frontend — contrato actual de estadísticas del local

**Fecha:** 2026-07-01  
**Backend:** Foodly API  
**Pantalla impactada:** `local-panel/estadisticas`

---

## Objetivo

Este documento deja cerrado el **contrato actual y vigente** del endpoint de estadísticas del local, para evitar que frontend implemente contra versiones intermedias del DTO.

La idea es simple:

- backend expone un DTO **analítico**
- frontend consume solo los campos **realmente soportados hoy**
- se evita mezclar datos de catálogo con métricas históricas

---

## Endpoint vigente

```http
GET /api/v1/locales/estadisticas/{idLocal}
```

### Filtros soportados

#### Preset

```http
GET /api/v1/locales/estadisticas/10?preset=ULTIMOS_7_DIAS
```

Valores válidos:

- `HOY`
- `ULTIMOS_7_DIAS`
- `ULTIMOS_30_DIAS`
- `MES_ACTUAL`
- `MES_ANTERIOR`

#### Rango libre

```http
GET /api/v1/locales/estadisticas/10?fechaDesde=2026-06-01&fechaHasta=2026-06-15
```

Formato:

- `YYYY-MM-DD`

#### Regla importante

Frontend NO debe enviar:

- `preset`
- y `fechaDesde` / `fechaHasta`

al mismo tiempo.

---

## Response actual

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

## Campos raíz

### `fechaDesde`
Fecha inicial efectivamente aplicada por backend.

### `fechaHasta`
Fecha final efectivamente aplicada por backend.

### `ventasConfirmadas`
Suma de ventas del período para pedidos válidos de estadísticas.

### `platosMasPedido`
Top de platos más vendidos.

### `ventasPorPlato`
Detalle completo de platos vendidos en el período.

---

## DTO analítico vigente

Cada item de `platosMasPedido` y `ventasPorPlato` tiene esta forma:

```ts
type PlatoEstadistica = {
  id: number;
  nombre: string;
  imagenes: string[];
  cantidadVendida: number;
  montoVendido: number;
};
```

---

## Qué frontend SÍ debe consumir

### En `platosMasPedido[]`

- `id`
- `nombre`
- `imagenes`
- `cantidadVendida`
- `montoVendido`

### En `ventasPorPlato[]`

- `id`
- `nombre`
- `imagenes`
- `cantidadVendida`
- `montoVendido`

### En raíz

- `fechaDesde`
- `fechaHasta`
- `ventasConfirmadas`

---

## Qué frontend YA NO debe asumir

Frontend NO debe esperar estos campos dentro de `platosMasPedido` ni `ventasPorPlato`:

- `descripcion`
- `categoria`
- `precio`
- `precioFinal`
- `tienePromocion`
- `disponible`
- `dtLocal`

Esos campos fueron excluidos del DTO analítico para evitar ambigüedad semántica.

---

## Semántica de `montoVendido`

`montoVendido`:

- representa dinero efectivamente vendido por ese plato en el período
- se calcula desde importes históricos del detalle del pedido
- NO depende del precio actual del catálogo

Por eso backend dejó de exponer precios de catálogo en este response.

---

## Reglas de orden

### `platosMasPedido`

- ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`
- top fijo de **5**

### `ventasPorPlato`

- incluye solo platos con ventas en el período
- ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`

---

## Comportamiento cuando no hay datos

Si no hay pedidos válidos para estadísticas en el período:

- backend responde error de negocio
- hoy NO devuelve `200` con arrays vacíos

Frontend debe seguir manejando ese caso como error funcional del flujo.

---

## Checklist de adaptación para frontend

1. actualizar el tipo/modelo de estadísticas
2. dejar de mapear campos de catálogo dentro del bloque analítico
3. usar `platosMasPedido` si solo quieren enriquecer el ranking actual
4. usar `ventasPorPlato` si quieren renderizar el detalle completo por plato
5. seguir contemplando error cuando no hay ventas en el período

---

## Mensaje corto de alineación

> El contrato vigente de `GET /api/v1/locales/estadisticas/{idLocal}` expone un DTO analítico reducido.  
> `platosMasPedido` y `ventasPorPlato` devuelven solo `id`, `nombre`, `imagenes`, `cantidadVendida` y `montoVendido`.  
> Frontend no debe seguir esperando `precio`, `precioFinal`, `descripcion`, `categoria`, `disponible` ni `dtLocal` dentro de esos bloques.  
> Si no hay ventas en el período, backend mantiene error de negocio y no responde `200` vacío.
