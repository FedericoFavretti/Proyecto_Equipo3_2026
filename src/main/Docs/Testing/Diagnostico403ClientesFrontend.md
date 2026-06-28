# Diagnóstico 403 en pantalla de clientes — guía para frontend

## Objetivo

Dejar por escrito, con evidencia concreta, qué está pasando con el error de la pantalla de clientes y dónde debería investigar frontend primero.

---

## Conclusión ejecutiva

El problema principal actual **NO** parece estar en el mapper de platos ni en `categoria`.

La evidencia más fuerte apunta a esto:

- frontend llama correctamente a backend
- CORS responde bien
- el endpoint conflictivo existe y está protegido
- pero el `Bearer token` usado en la request real estaba **vencido**

Por eso, la hipótesis principal hoy es:

> frontend está reutilizando un token expirado al entrar a la pantalla `/local/:restaurantId`

---

## Evidencia verificada

### 1) Endpoints conflictivos

Los endpoints involucrados son:

- `POST /api/v1/clientes/listar_locales`
- `POST /api/v1/clientes/busqueda`

Ambos están protegidos con:

- `anyRequest().authenticated()`
- `@PreAuthorize("hasRole('Cliente')")`

### 2) Request real observada en DevTools

Request fallida:

- URL: `https://proyectoequipo32026-test.up.railway.app/api/v1/clientes/listar_locales`
- método: `POST`
- status: `403 Forbidden`
- `X-Railway-Request-Id`: `ex4DgOKtSoCYU2avGbGh5g`
- response body: vacío (`content-length: 0`)

### 3) El token de esa request estaba vencido

Del JWT usado en esa request:

- `sub`: `roibethgarcia9@gmail.com`
- `iat`: `1782603964` → **2026-06-27 23:46:04 UTC**
- `exp`: `1782606664` → **2026-06-28 00:31:04 UTC**

Fecha de la request fallida:

- `Sun, 28 Jun 2026 00:32:42 GMT`

Eso significa que la llamada salió **98 segundos después del vencimiento** del token.

### 4) Esto es consistente con el backend

El backend:

- deshabilita CSRF globalmente
- permite el origen del frontend en CORS
- valida el JWT antes de poblar el `SecurityContext`
- exige autenticación y rol `Cliente` para esos endpoints

Si el token está vencido, el rechazo de seguridad es totalmente esperable.

### 5) `categoria` no explica este 403

El backend actual sí expone `categoria` en `DtPlato` y sí la mapea.

Además, el endpoint `/clientes/busqueda` devuelve la forma esperada:

```json
{
  "platos": [...],
  "promociones": [...]
}
```

Por lo tanto, `categoria` puede ser un tema secundario de UI o mapping, pero **NO** explica el `403` actual.

---

## Qué NO parece ser el problema principal

### No parece ser CORS

La respuesta incluye:

- `access-control-allow-origin: https://frontend-proyecto-foodly-test.up.railway.app`
- `access-control-allow-credentials: true`

Eso indica que el navegador sí está autorizado a hacer la llamada.

### No parece ser CSRF

En este backend, CSRF está deshabilitado globalmente.

### No parece ser el mapper mostrado

El archivo de mapping que revisamos:

- contempla `plato.categoria`
- espera `response.platos`
- espera `response.promociones`

Eso está alineado con el contrato backend actual.

---

## Dónde debería buscar frontend primero

Como no revisamos el repo frontend completo, acá van las **zonas correctas a inspeccionar**, en orden de prioridad.

### 1) Auth store / session store

Buscar dónde guardan el token:

- `localStorage`
- `sessionStorage`
- Zustand / Redux / Context / Signals / store propio

Verificar exactamente esto:

- si guardan `token` sin validar expiración
- si restauran sesión al recargar sin chequear `exp`
- si dejan navegar aunque el token ya venció
- si el logout no limpia estado viejo

### 2) API client / interceptor / fetch wrapper

Buscar dónde se agrega:

- `Authorization: Bearer <token>`

Verificar:

- si siempre toma el token más nuevo
- si puede estar leyendo un token cacheado viejo
- si reintenta requests con token expirado
- si ante `401/403` fuerza relogin o limpia sesión

### 3) Loader de la pantalla `/local/:restaurantId`

Buscar la lógica que dispara:

- `POST /clientes/listar_locales`
- `POST /clientes/busqueda`

Verificar:

- si esas requests salen apenas entra a la ruta, sin validar primero la sesión
- si disparan requests paralelas con estado auth todavía desactualizado
- si usan datos restaurados de una sesión anterior

### 4) Guard de rutas / bootstrap de app

Buscar:

- route guards
- auth bootstrap
- session hydration

Verificar:

- si el guard solo revisa “existe token” en vez de “token vigente”
- si el guard mira rol pero no expiración
- si la app monta la pantalla antes de reconciliar auth real

### 5) Refresh token o relogin automático

Si existe lógica de refresh, revisar:

- si realmente corre antes de llamar APIs protegidas
- si falla silenciosamente
- si deja el token expirado vivo en memoria

Si NO existe refresh, entonces frontend debería:

- detectar expiración
- cerrar sesión o pedir login nuevamente
- evitar llamadas protegidas con token viejo

---

## Qué revisar exactamente en frontend

Checklist concreto:

- [ ] dónde se decodifica o interpreta el JWT
- [ ] si se usa `exp`
- [ ] si `exp` se compara contra la hora actual
- [ ] si hay margen de seguridad antes del vencimiento
- [ ] si se limpia token vencido al iniciar la app
- [ ] si la navegación a `/local/:restaurantId` depende solo de “hay token”
- [ ] si el interceptor usa el token actual y no uno stale
- [ ] si al recibir `403` se deja al usuario en estado roto sin recuperación

---

## Prueba rápida recomendada

### Paso 1

Hacer login nuevamente para obtener un token fresco.

### Paso 2

Repetir en Postman:

- `POST /api/v1/clientes/listar_locales`
- `POST /api/v1/clientes/busqueda`

con:

- `Authorization: Bearer <token nuevo>`
- `Content-Type: application/json`

### Paso 3

Interpretación:

- si responde `200`, la causa raíz principal era el token expirado
- si sigue `403`, entonces el siguiente foco es backend:
  - usuario no resuelto como `Cliente`
  - sesión invalidada
  - deploy distinto al código revisado

---

## Archivos backend usados como evidencia

- `src/main/java/com/example/demo/config/SecurityConfig.java`
- `src/main/java/com/example/demo/config/CorsConfig.java`
- `src/main/java/com/example/demo/jwt/JwtAuthenticationFilter.java`
- `src/main/java/com/example/demo/jwt/JwtService.java`
- `src/main/java/com/example/demo/Logica/Controllers/ClienteController.java`
- `src/main/java/com/example/demo/Logica/Service/UsuarioUserDetails.java`
- `src/main/java/com/example/demo/Logica/DataTypes/shared/DtPlato.java`
- `src/main/java/com/example/demo/Logica/Mappers/PlatoMapper.java`
- `src/main/java/com/example/demo/Logica/DataTypes/response/DtBusquedaPlatosPromocionesResponse.java`

---

## Mensaje corto para frontend

Si quieren atacar el problema con precisión, NO arranquen por el mapper.  
Arranquen por:

1. almacenamiento del token
2. validación de expiración
3. interceptor/API client
4. bootstrap/guard de sesión

El dato más importante de esta investigación es simple:

> la request real que devolvió `403` salió con un JWT ya vencido

