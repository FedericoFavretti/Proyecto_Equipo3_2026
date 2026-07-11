# Alineación requerida en frontend — registro con Google, foto opcional y vínculo explícito

**Fecha:** 2026-07-10  
**Proyecto:** Foodly Front / integración con backend

---

## Objetivo de este documento

Dejar claro qué debe corregir frontend para alinearse con los cambios ya aplicados en backend al flujo de:

- **registro con Google**
- **login con Google**
- **uso de foto de perfil proveniente desde Google**

---

## Resumen ejecutivo

Hoy el backend quedó preparado para este comportamiento:

1. la foto en el alta con Google **ya no es obligatoria**
2. si el usuario no sube una foto manual, backend usa la **foto que viene desde Google**
3. el login con Google **solo** debe funcionar para cuentas **vinculadas explícitamente con Google**
4. una cuenta `Pendiente` o `Bloqueado` **no** debe poder entrar con Google

Si frontend sigue mostrando el input de archivo como obligatorio, entonces frontend está desalineado con el contrato actual.

---

## Verificación realizada en backend

Se verificó en este repositorio que el backend actual quedó así:

### 1. La foto en completar registro con Google es opcional

En:

- `src/main/java/com/example/demo/Logica/Controllers/ClienteController.java`

el endpoint:

```txt
POST /api/v1/clientes/google/registro/completar
```

acepta:

```java
@RequestPart(value = "foto", required = false)
```

Eso significa que frontend **puede completar el flujo sin adjuntar archivo**.

---

### 2. Si no se sube foto manual, backend usa la de Google

En:

- `src/main/java/com/example/demo/Logica/Service/ClienteService.java`

existe la resolución:

```txt
foto manual si viene
si no viene -> foto obtenida desde Google
```

Conceptualmente:

```txt
fotoFinal = fotoManual ?? fotoGoogle
```

Por lo tanto, frontend NO necesita forzar una nueva carga de imagen para terminar el alta.

---

### 3. El login con Google ahora exige vínculo explícito

En backend ya no alcanza con que el email coincida.

Ahora, para login con Google, la cuenta debe:

- existir
- estar `Activa`
- tener vínculo explícito con Google

Eso evita estos bugs previos:

- cuenta bloqueada entra por Google
- cuenta pendiente entra por Google
- cuenta creada por flujo normal entra por Google solo por compartir email

---

## Qué significa esto para frontend

Frontend debe dejar de modelar la foto en Google como:

```txt
campo obligatorio
```

y debe empezar a modelarla como:

```txt
foto sugerida / foto precargada desde Google
cambio manual opcional
```

Ese es el punto clave.

---

## Comportamiento correcto esperado en UI

## Registro con Google

### Flujo esperado

1. usuario inicia flujo con Google
2. frontend recibe datos básicos desde Google:
   - email
   - nombre
   - apellido
   - foto
3. frontend precarga esos datos
4. frontend muestra la foto de Google como imagen por defecto, o la guarda internamente aunque no la pinte
5. frontend permite cambiarla opcionalmente
6. frontend completa el alta aunque no se adjunte archivo nuevo

---

## Comportamiento incorrecto actual

La pantalla actual sugiere este comportamiento:

1. frontend recibe datos desde Google
2. frontend ignora o no usa la foto de Google
3. frontend muestra un input file como parte obligatoria
4. frontend no deja terminar el alta sin subir imagen

Eso contradice el backend actual.

---

## Qué debe corregir frontend

### 1. El input de foto NO debe ser obligatorio en registro Google

Revisar:

- validación del formulario
- schema de validación
- estado `required`
- condición de submit habilitado/deshabilitado
- mensajes de error

Si el botón de completar depende de que exista un archivo en el input, la validación está mal.

---

### 2. Usar la foto que ya trae Google

Frontend debe tomar la foto recibida del flujo Google y usarla como:

- preview por defecto
- valor interno del formulario
- o valor de respaldo si no hay reemplazo manual

Si no se quiere mostrar preview visual, al menos NO se debe exigir otro archivo.

---

### 3. Cambiar el texto visual del campo

En lugar de mostrar un file input desnudo como si fuera obligatorio, frontend debería mostrar algo como:

```txt
Foto de perfil obtenida desde Google
Cambiar foto (opcional)
```

o:

```txt
Usaremos tu foto de Google. Si querés, podés reemplazarla.
```

---

### 4. No bloquear el alta si no hay archivo nuevo

Si la UI hoy hace:

```txt
sin archivo => form inválido
```

eso debe cambiar a:

```txt
sin archivo => usar fotoGoogle y continuar
```

---

### 5. Alinear manejo de error de login con Google

Como el backend ahora exige vínculo explícito con Google, frontend debe contemplar estos errores funcionales:

#### Caso A — cuenta no vinculada a Google

Mensaje esperado de negocio:

```txt
La cuenta asociada al correo [correo] no está vinculada a Google. Inicie sesión con correo y contraseña.
```

Frontend NO debe mostrar esto como error genérico de infraestructura.

#### Caso B — cuenta pendiente o bloqueada

Mensaje esperado:

```txt
Usuario no activado o bloqueado.
```

Frontend debe mapearlo correctamente según el contexto del flujo.

---

## Contrato funcional que frontend debe asumir

### Endpoint de alta Google

```txt
POST /api/v1/clientes/google/registro/completar
Consumes: multipart/form-data
```

### Partes esperadas

- `datos` → requerido
- `foto` → **opcional**

### Regla funcional

Si `foto` no viene:

```txt
backend usa la foto obtenida desde Google
```

Si `foto` sí viene:

```txt
backend usa la foto manual subida por el usuario
```

---

## Checklist para frontend

- [ ] quitar obligatoriedad visual y funcional del input `foto` en registro Google
- [ ] usar la foto devuelta por Google como valor por defecto
- [ ] permitir reemplazo manual como acción opcional
- [ ] no invalidar el form por ausencia de archivo
- [ ] revisar si el botón “Completar registro con Google” depende incorrectamente del input file
- [ ] agregar preview de foto Google o, al menos, no exigir carga manual
- [ ] mapear correctamente errores de cuenta no vinculada a Google
- [ ] mapear correctamente errores de cuenta pendiente o bloqueada
- [ ] verificar que frontend desplegado esté usando esta versión del contrato

---

## Prueba manual recomendada para validar alineación

### Caso 1 — registro Google sin cambiar foto

1. iniciar registro con Google
2. completar dirección, documento y términos
3. NO adjuntar archivo de foto
4. enviar formulario

### Resultado esperado

- el formulario debe enviarse correctamente
- NO debe marcar foto como obligatoria
- la cuenta debe quedar creada usando la foto de Google

---

### Caso 2 — registro Google cambiando foto

1. iniciar registro con Google
2. completar datos faltantes
3. adjuntar una foto manual
4. enviar formulario

### Resultado esperado

- el formulario debe enviarse correctamente
- backend debe usar la foto manual en lugar de la de Google

---

### Caso 3 — login Google con cuenta no vinculada

1. intentar login Google con una cuenta creada por flujo normal
2. verificar respuesta

### Resultado esperado

- frontend debe mostrar que esa cuenta no está vinculada a Google
- NO debe mostrar un error genérico tipo “falló el servidor”

---

## Nota operativa importante

Además del cambio visual/funcional en frontend, este backend requiere que en base exista la columna:

```sql
autenticado_con_google
```

El script manual quedó documentado en:

- `src/main/Docs/Testing/sql-auth-google-vinculo-explicito.sql`

Esto no cambia la UI directamente, pero sí explica por qué login Google ahora tiene reglas distintas a las anteriores.

---

## Conclusión

El frontend debe alinearse con esta regla:

```txt
En registro con Google, la foto de Google es la foto por defecto.
Subir una nueva imagen es opcional.
```

Si la pantalla sigue obligando a elegir archivo, entonces está implementando una regla vieja y ya no compatible con el backend actual.
