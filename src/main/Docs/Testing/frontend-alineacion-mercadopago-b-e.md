# Alineación Frontend — Pedidos con Mercado Pago pendientes de pago

## Objetivo

Documentar el contrato que el frontend debe consumir después del ajuste **B + E**:

- **B**: no mostrar como `Pendiente` un pedido de Mercado Pago todavía no acreditado
- **E**: permitir **reintentar el pago** explícitamente

> Alcance: **sin cambios de BD**.  
> El cambio está implementado en backend y el frontend debe alinearse con el nuevo contrato.

---

## Problema funcional que se corrige

Antes, cuando un cliente:

- volvía atrás desde la pantalla de Mercado Pago
- o el pago fallaba / era rechazado

el pedido igual aparecía en `Mis Pedidos` con:

- `estado = Pendiente`

Eso generaba una UX confusa, porque para el usuario parecía un pedido operativo, cuando en realidad era un pedido **creado pero sin pago acreditado**.

---

## Decisión aplicada en backend

Para pedidos de Mercado Pago con:

- `estado = Pendiente`
- `pagado = false`
- `medioDePago != EFECTIVO`

el backend ahora expone un estado visible de UI:

- `estadoVisible = "Pendiente de pago"`

Además expone señales explícitas para la pantalla:

- `pagoPendiente = true`
- `permiteReintentarPago = true`

---

## Endpoints afectados

### 1. `GET /api/v1/pedidos/mi-historial`

Sigue siendo el endpoint principal para `Mis Pedidos`, pero ahora cada item puede traer campos nuevos.

### 2. `POST /api/v1/pedidos/{idPedido}/reintentar-pago`

**Nuevo endpoint**.

Uso:

- autenticado como `Cliente`
- solo para pedidos propios
- solo para pedidos de Mercado Pago todavía pendientes de pago

Respuesta:

- devuelve `DtPedidoResponse`
- incluye un `mpInitPoint` nuevo para redirigir otra vez al checkout

### 3. `POST /api/v1/pedidos/{idPedido}/cancelar`

El path no cambió, pero ahora el backend valida que:

- el pedido pertenezca al cliente autenticado

Esto no requiere cambio visual, pero sí mantener el flujo autenticado correctamente.

---

## Nuevos campos disponibles

### En `DtPedidoListadoResponse` (`GET /mi-historial`)

Se agregan:

```json
{
  "estadoVisible": "Pendiente de pago",
  "pagoPendiente": true,
  "permiteReintentarPago": true,
  "mpInitPoint": "https://www.mercadopago...."
}
```

### En `DtPedidoResponse`

Se agregan:

```json
{
  "estadoVisible": "Pendiente de pago",
  "pagoPendiente": true,
  "permiteReintentarPago": true,
  "mpInitPoint": "https://www.mercadopago...."
}
```

---

## Regla de interpretación para frontend

### Pedido pendiente de pago

Si llega:

```json
{
  "estado": "Pendiente",
  "pagado": false,
  "pagoPendiente": true,
  "estadoVisible": "Pendiente de pago",
  "permiteReintentarPago": true
}
```

el frontend debe mostrar algo equivalente a:

- badge: **Pendiente de pago**
- acciones:
  - **Reintentar pago**
  - **Cancelar pedido**

### Pedido pendiente operativo real

Si en el futuro llega:

```json
{
  "estado": "Pendiente",
  "pagado": true
}
```

el frontend **no debe inventar** `Pendiente de pago`; debe usar `estadoVisible` si está presente, y si no, el `estado`.

### Pedido aprobado / confirmado / entregado / cancelado

Para cualquier otro caso:

- usar `estadoVisible` si viene informado
- si no, caer al valor de `estado`

---

## Comportamiento esperado en `Mis Pedidos`

### Antes

- `Pendiente`
- botón `Cancelar pedido`

### Ahora para MP impago

- `Pendiente de pago`
- botón `Reintentar pago`
- botón `Cancelar pedido`

### Sugerencia UX

Texto recomendado:

- **Pendiente de pago**
- o **Pago no completado**

Pero si se usa texto distinto en UI, la lógica debe seguir basándose en:

- `pagoPendiente`
- `permiteReintentarPago`

NO en parsear cadenas.

---

## Flujo recomendado para `Reintentar pago`

### Acción de usuario

Click en `Reintentar pago`

### Request

`POST /api/v1/pedidos/{idPedido}/reintentar-pago`

Sin body.

### Respuesta esperada

```json
{
  "id": 101,
  "estado": "Pendiente",
  "estadoVisible": "Pendiente de pago",
  "pagoPendiente": true,
  "permiteReintentarPago": true,
  "mpInitPoint": "https://www.mercadopago.com/..."
}
```

### Acción del frontend

Si la respuesta trae `mpInitPoint`:

- redirigir al navegador a ese `mpInitPoint`

Si no lo trae:

- mostrar error genérico de reintento

---

## Flujo recomendado para `/pago/error`

La pantalla de error de frontend **NO debe asumir** que el pedido quedó cancelado.

Especialmente si Mercado Pago redirige con parámetros incompletos o nulos, por ejemplo:

- `collection_id=null`
- `status=null`
- etc.

### Regla correcta

La pantalla `/pago/error` debe:

1. leer `external_reference` si viene
2. permitir volver a `Mis Pedidos`
3. preferiblemente ofrecer `Reintentar pago`

Pero la verdad del estado debe salir de:

- `GET /api/v1/pedidos/mi-historial`

No del query string del redirect.

---

## Recomendaciones concretas para frontend

### 1. No renderizar el badge desde `estado` solamente

Usar prioridad:

1. `estadoVisible`
2. `estado`

### 2. No inferir “pagado” por haber llegado a `/pago/exito`

La fuente de verdad sigue siendo el backend.

### 3. No inferir “cancelado” o “rechazado” por haber llegado a `/pago/error`

La fuente de verdad sigue siendo el backend.

### 4. Usar flags semánticos

Preferir:

- `pagoPendiente`
- `permiteReintentarPago`

en vez de reglas frágiles basadas en strings.

---

## Ejemplo de render esperado en `Mis Pedidos`

### Caso A — pedido MP no acreditado

```json
{
  "id": 101,
  "estado": "Pendiente",
  "estadoVisible": "Pendiente de pago",
  "pagado": false,
  "pagoPendiente": true,
  "permiteReintentarPago": true,
  "mpInitPoint": "https://www.mercadopago.com/..."
}
```

UI sugerida:

- badge: `Pendiente de pago`
- botones:
  - `Reintentar pago`
  - `Cancelar pedido`

### Caso B — pedido acreditado y operativo

```json
{
  "id": 98,
  "estado": "Pendiente",
  "pagado": true,
  "pagoPendiente": false,
  "permiteReintentarPago": false
}
```

UI sugerida:

- badge: `Pendiente`
- sin botón de reintento

---

## Compatibilidad

Estos cambios:

- **no requieren migración de BD**
- **no cambian el path de `mi-historial`**
- agregan un endpoint nuevo para reintento
- agregan campos nuevos al contrato JSON

El frontend puede adoptar esto de forma incremental.

---

## Resumen ejecutivo

El frontend debe alinearse a esta idea:

> un pedido de Mercado Pago creado pero no acreditado **NO debe verse como pedido pendiente operativo**, sino como **pendiente de pago**, con posibilidad explícita de **reintentar pago** o **cancelar**.

