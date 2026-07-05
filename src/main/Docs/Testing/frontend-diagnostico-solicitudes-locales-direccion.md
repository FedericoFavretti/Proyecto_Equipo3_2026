# Diagnóstico para Frontend — solicitudes de locales, dirección y fecha

**Fecha:** 2026-07-05  
**Backend:** Foodly API  
**Pantallas impactadas:**  
- registro de local  
- admin / solicitudes de locales

---

## Objetivo

Este documento deja claro qué está verificado en backend y qué necesita revisar frontend para cerrar el problema de la dirección en solicitudes de locales.

También deja documentado el estado real del campo `fecha`, porque hoy la UI lo muestra pero el backend actual no lo expone.

---

## Resumen ejecutivo

### Dirección

El backend **no está perdiendo** la dirección en la bandeja admin.

El endpoint devuelve exactamente lo que está persistido.

Evidencia real recibida desde `GET /api/v1/admins/solicitudes-locales/pendientes`:

```json
[
  {
    "id": 52,
    "email": "mahgranny@foodly.com",
    "nombre": "MAHGRANNY",
    "direccion": {
      "calle": "Sin especificar",
      "numero": "S/N",
      "ciudad": "N/D",
      "codigoPostal": "0000"
    },
    "descripcion": "Pizza",
    "imagenes": [
      "https://res.cloudinary.com/dh8f9uvlu/image/upload/v1783220550/r9nqh7huhdwqedxrsy9i.png"
    ]
  }
]
```

Conclusión:

- `direccion` **sí llega**
- pero llega con **placeholders persistidos**, no con una dirección real

### Fecha

El backend actual **no expone** ningún campo `fecha` en el flujo de solicitudes de locales.

Conclusión:

- la columna `Fecha` del frontend hoy no tiene dato real para renderizar
- si se deja visible, va a seguir mostrando `—` o vacío

---

## Qué está verificado en backend

### 1. El contrato admin sí expone dirección

El endpoint:

```http
GET /api/v1/admins/solicitudes-locales/pendientes
```

devuelve un DTO con:

- `id`
- `email`
- `nombre`
- `direccion`
- `descripcion`
- `imagenes`

### 2. El backend de registro no genera esos placeholders

Se verificó el flujo:

- `LocalController`
- `LocalService`
- `LocalMapper`
- `LocalRepositorioImpl`

y no hay lógica que reemplace una dirección real por:

- `Sin especificar`
- `S/N`
- `N/D`
- `0000`

### 3. La validación actual solo controla “no vacío”

El backend actual exige que existan:

- `calle`
- `numero`
- `ciudad`
- `codigoPostal`

pero **no valida** que el contenido sea semánticamente útil.

Eso significa que estos valores hoy pasan como válidos:

- `Sin especificar`
- `S/N`
- `N/D`
- `0000`

---

## Hipótesis principal

Si un local nuevo sigue quedando con esa dirección placeholder, la causa más probable está **antes de persistir**:

- payload enviado desde frontend
- state del formulario
- armado del `FormData`
- defaults/fallbacks del front

NO parece ser un problema del listado admin.

---

## Endpoints involucrados

### Alta de solicitud de local

```http
POST /api/v1/locales/solicitudes-habilitacion
Content-Type: multipart/form-data
```

Parts esperadas:

- `datos`
- `logo`
- `imagenes`

### Bandeja admin

```http
GET /api/v1/admins/solicitudes-locales/pendientes
```

---

## Contrato esperado para `datos`

Conceptualmente, frontend debe enviar algo con esta forma:

```json
{
  "email": "local@foodly.com",
  "passwd": "123456",
  "nombre": "La Cocina",
  "direccion": {
    "calle": "Av. Italia",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11600"
  },
  "descripcion": "Comida casera"
}
```

Si en el request real `datos` ya sale con placeholders, el problema está en frontend.

---

## Checklist de verificación para Frontend

## 1. Revisar el request real de creación

Abrir DevTools → Network → request:

```http
POST /api/v1/locales/solicitudes-habilitacion
```

Verificar el contenido exacto del part `datos`.

### Resultado esperado

`direccion` debería salir con la dirección escrita por el usuario.

### Si ocurre esto

```json
"direccion": {
  "calle": "Sin especificar",
  "numero": "S/N",
  "ciudad": "N/D",
  "codigoPostal": "0000"
}
```

entonces el problema ya está en el request de frontend.

---

## 2. Revisar el state real del formulario antes del submit

Verificar:

- valor visual del input
- valor real del state
- valor final serializado

El problema puede ser:

- el input muestra una cosa
- pero el objeto enviado mantiene otra

Esto suele pasar cuando el form no está enlazado al mismo objeto que luego se serializa.

---

## 3. Revisar cómo se arma el `FormData`

Validar:

- de dónde sale `datos`
- si se hace `JSON.stringify(...)`
- si `direccion` viaja anidada correctamente
- si no se están mezclando campos planos y anidados

Ejemplos de riesgo:

- usar `direccion.calle` en UI pero construir después otro objeto `direccion`
- enviar un objeto parcial y completar con defaults
- sobrescribir `direccion` justo antes del append al `FormData`

---

## 4. Buscar defaults o fallbacks en frontend

Buscar estos literales en el repo frontend:

- `Sin especificar`
- `S/N`
- `N/D`
- `0000`

Si aparecen en:

- estado inicial
- mapper
- adapter
- normalizador
- fallback
- mock

entonces probablemente ahí está la fuente.

---

## 5. Verificar inmediatamente el GET admin después del alta

Después de crear un local nuevo:

1. refrescar `GET /api/v1/admins/solicitudes-locales/pendientes`
2. revisar si el nuevo local ya aparece con placeholders

### Interpretación

- si el POST ya envió placeholders y el GET devuelve placeholders → frontend está enviando mal
- si el POST envió dirección real y el GET devuelve placeholders → revisar serialización backend / persistencia / transformación intermedia

Hoy, por la evidencia disponible, el escenario más probable es el primero.

---

## Qué hacer con la columna `Fecha`

Hoy el backend actual no expone fecha de solicitud.

Frontend tiene tres opciones:

### Opción A — inmediata

Ocultar la columna `Fecha`.

### Opción B — UX transitoria

Mostrar algo como:

`No disponible`

en vez de simular un dato inexistente.

### Opción C — solución correcta

Esperar a que backend modele y exponga formalmente una `fechaSolicitud`.

---

## Conclusión para Frontend

### Dirección

La dirección no desaparece en la vista admin.  
La API devuelve una dirección placeholder que ya fue persistida así.

Frontend debe concentrarse en revisar:

1. state del formulario
2. contenido real de `datos`
3. armado del `FormData`
4. defaults/fallbacks

### Fecha

No hay bug de render aislado: el dato no existe en el contrato backend actual.

---

## Decisión recomendada

### Corto plazo

- revisar el payload real del `POST /solicitudes-habilitacion`
- revisar si frontend está mandando placeholders
- ocultar o desactivar visualmente la columna `Fecha`

### Mediano plazo

- endurecer validación funcional para dirección real
- modelar `fechaSolicitud` si producto la necesita

---

## Archivos backend relevantes para contexto

- `src/main/java/com/example/demo/Logica/Controllers/LocalController.java`
- `src/main/java/com/example/demo/Logica/Service/LocalService.java`
- `src/main/java/com/example/demo/Logica/Mappers/LocalMapper.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/LocalRepositorioImpl.java`
- `src/main/java/com/example/demo/Logica/DataTypes/response/DtSolicitudLocalPendienteResponse.java`
