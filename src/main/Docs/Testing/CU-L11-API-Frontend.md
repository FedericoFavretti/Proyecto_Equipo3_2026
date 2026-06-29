# CU-L11 — API para Frontend

## Objetivo

Este documento resume cómo frontend debe consumir correctamente la API de **Obtener Estadísticas del Local** después del cambio de contrato.

El objetivo del cambio fue soportar:

- selección de período por **preset**
- selección de período por **rango libre**
- métricas calculadas **solo con pedidos en estado `Confirmado`**

> Base path general: `/api/v1`

---

## Resumen ejecutivo

### Qué se mantiene

- la ruta del endpoint **no cambió**

### Qué cambió

- el endpoint ahora acepta **query params de período**
- el response ya no devuelve `gananciasMensuales`
- el response ahora devuelve:
  - `fechaDesde`
  - `fechaHasta`
  - `platosMasPedido`
  - `ventasConfirmadas`

### Impacto para frontend

Si frontend hoy consume `gananciasMensuales`, ese consumo debe cambiar porque **ese campo ya no existe**.

---

# 1. Endpoint

## Ruta

`GET /api/v1/locales/estadisticas/{idLocal}`

## Autorización

- requiere usuario autenticado con rol **Local**

## Qué hace

Devuelve estadísticas del local para el período solicitado.

Las métricas actuales son:

- ventas confirmadas
- platos más pedidos

## Importante

Las estadísticas se calculan **únicamente con pedidos en estado `Confirmado`**.

No cuentan:

- `Pendiente`
- `Entregado`
- `Rechazado`
- `Cancelado`

---

# 2. Cómo enviar el período

Frontend puede enviar el período de **dos formas**, pero debe elegir **solo una**.

## Opción A — preset

Enviar el query param:

`preset`

### Valores permitidos

- `HOY`
- `ULTIMOS_7_DIAS`
- `ULTIMOS_30_DIAS`
- `MES_ACTUAL`
- `MES_ANTERIOR`

### Ejemplo

```http
GET /api/v1/locales/estadisticas/10?preset=ULTIMOS_7_DIAS
```

---

## Opción B — rango libre

Enviar ambos query params:

- `fechaDesde`
- `fechaHasta`

Formato esperado:

`YYYY-MM-DD`

### Ejemplo

```http
GET /api/v1/locales/estadisticas/10?fechaDesde=2026-06-01&fechaHasta=2026-06-15
```

---

## Comportamiento por defecto

Si frontend **no envía ningún período**, backend toma:

- `preset = MES_ACTUAL`

Eso significa:

- desde el primer día del mes actual
- hasta hoy

### Recomendación

Aunque backend tiene default, es MEJOR que frontend envíe explícitamente el período seleccionado para que la UI y la API queden alineadas sin ambigüedad.

---

## Regla importante

Frontend **NO debe enviar preset y rango libre al mismo tiempo**.

Esto es inválido:

```http
GET /api/v1/locales/estadisticas/10?preset=MES_ACTUAL&fechaDesde=2026-06-01&fechaHasta=2026-06-15
```

Ese caso devuelve error `400`.

---

# 3. Response 200

## Estructura actual

```json
{
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-15",
  "platosMasPedido": [
    {
      "id": 20,
      "nombre": "Milanesa al pan",
      "descripcion": "Milanesa con lechuga y tomate",
      "categoria": "Sandwiches",
      "precio": 350.0,
      "precioFinal": 350.0,
      "tienePromocion": false,
      "imagenes": [
        "https://.../milanesa.jpg"
      ],
      "disponible": true,
      "dtLocal": {
        "id": 10
      }
    }
  ],
  "ventasConfirmadas": 2450.0
}
```

## Significado de los campos

### `fechaDesde`
Fecha inicial realmente aplicada por backend.

### `fechaHasta`
Fecha final realmente aplicada por backend.

### `platosMasPedido`
Top de platos más pedidos dentro del período solicitado.

### `ventasConfirmadas`
Suma de `total` de pedidos `Confirmado` dentro del período solicitado.

---

# 4. Cambio de contrato respecto a la versión anterior

## Antes

Frontend recibía:

```json
{
  "platosMasPedido": [...],
  "gananciasMensuales": 1200.0
}
```

## Ahora

Frontend recibe:

```json
{
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-15",
  "platosMasPedido": [...],
  "ventasConfirmadas": 2450.0
}
```

## Impacto concreto

Frontend debe:

1. dejar de leer `gananciasMensuales`
2. empezar a leer `ventasConfirmadas`
3. actualizar el tipado/modelo DTO
4. usar `fechaDesde` y `fechaHasta` para renderizar el período aplicado

---

# 5. Errores esperables

## 400 Bad Request

### Caso: preset y rango libre al mismo tiempo

Mensaje esperado:

```json
{
  "mensaje": "Debe enviar un preset o un rango libre, pero no ambos."
}
```

### Caso: rango libre incompleto

Mensaje esperado:

```json
{
  "mensaje": "Para usar rango libre debe indicar fechaDesde y fechaHasta."
}
```

### Caso: fechas invertidas

Mensaje esperado:

```json
{
  "mensaje": "La fechaDesde no puede ser posterior a fechaHasta."
}
```

### Caso: sin datos en el período

Mensaje esperado:

```json
{
  "mensaje": "No hay informacion disponible para el periodo seleccionado. Intente con un rango de fechas diferente."
}
```

## 401 Unauthorized

Usuario no autenticado.

## 403 Forbidden

Usuario autenticado pero sin rol `Local`.

## 404 Not Found

Local inexistente.

---

# 6. Recomendación de UX para frontend

## Filtros sugeridos

### Presets rápidos

- Hoy
- Últimos 7 días
- Últimos 30 días
- Mes actual
- Mes anterior

### Rango libre

Permitir selección manual de:

- fecha desde
- fecha hasta

## Flujo recomendado

1. cargar la pantalla con `MES_ACTUAL`
2. mostrar ventas y top platos
3. cuando el usuario cambie el filtro:
   - enviar nuevo request
   - reemplazar métricas con la respuesta nueva
4. mostrar en la UI el período aplicado usando:
   - `fechaDesde`
   - `fechaHasta`

## Recomendación importante

Si la UI muestra un texto como:

- `Ventas del mes`

eso ya puede ser incorrecto.

Ahora conviene usar copies neutros como:

- `Ventas del período`
- `Top platos del período`

Porque el período puede cambiar.

---

# 7. Qué NO debe esperar frontend

Hoy backend **NO devuelve**:

- cantidad de pedidos por estado
- estadísticas mezclando otros estados
- comentario/resumen agregado por plato

Si frontend necesita alguna de esas métricas, eso requiere otro cambio de contrato.

---

# 8. Contrato resumido para implementar rápido

## Endpoint

- `GET /api/v1/locales/estadisticas/{idLocal}`

## Autorización

- rol `Local`

## Query params válidos

### preset

Uno de:

- `HOY`
- `ULTIMOS_7_DIAS`
- `ULTIMOS_30_DIAS`
- `MES_ACTUAL`
- `MES_ANTERIOR`

### o rango libre

- `fechaDesde=YYYY-MM-DD`
- `fechaHasta=YYYY-MM-DD`

## Response útil

```json
{
  "fechaDesde": "2026-06-01",
  "fechaHasta": "2026-06-15",
  "platosMasPedido": [...],
  "ventasConfirmadas": 2450.0
}
```

## Breaking change principal

- `gananciasMensuales` → **reemplazado por** `ventasConfirmadas`

---

# 9. Archivos backend relacionados

Por si frontend quiere rastrear origen del contrato:

- `src/main/java/com/example/demo/Logica/Controllers/LocalController.java`
- `src/main/java/com/example/demo/Logica/Service/LocalService.java`
- `src/main/java/com/example/demo/Logica/DataTypes/request/DtEstadisticasLocalFiltro.java`
- `src/main/java/com/example/demo/Logica/DataTypes/response/DtEstadisticasLocal.java`
- `src/main/java/com/example/demo/Logica/Enums/PeriodoEstadisticasPreset.java`
- `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

