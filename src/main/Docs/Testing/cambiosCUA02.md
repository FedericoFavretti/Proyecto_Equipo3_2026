## CU-A02 - Aprobar o rechazar solicitud de registro de local

### Estado actual
Caso de uso funcional a nivel backend, con deudas puntuales todavia abiertas para darlo por cerrado del todo.

### API implementada
- `GET /api/v1/admins/solicitudes-locales/pendientes`
- `PUT /api/v1/admins/solicitudes-locales/{idLocal}`

### Request de resolucion
```json
{
  "estadoObjetivo": "Habilitado"
}
```

o

```json
{
  "estadoObjetivo": "Rechazado"
}
```

### Ajustes aplicados
- se reemplazo el contrato generico basado en `DtLocal` por `DtResolverSolicitudLocalRequest`
- se agrego `DtSolicitudLocalPendienteResponse` para listar pendientes con los datos requeridos por el caso de uso
- `AdminService` ahora valida que solo se permita resolver solicitudes `Pendiente`
- se corrigio el bug que reaplicaba el mismo estado actual en vez del estado pedido por el administrador
- aprobar deja `estadoLocal=Habilitado` y `estadoCuenta=Activo`
- rechazar deja `estadoLocal=Rechazado` y `estadoCuenta=Bloqueado`
- se mantiene `estaAbierto=false` al resolver
- se notifica al local mediante `RegistroLocalNotificador`
- se agrego `RestExceptionHandler` para mapear:
  - `400` en request invalido
  - `404` si el local no existe
  - `409` si la solicitud ya no esta pendiente
- `LocalRepositorioImpl` ahora hace `LEFT JOIN` con `usuario` para hidratar `email`, `foto`, `tipo` y `estado_cuenta`
- `LocalRepositorioImpl` dejo de parsear `imagenes` como string con llaves de PostgreSQL y ahora usa `ResultSet#getArray`
- `UsuarioRepositorio`/`UsuarioRepositorioImpl` agregaron `actualizarEstado(...)` para persistir el `estado` real del usuario
- `AdminService` dejo de usar `usuarioRepositorio.actualizar(local)` para este caso y ahora persiste explicitamente `usuario.estado`
- `AdminService` agrega validacion de `idLocal` obligatorio y valida que el local tenga correo asociado antes de intentar notificar la resolucion
- en paralelo se corrigio CU-L01 para exigir `passwd`, encodearla y fijar defaults de dominio en backend; eso era necesario para que las nuevas solicitudes queden consistentes y aparezcan bien en CU-A02

### Por que se hicieron estos cambios
- se reemplazo `DtLocal` por `DtResolverSolicitudLocalRequest` porque CU-A02 no necesita recibir el agregado completo del local: solo necesita saber que solicitud se esta resolviendo y con que estado objetivo. Mantener `DtLocal` hacia el contrato ambiguo y permitia mezclar datos que no pertenecen a esta accion administrativa.
- se agrego `DtSolicitudLocalPendienteResponse` porque la guia pide mostrar al administrador una bandeja de pendientes con correo, direccion, descripcion e imagenes. Reutilizar un DTO generico obligaba a exponer mas datos de los necesarios o a dejar campos inconsistentes.
- se agrego validacion de estado `Pendiente` en `AdminService` porque la resolucion solo tiene sentido sobre solicitudes aun no procesadas. Sin esa regla se podia aprobar o rechazar varias veces el mismo local, violando la logica del caso de uso.
- se corrigio el bug que reaplicaba el mismo estado actual del local porque hacia que la API respondiera OK sin cambiar realmente el resultado de la solicitud. Era un falso positivo funcional.
- se fijo `estadoLocal` y `estadoCuenta` de forma coordinada porque aprobar o rechazar no es solo cambiar el estado del local: tambien cambia el acceso del usuario al perfil del local segun la guia.
- se mantiene `estaAbierto=false` al resolver porque un local recien aprobado no deberia aparecer automaticamente abierto para operar; eso es responsabilidad de CU-L04.
- se incorporo el notificador en la resolucion porque la postcondicion del CU exige informar al local por correo.
- se agrego `RestExceptionHandler` porque antes los errores de negocio terminaban degradados a 500 genericos. Para un CU administrativo eso es inaceptable: si falta body, el local no existe o la solicitud ya fue resuelta, el cliente necesita una respuesta HTTP explicita.
- se corrigio `LocalRepositorioImpl` con `LEFT JOIN usuario` porque el listado de pendientes mostraba `email = null` aunque el correo existiera en la tabla `usuario`. El repositorio estaba leyendo solo la tabla `Local` y por eso devolvia un agregado incompleto.
- se cambio el parseo de `imagenes` a `ResultSet#getArray` porque PostgreSQL devuelve arrays con formato `{...}` y al tratarlos como string terminaban apareciendo llaves sobrantes en la API. Eso no era un detalle cosmetico: rompia el shape real del contrato.
- se agrego `actualizarEstado(...)` en `UsuarioRepositorio` porque `usuarioRepositorio.actualizar(local)` no expresaba bien la intencion del caso y ademas no garantizaba persistir el estado del usuario donde correspondia. La resolucion administrativa necesita actualizar `usuario.estado` de forma explicita.
- `AdminService` ahora valida `idLocal` y correo asociado porque el caso de uso no se completa solo con cambiar estados: tambien debe poder notificar al local. Si no hay correo, el backend debe fallar con una razon de negocio clara.
- en paralelo se corrigio CU-L01 porque CU-A02 depende de que las solicitudes se creen de forma consistente. Si CU-L01 dejaba registros sin `passwd`, sin defaults o sin fila consistente en `usuario`, la bandeja de pendientes de CU-A02 quedaba contaminada por datos defectuosos.

### Cobertura automatica validada
- `AdminServiceTest`
  - lista pendientes
  - aprueba solicitud pendiente
  - rechaza solicitud pendiente
  - rechaza estado objetivo invalido
  - rechaza request nulo
  - falla si el local no existe
  - falla si la solicitud ya fue resuelta
- `AdminControllerTest`
  - GET pendientes responde shape esperado
  - PUT resolver responde `204`
  - `400` si falta `estadoObjetivo`
  - `404` si el local no existe
  - `409` si la solicitud ya fue resuelta
- `LocalServiceTest`
  - ahora exige `passwd` para solicitar habilitacion
  - encodea la password antes de persistir `usuario`
  - ignora estados internos enviados por el cliente al crear la solicitud

### Verificacion manual realizada hoy
- `GET /api/v1/admins/solicitudes-locales/pendientes` devuelve la bandeja de solicitudes pendientes del CU-A02
- `PUT /api/v1/admins/solicitudes-locales/{idLocal}` con body JSON responde `204 No Content` cuando la resolucion se procesa correctamente
- se detecto y corrigio que antes el listado devolvia `email = null` e `imagenes` con llaves sobrantes

### Que falta para cerrar el caso de uso de verdad
- validar manualmente sobre base de datos que, luego de aprobar/rechazar:
  - `local.estado` quede en `Habilitado` o `Rechazado`
  - `usuario.estado` quede en `Activo` o `Bloqueado`
- agregar o ajustar tests de `AdminServiceTest` para reflejar el nuevo contrato de persistencia con `usuarioRepositorio.actualizarEstado(...)`
- agregar test de repositorio o integracion para `LocalRepositorioImpl` que cubra:
  - hidratacion de `email`
  - parseo correcto de `imagenes`
  - listado de pendientes con `LEFT JOIN usuario`
- verificar y decidir que hacer con datos legacy creados por versiones viejas que puedan no tener fila consistente en `usuario`
- validar de punta a punta la notificacion real por correo; hoy solo esta cubierta la invocacion del notificador, no la entrega efectiva
- la cancelacion previa a confirmar sigue resuelta implicitamente por UI/no envio del request; si se quiere cierre literal contra la guia, falta decidir si modelar o documentar mejor ese alternativo

### Limitacion conocida
No se puede validar manualmente una notificacion visual de frontend porque todavia no existe frontend. En backend si queda cubierto que el flujo invoca `RegistroLocalNotificador`, pero no la entrega final al usuario.
