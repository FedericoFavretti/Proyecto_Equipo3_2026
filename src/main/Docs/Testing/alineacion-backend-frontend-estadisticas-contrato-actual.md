# Alineacion Backend / Frontend - contrato actual de estadisticas del local

**Fecha:** 2026-07-07  
**Backend:** Foodly API  
**Pantalla impactada:** `local-panel/estadisticas`

---

## Objetivo

Este documento deja cerrado el contrato vigente del endpoint de estadisticas del local para que frontend implemente contra el DTO real del backend.

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

Valores validos:

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
      "imagen": "https://.../milanesa.jpg",
      "cantidadVendida": 7,
      "montoVendido": 2450.0
    }
  ],
  "ventasPorPlato": [
    {
      "id": 20,
      "nombre": "Milanesa al pan",
      "imagen": "https://.../milanesa.jpg",
      "cantidadVendida": 7,
      "montoVendido": 2450.0
    }
  ],
  "ventasMensuales": [
    {
      "anio": 2026,
      "mes": 6,
      "montoVendido": 2450.0
    }
  ],
  "ventasConfirmadas": 2450.0
}
```

---

## Campos raiz

### `fechaDesde`
Fecha inicial efectivamente aplicada por backend.

### `fechaHasta`
Fecha final efectivamente aplicada por backend.

### `ventasConfirmadas`
Suma de ventas del periodo para pedidos validos de estadisticas.

### `platosMasPedido`
Top de platos mas vendidos.

### `ventasPorPlato`
Detalle completo de platos vendidos en el periodo.

### `ventasMensuales`
Serie temporal agregada por mes dentro del rango pedido. Si entre dos meses con ventas existe un mes sin movimientos, backend lo devuelve igual con `montoVendido = 0.0` para que el grafico no quede incompleto.

---

## DTO analitico vigente

Cada item de `platosMasPedido` y `ventasPorPlato` tiene esta forma:

```ts
type PlatoEstadistica = {
  id: number;
  nombre: string;
  imagen: string;
  cantidadVendida: number;
  montoVendido: number;
};
```

La serie mensual tiene esta forma:

```ts
type VentaMensualEstadistica = {
  anio: number;
  mes: number;
  montoVendido: number;
};
```

---

## Que frontend SI debe consumir

### En `platosMasPedido[]`

- `id`
- `nombre`
- `imagen`
- `cantidadVendida`
- `montoVendido`

### En `ventasPorPlato[]`

- `id`
- `nombre`
- `imagen`
- `cantidadVendida`
- `montoVendido`

### En `ventasMensuales[]`

- `anio`
- `mes`
- `montoVendido`

### En raiz

- `fechaDesde`
- `fechaHasta`
- `ventasConfirmadas`

---

## Que frontend YA NO debe asumir

Frontend NO debe esperar estos campos dentro de `platosMasPedido` ni `ventasPorPlato`:

- `descripcion`
- `categoria`
- `precio`
- `precioFinal`
- `tienePromocion`
- `disponible`
- `dtLocal`

Esos campos fueron excluidos del DTO analitico para evitar ambiguedad semantica.

---

## Semantica de `montoVendido`

`montoVendido`:

- representa dinero efectivamente vendido en el periodo
- en `platosMasPedido` y `ventasPorPlato` se calcula por plato
- en `ventasMensuales` se calcula por mes
- NO depende del precio actual del catalogo

---

## Reglas de orden

### `platosMasPedido`

- ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`
- top fijo de **5**

### `ventasPorPlato`

- incluye solo platos con ventas en el periodo
- ordenado por `cantidadVendida DESC`
- desempata por `montoVendido DESC`
- luego por `id ASC`

### `ventasMensuales`

- ordenado por `anio ASC`, `mes ASC`
- incluye meses sin ventas dentro del rango con `montoVendido = 0.0`

---

## Comportamiento cuando no hay datos

Si no hay pedidos validos para estadisticas en el periodo:

- backend responde error de negocio
- hoy NO devuelve `200` con arrays vacios

Frontend debe seguir manejando ese caso como error funcional del flujo.

---

## Checklist de adaptacion para frontend

1. actualizar el tipo/modelo de estadisticas
2. dejar de mapear campos de catalogo dentro del bloque analitico
3. usar `platosMasPedido` si solo quieren enriquecer el ranking actual
4. usar `ventasPorPlato` si quieren renderizar el detalle completo por plato
5. usar `ventasMensuales` para el grafico de barras por mes
6. seguir contemplando error cuando no hay ventas en el periodo

---

## Mensaje corto de alineacion

> El contrato vigente de `GET /api/v1/locales/estadisticas/{idLocal}` expone un DTO analitico reducido.  
> `platosMasPedido` y `ventasPorPlato` devuelven solo `id`, `nombre`, `imagen`, `cantidadVendida` y `montoVendido`.  
> `ventasMensuales` devuelve la serie agregada por mes usando `anio`, `mes` y `montoVendido`.  
> Si no hay ventas en el periodo, backend mantiene error de negocio y no responde `200` vacio.
