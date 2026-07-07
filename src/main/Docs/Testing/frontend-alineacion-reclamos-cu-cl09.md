# Alineación Frontend — CU-CL09 Realizar Reclamo

## Objetivo

Alinear el frontend con la nueva regla funcional y técnica del backend para **realizar reclamos**.

---

## Regla funcional vigente

Un cliente **solo puede realizar un reclamo** si el pedido:

- está en estado **`Confirmado`** o **`Entregado`**
- **pertenece al cliente autenticado**
- **no tiene un reclamo previo**

En cualquier otro caso, el backend rechazará la operación.

---

## Qué debe hacer el frontend

### 1. Mostrar la acción `Realizar Reclamo` solo para pedidos válidos

El botón, enlace o CTA para reclamar debe mostrarse **solo** cuando el pedido esté en:

- `Confirmado`
- `Entregado`

No debe mostrarse para:

- `Pendiente`
- `Cancelado`
- `Rechazado`

---

### 2. No confiar en la UI como validación final

Aunque el frontend oculte la acción para estados inválidos, la validación real vive en backend.

Por lo tanto, el frontend debe asumir que el endpoint puede responder error y debe manejarlo correctamente.

---

### 3. Ocultar la acción si ya existe reclamo para ese pedido

Si el frontend ya conoce que el pedido tiene reclamo asociado, debe evitar mostrar nuevamente la acción `Realizar Reclamo`.

Referencia útil del backend:

- `GET /api/v1/reclamos/mi-reclamo/{idPedido}`

Comportamiento esperado:

- si existe reclamo: mostrar estado/detalle del reclamo y **no** ofrecer crear otro
- si no existe reclamo: permitir la acción solo si el pedido además está en `Confirmado` o `Entregado`

---

## Endpoint involucrado

### Crear reclamo

- **Método:** `POST`
- **Ruta:** `/api/v1/reclamos/realizar_reclamo`

### Body esperado

```json
{
  "motivo": "El pedido llegó frío",
  "tipoCompensacion": "Reintegro",
  "dtPedido": {
    "id": 44
  }
}
```

Notas:

- `motivo` es obligatorio
- `dtPedido.id` es obligatorio
- el backend toma el cliente desde la autenticación, **no** desde el body

---

## Respuestas y manejo de errores

### 200 OK

Reclamo creado correctamente.

Acción sugerida en frontend:

- mostrar confirmación
- refrescar el detalle o historial del pedido
- reemplazar CTA de reclamo por estado del reclamo

---

### 400 Bad Request

Casos esperables:

- motivo vacío
- pedido en estado inválido para reclamo
- datos incompletos

Mensajes funcionales relevantes:

- `Debe ingresar un motivo.`
- `Solo se pueden realizar reclamos sobre pedidos confirmados o entregados.`
- `Debe completar todos los datos.`

Acción sugerida en frontend:

- mostrar mensaje funcional al usuario
- no mostrar error técnico genérico

---

### 401 Unauthorized

Caso esperable:

- sesión inexistente, vencida o inválida

Acción sugerida en frontend:

- pedir reautenticación
- redirigir a login si aplica

---

### 403 Forbidden

Caso esperable:

- intento de reclamar un pedido que no pertenece al cliente autenticado

Mensaje funcional:

- `No puede realizar reclamos sobre pedidos que no le pertenecen.`

Acción sugerida en frontend:

- mostrar mensaje claro
- registrar el caso si tienen observabilidad de errores funcionales

---

### 409 Conflict

Caso esperable:

- ya existe un reclamo para ese pedido

Mensaje funcional:

- `Ya existe un reclamo para este pedido.`

Acción sugerida en frontend:

- ocultar CTA de creación
- redirigir o mostrar el reclamo existente si la pantalla lo soporta

---

## Regla de render recomendada

La decisión de UI para mostrar `Realizar Reclamo` debería seguir esta lógica:

1. el usuario está autenticado
2. el pedido pertenece al usuario logueado
3. el pedido está en `Confirmado` o `Entregado`
4. el pedido no tiene reclamo previo

Si cualquiera de esas condiciones falla:

- no mostrar el CTA de creación de reclamo

---

## Textos de interfaz a actualizar

Si hoy existen textos del tipo:

- `Solo puedes reclamar pedidos confirmados`

deben cambiarse a:

- `Solo puedes reclamar pedidos confirmados o entregados`

También conviene revisar:

- tooltips
- textos vacíos
- helpers del formulario
- mensajes de validación
- documentación interna del frontend

---

## Estados a mapear correctamente

El frontend debe validar que sus enums, mappers o badges estén alineados con los valores reales del backend:

- `Confirmado`
- `Entregado`
- `Pendiente`
- `Cancelado`
- `Rechazado`

No asumir nombres alternativos sin verificar el contrato real.

---

## Recomendación de UX

Si el pedido ya tiene reclamo:

- mostrar acceso a **ver reclamo**
- mostrar estado del reclamo si ya está disponible
- no ofrecer una segunda creación

Eso evita fricción y además alinea la experiencia con la regla de negocio de **un reclamo por pedido**.

---

## Resumen para implementación frontend

1. habilitar reclamo solo para `Confirmado` y `Entregado`
2. ocultar reclamo si ya existe uno para ese pedido
3. manejar correctamente `400`, `401`, `403` y `409`
4. actualizar textos de UX
5. no asumir que la UI reemplaza la validación del backend

---

## Fuente de verdad

La fuente de verdad para esta funcionalidad ahora es:

- backend endurecido en `ReclamoService` y `ReclamoController`
- documentación funcional actualizada del **CU-CL09**

Si el frontend se aparta de esta regla, volverá a aparecer inconsistencia entre UI, API y testing.
