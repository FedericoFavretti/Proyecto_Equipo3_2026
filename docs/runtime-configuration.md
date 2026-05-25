# Configuración runtime local

Este proyecto usa configuración externalizada. No guardes credenciales reales en Git.

## Variables relevantes

```properties
PORT=8082
DB_URL=jdbc:postgresql://localhost:5432/proyectoFinal
DB_NAME=proyectoFinal
DB_USER=root
DB_PASSWORD=root123
JWT_SECRET=change-this-secret-key-change-this-secret-key-123456
JWT_EXPIRATION_MS=900000
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
APP_EMAIL_FROM=no-reply@foodly.local
PASSWORD_RESET_FRONTEND_URL=http://localhost:4200/reset-password
PASSWORD_RESET_TTL=30m
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

## Mailpit en desarrollo

`docker-compose.yml` incluye Mailpit:

- SMTP: `localhost:1025`
- UI web: `http://localhost:8025`

Cuando el backend usa `MAIL_HOST=mailpit` dentro de Docker, los correos de recuperación aparecen en la UI de Mailpit. Esto permite probar el flujo sin usar un proveedor real.

## CORS

`APP_CORS_ALLOWED_ORIGINS` acepta una lista separada por comas, por ejemplo:

```properties
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173
```

Mantener esta lista acotada es importante. Abrir CORS sin criterio es cómodo hoy y deuda técnica mañana.
