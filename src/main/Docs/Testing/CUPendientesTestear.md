Casos de uso críticos pendientes de testear o cerrar

Fuente tomada como actualizada: `ListaCUTesteados.md`.

## Resumen ejecutivo

Casos críticos que ya NO deberían figurar como pendientes completos:

- CU-C01 — Iniciar sesión
- CU-L02 — Gestionar platos de comida

Casos críticos que siguen con pendientes reales:

- CU-A02 — Aprobar o rechazar solicitud de registro de local
- CU-L01 — Solicitar registro como local habilitado
- CU-L04 / CU-L05 — Apertura y cierre de local
- CU-L06 — Buscar y listar pedidos recibidos
- CU-L07 — Confirmar pedido de cliente
- CU-CL01 — Crear cuenta de usuario
- CU-CL05 — Buscar y listar platos y promociones
- CU-CL06 — Realizar un pedido

## Pendientes por caso de uso

### CU-A02 — Aprobar o rechazar solicitud de registro de local
Estado actual: pendiente crítico.

Falta validar/cerrar:
- flujo de rechazo desde controller/API
- notificación al local luego de aprobar o rechazar
- cobertura de cancelación y errores del flujo administrativo

### CU-L01 — Solicitar registro como local habilitado
Estado actual: probado parcialmente.

Falta validar/cerrar:
- caso contrario al alta como habilitado
- comportamiento cuando el local no debería quedar habilitado automáticamente

### CU-L04 / CU-L05 — Registrar apertura y cierre del local
Estado actual: parcial.

Falta validar/cerrar:
- confirmación explícita de apertura/cierre según el caso de uso
- advertencia funcional con cantidad de pedidos pendientes antes de cerrar
- manejo HTTP correcto de errores de negocio

### CU-L06 — Buscar y listar pedidos recibidos
Estado actual: parcial.

Falta validar/cerrar:
- DTO de salida específico
- no exponer datos internos/sensibles
- corregir mapeo/serialización de imágenes del local
- filtros por estado y fecha
- ordenamientos
- respuesta correcta cuando el local no existe

### CU-L07 — Confirmar pedido de cliente
Estado actual: parcial.

Falta validar/cerrar:
- manejo HTTP correcto de errores de negocio
- alternativo de error PDF con reintento automático
- notificación real por web y push mobile

### CU-CL01 — Crear cuenta de usuario
Estado actual: parcial.

Falta validar/cerrar:
- tests específicos de duplicado de email
- tests específicos de duplicado de documento
- tests específicos para comprobar persistencia de password encodeada
- tests específicos para comprobar envío de mail de activación
- validar estado inicial correcto de la cuenta y su activación end-to-end
- robustecer modelado del tipo de usuario
- completar bien el flujo de activación

### CU-CL05 — Buscar y listar platos y promociones
Estado actual: no implementado.

Falta validar/cerrar:
- implementación real del service
- respuesta funcional desde API
- cobertura de filtros y listado

### CU-CL06 — Realizar un pedido
Estado actual: parcial.

Falta validar/cerrar:
- notificación al local por correo y web
- manejo HTTP correcto de errores de negocio
- test dedicado del alternativo de local cerrado

## Casos de uso críticos faltantes hoy

Si hablamos con RIGOR, los casos de uso críticos que hoy siguen faltando por cerrar de verdad son:

1. CU-A02 — Aprobar o rechazar solicitud de registro de local
2. CU-L01 — Solicitar registro como local habilitado (faltan alternativos)
3. CU-L04 / CU-L05 — Apertura y cierre de local
4. CU-L06 — Buscar y listar pedidos recibidos
5. CU-L07 — Confirmar pedido de cliente
6. CU-CL01 — Crear cuenta de usuario
7. CU-CL05 — Buscar y listar platos y promociones
8. CU-CL06 — Realizar un pedido

## Observación importante

Aunque CU-L02 aparece testeado, todavía quedaron observaciones técnicas fuera de fase:
- autenticación/autorización real del actor Local
- manejo HTTP consistente de errores de negocio

Eso NO lo vuelve “no implementado”, pero SÍ indica deuda funcional/técnica pendiente.
