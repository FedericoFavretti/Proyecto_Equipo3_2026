# Alineación Frontend — Flujo Google con la nueva API

## Objetivo

Alinear el frontend con el contrato REAL del backend para autenticación y registro con Google.

**IMPORTANTE:** el backend actual **NO** usa un flag tipo `requiereDatosAdicionales` para decidir el flujo.  
El flujo se define por **rutas distintas**.

---

## Resumen ejecutivo

### Login con Google
- Endpoint: `POST /api/v1/clientes/google`
- Uso: **solo** cuando el usuario ya tiene cuenta de cliente creada

### Registro con Google
- Paso 1: `POST /api/v1/clientes/google/registro/iniciar`
- Paso 2: `POST /api/v1/clientes/google/registro/completar`
- Uso: cuando el usuario viene desde **Registrarse con Google**

---

## Regla de negocio clave

Si el usuario está en pantalla de **registro**, el frontend **NO debe** llamar a:

`POST /api/v1/clientes/google`

Ese endpoint es de **login**, no de alta.

Si se invoca desde registro con un correo inexistente, el backend responderá con error similar a:

`No existe una cuenta de cliente asociada al correo [correo]. Regístrese con Google para continuar.`

---

## Contrato de endpoints

## 1) Login con Google

### Request
**POST** `/api/v1/clientes/google`

**Content-Type:** `application/json`

```json
{
  "idToken": "google_token",
  "direccion": null,
  "documento": null,
  "esRegistro": false
}
```

### Qué hace
- Valida el token de Google
- Obtiene el correo del usuario
- Busca un cliente existente por email
- Si existe, devuelve login exitoso
- Si no existe, devuelve `400 Bad Request`

### Response exitosa
```json
{
  "id": 10,
  "token": "jwt-login",
  "tipo": "cliente",
  "email": "cliente@correo.com",
  "nombre": "Ana",
  "apellido": "Pérez",
  "direccion": {
    "calle": "18 de Julio",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11200"
  },
  "foto": "https://...",
  "calificacionGlobal": 4.8
}
```

### Cuándo usarlo en frontend
- Botón: **Continuar/Iniciar sesión con Google**
- Pantalla: **login**

---

## 2) Iniciar registro con Google

### Request
**POST** `/api/v1/clientes/google/registro/iniciar`

**Content-Type:** `application/json`

```json
{
  "idToken": "google_token",
  "direccion": null,
  "documento": null,
  "esRegistro": true
}
```

### Qué hace
- Valida el token de Google
- Obtiene email, nombre, apellido y foto
- Verifica que el correo no exista ya en el sistema
- Genera un `tokenRegistro` temporal

### Response exitosa
```json
{
  "tokenRegistro": "jwt_registro_temporal",
  "email": "nuevo@correo.com",
  "nombre": "Ana",
  "apellido": "Pérez",
  "foto": "https://..."
}
```

### Qué debe hacer el frontend después
Con esa respuesta debe:

1. guardar `tokenRegistro`
2. mostrar formulario de datos complementarios
3. pedir:
   - documento
   - dirección
   - foto de perfil
   - aceptación de términos
4. enviar todo al paso 2

---

## 3) Completar registro con Google

### Request
**POST** `/api/v1/clientes/google/registro/completar`

**Content-Type:** `multipart/form-data`

### Partes esperadas

#### Parte `datos`
JSON serializado con esta estructura:

```json
{
  "tokenRegistro": "jwt_registro_temporal",
  "documento": "51234567",
  "direccion": {
    "calle": "18 de Julio",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11200"
  },
  "aceptaTerminos": true
}
```

#### Parte `foto`
Archivo de imagen

### Qué hace
- Valida `tokenRegistro`
- valida datos faltantes
- valida aceptación de términos
- valida duplicado de correo/documento
- crea cuenta cliente activa
- devuelve login final con JWT

### Response exitosa
```json
{
  "id": 10,
  "token": "jwt-final",
  "tipo": "cliente",
  "email": "nuevo@correo.com",
  "nombre": "Ana",
  "apellido": "Pérez",
  "direccion": {
    "calle": "18 de Julio",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11200"
  },
  "foto": "https://...",
  "calificacionGlobal": 0.0
}
```

---

## Flujo correcto para frontend

## Caso A — Usuario ya tiene cuenta

1. Usuario entra a **login**
2. Click en **Continuar con Google**
3. Frontend obtiene `idToken`
4. Frontend llama `POST /api/v1/clientes/google`
5. Si éxito:
   - guardar JWT
   - guardar perfil
   - redirigir al home/panel

---

## Caso B — Usuario quiere registrarse

1. Usuario entra a **registro**
2. Click en **Registrarse con Google**
3. Frontend obtiene `idToken`
4. Frontend llama `POST /api/v1/clientes/google/registro/iniciar`
5. Si éxito:
   - guardar `tokenRegistro`
   - mostrar pantalla/formulario de datos adicionales
6. Usuario completa:
   - documento
   - dirección
   - foto
   - términos
7. Frontend llama `POST /api/v1/clientes/google/registro/completar`
8. Si éxito:
   - guardar JWT final
   - guardar perfil
   - redirigir al home/panel

---

## Qué NO debe hacer el frontend

- NO usar `/api/v1/clientes/google` desde la pantalla de registro
- NO esperar un flag `requiereDatosAdicionales` en la respuesta del login
- NO asumir que `esRegistro` cambia el comportamiento si la ruta es la incorrecta
- NO enviar el paso final como JSON simple; debe ser `multipart/form-data`

---

## Aclaración importante sobre `esRegistro`

El DTO del backend tiene un campo `esRegistro`, pero el flujo **no se decide con ese campo**.

El flujo se decide por la **ruta HTTP**:

- `/google` → login
- `/google/registro/iniciar` → inicio de registro
- `/google/registro/completar` → finalización de registro

Por eso:

```json
{
  "idToken": "token",
  "esRegistro": true
}
```

enviado a `/api/v1/clientes/google`

**NO lo convierte** en registro.

---

## Manejo recomendado de errores en frontend

## Si `/api/v1/clientes/google` responde:
`No existe una cuenta de cliente asociada...`

### Recomendación UX
- Mostrar mensaje claro
- Ofrecer acción inmediata:
  - **Registrarse con Google**
- Redirigir al flujo:
  - `/api/v1/clientes/google/registro/iniciar`

---

## Si `/api/v1/clientes/google/registro/iniciar` responde conflicto
Mensaje esperado:

`El correo [correo] ya está asociado a una cuenta existente. ¿Desea iniciar sesión en su lugar?`

### Recomendación UX
- Mostrar CTA para ir a login con Google

---

## Si `/api/v1/clientes/google/registro/completar` responde campos faltantes
Mensaje esperado:

`Los siguientes campos son requeridos: ...`

### Recomendación UX
- mapear errores al formulario
- no perder `tokenRegistro`
- permitir reintento sin rehacer OAuth si el token sigue vigente

---

## Checklist de implementación frontend

- [ ] La pantalla de login usa `POST /api/v1/clientes/google`
- [ ] La pantalla de registro usa `POST /api/v1/clientes/google/registro/iniciar`
- [ ] Existe pantalla o modal de datos complementarios post-Google
- [ ] Se persiste `tokenRegistro` entre paso 1 y paso 2
- [ ] El paso final usa `multipart/form-data`
- [ ] Se envía `datos` + `foto`
- [ ] Se manejan errores de correo ya existente
- [ ] Se manejan errores de campos obligatorios
- [ ] No se espera `requiereDatosAdicionales` en el contrato

---

## Decisión recomendada

El frontend debe alinearse con esta API tal como está hoy.

La arquitectura correcta NO es “probar y ver qué pasa”, sino respetar el contrato:

- login por un endpoint
- registro por dos endpoints
- datos complementarios en segundo paso

Si frontend sigue el contrato viejo hablado en otro chat, el flujo se va a romper AUNQUE backend compile perfecto.
