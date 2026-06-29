# CU-CL10 — API para Frontend

## Objetivo

Este documento resume la API que frontend necesita para el flujo **Calificar a un Local** con la nueva lógica acordada:

- un cliente puede **calificar un local**
- si ya lo calificó antes, puede **editar su calificación existente**
- frontend puede consultar si ya existe una calificación previa para mostrar **"Calificar"** o **"Editar calificación"**
- frontend puede acceder al **perfil público del local**

> Base path general: `/api/v1`

---

## Regla de negocio vigente

### Comportamiento actual

Para **cliente -> local** la calificación funciona por **par cliente-local**:

- si no existe calificación previa: se crea
- si ya existe una calificación previa: se actualiza

### Restricción importante

El cliente **solo puede calificar un local si tuvo al menos un pedido con ese local**.

### Importante para frontend

La calificación **NO es por pedido**.

Eso significa que:

- no tiene sentido guardar una calificación diferente por cada pedido
- si el usuario entra desde historial de pedidos, la acción debe entenderse como:
  - **"Calificar este local"**
  - no **"Calificar este pedido"**

---

# 1. Obtener perfil público del local

## Endpoint

`GET /api/v1/locales/{idLocal}/perfil`

## Autorización

- requiere usuario autenticado con rol **Cliente**

## Qué hace

Devuelve la información pública del local para renderizar su perfil.

## Path params

- `idLocal`: `number`

## Response 200

```json
{
  "id": 10,
  "nombre": "La Cocina",
  "foto": "https://.../logo.jpg",
  "direccion": {
    "calle": "18 de Julio",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11100"
  },
  "descripcion": "Comida casera",
  "calificacionGlobal": 4.5,
  "estaAbierto": true,
  "imagenes": [
    "https://.../1.jpg",
    "https://.../2.jpg"
  ]
}
```

## Uso recomendado en frontend

Usar este endpoint para la pantalla de:

- detalle/perfil del local
- cabecera del local desde donde se mostrará:
  - botón **Calificar local**
  - o botón **Editar calificación**

## Errores esperables

### 401 Unauthorized
Usuario no autenticado.

### 403 Forbidden
Usuario autenticado pero sin rol Cliente.

### 404 Not Found
Local inexistente.

---

# 2. Consultar mi calificación actual sobre un local

## Endpoint

`GET /api/v1/calificaciones/locales/{idLocal}/mi-calificacion`

## Autorización

- requiere usuario autenticado con rol **Cliente**

## Qué hace

Devuelve la calificación actual que el **cliente autenticado** ya hizo sobre ese local.

Sirve para saber si la UI debe mostrar:

- **Crear calificación**
- o **Editar calificación**

## Path params

- `idLocal`: `number`

## Response 200

Cuando **ya existe** calificación previa:

```json
{
  "id": 8,
  "puntaje": 5,
  "comentario": "Excelente",
  "fecha": "2026-06-28T12:00:00"
}
```

## Response 204 No Content

Cuando **todavía no existe** una calificación del cliente sobre ese local.

> Este caso NO es error.
> Frontend debe interpretarlo como: **el usuario puede crear una nueva calificación**.

## Errores esperables

### 401 Unauthorized
Usuario no autenticado.

### 403 Forbidden
Usuario autenticado pero sin rol Cliente.

### 404 Not Found
Local inexistente.

## Uso recomendado en frontend

### Flujo recomendado

1. abrir perfil del local
2. llamar a `GET /api/v1/locales/{idLocal}/perfil`
3. llamar a `GET /api/v1/calificaciones/locales/{idLocal}/mi-calificacion`
4. si responde:
   - `204`: mostrar **"Calificar local"**
   - `200`: mostrar **"Editar calificación"** y precargar formulario

---

# 3. Crear o editar calificación de un local

## Endpoint

`POST /api/v1/calificaciones/calificar`

## Autorización

- requiere usuario autenticado
- para este flujo puntual debe ser un usuario con rol **Cliente**

## Qué hace

Para **cliente -> local** este endpoint hace **upsert**:

- si no existe calificación previa para ese cliente/local: **crea**
- si ya existe: **actualiza** la existente

## Body recomendado para frontend

```json
{
  "puntaje": 5,
  "comentario": "Muy buena atención",
  "dtLocal": {
    "id": 10
  }
}
```

## Campos necesarios en este flujo

### requeridos
- `puntaje`: entero de `1` a `5`
- `dtLocal.id`: id del local

### opcionales
- `comentario`: string

## Campos que frontend NO necesita mandar

Para el caso **cliente califica local**, frontend **no necesita** enviar:

- `tipo`
- `dtCliente`
- `fecha`
- `id`

El backend los resuelve o los ingresa internamente según el usuario autenticado.

## Response 204 No Content

Cuando la operación salió bien.

Aplica tanto para:

- creación
- edición

## Errores esperables

### 400 Bad Request

Casos típicos:

#### puntaje inválido
```json
{
  "mensaje": "El puntaje debe estar comprendido entre 1 y 5.",
  "status": 400,
  "timestamp": "2026-06-28T19:00:00",
  "path": "uri=/api/v1/calificaciones/calificar"
}
```

#### cliente sin pedidos previos en el local
```json
{
  "mensaje": "Solo puede calificar locales en los que haya realizado al menos un pedido.",
  "status": 400,
  "timestamp": "2026-06-28T19:00:00",
  "path": "uri=/api/v1/calificaciones/calificar"
}
```

#### local faltante en request
```json
{
  "mensaje": "Debe indicar el local a calificar.",
  "status": 400,
  "timestamp": "2026-06-28T19:00:00",
  "path": "uri=/api/v1/calificaciones/calificar"
}
```

### 401 Unauthorized
Usuario no autenticado.

### 404 Not Found
Local inexistente.

---

# 4. Resumen de UX recomendado para frontend

## En perfil del local

### Caso A — no existe calificación previa
- mostrar botón: **Calificar local**
- abrir modal/formulario vacío
- enviar POST al guardar

### Caso B — ya existe calificación previa
- mostrar botón: **Editar calificación**
- abrir modal/formulario precargado con:
  - `puntaje`
  - `comentario`
- enviar POST al guardar

## En historial de pedidos

Si desde un pedido se ofrece acción de calificación, el copy recomendado es:

- **Calificar local**
- o **Editar calificación del local**

NO usar:

- **Calificar pedido**

Porque la lógica backend actual **no califica por pedido**.

---

# 5. Contrato resumido para implementar rápido

## Consultar perfil del local
- `GET /api/v1/locales/{idLocal}/perfil`
- rol: `Cliente`
- devuelve datos del local

## Consultar si ya califiqué ese local
- `GET /api/v1/calificaciones/locales/{idLocal}/mi-calificacion`
- rol: `Cliente`
- `200` si existe
- `204` si no existe

## Crear o editar calificación
- `POST /api/v1/calificaciones/calificar`
- rol: autenticado (`Cliente` en este flujo)
- body mínimo:

```json
{
  "puntaje": 4,
  "comentario": "Muy bien",
  "dtLocal": {
    "id": 10
  }
}
```

- `204` si salió bien

---

# 6. Nota importante para frontend

Hoy el backend resolvió la funcionalidad como:

- **una calificación editable por cliente-local**

No como:

- una calificación por pedido

Si más adelante negocio quiere calificar por pedido, habrá que cambiar contrato, modelo y UX.

---

# 7. Archivos backend relacionados

Por si frontend necesita rastrear el origen:

- `src/main/java/com/example/demo/Logica/Controllers/LocalController.java`
- `src/main/java/com/example/demo/Logica/Controllers/CalificacionController.java`
- `src/main/java/com/example/demo/Logica/Service/LocalService.java`
- `src/main/java/com/example/demo/Logica/Service/CalificacionService.java`
- `src/main/java/com/example/demo/Logica/DataTypes/response/DtLocalPerfilResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/response/DtMiCalificacionLocalResponse.java`


