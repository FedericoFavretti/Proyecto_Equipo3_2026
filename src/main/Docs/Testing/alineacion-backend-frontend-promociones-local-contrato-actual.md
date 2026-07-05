# Alineacion Backend / Frontend - contrato actual de promociones del local

**Fecha:** 2026-07-04  
**Backend:** Foodly API  
**Pantalla impactada:** `local-panel/promociones`

---

## Objetivo

Este documento deja cerrado el **contrato actual y vigente** del endpoint de promociones del local, para que frontend se alinee con los cambios hechos en backend y no siga consumiendo una lista plana.

La idea es simple:

- backend ya no devuelve una lista unica de promociones
- backend clasifica promociones por estado temporal
- frontend debe renderizar grupos separados usando el nuevo shape

---

## Endpoint vigente

```http
GET /api/v1/locales/busqueda_promocion_local/{idLocal}
```

### Ejemplo

```http
GET /api/v1/locales/busqueda_promocion_local/10
```

---

## Cambio de contrato

### Antes

Frontend recibia una **lista plana**:

```json
[
  {
    "id": 1,
    "descuento": 15.0,
    "fechaInicio": "2026-07-01T00:00:00",
    "fechaFin": "2026-07-10T00:00:00",
    "descripcion": "Promo invierno",
    "dtPlato": {
      "id": 20,
      "nombre": "BigMac"
    }
  }
]
```

### Ahora

Frontend recibe un **objeto agrupado**:

```json
{
  "vigentes": [
    {
      "id": 1,
      "descuento": 15.0,
      "fechaInicio": "2026-07-01T00:00:00",
      "fechaFin": "2026-07-10T00:00:00",
      "descripcion": "Promo invierno",
      "dtPlato": {
        "id": 20,
        "nombre": "BigMac"
      }
    }
  ],
  "vencidas": [
    {
      "id": 2,
      "descuento": 10.0,
      "fechaInicio": "2026-06-01T00:00:00",
      "fechaFin": "2026-06-10T00:00:00",
      "descripcion": "Promo pasada",
      "dtPlato": {
        "id": 21,
        "nombre": "McNuggets"
      }
    }
  ],
  "proximas": [
    {
      "id": 3,
      "descuento": 20.0,
      "fechaInicio": "2026-07-20T00:00:00",
      "fechaFin": "2026-07-25T00:00:00",
      "descripcion": "Promo vacaciones",
      "dtPlato": {
        "id": 22,
        "nombre": "Combo Doble"
      }
    }
  ]
}
```

---

## Regla de clasificacion

Backend clasifica cada promocion con estas reglas:

- **vigentes**: `fechaInicio <= hoy <= fechaFin`
- **vencidas**: `hoy > fechaFin`
- **proximas**: `hoy < fechaInicio`

### Importante

Frontend **NO debe recalcular** esta logica para decidir en que bloque mostrar cada promocion.

Frontend debe confiar en:

- `response.vigentes`
- `response.vencidas`
- `response.proximas`

---

## Shape estable a consumir

Siempre esperar este objeto:

```ts
type PromocionesLocalResponse = {
  vigentes: DtPromocion[];
  vencidas: DtPromocion[];
  proximas: DtPromocion[];
}
```

### Regla de consumo

- no asumir `null`
- manejar arrays vacios
- no intentar iterar la respuesta completa como si fuera `DtPromocion[]`

---

## Impacto concreto en frontend

## 1. Cambiar el tipado de la respuesta

Donde hoy el frontend espere:

```ts
DtPromocion[]
```

debe pasar a esperar:

```ts
{
  vigentes: DtPromocion[];
  vencidas: DtPromocion[];
  proximas: DtPromocion[];
}
```

## 2. Cambiar el render

En lugar de una sola card/lista:

- renderizar bloque de **Promociones vigentes**
- renderizar bloque de **Proximas promociones**
- renderizar bloque de **Promociones vencidas**

## 3. Cambiar estados vacios

Ejemplos recomendados:

- si `vigentes.length === 0` -> "No hay promociones vigentes."
- si `proximas.length === 0` -> "No hay promociones proximas."
- si `vencidas.length === 0` -> "No hay promociones vencidas."

## 4. No mezclar conceptos

- **vigente** no significa "existe"
- **proxima** no significa "activa"
- **vencida** no debe aparecer en el bloque de vigentes

---

## Campos que no cambiaron dentro de cada promocion

Cada item de `vigentes`, `vencidas` y `proximas` sigue usando el mismo DTO de promocion:

- `id`
- `descuento`
- `fechaInicio`
- `fechaFin`
- `descripcion`
- `dtPlato`

O sea: el cambio fuerte esta en el **contenedor de la respuesta**, no en el shape interno de cada promocion.

---

## Checklist de alineacion para frontend

- [ ] actualizar interface/type del endpoint
- [ ] dejar de tratar la respuesta como lista plana
- [ ] renderizar `vigentes`
- [ ] renderizar `proximas`
- [ ] renderizar `vencidas`
- [ ] agregar empty states por bloque
- [ ] revisar componentes que usen el endpoint viejo
- [ ] revisar tests/unit tests del front que mockeen esta API

---

## Nota tecnica importante

El backend hoy clasifica por fecha usando el dia actual.  
Ademas, la persistencia actual de promociones conserva la fecha y no una hora fina de vigencia.

Para frontend, esto significa:

- tratar `fechaInicio` y `fechaFin` como valores informativos
- usar la clasificacion que ya viene armada desde backend

---

## Conclusion

El frontend debe migrar de:

- **lista plana de promociones**

a:

- **objeto agrupado por estado temporal**

Contrato vigente del endpoint:

- `vigentes`
- `proximas`
- `vencidas`

