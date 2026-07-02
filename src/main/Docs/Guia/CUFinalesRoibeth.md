# CUFinalesRoibeth

Documento de referencia para los casos de uso restantes asignados a **Roibeth**.

## Verificación realizada

Se contrastó la captura compartida con `src/main/Docs/Guia/GuiaCasosDeUso.md` y con el estado actual del código backend.

### Observación importante

En la captura aparece **"CU-L08: Buscar y Listar Historial de Pedidos Propios"**, pero en la guía oficial ese caso está documentado como **`CU-CL08`**.  
El `CU-L08` correcto en la guía es **"Rechazar Pedido de Cliente"**.

## Casos de uso pendientes de Roibeth

| Código verificado | Nombre | Actor principal |
| --- | --- | --- |
| CU-L08 | Rechazar Pedido de Cliente | Local |
| CU-C05 | Editar Datos de Cuenta de Usuario | Administrador, Local, Cliente |
| CU-CL03 | Eliminar Cuenta de Usuario Propia | Cliente |
| CU-L14 | Consultar Calificación Global del Local | Local |
| CU-CL08 | Buscar y Listar Historial de Pedidos Propios | Cliente |

---

## Orden sugerido por complejidad (de más simple a más complejo)

| Orden | Código | Nombre | Complejidad estimada | Por qué |
| --- | --- | --- | --- | --- |
| 1 | CU-CL08 | Buscar y Listar Historial de Pedidos Propios | Baja | Ya existe una base MUY parecida en `PedidoController.listarPedidos(...)` y `PedidoService.listarPedidos(...)`; el patrón de filtros y listado ya está implementado para locales. |
| 2 | CU-L14 | Consultar Calificación Global del Local | Baja-media | El dominio ya tiene `calificacionGlobal` en `Local`, pero falta exponer un endpoint y resolver el detalle por puntuación pedido por el CU. |
| 3 | CU-L08 | Rechazar Pedido de Cliente | Media | Ya existe endpoint `POST /api/v1/pedidos/{idPedido}/rechazar` y método `PedidoService.rechazarPedido(...)`, pero el servicio está vacío y el CU exige motivo + notificaciones. |
| 4 | CU-C05 | Editar Datos de Cuenta de Usuario | Media-alta | Abarca tres perfiles y probablemente implique actualización parcial de datos, validaciones, foto y coordinación entre `Usuario`, `Cliente` y `Local`. |
| 5 | CU-CL03 | Eliminar Cuenta de Usuario Propia | Alta | Tiene mayor riesgo de negocio: exige verificar pedidos activos, reclamos pendientes, mantener consistencia histórica y definir si se elimina o anonimiza información. |

## Recomendación práctica de implementación

### 1. Empezar por `CU-CL08`

**Por qué primero:**
- Ya existe infraestructura reutilizable en pedidos:
  - `src/main/java/com/example/demo/Logica/Controllers/PedidoController.java`
  - `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
  - `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`
- El backend ya sabe listar pedidos por local con filtros. Conceptualmente, para cliente es el MISMO problema, pero cambiando el criterio de consulta.

### 2. Seguir con `CU-L14`

**Por qué segundo:**
- `Local` y `DtLocal` ya tienen `calificacionGlobal`.
- Existe `CalificacionController` y `CalificacionService`, aunque todavía están prácticamente vacíos.
- Es un CU más de lectura que de mutación, así que el riesgo es menor que editar o eliminar cuenta.

### 3. Después `CU-L08`

**Por qué tercero:**
- El controller ya tiene el endpoint:
  - `POST /api/v1/pedidos/{idPedido}/rechazar`
- `PedidoService.rechazarPedido(long idPedido)` existe, pero hoy está vacío.
- La complejidad sube porque el CU no es solo cambiar estado: también pide **motivo obligatorio** y **notificación al cliente**.

### 4. Luego `CU-C05`

**Por qué cuarto:**
- No encontré en controllers actuales un flujo ya armado para edición de cuenta.
- Afecta a varios tipos de usuario y seguramente requiere decidir QUÉ campos realmente son editables y cómo validar foto/domicilio sin romper contratos existentes.

### 5. Dejar `CU-CL03` para el final

**Por qué último:**
- Es el más delicado a nivel arquitectura.
- No es solo un delete: el CU pide reglas de negocio previas y consistencia histórica.
- Además, hoy `ReclamoRepositorio` y `PedidoRepositorio` no muestran un contrato listo para esa verificación cruzada completa, así que probablemente vas a tener que extender repositorios y definir bien la estrategia de borrado/anónimización.

---

## 1) CU-L08 — Rechazar Pedido de Cliente

- **Actor**: Local.
- **Precondición**: pedido en estado `Pendiente` y ejecución previa de `CU-L06`.
- **Flujo principal**:
  1. Seleccionar `Rechazar Pedido`.
  2. Seleccionar o escribir motivo.
  3. Solicitar confirmación.
  4. Marcar pedido como `Rechazado`.
  5. Notificar al cliente por correo, web y push mobile.
- **Validación obligatoria**: si no hay motivo, mostrar: `Debe seleccionar o escribir un motivo de rechazo antes de continuar.`
- **Resultado esperado**: pedido rechazado y cliente informado.

## 2) CU-C05 — Editar Datos de Cuenta de Usuario

- **Actores**: Administrador, Local, Cliente.
- **Precondición**: usuario autenticado.
- **Flujo principal**:
  1. Acceso desde `Mi Perfil` o `Configuración de Cuenta`.
  2. Mostrar datos actuales en formulario editable.
  3. Permitir modificar domicilio, foto de perfil y demás datos documentados.
  4. Guardar solo después de validar formato y contenido.
- **Validaciones obligatorias**:
  - Dato inválido: `El campo [nombre del campo] contiene un formato inválido. Por favor, revíselo e inténtelo de nuevo.`
  - Foto inválida: `El formato de imagen no es compatible. Se aceptan archivos JPG, PNG o GIF de hasta 5 MB.`
- **Resultado esperado**: perfil actualizado.

## 3) CU-CL03 — Eliminar Cuenta de Usuario Propia

- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Flujo principal**:
  1. Acceder a configuración de cuenta.
  2. Ejecutar acción `Eliminar Cuenta`.
  3. Informar implicancias de la eliminación.
  4. Solicitar confirmación.
  5. Verificar que no existan pedidos activos ni reclamos pendientes.
  6. Eliminar o anonimizar datos personales manteniendo consistencia histórica.
  7. Cerrar sesión activa y redirigir a inicio.
- **Validaciones obligatorias**:
  - Pedidos activos: `No es posible eliminar la cuenta mientras tenga pedidos en curso. Espere a que todos sus pedidos sean resueltos.`
  - Reclamos pendientes: `No es posible eliminar la cuenta mientras tenga reclamos pendientes de resolución.`
- **Resultado esperado**: cuenta eliminada y credenciales inutilizables.

### Nota de implementación backend actual

- Se implementó con **anonimización segura** en lugar de delete físico para preservar historial de pedidos y reclamos.
- La cuenta recreada luego de una eliminación se considera **una identidad nueva**: inicia con carrito vacío y no hereda estado personal previo.
- Las calificaciones históricas asociadas a la cuenta eliminada se **archivan** y quedan excluidas del promedio, estadísticas visibles y vistas operativas del local.
- Decisiones operativas documentadas:
  - `Pedido` en estado `Pendiente` o `Confirmado` se considera “en curso”.
  - La eliminación mantiene el historial de pedidos sin reescribir sus estados originales.

## 4) CU-L14 — Consultar Calificación Global del Local

- **Actor**: Local.
- **Precondición**: local autenticado.
- **Flujo principal**:
  1. Acceder a `Mi Calificación` o `Perfil`.
  2. Calcular promedio global del local.
  3. Mostrar promedio, total de valoraciones y detalle por puntuación.
- **Validación obligatoria**: si no existen calificaciones, mostrar: `Su local todavía no ha recibido calificaciones de los clientes.`
- **Resultado esperado**: local visualiza su calificación global actualizada.

## 5) CU-CL08 — Buscar y Listar Historial de Pedidos Propios

- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Flujo principal**:
  1. Acceder a `Mis Pedidos`.
  2. Mostrar historial de pedidos propios.
  3. Filtrar por estado, fecha, local u otros criterios.
  4. Ordenar resultados.
  5. Mostrar listado actualizado.
- **Validaciones obligatorias**:
  - Sin pedidos: `Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!`
  - Filtros sin resultados: `No se encontraron pedidos que coincidan con los criterios seleccionados.`
- **Resultado esperado**: historial visible según criterios.

## Fuente

- `src/main/Docs/Guia/GuiaCasosDeUso.md`
- `src/main/java/com/example/demo/Logica/Controllers/PedidoController.java`
- `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
- `src/main/java/com/example/demo/Logica/Controllers/CalificacionController.java`
- `src/main/java/com/example/demo/Logica/Service/CalificacionService.java`
- `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`
- `src/main/java/com/example/demo/Persistencia/Repositorios/ReclamoRepositorio.java`
- Captura compartida por el usuario con la asignación de casos.
