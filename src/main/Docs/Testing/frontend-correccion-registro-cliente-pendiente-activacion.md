# Corrección requerida en frontend — registro estándar de cliente con activación por mail

**Fecha:** 2026-07-10  
**Proyecto:** Foodly Front / integración con backend

---

## Objetivo de este documento

Dejar claro qué debe corregir frontend en el flujo de **registro estándar de cliente** para no mostrar un error incorrecto cuando la cuenta queda **pendiente de activación por correo**.

---

## Comportamiento observado

Al registrar un cliente por formulario normal, backend responde exitosamente con un usuario así:

```json
{
  "activo": false,
  "apellido": "García",
  "calificacionGlobal": null,
  "direccion": {
    "calle": "colonia",
    "numero": "2277",
    "ciudad": "Miami",
    "codigoPostal": "33192"
  },
  "documento": "78983210",
  "email": "roibeth5@gmail.com",
  "estado": "Pendiente",
  "foto": "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783733219/qd2zoxaxqhoctygf1mru.png",
  "id": 77,
  "nombre": "Roibeth",
  "sesionesInvalidadasDesde": null,
  "tipo": "cliente"
}
```

Inmediatamente después aparece un error visual en frontend y además se observa una llamada a login que responde:

```json
{
  "mensaje": "Usuario no activado o bloqueado.",
  "status": 404,
  "timestamp": "2026-07-10T22:27:01.127251662",
  "path": "uri=/api/v1/usuarios/login"
}
```

---

## Diagnóstico

El problema visible para el usuario NO es que el registro haya fallado.

El problema es este:

1. el registro termina correctamente
2. la cuenta queda en estado `Pendiente`
3. frontend está disparando un login automático, o una navegación que termina intentando login
4. backend rechaza ese login porque la cuenta aún no fue activada por mail
5. frontend muestra ese rechazo como si fuera el resultado final del registro

---

## Interpretación correcta del response de registro

Si frontend recibe:

```txt
activo = false
estado = Pendiente
tipo = cliente
```

eso debe interpretarse como:

```txt
Registro exitoso. Cuenta creada. Pendiente de activación por correo.
```

NO debe interpretarse como:

```txt
Usuario bloqueado
Usuario inválido
Error de registro
```

---

## Qué debe corregir frontend

### 1. NO hacer login automático después del registro estándar

En el flujo de **registro con correo**, frontend no debe:

- llamar automáticamente a `/api/v1/usuarios/login`
- intentar crear sesión apenas termina el alta
- redirigir a una pantalla que haga auto-login

Eso rompe el flujo funcional, porque la cuenta todavía está pendiente de activación.

---

### 2. Cerrar el registro como exitoso

Si el alta respondió `200/OK` y el usuario creado viene con:

- `estado = Pendiente`
- `activo = false`

frontend debe considerar el registro como **exitoso** y terminar el flujo mostrando confirmación.

---

### 3. Mostrar mensaje correcto al usuario

Después del registro estándar, frontend debe mostrar un mensaje como este:

```txt
Tu cuenta fue creada correctamente. Te enviamos un correo de activación. Revisá tu bandeja de entrada y, si no lo encontrás, verificá spam o correo no deseado.
```

---

### 4. No mostrar el toast de login fallido como resultado del alta

Si por algún motivo todavía existe una llamada automática a login, frontend no debe mostrar ese error al usuario como mensaje principal del flujo de registro.

Ese error pertenece al intento de autenticación, no al alta.

---

### 5. Mapear visualmente `Pendiente` como “esperando activación”

Si frontend muestra estado de cuenta, entonces:

- `Pendiente` debe verse como **pendiente de activación**
- `Bloqueado` debe verse como **bloqueado**

NO deben compartir el mismo texto visual.

---

## Flujo correcto esperado en frontend

### Registro estándar con correo

1. usuario completa formulario
2. frontend envía `POST /api/v1/clientes/registro`
3. backend crea cuenta con estado pendiente
4. frontend muestra confirmación de registro
5. frontend informa que debe activar la cuenta por correo
6. frontend NO intenta iniciar sesión automáticamente

---

## Flujo incorrecto actual

1. usuario completa formulario
2. frontend envía `POST /api/v1/clientes/registro`
3. backend crea cuenta pendiente correctamente
4. frontend llama a `/api/v1/usuarios/login`
5. backend rechaza porque la cuenta no está activa
6. frontend muestra toast:

```txt
Usuario no activado o bloqueado.
```

Eso es lo que debe corregirse.

---

## Checklist para frontend

- [ ] revisar si después del registro estándar se dispara `/api/v1/usuarios/login`
- [ ] revisar si existe auto-login en el submit del registro
- [ ] revisar si existe redirección posterior que termine logueando automáticamente
- [ ] revisar si el interceptor global muestra el error de login sin contexto
- [ ] revisar si `estado=Pendiente` se interpreta como bloqueo
- [ ] revisar si `activo=false` se interpreta como error fatal
- [ ] agregar mensaje de éxito de registro con activación por correo

---

## Resultado esperado después de la corrección

Cuando el usuario se registre por formulario normal:

- NO debe aparecer el toast `Usuario no activado o bloqueado.`
- SÍ debe aparecer un mensaje de confirmación de registro
- SÍ debe quedar claro que la cuenta está pendiente de activación por mail

---

## Nota importante para el equipo

Backend todavía tiene deudas de contrato y mensaje para este caso, pero el síntoma visible actual en UI se explica principalmente porque frontend está tratando un **registro pendiente de activación** como si debiera poder **loguearse inmediatamente**.

Esa lógica debe corregirse en frontend.

