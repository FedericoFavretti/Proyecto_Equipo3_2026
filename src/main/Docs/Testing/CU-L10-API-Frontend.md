# CU-L10 — API para Frontend

## Objetivo

Este documento alinea al frontend con el flujo vigente de **atender o rechazar un reclamo** desde el lado del local.

El backend ahora permite dos resoluciones explícitas:

- **Atendido**
- **Rechazado**

y exige reglas distintas para cada una.

> Base path general: `/api/v1`

---

## Regla funcional vigente

Un local solo puede resolver un reclamo si:

- el reclamo existe
- está en estado **`Pendiente`**
- pertenece al **local autenticado**
- el local está **abierto**

Además:

- si la resolución es **`Atendido`**, debe enviarse `tipoCompensacion`
- si la resolución es **`Rechazado`**, debe enviarse `motivoRechazo`

---

## 1. Buscar reclamos para bandeja del local

### Endpoint

`GET /api/v1/reclamos/buscar_reclamo`

### Autorización

- requiere usuario autenticado con rol **Local**

### Parámetros soportados

- `idLocal`
- `idCliente`
- `estadoPedido`
- `estadoReclamo`
- `fechaReclamo`
- `pagina`
- `tamanio`

### Importante para frontend

El backend exige **al menos un filtro**.  
No llamar este endpoint “vacío”.

### Estados de reclamo a mapear

El frontend debe soportar explícitamente:

- `Pendiente`
- `Atendido`
- `Rechazado`

### Recomendación de UI

En la bandeja:

- mostrar acción **Resolver reclamo** solo para `Pendiente`
- para `Atendido` o `Rechazado`, mostrar estado y detalle, pero no ofrecer nueva resolución

---

## 2. Resolver reclamo

### Endpoint

`POST /api/v1/reclamos/resolver_reclamo`

### Autorización

- requiere usuario autenticado con rol **Local**
- el backend toma el local desde la autenticación

### Qué hace

Permite cerrar un reclamo pendiente como:

- **`Atendido`**
- o **`Rechazado`**

---

## 3. Body esperado para cada caso

### Caso A — reclamo atendido

```json
{
  "id": 99,
  "estado": "Atendido",
  "tipoCompensacion": "Reintegro"
}
```

También puede enviarse otro valor funcional de compensación, por ejemplo:

- `Reintegro`
- `Compensación alternativa`

### Caso B — reclamo rechazado

```json
{
  "id": 99,
  "estado": "Rechazado",
  "motivoRechazo": "Se verificó que el pedido fue rehecho y aceptado por el cliente."
}
```

---

## 4. Validaciones que frontend debe respetar

### Siempre requeridos

- `id`
- `estado`

### Si `estado = Atendido`

Requerido:

- `tipoCompensacion`

No usar:

- `motivoRechazo` como reemplazo

### Si `estado = Rechazado`

Requerido:

- `motivoRechazo`

No confiar en:

- reutilizar `motivo` del reclamo del cliente

Ese campo representa el reclamo original, no la respuesta del local.

---

## 5. Qué debe hacer el frontend en UX

### Modal o formulario de resolución

El formulario debería cambiar según la resolución elegida:

#### Si el local elige `Atendido`

Mostrar:

- selector de compensación

Ocultar:

- campo `motivoRechazo`

#### Si el local elige `Rechazado`

Mostrar:

- textarea o input para `motivoRechazo`

Ocultar o deshabilitar:

- selector de compensación

### Recomendación importante

No mandar ambos campos “por las dudas”.

Mandar solo lo que corresponde al estado elegido.

---

## 6. Respuestas y errores esperables

### 200 OK

Resolución aplicada correctamente.

Acción sugerida en frontend:

- cerrar modal
- refrescar lista o detalle
- actualizar badge de estado
- mostrar toast de éxito

---

### 400 Bad Request

Casos esperables:

- faltan datos
- `estado` inválido
- falta `tipoCompensacion` para `Atendido`
- falta `motivoRechazo` para `Rechazado`
- el reclamo ya no está `Pendiente`
- el local está cerrado
- se intenta buscar reclamos sin filtros

Mensajes funcionales relevantes:

- `Debe completar todos los datos.`
- `Debe seleccionar el tipo de resolución (reintegro o compensación).`
- `Debe ingresar un motivo de rechazo.`
- `El reclamo debe estar en estado pendiente.`
- `El local debe estar abierto para poder resolver un reclamo`
- `Debe ingresar algun filtro para obtener los reclamos.`

Acción sugerida:

- mostrar mensaje funcional
- no taparlo con error genérico

---

### 401 Unauthorized

Caso esperable:

- autenticación inexistente, vencida o inválida

Acción sugerida:

- pedir login nuevamente
- redirigir si aplica

---

### 403 Forbidden

Caso esperable:

- el reclamo no pertenece al local autenticado

Mensaje funcional:

- `No puede resolver reclamos de otro local.`

Acción sugerida:

- mostrar mensaje claro
- refrescar bandeja si puede haber desalineación de sesión/contexto

---

### 404 Not Found

Caso esperable:

- reclamo inexistente

Acción sugerida:

- informar que el reclamo ya no existe
- refrescar listado

---

## 7. Contrato visual recomendado

### En listado de reclamos

#### `Pendiente`

- mostrar botón **Atender**
- mostrar botón **Rechazar**

#### `Atendido`

- mostrar estado
- mostrar compensación aplicada
- no permitir nueva resolución

#### `Rechazado`

- mostrar estado
- mostrar `motivoRechazo`
- no permitir nueva resolución

---

## 8. Textos de interfaz a revisar

Actualizar cualquier copy que hoy asuma que todo reclamo termina compensado.

Ejemplos a revisar:

- `Resolver reclamo`
- `Compensar reclamo`
- `El cliente recibirá compensación`
- `Todos los reclamos deben atenderse`

Ahora la UI debe contemplar ambas salidas:

- reclamo atendido
- reclamo rechazado

---

## 9. Ejemplo de flujo recomendado

1. local abre bandeja de reclamos
2. frontend consulta `GET /api/v1/reclamos/buscar_reclamo` con filtros
3. usuario selecciona un reclamo `Pendiente`
4. frontend abre modal de resolución
5. usuario elige:
   - `Atendido` → requiere `tipoCompensacion`
   - `Rechazado` → requiere `motivoRechazo`
6. frontend envía `POST /api/v1/reclamos/resolver_reclamo`
7. si responde `200`, refresca estado

---

## 10. Resumen para implementar rápido

- soportar estados `Pendiente`, `Atendido`, `Rechazado`
- resolver solo reclamos `Pendiente`
- pedir `tipoCompensacion` para `Atendido`
- pedir `motivoRechazo` para `Rechazado`
- no reutilizar `motivo` del cliente como justificación del local
- manejar correctamente `400`, `401`, `403` y `404`
- refrescar bandeja luego de resolver

---

## 11. Fuente de verdad

La fuente de verdad para este flujo ahora es:

- `ReclamoService`
- `ReclamoController`
- `NotificarReclamoService`
- `EstadoReclamo`

Y para frontend esta guía debe considerarse el contrato vigente hasta nuevo cambio funcional.
