# CU-L08 — API y Comportamiento Esperado para Frontend

## Objetivo

Este documento explica cómo frontend debe implementar correctamente el flujo **Rechazar Pedido de Cliente** para que quede alineado con el caso de uso y con el contrato actual del backend.

El problema reportado por testing fue:

> si se selecciona **"Otro"** como motivo, se debe permitir escribir un motivo personalizado.

---

## Resumen ejecutivo

### Qué exige el caso de uso

La guía funcional indica que el actor Local debe poder:

- **seleccionar motivo**
- o **escribir motivo**

Referencia funcional:

- `src/main/Docs/Guia/GuiaCasosDeUso.md`
  - paso: **"Seleccionar o escribir motivo"**
  - validación: **"Debe seleccionar o escribir un motivo de rechazo antes de continuar."**

### Qué soporta hoy el backend

El backend actual ya acepta un motivo libre como texto:

- request con un único campo `motivo`
- validación de que `motivo` no venga vacío
- notificación al cliente usando exactamente ese texto

### Conclusión

El gap reportado por testing **no parece estar en backend**, sino en la **interfaz frontend** o en el flujo UX.

SI frontend envía literalmente `"Otro"` como motivo, el cliente terminará recibiendo una notificación genérica y pobre, por ejemplo:

> Motivo: Otro

Eso NO representa correctamente la intención del caso de uso.

---

# 1. Contrato actual de backend

## Endpoint

`POST /api/v1/pedidos/{idPedido}/rechazar`

> Base path general: `/api/v1`

## Autorización

- requiere usuario autenticado con rol **Local**

## Path params

- `idPedido`: `number`

## Request body actual

```json
{
  "motivo": "No contamos con disponibilidad para prepararlo"
}
```

## Regla de validación vigente

Backend rechaza la operación si:

- `motivo` es `null`
- `motivo` está vacío
- `motivo` tiene solo espacios

## Qué NO valida hoy backend

Backend **no distingue** entre:

- motivo predefinido
- motivo escrito manualmente
- valor literal `"Otro"`

Para backend, todo eso hoy es simplemente un `String motivo`.

---

# 2. Problema funcional detectado

## Escenario incorrecto

Si frontend presenta una lista de motivos como:

- Sin stock
- Demora excesiva
- Local cerró
- Otro

y cuando el usuario selecciona **"Otro"**:

- no muestra campo adicional
- o muestra campo pero igual envía `"Otro"`

entonces el flujo queda MAL implementado.

## Por qué está mal

Porque el caso de uso no dice solo **"seleccionar motivo"**.

Dice **"seleccionar o escribir motivo"**.

Eso significa que **"Otro"** no es un motivo final: es un disparador para capturar texto libre.

---

# 3. Comportamiento recomendado para frontend

## Regla principal

Frontend **nunca debería enviar `"Otro"` como motivo final**.

Si el usuario elige un motivo estándar:

- se envía ese texto en `motivo`

Si el usuario elige **"Otro"**:

- se debe mostrar un campo de texto o textarea obligatorio
- el valor enviado en `motivo` debe ser el texto escrito por el usuario

## Flujo esperado

1. El local presiona **Rechazar Pedido**
2. Frontend muestra motivos predefinidos
3. Si el usuario elige un motivo estándar:
   - no se muestra textarea adicional
4. Si el usuario elige **"Otro"**:
   - se muestra textarea/input para motivo personalizado
5. Si no hay motivo efectivo:
   - frontend no debe permitir confirmar
6. Al confirmar:
   - se envía un único `motivo` textual al backend

---

# 4. Solución recomendada

## Opción recomendada ahora

Resolverlo **solo en frontend**, sin cambiar la API.

## Motivo

Esta opción es la mejor en este momento porque:

- el backend ya soporta texto libre
- no requiere cambio de contrato
- no obliga a tocar controller, service ni tests backend
- cierra el bug reportado con impacto mínimo

## Regla de implementación

### Caso A — motivo estándar

Ejemplo:

```json
{
  "motivo": "Sin disponibilidad"
}
```

### Caso B — motivo personalizado

Si el usuario elige **"Otro"** y escribe:

`El horno presentó una falla y no podemos preparar el pedido`

frontend debe enviar:

```json
{
  "motivo": "El horno presentó una falla y no podemos preparar el pedido"
}
```

### Caso incorrecto que debe evitarse

```json
{
  "motivo": "Otro"
}
```

---

# 5. Validaciones que frontend DEBE aplicar

## Validación funcional mínima

Frontend debe impedir confirmar si:

- no se eligió motivo
- se eligió **"Otro"** y el texto está vacío
- se eligió **"Otro"** y el texto tiene solo espacios

## Mensaje recomendado

Usar el mismo mensaje funcional del caso de uso:

`Debe seleccionar o escribir un motivo de rechazo antes de continuar.`

## Buenas prácticas adicionales

También se recomienda:

- hacer `trim()` visual/lógico antes de enviar
- limpiar el texto personalizado si el usuario cambia de **"Otro"** a un motivo estándar
- no conservar texto residual oculto que pueda enviarse por error

---

# 6. Criterios de aceptación para frontend

## Debe cumplir

1. Si el local selecciona un motivo estándar, puede rechazar el pedido.
2. Si el local selecciona **"Otro"**, aparece un campo para escribir.
3. Si el local selecciona **"Otro"** y no escribe nada, no puede confirmar.
4. Si el local selecciona **"Otro"** y escribe texto válido, ese texto se envía en `motivo`.
5. La API no debe recibir el literal `"Otro"` como motivo final.
6. La notificación/correo resultante debe contener el motivo real escrito por el local.
7. Si el usuario cambia de **"Otro"** a un motivo estándar, el motivo final enviado debe ser el estándar seleccionado.

---

# 7. Casos de prueba sugeridos para frontend

## Caso 1 — motivo predefinido

- seleccionar: `Sin disponibilidad`
- confirmar rechazo
- request esperado:

```json
{
  "motivo": "Sin disponibilidad"
}
```

## Caso 2 — otro con texto válido

- seleccionar: `Otro`
- escribir: `Se cortó la energía en el local`
- confirmar rechazo
- request esperado:

```json
{
  "motivo": "Se cortó la energía en el local"
}
```

## Caso 3 — otro sin texto

- seleccionar: `Otro`
- dejar el campo vacío
- intentar confirmar
- comportamiento esperado:
  - no enviar request
  - mostrar mensaje funcional

## Caso 4 — otro con espacios

- seleccionar: `Otro`
- escribir: `   `
- intentar confirmar
- comportamiento esperado:
  - no enviar request
  - mostrar mensaje funcional

## Caso 5 — cambio de otro a motivo estándar

- seleccionar: `Otro`
- escribir texto
- cambiar selección a `Sin stock`
- confirmar
- request esperado:

```json
{
  "motivo": "Sin stock"
}
```

---

# 8. Alternativas futuras

## Alternativa A — mantener contrato actual

Seguir usando:

```json
{
  "motivo": "string libre"
}
```

### Ventajas

- simple
- bajo impacto
- suficiente para cerrar este bug

### Desventajas

- no permite distinguir analíticamente si el motivo fue predefinido o libre

## Alternativa B — contrato más explícito

En el futuro podría modelarse algo como:

- tipo de motivo
- motivo personalizado

Eso serviría si negocio quiere:

- métricas por categoría
- auditoría más precisa
- reportes de razones de rechazo

PERO hoy sería un cambio de mayor alcance y NO es necesario para resolver el problema reportado.

---

# 9. Recomendación final

La recomendación para frontend es:

- mantener el contrato actual con backend
- tratar **"Otro"** como una opción de UI, no como el valor final de negocio
- enviar siempre en `motivo` el texto real que debe recibir el cliente

En otras palabras:

**"Otro" no es el motivo. "Otro" es la señal para pedir el motivo real.**

---

# 10. Referencias verificadas

- `src/main/Docs/Guia/GuiaCasosDeUso.md`
- `src/main/java/com/example/demo/Logica/Controllers/PedidoController.java`
- `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
- `src/main/java/com/example/demo/Logica/DataTypes/request/DtRechazarPedidoRequest.java`
- `src/main/java/com/example/demo/Logica/Service/NotificacionPedidoService.java`
- `src/main/Docs/Testing/ListaCUTesteados.md`
