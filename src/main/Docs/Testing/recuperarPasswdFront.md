# Verificación front/back — recuperar contraseña por correo

## Objetivo

Confirmar si el `500` en:

`POST /api/v1/usuarios/recuperar_contra_correo`

se debe a:

1. un request incorrecto desde frontend, o
2. un problema real del backend.

---

## Cambio verificado en backend

El endpoint cambió y **ahora espera un JSON** con esta forma:

```json
{
  "correo": "usuario@mail.com"
}
```

Ya **no** espera un `String` crudo en el body.

### Backend actual

- Endpoint: `/api/v1/usuarios/recuperar_contra_correo`
- Método: `POST`
- Content-Type esperado: `application/json`
- Body esperado:

```json
{
  "correo": "usuario@mail.com"
}
```

---

## Qué NO debe enviar el frontend

### Incorrecto 1: string crudo

```json
"usuario@mail.com"
```

### Incorrecto 2: otra clave

```json
{
  "email": "usuario@mail.com"
}
```

### Incorrecto 3: form-data o text/plain

Si se manda como texto plano, `FormData` o con `Content-Type` distinto a `application/json`, el backend puede fallar al deserializar.

---

## Ejemplo correcto en frontend

### fetch

```ts
await fetch("/api/v1/usuarios/recuperar_contra_correo", {
  method: "POST",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    correo: email
  })
});
```

### axios

```ts
await axios.post("/api/v1/usuarios/recuperar_contra_correo", {
  correo: email
}, {
  headers: {
    "Content-Type": "application/json"
  }
});
```

---

## Respuesta esperada si el request está bien

Si el request cumple el contrato, el backend debería responder `200 OK` con este mensaje:

```text
Si el correo ingresado está asociado a una cuenta, recibirá un enlace de recuperación en breve.
```

Esto debe pasar incluso si el correo no existe, para no filtrar información.

---

## Prueba manual rápida

Probar exactamente este request:

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/recuperar_contra_correo ^
  -H "Content-Type: application/json" ^
  -d "{\"correo\":\"usuario@mail.com\"}"
```

> En Windows PowerShell también puede probarse con `Invoke-RestMethod` o Postman.

---

## Cómo diagnosticar el origen del error

### Caso A — el frontend manda mal el body

Si el frontend manda:

- un string crudo,
- una propiedad distinta de `correo`,
- o un `Content-Type` incorrecto,

entonces el problema es de **contrato front-back**.

### Caso B — el frontend manda exactamente `{ "correo": "..." }` y sigue dando 500

Entonces el problema es del **backend** o de su infraestructura.

Los sospechosos principales verificados son:

1. **Falta la tabla `token_recuperacion_passwd` en la base**
   - el flujo nuevo la usa para guardar tokens de recuperación
   - si la base no fue migrada, el endpoint va a fallar

2. **El backend convierte excepciones genéricas en 500**
   - incluso errores que deberían responder `400`

3. **Error SQL o de schema**
   - tabla inexistente
   - columnas faltantes
   - permisos o conexión

> El envío de mail NO parece ser la causa principal del `500`, porque ese envío está dentro de un `try/catch` y su falla debería quedar logueada sin romper la respuesta.

---

## Qué revisar en logs del backend

Buscar alguno de estos errores:

### Si aparece algo como esto

- `HttpMessageNotReadableException`
- error de deserialización JSON

Entonces el frontend está mandando mal el request.

### Si aparece algo como esto

- `relation "token_recuperacion_passwd" does not exist`
- error SQL al hacer `INSERT` o `UPDATE` en `token_recuperacion_passwd`

Entonces el problema es del backend/base de datos.

---

## Criterio final

### Si este request funciona:

```json
{
  "correo": "usuario@mail.com"
}
```

Entonces el problema era del frontend anterior o de cómo arma la request.

### Si este request sigue devolviendo 500:

Entonces el error es de backend, muy probablemente por:

- tabla faltante,
- migración no aplicada,
- o manejo incorrecto de excepciones.

---

## Recomendación

1. Ajustar el frontend para enviar exactamente:

```json
{
  "correo": "usuario@mail.com"
}
```

2. Probar manualmente con Postman o curl
3. Si sigue el `500`, revisar logs del backend
4. Verificar que exista la tabla `token_recuperacion_passwd`

