# Diagnóstico para frontend — registro de cliente queda desactivado/pediente

**Fecha:** 2026-07-10  
**Proyecto:** Foodly Front / integración con backend

---

## Problema reportado

Testing informó este comportamiento en el registro estándar de cliente:

- al registrarse completando el formulario normal
- la cuenta aparece como desactivada o bloqueada
- no se muestra ningún mensaje indicando que se envió un correo de activación

---

## Verificación realizada en backend

Se revisó el flujo actual del backend y hoy ocurre esto:

### 1. El registro estándar crea la cuenta en estado pendiente

Backend, al registrar un cliente, deja estos valores:

- `estadoCuenta = Pendiente`
- `activo = false`

Eso está en:

- `src/main/java/com/example/demo/Logica/Service/ClienteService.java`

Conceptualmente esto significa:

- la cuenta **todavía no está bloqueada**
- la cuenta **todavía no está activa**
- la cuenta **queda pendiente de activación por correo**

---

### 2. El endpoint de registro devuelve el `Cliente` crudo

El endpoint:

```txt
POST /api/v1/clientes/registro
```

devuelve actualmente un `ResponseEntity<Cliente>`.

Eso está en:

- `src/main/java/com/example/demo/Logica/Controllers/ClienteController.java`

Por lo tanto, frontend recibe una respuesta que contiene un cliente con:

```txt
estado = Pendiente
activo = false
```

Si frontend usa esos campos para pintar el estado de la cuenta inmediatamente después del alta, entonces va a mostrar algo equivalente a:

- usuario desactivado
- cuenta no habilitada
- error de acceso

aunque en realidad el flujo esperado sea:

- cuenta creada
- correo de activación enviado
- usuario pendiente de confirmar email

---

### 3. El login no distingue bien pendiente vs bloqueado

En backend, el login todavía agrupa ambos casos con un mensaje genérico:

```txt
Usuario no activado o bloqueado.
```

Eso significa que si frontend intenta loguear automáticamente al usuario recién creado, o interpreta ese error sin discriminar, puede terminar mostrando un mensaje incorrecto.

---

## Conclusión funcional para frontend

Frontend NO debe interpretar el resultado del registro estándar como:

- cuenta bloqueada
- cuenta inválida
- error de negocio final

Frontend debe interpretarlo como:

- cuenta creada correctamente
- cuenta pendiente de activación
- se debe informar al usuario que revise su correo

---

## Qué debe revisar frontend

### 1. Qué hace después de `POST /api/v1/clientes/registro`

Revisar si, después del alta:

- se toma `activo=false` como error
- se toma `estado=Pendiente` como bloqueo
- se intenta redirigir a login automáticamente
- se intenta loguear automáticamente al usuario

Si ocurre cualquiera de esas cosas, ahí está gran parte del problema.

---

### 2. Qué campo usa para decidir el estado visual

Revisar si la UI decide qué mostrar usando:

- `activo`
- `estado`
- ambos

Hoy backend devuelve:

```txt
activo = false
estado = Pendiente
```

Eso NO debería traducirse en:

```txt
Tu usuario está bloqueado
```

Debería traducirse en algo como:

```txt
Tu cuenta fue creada. Te enviamos un correo para activarla.
```

---

### 3. Si existe lógica de auto-login o navegación directa al login

Si frontend:

- registra
- luego intenta iniciar sesión automáticamente

el backend puede responder con el mensaje de cuenta no activada, y la UI puede terminar mostrando un error que en realidad pertenece al flujo normal de activación.

En el registro estándar, el comportamiento correcto NO debería depender de un login inmediato.

---

## Comportamiento esperado de frontend

Después de un registro estándar exitoso, frontend debería:

1. considerar la operación como exitosa
2. NO tratar `Pendiente` como `Bloqueado`
3. mostrar un mensaje claro de post-registro
4. indicar que debe revisar su correo
5. idealmente ofrecer opción de reintentar o reenviar activación cuando exista ese flujo

---

## Mensaje recomendado para UI

Texto sugerido:

```txt
Tu cuenta fue creada correctamente. Te enviamos un correo de activación. Revisá tu bandeja de entrada y, si no lo encontrás, verificá spam o correo no deseado.
```

---

## Checklist para frontend

- [ ] revisar qué hace la pantalla al recibir respuesta exitosa de `POST /api/v1/clientes/registro`
- [ ] revisar si `activo=false` se interpreta como error de negocio
- [ ] revisar si `estado=Pendiente` se interpreta como cuenta bloqueada
- [ ] revisar si se intenta login automático después del registro
- [ ] revisar si la UI muestra un mensaje de confirmación de envío de correo
- [ ] revisar si el mensaje actual al usuario menciona activación por correo
- [ ] revisar si el estado visual “bloqueado/desactivado” está mal mapeado para el caso `Pendiente`

---

## Qué sería incorrecto en frontend

Sería incorrecto que la UI, después del alta exitosa, muestre cualquiera de estos comportamientos:

- “usuario bloqueado”
- “cuenta desactivada” como error final
- “credenciales inválidas”
- redirección automática a login sin explicar activación por correo
- intento de sesión automática con manejo de error visible para el usuario

---

## Qué sería correcto en frontend

Sería correcto que la UI:

- cierre el flujo de registro como exitoso
- no intente tratar la cuenta pendiente como cuenta bloqueada
- muestre un estado de “pendiente de activación”
- explique claramente que se envió un correo

---

## Nota técnica importante

Hoy el backend expone el estado real de la cuenta recién creada (`Pendiente` / `activo=false`), pero eso NO significa que el frontend deba mostrar ese dato en bruto al usuario final.

Frontend debe mapear el estado técnico a un mensaje funcional correcto.

En este caso:

```txt
Pendiente + activo=false
```

debe significar:

```txt
Cuenta creada, esperando activación por correo
```

y NO:

```txt
Cuenta bloqueada
```

