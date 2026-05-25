# Recuperación de contraseña

La recuperación de contraseña queda expuesta sin JWT para que el frontend pueda iniciar y completar el flujo cuando el usuario no puede autenticarse.

## Endpoints

### Solicitar recuperación

`POST /password/forgot`

Body:

```json
{
  "email": "cliente@foodly.local"
}
```

Respuesta esperada:

`202 Accepted`

```json
{
  "message": "Si el correo existe, se enviaron instrucciones de recuperación."
}
```

La respuesta es intencionalmente genérica: NO debe revelar si el correo existe. Eso evita enumeración de usuarios.

### Cambiar contraseña

`POST /password/reset`

Body:

```json
{
  "token": "token-recibido-por-email",
  "newPassword": "NuevaPassword123"
}
```

Respuesta esperada:

`200 OK`

```json
{
  "message": "La contraseña se actualizó correctamente."
}
```

Si el token es inválido, vencido o ya fue usado, el backend responde `400 Bad Request`.

## Flujo esperado en frontend

1. El usuario ingresa su email en "Olvidé mi contraseña".
2. El frontend llama a `POST /password/forgot`.
3. El backend genera un token seguro, guarda solo su hash y envía un link al correo.
4. El usuario abre el link configurado en `PASSWORD_RESET_FRONTEND_URL` con query param `token`.
5. El frontend pide la nueva contraseña y llama a `POST /password/reset`.

## Seguridad aplicada

- El token crudo solo viaja por email.
- En base de datos se guarda `SHA-256(token)`, no el token plano.
- Los tokens anteriores activos del usuario se invalidan al generar uno nuevo.
- Cada token es de un solo uso.
- La contraseña nueva se guarda usando el `PasswordEncoder` de Spring Security.
