# Backend necesario para frontend

> Documento verificado contra el código actual del backend el **27/06/2026**.  
> Objetivo: que frontend consuma lo que **REALMENTE** existe hoy, no lo que “debería existir”.

---

## 1. Base técnica mínima

- **Base URL local:** `http://localhost:8080`
- **Prefijo API:** `/api/v1`
- **Content-Type JSON:** `application/json`
- **Auth:** `Authorization: Bearer <jwt>`
- **CORS permitidos hoy:**
  - `http://localhost:3000`
  - `http://localhost:5173`
  - `http://127.0.0.1:5173`
  - `https://frontend-proyecto-foodly-test.up.railway.app`
- **Métodos CORS habilitados:** `GET, POST, PUT, DELETE, PATCH, OPTIONS`
- **Headers permitidos:** `Authorization, Content-Type, Accept`
- **Header expuesto:** `Authorization`
- **Archivos:**
  - multipart general: máximo `10MB` por archivo
  - request multipart: máximo `20MB`
  - foto de perfil: validación propia de backend `<= 5MB`, `jpg/jpeg/png/gif`
  - imágenes de local/plato: `jpg/jpeg/png`

---

## 2. Autenticación y roles

### Login

`POST /api/v1/usuarios/login`

```json
{
  "email": "cliente@mail.com",
  "passwd": "Clave123"
}
```

Respuesta:

```json
{
  "id": 1,
  "token": "jwt",
  "tipo": "cliente",
  "email": "cliente@mail.com"
}
```

### Cómo usar el token

- Guardar `token`
- Enviar siempre:

```http
Authorization: Bearer <token>
```

### Roles reales del backend

- `Admin`
- `Local`
- `Cliente`

### IMPORTANTE

- `login.tipo` devuelve el tipo persistido (`cliente`, `local`, etc.), **no** el authority Spring.
- Si el usuario está `Pendiente` o `Bloqueado`, login falla.
- La expiración JWT configurada por default es **45 minutos** (`2700000 ms`).
- `logout` y cambios de credenciales invalidan sesiones por fecha; frontend debe limpiar sesión local al recibir `401`.

---

## 3. Formato de errores

El backend maneja errores con esta estructura:

```json
{
  "mensaje": "Texto del error",
  "status": 400,
  "timestamp": "2026-06-27T17:00:00",
  "path": "uri=/api/v1/..."
}
```

### Mapeo de estados

- `400` reglas de negocio / validación
- `401` no autenticado / credenciales incorrectas
- `403` sin permisos
- `404` recurso no encontrado
- `409` conflicto de estado o duplicado
- `402` pago rechazado
- `503` servicio externo caído
- `500` error interno

> OJO: algunos tests viejos usan `message`, pero el handler real devuelve **`mensaje`**.

---

## 4. Enums que frontend debe respetar

### `EstadoCuenta`

- `Activo`
- `Pendiente`
- `Bloqueado`

### `EstadoLocal`

- `Pendiente`
- `Habilitado`
- `Rechazado`
- `Bloqueado`

### `EstadoPedido`

- `Pendiente`
- `Confirmado`
- `Rechazado`
- `Cancelado`

### `TipoCalificacion`

- `Cliente_a_local`
- `Local_a_cliente`

### `CanalNotificacion`

- `Email`
- `Web`
- `Push_mobile`

### `TipoNotificacion`

- `Pedido`
- `Reclamo`

---

## 5. Reglas de serialización que frontend debe asumir

- `LocalDateTime` sale como string ISO-8601, por ejemplo: `2026-06-27T17:10:00`
- `LocalDate` entra/sale como `YYYY-MM-DD`
- `Duration` probablemente sale como ISO-8601, por ejemplo: `PT30M`
- En requests JSON, si un campo no es necesario, **mejor omitirlo**

---

## 6. Contratos globales IMPORTANTES

### 6.1 Multipart con `@RequestPart("datos")`

Estos endpoints **NO** esperan un JSON puro:

- `POST /api/v1/clientes/registro`
- `POST /api/v1/locales/solicitudes-habilitacion`
- `POST /api/v1/locales/platos`
- `PUT /api/v1/locales/platos/{idPlato}`

Frontend debe enviar `FormData` con:

- una parte `datos` con JSON
- una parte `foto` o `imagenes` con archivos

Ejemplo conceptual:

```ts
const form = new FormData();
form.append("datos", new Blob([JSON.stringify(payload)], { type: "application/json" }));
form.append("foto", file);
```

o

```ts
imagenes.forEach(img => form.append("imagenes", img));
```

### 6.2 Edición de perfil NO usa `datos`

`PUT /api/v1/usuarios/perfil` espera:

- campos simples como `RequestParam`
- opcional `foto`

O sea: enviar `FormData` con keys planas:

- `nombre`
- `apellido`
- `email`
- `password`
- `direccion.calle`
- `direccion.numero`
- `direccion.ciudad`
- `direccion.codigoPostal`
- `descripcion` (solo local)
- `foto` (opcional)

**NO** enviar un objeto JSON anidado llamado `datos`.

### 6.3 Inconsistencia real de nombres de password

El backend NO es consistente. Frontend debe mapear así:

- login: `passwd`
- registro: `passwd`
- editar perfil: `password`
- recuperación por token: `nuevaPasswd`
- cambio autenticado:
  - `passwdActual`
  - `passwdNueva`
  - `passwdConfirmacion`

Si frontend unifica esto “por intuición”, ROMPE.

---

## 7. Endpoints públicos

## 7.1 Usuarios

### `POST /api/v1/usuarios/login`
- Auth: no
- Body: `DtLoginRequest`
- Response: `DtLoginResponse`

### `POST /api/v1/usuarios/activar?email=correo@dominio.com`
- Auth: no
- Sin body
- Response: texto plano

### `POST /api/v1/usuarios/recuperar_contra_correo`
- Auth: no
- Body: **string plano JSON**, ejemplo:

```json
"cliente@mail.com"
```

### `POST /api/v1/usuarios/recuperar`
- Auth: no

```json
{
  "token": "token-recuperacion",
  "nuevaPasswd": "NuevaClave123"
}
```

## 7.2 Cliente

### `POST /api/v1/clientes/registro`
- Auth: no
- Multipart:
  - `datos`: `DtCliente`
  - `foto`: archivo obligatorio
- Backend completa/override:
  - `activo=false`
  - `estadoCuenta=Pendiente`
  - `tipo=cliente`
- Response: **entidad `Cliente`**

`DtCliente` útil para enviar:

```json
{
  "email": "cliente@mail.com",
  "passwd": "Clave123",
  "documento": "12345678",
  "nombre": "Ana",
  "apellido": "Pérez",
  "direccion": {
    "calle": "Av. Italia",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11600"
  }
}
```

### `POST /api/v1/clientes/google`
- Auth: no
- Body: `DtCliente`
- **Estado real hoy:** endpoint sin implementar; service devuelve `null`
- **Recomendación frontend:** no usarlo / ocultarlo hasta implementarlo

## 7.3 Local

### `POST /api/v1/locales/solicitudes-habilitacion`
- Auth: no
- Multipart:
  - `datos`: `DtLocal`
  - `imagenes`: lista de archivos
- Backend completa/override:
  - `estadoCuenta=Pendiente`
  - `estadoLocal=Pendiente`
  - `tipo=local`
  - `estaAbierto=false`
  - `calificacionGlobal=0.0`

Payload útil:

```json
{
  "email": "local@mail.com",
  "passwd": "Clave123",
  "nombre": "Foodly Pocitos",
  "descripcion": "Comida casera",
  "direccion": {
    "calle": "26 de Marzo",
    "numero": "3210",
    "ciudad": "Montevideo",
    "codigoPostal": "11300"
  }
}
```

---

## 8. Endpoints autenticados por módulo

## 8.1 Usuarios

### `POST /api/v1/usuarios/logout`
- Auth: cualquier usuario autenticado
- Header obligatorio `Authorization`
- Response: `200`

### `PUT /api/v1/usuarios/perfil`
- Auth: cualquier usuario autenticado
- Multipart `FormData`
- Campos permitidos:
  - **Cliente:** `nombre`, `apellido`, `email`, `password`, `direccion.*`
  - **Local:** `nombre`, `descripcion`, `email`, `password`, `direccion.*`
  - **Admin:** `email`, `password`
- Si cambia `email` o `password`, backend invalida sesiones
- Foto opcional

### `DELETE /api/v1/usuarios/mi-cuenta`
- Auth: solo clientes en la práctica
- Response: `204`
- Falla si hay pedidos activos o reclamos pendientes

### Cambio de contraseña autenticado

#### `POST /api/v1/usuarios/cambiar-passwd/iniciar`

```json
{
  "idUsuario": 1,
  "passwdActual": "Clave123"
}
```

#### `POST /api/v1/usuarios/cambiar-passwd/verificar-codigo`

```json
{
  "idUsuario": 1,
  "codigo": "123456"
}
```

- vence a los 10 minutos
- al tercer error bloquea 15 minutos

#### `POST /api/v1/usuarios/cambiar-passwd/confirmar`

```json
{
  "idUsuario": 1,
  "passwdNueva": "NuevaClave123",
  "passwdConfirmacion": "NuevaClave123"
}
```

Regla de password: mínimo 8 caracteres, una mayúscula y un número.

---

## 8.2 Cliente

### `POST /api/v1/clientes/busqueda`
- Auth: `Cliente`
- Body:

```json
{
  "nombre": "mila",
  "precioMasBajo": false,
  "precioMasAlto": false,
  "promocionActiva": true,
  "alfabetico": true,
  "dtLocal": {
    "id": 10
  }
}
```

- Response:

```json
{
  "platos": [],
  "promociones": []
}
```

### `POST /api/v1/clientes/listar_locales`
- Auth: `Cliente`
- Body:

```json
{
  "nombre": "food",
  "calificacionMinima": 4.0,
  "estaAbierto": true,
  "ordenarPor": "nombre",
  "direccion": "asc"
}
```

- `ordenarPor` válidos: `nombre`, `calificacion`
- `direccion` válidos: `asc`, `desc`
- Response item:

```json
{
  "id": 1,
  "nombre": "Foodly",
  "direccion": {
    "calle": "Av. Italia",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11600"
  },
  "descripcion": "Comida casera",
  "calificacionGlobal": 4.5,
  "estaAbierto": true,
  "imagenes": ["https://..."]
}
```

---

## 8.3 Local

### Platos

#### `POST /api/v1/locales/platos`
- Auth: `Local`
- Multipart:
  - `datos`: `DtPlato`
  - `imagenes`: lista de archivos

Payload mínimo útil:

```json
{
  "nombre": "Milanesa al pan",
  "descripcion": "Con fritas",
  "precio": 450,
  "disponible": true,
  "dtLocal": {
    "id": 5
  }
}
```

- Response: entidad `Plato`
- Validaciones:
  - nombre obligatorio
  - precio > 0
  - imágenes obligatorias
  - local debe estar `Habilitado`

#### `PUT /api/v1/locales/platos/{idPlato}`
- Auth: `Local`
- Igual contrato que alta
- `imagenes` opcional
- Si no se envían nuevas imágenes, se mantienen las anteriores

#### `DELETE /api/v1/locales/platos/{idPlato}`
- Auth: `Local`
- No elimina físicamente: marca `disponible=false`

### Promociones

#### `POST /api/v1/locales/promociones`
#### `PUT /api/v1/locales/promociones/{idPromocion}`

Body:

```json
{
  "idPlato": 10,
  "descuento": 20,
  "fechaInicio": "2026-06-27T18:00:00",
  "fechaFin": "2026-06-30T23:59:59",
  "descripcion": "Promo finde"
}
```

Validaciones:

- descuento entre `1` y `100`
- `fechaFin >= fechaInicio`

### Apertura y cierre

#### `PUT /api/v1/locales/{idLocal}/apertura`
- Auth: `Local`
- local debe estar `Habilitado`

#### `PUT /api/v1/locales/{idLocal}/cierre`
- Auth: `Local`
- falla si hay pedidos pendientes

### Estadísticas

#### `GET /api/v1/locales/estadisticas/{idLocal}`
- Auth: `Local`
- Response:

```json
{
  "platosMasPedido": [],
  "gananciasMensuales": 0.0
}
```

### Clientes de un local

#### `POST /api/v1/locales/{idLocal}/clientes`
- Auth: `Local`
- Body:

```json
{
  "nombre": "ana",
  "calificacionMinima": 4.0,
  "ordenarPor": "nombre",
  "direccion": "asc"
}
```

- Response item:

```json
{
  "id": 1,
  "nombre": "Ana",
  "apellido": "Pérez",
  "calificacionGlobal": 4.8
}
```

---

## 8.4 Pedidos

### Crear pedido

#### `POST /api/v1/pedidos`
- Auth: `Cliente`

Body mínimo RECOMENDADO:

```json
{
  "dtPedido": {
    "domicilioEntrega": {
      "calle": "Av. Brasil",
      "numero": "2020",
      "ciudad": "Montevideo",
      "codigoPostal": "11300"
    },
    "medioDePago": "EFECTIVO",
    "pagoSimulado": false,
    "dtLocal": {
      "id": 5
    },
    "dtCliente": {
      "id": 3
    }
  },
  "detalles": [
    {
      "cantidad": 2,
      "dtPlato": {
        "id": 10
      }
    }
  ]
}
```

### Lo que frontend NO debe mandar como fuente de verdad

Aunque existan en DTO, backend genera o recalcula:

- `id`
- `fecha`
- `estado`
- `tiempoEstEntrega`
- `total`
- `precioUnitario`
- `subtotal`

### Respuesta

`DtPedidoResponse`

```json
{
  "id": 99,
  "fecha": "2026-06-27T17:30:00",
  "tiempoEstEntrega": null,
  "total": 900.0,
  "domicilioEntrega": {
    "calle": "Av. Brasil",
    "numero": "2020",
    "ciudad": "Montevideo",
    "codigoPostal": "11300"
  },
  "medioDePago": "EFECTIVO",
  "pagoSimulado": false,
  "estado": "Pendiente",
  "local": {
    "id": 5,
    "nombre": "Foodly Pocitos"
  },
  "cliente": {
    "id": 3,
    "nombre": "Ana",
    "apellido": "Pérez"
  },
  "detalles": null
}
```

> IMPORTANTE: `DtPedidoResponse` tiene campo `detalles`, PERO el mapper actual **no lo llena**. Frontend NO debe depender de ese campo hoy.

### Confirmar pedido

#### `POST /api/v1/pedidos/{idPedido}/confirmar`
- Auth: `Local`

```json
{
  "tiempoEstimadoEntregaMinutos": 30
}
```

- marca:
  - `pagoSimulado=true`
  - `estado=Confirmado`

### Rechazar pedido

#### `POST /api/v1/pedidos/{idPedido}/rechazar`
- Auth: `Local`

```json
{
  "motivo": "Sin stock"
}
```

### Cancelar pedido

#### `POST /api/v1/pedidos/{idPedido}/cancelar`
- Auth: `Cliente`
- solo si sigue `Pendiente`

### Listado para local

#### `GET /api/v1/pedidos/listar-pedido-local/{idLocal}`
- Auth: `Local`
- Filtros por query string:
  - `estado`
  - `fechaDesde`
  - `fechaHasta`
  - `ordenarPor`
  - `direccion`

Ejemplo:

`/api/v1/pedidos/listar-pedido-local/5?estado=Pendiente&fechaDesde=2026-06-01&fechaHasta=2026-06-30&ordenarPor=fecha&direccion=desc`

### Historial propio cliente

#### `GET /api/v1/pedidos/mi-historial`
- Auth: `Cliente`
- mismos filtros por query string

`ordenarPor` válidos:

- `fecha`
- `total`
- `estado`

`direccion` válidos:

- `asc`
- `desc`

Response item:

```json
{
  "id": 99,
  "fecha": "2026-06-27T17:30:00",
  "estado": "Pendiente",
  "total": 900.0,
  "tiempoEstEntrega": "PT30M",
  "cliente": {
    "id": 3,
    "nombre": "Ana",
    "apellido": "Pérez"
  },
  "local": {
    "id": 5,
    "nombre": "Foodly Pocitos"
  },
  "cantidadItems": 2
}
```

---

## 8.5 Reclamos

### `POST /api/v1/reclamos/realizar_reclamo`
- Auth: `Cliente`

Payload mínimo útil:

```json
{
  "motivo": "Pedido incompleto",
  "tipoCompensacion": "Reintegro",
  "dtPedido": {
    "id": 99
  }
}
```

Notas:

- backend completa `montoReintegro` con `pedido.total`
- backend completa `fecha` con `now`

### `POST /api/v1/reclamos/buscar_reclamo`
- Auth: `Local`

```json
{
  "idCliente": 3,
  "estadoPedido": "Confirmado",
  "fechaReclamo": "2026-06-27"
}
```

- Debe venir al menos un filtro

### `POST /api/v1/reclamos/resolver_reclamo`
- Auth: `Local`
- Body: `DtReclamo`
- Para resolver, enviar al menos `id` y los datos finales que backend deba persistir

---

## 8.6 Calificaciones

### `POST /api/v1/calificaciones/calificar`
- Auth: `Cliente` o `Local`

Si autentica **cliente**, enviar:

```json
{
  "puntaje": 5,
  "comentario": "Excelente",
  "dtLocal": {
    "id": 5
  }
}
```

Si autentica **local**, enviar:

```json
{
  "puntaje": 4,
  "comentario": "Buen trato",
  "dtCliente": {
    "id": 3
  }
}
```

Notas:

- backend completa `tipo`
- backend completa `fecha`
- `puntaje` debe estar entre `1` y `5`
- response `204`

### `GET /api/v1/calificaciones/local/mi-calificacion`
- Auth: `Local`
- Response:

```json
{
  "calificacionGlobal": 4.5,
  "totalValoraciones": 10,
  "detallePorPuntuacion": {
    "1": 0,
    "2": 1,
    "3": 1,
    "4": 3,
    "5": 5
  }
}
```

### `GET /api/v1/calificaciones/{idCliente}/calificacion`
- Auth: `Cliente`
- Response:

```json
{
  "promedio": 4.2,
  "totalCalificaciones": 8,
  "detallePorPuntuacion": {
    "1": 0,
    "2": 1,
    "3": 2,
    "4": 3,
    "5": 2
  }
}
```

---

## 8.7 Admin

### `GET /api/v1/admins/solicitudes-locales/pendientes`
- Auth: `Admin`

Response item:

```json
{
  "id": 5,
  "email": "local@mail.com",
  "nombre": "Foodly Pocitos",
  "direccion": {
    "calle": "26 de Marzo",
    "numero": "3210",
    "ciudad": "Montevideo",
    "codigoPostal": "11300"
  },
  "descripcion": "Comida casera",
  "imagenes": ["https://..."]
}
```

### `PUT /api/v1/admins/solicitudes-locales/resolver_solicitud`
- Auth: `Admin`

```json
{
  "idLocal": 5,
  "estadoObjetivo": "Habilitado"
}
```

`estadoObjetivo` válidos:

- `Habilitado`
- `Rechazado`

### `POST /api/v1/admins/cuentas-usuario/resolver`
- Auth: `Admin`

```json
{
  "id": 3,
  "activo": false
}
```

### `POST /api/v1/admins/usuarios`
- Auth: `Admin`

```json
{
  "texto": "ana",
  "tipoUsuario": "cliente",
  "estado": "Activo",
  "ordenarPor": "calificacion",
  "direccion": "desc"
}
```

Response item:

```json
{
  "id": 3,
  "email": "cliente@mail.com",
  "tipoUsuario": "cliente",
  "nombreVisible": "Ana Pérez",
  "estado": "Activo",
  "calificacionGlobal": 4.8
}
```

---

## 9. Pagos / MercadoPago

### Lo que SÍ existe

- `POST /api/v1/pagos/webhook`
- procesa `type=payment` y `data.id`
- si MercadoPago confirma `approved`, backend marca pedido como confirmado

### Lo que frontend NO tiene hoy

Verifiqué el código y HOY no existe:

- endpoint para crear preferencia MercadoPago
- response que devuelva `mpInitPoint`
- uso efectivo de `pedidoRepositorio.actualizarDatosMp(...)`

### Conclusión práctica

Si frontend quiere checkout MercadoPago real, **hoy backend no le da ese contrato**.  
Solo existe procesamiento posterior de webhook.

---

## 10. Inconsistencias y limitaciones REALES que frontend debe contemplar

Estas NO son opiniones. Están verificadas en código.

1. **`POST /api/v1/clientes/google` no está implementado**
   - devuelve `200` con body `null`

2. **`DtPedidoResponse.detalles` no se llena**
   - el mapper no setea `detalles`

3. **MercadoPago checkout no está completo**
   - hay webhook, pero no creación de preferencia

4. **Webhook público tiene posible desalineación de seguridad**
   - `SecurityConfig` permite `/pagos/webhook`
   - controller expone `/api/v1/pagos/webhook`
   - frontend no debería consumirlo, pero backend debe corregirlo

5. **Naming de password inconsistente**
   - `passwd`, `password`, `nuevaPasswd`, etc.

6. **Alta de pedido debe mandar IDs mínimos**
   - frontend no debe intentar construir entidad completa

---

## 11. DTOs mínimos recomendados para frontend

## `Direccion`

```ts
type Direccion = {
  calle: string;
  numero: string;
  ciudad: string;
  codigoPostal: string;
};
```

## `LoginResponse`

```ts
type LoginResponse = {
  id: number;
  token: string;
  tipo: string;
  email: string;
};
```

## `PedidoListadoResponse`

```ts
type PedidoListadoResponse = {
  id: number;
  fecha: string;
  estado: "Pendiente" | "Confirmado" | "Rechazado" | "Cancelado";
  total: number;
  tiempoEstEntrega?: string | null;
  cantidadItems?: number | null;
  cliente?: { id: number; nombre: string; apellido: string } | null;
  local?: { id: number; nombre: string } | null;
};
```

## `ApiError`

```ts
type ApiError = {
  mensaje: string;
  status: number;
  timestamp: string;
  path: string;
};
```

---

## 12. Checklist para que frontend quede alineado DE VERDAD

- [ ] usar base URL `http://localhost:8080/api/v1`
- [ ] enviar JWT en `Authorization`
- [ ] modelar errores con `mensaje`, no `message`
- [ ] respetar enums exactos con mayúsculas/minúsculas reales
- [ ] usar `FormData` correcto en multipart
- [ ] NO usar login Google todavía
- [ ] NO depender de `detalles` en `DtPedidoResponse`
- [ ] NO esperar `mpInitPoint` desde backend
- [ ] usar nombres de password según endpoint
- [ ] para pedidos, enviar solo IDs mínimos y datos de negocio

---

## 13. Archivos verificados para construir este documento

- `src/main/java/com/example/demo/config/SecurityConfig.java`
- `src/main/java/com/example/demo/config/CorsConfig.java`
- `src/main/resources/application.properties`
- `src/main/java/com/example/demo/Logica/Controllers/*.java`
- `src/main/java/com/example/demo/Logica/Service/*.java`
- `src/main/java/com/example/demo/Logica/DataTypes/**/*`
- `src/main/java/com/example/demo/Logica/Enums/*.java`
- `src/main/java/com/example/demo/Logica/Exceptions/GlobalExceptionHandler.java`
- `src/main/java/com/example/demo/Logica/Mappers/PedidoResponseMapper.java`

