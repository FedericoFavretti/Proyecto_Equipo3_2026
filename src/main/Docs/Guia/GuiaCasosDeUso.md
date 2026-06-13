## 6. Contrato de implementación de casos de uso


Esta sección es la guía operativa para implementar Foodly. No es una sugerencia: cada caso de uso debe convertirse en comportamiento verificable de backend/frontend/mobile según corresponda.


Reglas generales para TODOS los casos de uso:


- Implementar el flujo principal, las precondiciones, los flujos alternativos y las postcondiciones tal como están descritos en el documento de casos de uso.
- Respetar literalmente los mensajes de error/aviso definidos por el documento cuando existan.
- No inventar pasos, estados, validaciones ni integraciones fuera de alcance.
- Toda operación que cambia estado debe estar en un servicio de aplicación/backend, no solo en el frontend.
- Los controladores deben delegar reglas de negocio; los servicios deben validar precondiciones y transiciones de estado.
- Los casos críticos deben tener pruebas del flujo principal y de los alternativos relevantes antes de considerarse terminados.
- Si un caso de uso contradice el modelo de dominio, arquitectura o alcance, detenerse y pedir decisión. NO resolver silenciosamente.


### 6.1. Casos comunes a todos los perfiles


#### CU-C01 — Iniciar Sesión


- **Actores**: Administrador, Local, Cliente.
- **Precondición**: el usuario debe tener cuenta registrada y activa.
- **Implementar**:
  1. Recibir correo electrónico y contraseña desde pantalla de inicio de sesión.
  2. Validar credenciales.
  3. Verificar que la cuenta no esté bloqueada.
  4. Redirigir al panel correspondiente según perfil/rol.
- **Alternativos obligatorios**:
  - Credenciales incorrectas: `El correo electrónico o la contraseña son incorrectos. Por favor, inténtelo nuevamente.`
  - Cuenta bloqueada: `Su cuenta ha sido suspendida. Contacte al administrador para más información.`
  - Debe existir camino alternativo hacia `CU-CL03/CU-M01 — Iniciar Sesión con Face ID` para mobile.
- **Postcondición**: usuario autenticado y autorizado a usar funciones de su perfil.


#### CU-C02 — Cerrar Sesión


- **Actores**: Administrador, Local, Cliente.
- **Precondición**: usuario autenticado.
- **Implementar**:
  1. Recibir acción `Cerrar Sesión`.
  2. Invalidar token/sesión activa.
  3. Redirigir a pantalla de inicio de sesión.
- **Alternativos**: no aplica.
- **Postcondición**: el usuario no puede acceder a funciones protegidas sin volver a autenticarse.


#### CU-C03 — Cambiar Contraseña


- **Actores**: Administrador, Local, Cliente.
- **Precondición**: usuario autenticado con correo electrónico válido registrado.
- **Implementar**:
  1. Mostrar opción desde `Configuración de Cuenta`.
  2. Solicitar contraseña actual y validarla.
  3. Generar código 2FA numérico de 6 dígitos con expiración de 10 minutos.
  4. Enviar código al correo registrado con asunto `Código de verificación para cambio de contraseña`.
  5. Mostrar mensaje: `Se ha enviado un código de 6 dígitos a su correo. Ingréselo a continuación para continuar.`
  6. Validar código correcto, no expirado y con máximo de intentos.
  7. Solicitar nueva contraseña y confirmación.
  8. Validar política: mínimo 8 caracteres, al menos una mayúscula y un número.
  9. Validar coincidencia de ambas contraseñas.
  10. Actualizar contraseña, invalidar código 2FA usado, mostrar confirmación y enviar correo de confirmación.
- **Alternativos obligatorios**:
  - Contraseña actual incorrecta: `La contraseña actual ingresada es incorrecta. Por favor, inténtelo nuevamente.`
  - Código 2FA incorrecto en 1.er o 2.º intento: `El código ingresado es incorrecto. Por favor, verifique su correo e inténtelo nuevamente. Le quedan [N] intentos.`
  - Código 2FA incorrecto en 3.er intento: `Ha superado el número máximo de intentos. Por seguridad, el proceso ha sido cancelado. Puede volver a intentarlo en 15 minutos.`
  - Código expirado: `El código de verificación ha expirado. Haga clic en «Reenviar código» para recibir uno nuevo.`
  - Nueva contraseña inválida: `La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.`
  - Contraseñas no coinciden: `Las contraseñas ingresadas no coinciden. Por favor, verifique e inténtelo de nuevo.`
  - Error al enviar correo: `No fue posible enviar el código de verificación. Por favor, verifique su conexión e inténtelo nuevamente.`
- **Postcondición**: contraseña actualizada, correo de confirmación enviado y código 2FA invalidado.


#### CU-C04 — Recuperar Contraseña por Correo Electrónico


- **Actores**: Administrador, Local, Cliente.
- **Precondición**: el usuario tiene cuenta registrada con correo válido y está en pantalla de inicio de sesión.
- **Implementar**:
  1. Mostrar opción `¿Olvidaste tu contraseña?`.
  2. Recibir correo electrónico.
  3. Verificar si corresponde a una cuenta registrada sin revelar existencia.
  4. Generar enlace de recuperación con expiración de 30 minutos.
  5. Enviar enlace por correo.
  6. Al abrir el enlace, mostrar formulario de nueva contraseña.
  7. Validar nueva contraseña y confirmación.
  8. Actualizar contraseña e invalidar enlace.
- **Alternativos obligatorios**:
  - Correo no registrado: `Si el correo ingresado está asociado a una cuenta, recibirá un enlace de recuperación en breve.`
  - Enlace expirado: `El enlace de recuperación ha expirado. Por favor, solicite uno nuevo.`
  - Nueva contraseña inválida: `La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.`
- **Postcondición**: el usuario puede ingresar con la nueva contraseña.


#### CU-C05 — Editar Datos de Cuenta de Usuario


- **Actores**: Administrador, Local, Cliente.
- **Precondición**: usuario autenticado.
- **Implementar**:
  1. Acceso desde `Mi Perfil` o `Configuración de Cuenta`.
  2. Mostrar datos actuales en formulario editable.
  3. Permitir modificar domicilio, foto de perfil y demás datos documentados.
  4. Guardar solo después de validar formato y contenido.
- **Alternativos obligatorios**:
  - Dato inválido: `El campo [nombre del campo] contiene un formato inválido. Por favor, revíselo e inténtelo de nuevo.`
  - Foto inválida: `El formato de imagen no es compatible. Se aceptan archivos JPG, PNG o GIF de hasta 5 MB.`
- **Postcondición**: perfil actualizado.


### 6.2. Casos del perfil Administrador


#### CU-A01 — Buscar y Listar Usuarios Registrados


- **Actor**: Administrador.
- **Precondición**: administrador autenticado.
- **Implementar**:
  1. Acceso al módulo `Gestión de Usuarios`.
  2. Mostrar listado completo de usuarios registrados.
  3. Permitir búsqueda por nombre, correo, tipo de usuario u otros criterios documentados.
  4. Permitir filtros por tipo de usuario, estado de cuenta, etc.
  5. Permitir ordenamiento por calificación global u otros criterios disponibles.
  6. Actualizar listado según criterios aplicados.
- **Alternativo obligatorio**: sin resultados: `No se encontraron usuarios que coincidan con los criterios de búsqueda seleccionados.`
- **Postcondición**: listado filtrado y ordenado visible.


#### CU-A02 — Aprobar o Rechazar Solicitud de Registro de Local


- **Actor**: Administrador.
- **Precondición**: existe al menos una solicitud de local en estado `Pendiente`.
- **Implementar**:
  1. Acceso al módulo `Solicitudes de Locales`.
  2. Mostrar solicitudes pendientes con correo, dirección, descripción e imágenes.
  3. Permitir revisar información del local solicitante.
  4. Permitir seleccionar `Aprobar` o `Rechazar`.
  5. Solicitar confirmación.
  6. Al confirmar, registrar resolución.
  7. Si aprueba, habilitar acceso al perfil de local.
  8. Si rechaza, denegar acceso al perfil de local.
  9. Enviar notificación por correo al local con la resolución.
- **Alternativo obligatorio**: si el administrador cancela antes de confirmar, no se genera ningún cambio y se vuelve al listado.
- **Postcondición**: local habilitado o rechazado y notificado por correo.


#### CU-A03 — Bloquear o Desbloquear Cuenta de Usuario


- **Actor**: Administrador.
- **Precondición**: administrador autenticado y usuario objetivo existente.
- **Implementar**:
  1. Localizar usuario mediante `CU-A01`.
  2. Permitir acción `Bloquear` o `Desbloquear`.
  3. Mostrar diálogo de confirmación con nombre del usuario y acción.
  4. Actualizar estado de cuenta al confirmar.
  5. Si se bloquea, invalidar todas las sesiones activas del usuario.
- **Alternativos obligatorios**:
  - Cancelación del diálogo: no aplicar cambios.
  - Error de actualización: `No se pudo actualizar el estado de la cuenta. Por favor, inténtelo nuevamente.`
- **Postcondición**: cuenta en estado bloqueada o desbloqueada.


### 6.3. Casos del perfil Local


#### CU-L01 — Solicitar Registro como Local Habilitado


- **Actor**: Local.
- **Precondición**: el local tiene cuenta creada pero aún no habilitada.
- **Implementar**:
  1. Acceso a formulario `Solicitar Habilitación`.
  2. Capturar correo electrónico, dirección física y descripción del tipo de comida.
  3. Adjuntar imágenes del local y/o productos.
  4. Enviar solicitud.
  5. Validar campos obligatorios, formato de correo, formato/tamaño de imágenes y que el nombre del local no esté registrado previamente.
  6. Registrar solicitud en estado `Pendiente`.
  7. Notificar al administrador.
- **Alternativos obligatorios**:
  - Campos faltantes: `Los siguientes campos son requeridos: [lista de campos faltantes]. Por favor, complételos antes de enviar.`
  - Correo inválido: `El correo electrónico ingresado no tiene un formato válido.`
  - Imágenes inválidas: `Solo se aceptan imágenes en formato JPG o PNG de hasta 10 MB cada una.`
- **Postcondición**: solicitud `Pendiente` para revisión por `CU-A02`.


#### CU-L02 — Gestionar Platos de Comida (Alta, Baja, Modificación)


- **Actor**: Local.
- **Precondición**: local habilitado y autenticado.
- **Implementar**:
  - **Alta**: `Agregar Plato`, completar nombre, descripción, precio, categoría e imagen; validar y registrar.
  - **Modificación**: seleccionar plato existente, `Editar`, actualizar datos y guardar tras validar.
  - **Baja**: seleccionar plato, `Eliminar`, solicitar confirmación y desactivar del catálogo.
- **Alternativos obligatorios**:
  - Precio inválido: `El precio debe ser un valor numérico mayor a cero.`
  - Nombre vacío: `El nombre del plato es obligatorio.`
  - Imagen inválida: `Solo se aceptan imágenes JPG o PNG de hasta 5 MB.`
- **Postcondición**: catálogo de platos actualizado.
- **Cuidado**: el caso menciona `categoría`, pero el modelo de dominio visible no la incluye. No agregarla al dominio sin decisión del equipo.


#### CU-L03 — Gestionar Promociones en Platos (Alta, Baja, Modificación)


- **Actor**: Local.
- **Precondición**: local habilitado/autenticado con al menos un plato registrado.
- **Implementar**:
  - **Alta**: `Nueva Promoción`, definir tipo, platos involucrados, descuento/beneficio y vigencia; validar y registrar.
  - **Modificación**: seleccionar promoción, `Editar`, actualizar y guardar tras validar.
  - **Baja**: seleccionar promoción, `Eliminar`, confirmar y desactivar.
- **Alternativos obligatorios**:
  - Fecha fin anterior a fecha inicio: `La fecha de fin de la promoción debe ser posterior a la fecha de inicio.`
  - Descuento fuera de rango: `El porcentaje de descuento debe estar entre 1% y 100%.`
  - Ningún plato seleccionado: `Debe seleccionar al menos un plato para aplicar la promoción.`
- **Postcondición**: promociones actualizadas y visibles para clientes.


#### CU-L04 — Registrar Apertura del Local


- **Actor**: Local.
- **Precondición**: local habilitado, autenticado y en estado `Cerrado`.
- **Implementar**:
  1. Acción `Abrir Local`.
  2. Verificar si ya está abierto.
  3. Solicitar confirmación.
  4. Al confirmar, registrar estado `Abierto` con fecha y hora actuales.
  5. Hacer visible el local para pedidos de clientes.
- **Alternativo obligatorio**: local ya abierto: `El local ya se encuentra registrado como abierto para el día de hoy.`
- **Postcondición**: local disponible para recibir pedidos.


#### CU-L05 — Registrar Cierre del Local


- **Actor**: Local.
- **Precondición**: local en estado `Abierto`.
- **Implementar**:
  1. Acción `Cerrar Local`.
  2. Verificar pedidos pendientes de confirmación.
  3. Si hay pendientes, mostrar advertencia y consultar si desea cerrar igual.
  4. Solicitar/recibir confirmación.
  5. Actualizar estado a `Cerrado`.
  6. Ocultar local para nuevos pedidos.
- **Alternativo obligatorio**: pedidos pendientes: `Tiene [N] pedido(s) pendiente(s) de confirmación. ¿Desea cerrar el local de todas formas? Los pedidos pendientes deberán ser atendidos manualmente.`
- **Postcondición**: local cerrado y sin recepción de nuevos pedidos.


#### CU-L06 — Buscar y Listar Pedidos Recibidos


- **Actor**: Local.
- **Precondición**: local autenticado.
- **Implementar**:
  1. Acceso a `Mis Pedidos`.
  2. Mostrar pedidos recibidos.
  3. Filtrar por estado (`pendiente`, `confirmado`, `rechazado`) y/o fecha.
  4. Ordenar por fecha, monto u otro criterio disponible.
  5. Mostrar listado actualizado.
- **Alternativo obligatorio**: sin resultados: `No se encontraron pedidos que coincidan con los criterios seleccionados.`
- **Postcondición**: pedidos filtrados y ordenados visibles.


#### CU-L07 — Confirmar Pedido de Cliente


- **Actor**: Local.
- **Precondición**: existe pedido en estado `Pendiente` enviado por un cliente.
- **Implementar**:
  1. Visualizar pedido pendiente desde `CU-L06`.
  2. Seleccionar `Confirmar Pedido`.
  3. Solicitar tiempo estimado de entrega en minutos.
  4. Simular pago electrónico, por ejemplo PayPal/Stripe.
  5. Si el pago se procesa, marcar pedido `Confirmado`.
  6. Generar factura PDF.
  7. Enviar factura al cliente por correo.
  8. Notificar al cliente por correo, interfaz web y push mobile.
- **Alternativos obligatorios**:
  - Tiempo no ingresado: `Debe ingresar el tiempo estimado de entrega para confirmar el pedido.`
  - Error de pago simulado: `No se pudo procesar el pago. El pedido no ha sido confirmado. Por favor, inténtelo nuevamente.`
  - Error PDF: `El pedido fue confirmado pero hubo un error al generar la factura. Se reintentará el envío automáticamente.`
- **Postcondición**: pedido confirmado, factura generada/en reintento si falló PDF y cliente notificado por los tres canales.


#### CU-L08 — Rechazar Pedido de Cliente


- **Actor**: Local.
- **Precondición**: pedido `Pendiente` y ejecución previa de `CU-L06`.
- **Implementar**:
  1. Seleccionar `Rechazar Pedido`.
  2. Seleccionar o escribir motivo.
  3. Solicitar confirmación.
  4. Al confirmar, marcar pedido `Rechazado`.
  5. Notificar al cliente por correo, web y push mobile.
- **Alternativo obligatorio**: sin motivo: `Debe seleccionar o escribir un motivo de rechazo antes de continuar.`
- **Postcondición**: pedido rechazado y cliente informado por los tres canales.


#### CU-L09 — Buscar y Listar Reclamos de Clientes


- **Actor**: Local.
- **Precondición**: local autenticado.
- **Implementar**:
  1. Acceso a `Reclamos`.
  2. Mostrar reclamos recibidos.
  3. Filtrar por estado (`pendiente`, `atendido`), fecha o cliente.
  4. Ordenar reclamos.
  5. Mostrar listado actualizado.
- **Alternativo obligatorio**: sin resultados: `No se encontraron reclamos que coincidan con los criterios seleccionados.`
- **Postcondición**: reclamos filtrados y ordenados visibles.


#### CU-L10 — Atender Reclamo de Cliente


- **Actor**: Local.
- **Precondición**: reclamo `Pendiente` asociado a pedido confirmado.
- **Implementar**:
  1. Acceder al reclamo desde `CU-L09`.
  2. Revisar motivo y detalles.
  3. Seleccionar resolución: reintegro del monto o compensación alternativa.
  4. Permitir nota/comentario opcional.
  5. Solicitar confirmación.
  6. Registrar resolución y actualizar reclamo a `Atendido`.
  7. Notificar al cliente por correo, web y push mobile.
- **Alternativos obligatorios**:
  - Sin tipo de resolución: `Debe seleccionar el tipo de resolución (reintegro o compensación) antes de confirmar.`
  - Cancelación antes de confirmar: reclamo permanece `Pendiente`.
- **Postcondición**: reclamo atendido y cliente notificado por los tres canales.


#### CU-L11 — Obtener Estadísticas del Local


- **Actor**: Local.
- **Precondición**: local autenticado con al menos un pedido registrado.
- **Implementar**:
  1. Acceso a `Estadísticas`.
  2. Seleccionar período.
  3. Calcular total de ventas, platos más pedidos y cantidad de pedidos por estado.
  4. Actualizar métricas al cambiar período.
- **Alternativo obligatorio**: sin datos: `No hay información disponible para el período seleccionado. Intente con un rango de fechas diferente.`
- **Postcondición**: estadísticas visibles para el período indicado.


#### CU-L12 — Buscar y Listar Clientes del Local


- **Actor**: Local.
- **Precondición**: local autenticado.
- **Implementar**:
  1. Acceso a `Mis Clientes`.
  2. Mostrar clientes que realizaron al menos un pedido al local.
  3. Filtrar por nombre, calificación u otros criterios disponibles.
  4. Ordenar listado.
  5. Mostrar listado actualizado.
- **Alternativo obligatorio**: sin clientes: `Aún no tiene clientes registrados. Aparecerán aquí una vez que realicen su primer pedido.`
- **Postcondición**: clientes filtrados y ordenados visibles.


#### CU-L13 — Calificar a un Cliente


- **Actor**: Local.
- **Precondición**: cliente con al menos un pedido al local y no calificado ya para ese pedido.
- **Implementar**:
  1. Seleccionar cliente desde `CU-L12`.
  2. Acción `Calificar Cliente`.
  3. Mostrar formulario con escala numérica y comentario.
  4. Registrar puntaje 1 a 5 y comentario opcional.
  5. Actualizar calificación global del cliente.
- **Alternativos obligatorios**:
  - Cliente sin pedidos en el local: `Solo puede calificar a clientes que hayan realizado al menos un pedido en su local.`
  - Doble calificación del mismo pedido: `Ya ha calificado a este cliente por el pedido seleccionado.`
- **Postcondición**: calificación registrada y global del cliente actualizada.


#### CU-L14 — Consultar Calificación Global del Local


- **Actor**: Local.
- **Precondición**: local autenticado.
- **Implementar**:
  1. Acceso a `Mi Calificación` o `Perfil`.
  2. Calcular promedio global del local.
  3. Mostrar promedio, total de valoraciones y detalle por puntuación.
- **Alternativo obligatorio**: sin calificaciones: `Su local todavía no ha recibido calificaciones de los clientes.`
- **Postcondición**: local visualiza su calificación global actualizada.


### 6.4. Casos del perfil Cliente


#### CU-CL01 — Crear Cuenta de Usuario (registro estándar y con Google)


- **Actor**: Cliente nuevo.
- **Precondición**: no debe existir cuenta con el correo o documento usado. Para Google, debe existir cuenta Google activa.


##### Modalidad A — Registro estándar


- **Implementar**:
  1. Desde pantalla de inicio, seleccionar `Registrarse con correo`.
  2. Capturar documento, nombre, apellido, domicilio y foto de perfil.
  3. Capturar correo y contraseña.
  4. Aceptar términos y condiciones.
  5. Validar unicidad de correo y documento.
  6. Validar formato de campos obligatorios.
  7. Crear cuenta y enviar correo de activación vigente por 24 horas.
  8. Al acceder al enlace, activar cuenta y redirigir al panel principal.
- **Alternativos obligatorios**:
  - Correo registrado: `El correo electrónico ingresado ya está asociado a una cuenta existente.`
  - Documento registrado: `El documento de identidad ingresado ya está asociado a una cuenta existente.`
  - Contraseña inválida: `La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.`
  - Enlace expirado: `El enlace de activación ha expirado. Se ha enviado uno nuevo a su correo.`
- **Postcondición**: cuenta creada y activa solo después de confirmación por correo.


##### Modalidad B — Registro con Google OAuth 2.0


- **Implementar**:
  1. Seleccionar `Registrarse con Google`.
  2. Redirigir a flujo OAuth 2.0 de Google.
  3. Recibir token de autorización y datos básicos: nombre, apellido y correo.
  4. Verificar que el correo de Google no esté asociado a cuenta existente.
  5. Solicitar datos complementarios: documento, domicilio y foto de perfil.
  6. Aceptar términos y condiciones.
  7. Validar datos complementarios.
  8. Crear cuenta vinculada a Google activa inmediatamente.
  9. Redirigir al panel principal.
- **Alternativos obligatorios**:
  - Usuario cancela Google: volver a registro sin crear cuenta.
  - Correo ya registrado: `El correo [correo] ya está asociado a una cuenta existente. ¿Desea iniciar sesión en su lugar?`
  - Google deniega permisos: `No fue posible completar la autenticación con Google. Por favor, intente nuevamente o regístrese con correo y contraseña.`
  - Datos incompletos: `Los siguientes campos son requeridos: [lista de campos]. Por favor, complételos para finalizar el registro.`
  - Error de conexión con Google: `No se pudo conectar con Google en este momento. Por favor, verifique su conexión e inténtelo nuevamente.`
- **Postcondición**: cuenta Google activa inmediatamente al completar datos.


#### CU-CL03 / CU-M01 — Iniciar Sesión con Google


- **Actor**: Cliente mobile.
- **Precondición**: cuenta activa; login previo con usuario/contraseña desde el dispositivo; dispositivo compatible; Face ID habilitado para la app.
- **Implementar**:
  1. Al abrir app mobile, detectar Face ID habilitado.
  2. Permitir `Iniciar sesión con Face ID`.
  3. Solicitar permiso al sistema operativo para reconocimiento facial/cámara.
  4. Activar escáner biométrico.
  5. Validar identidad.
  6. Verificar cuenta no bloqueada/suspendida.
  7. Autenticar y redirigir al panel principal.
- **Alternativos obligatorios**:
  - Permiso denegado: `Para usar Face ID es necesario permitir el acceso a la cámara. Puede habilitarlo desde la configuración del dispositivo.`
  - Rostro no coincide en 1.er intento: `No se pudo verificar su identidad. Por favor, intente nuevamente.`
  - 3 intentos fallidos: `No fue posible reconocer su rostro. Por seguridad, ingrese con su correo y contraseña.` Luego desactivar temporalmente Face ID y redirigir a login estándar.
  - Cuenta bloqueada: `Su cuenta ha sido suspendida. Contacte al administrador para más información.`
  - Face ID no configurado en dispositivo: `Su dispositivo no tiene Face ID configurado. Active el reconocimiento facial en los ajustes del teléfono para usar esta función.`
  - Face ID no habilitado en app: `No ha habilitado el inicio de sesión con Face ID para esta aplicación. Puede activarlo desde Configuración > Seguridad.`
  - Sesión anterior expirada: `Su sesión ha expirado. Por favor, ingrese con su correo y contraseña para reactivar el acceso con Face ID.`
- **Postcondición**: cliente autenticado en mobile.
- **Cuidado**: el documento nombra este caso como `CU-CL03` en el encabezado, pero el cuerpo dice `CU-M01`. No normalizar sin decisión.


#### CU-CL03 — Eliminar Cuenta de Usuario Propia


- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Implementar**:
  1. Acceso a configuración de cuenta.
  2. Acción `Eliminar Cuenta`.
  3. Informar implicancias de eliminación.
  4. Solicitar confirmación.
  5. Verificar que no existan pedidos activos ni reclamos pendientes.
  6. Eliminar o anonimizar datos personales manteniendo consistencia histórica.
  7. Cerrar sesión activa y redirigir a inicio.
- **Alternativos obligatorios**:
  - Pedidos activos: `No es posible eliminar la cuenta mientras tenga pedidos en curso. Espere a que todos sus pedidos sean resueltos.`
  - Reclamos pendientes: `No es posible eliminar la cuenta mientras tenga reclamos pendientes de resolución.`
- **Postcondición**: cuenta eliminada y credenciales inutilizables.


#### CU-CL04 — Buscar y Listar Locales


- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Implementar**:
  1. Acceso a `Locales`.
  2. Mostrar locales habilitados.
  3. Buscar por nombre, tipo de comida, etc.
  4. Filtrar por calificación mínima, estado abierto/cerrado, etc.
  5. Ordenar resultados.
  6. Mostrar listado actualizado.
- **Alternativo obligatorio**: sin resultados: `No se encontraron locales que coincidan con su búsqueda. Intente con otros criterios.`
- **Postcondición**: listado de locales según criterios.


#### CU-CL05 — Buscar y Listar Platos y Promociones


- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Implementar**:
  1. Acceso al módulo de platos general o desde un local específico.
  2. Mostrar platos y promociones activas.
  3. Filtrar por categoría, precio, nombre u otros criterios.
  4. Ordenar por precio, popularidad, etc.
  5. Mostrar listado actualizado.
- **Alternativo obligatorio**: sin resultados: `No se encontraron platos o promociones que coincidan con su búsqueda.`
- **Postcondición**: listado de platos/promociones visible según criterios.


#### CU-CL06 — Realizar un Pedido


- **Actor**: Cliente.
- **Precondición**: cliente autenticado y local en estado `Abierto`.
- **Implementar**:
  1. Seleccionar local abierto.
  2. Seleccionar platos y cantidades.
  3. Validar cantidades enteras mayores a cero.
  4. Revisar resumen con platos, cantidades y total.
  5. Validar que exista al menos un plato.
  6. Confirmar con `Realizar Pedido`.
  7. Revalidar que el local siga `Abierto` al confirmar.
  8. Registrar pedido en estado `Pendiente`.
  9. Notificar al local por correo y web.
- **Alternativos obligatorios**:
  - Local cerró durante preparación: `Lo sentimos, el local seleccionado cerró y no acepta más pedidos por el momento.`
  - Sin platos: `Debe agregar al menos un plato para realizar el pedido.`
  - Cantidad inválida: `La cantidad debe ser un número entero mayor a cero.`
- **Postcondición**: pedido `Pendiente` y local notificado.


#### CU-CL07 — Cancelar un Pedido


- **Actor**: Cliente.
- **Precondición**: pedido en estado `Pendiente`.
- **Implementar**:
  1. Acceso al historial de pedidos.
  2. Seleccionar pedido.
  3. Acción `Cancelar Pedido`.
  4. Verificar que siga `Pendiente`.
  5. Solicitar confirmación.
  6. Cambiar estado a `Cancelado`.
  7. Notificar al local por correo y web.
- **Alternativo obligatorio**: pedido confirmado: `No es posible cancelar este pedido porque ya fue confirmado por el local. Puede realizar un reclamo si lo considera necesario.`
- **Postcondición**: pedido cancelado y local notificado.


#### CU-CL08 — Buscar y Listar Historial de Pedidos Propios


- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Implementar**:
  1. Acceso a `Mis Pedidos`.
  2. Mostrar historial de pedidos propios.
  3. Filtrar por estado, fecha, local u otros criterios.
  4. Ordenar resultados.
  5. Mostrar listado actualizado.
- **Alternativos obligatorios**:
  - Sin pedidos: `Aún no ha realizado ningún pedido. ¡Explore los locales disponibles y realice su primer pedido!`
  - Filtros sin resultados: `No se encontraron pedidos que coincidan con los criterios seleccionados.`
- **Postcondición**: historial visible según criterios.


#### CU-CL09 — Realizar un Reclamo


- **Actor**: Cliente.
- **Precondición**: pedido `Confirmado` y sin reclamo previo para ese pedido.
- **Implementar**:
  1. Acceder al historial y seleccionar pedido confirmado.
  2. Acción `Realizar Reclamo`.
  3. Capturar motivo.
  4. Capturar tipo de compensación solicitada.
  5. Validar campos obligatorios.
  6. Registrar reclamo `Pendiente`.
  7. Notificar al local por correo y web.
- **Alternativos obligatorios**:
  - Motivo vacío: `Debe describir el motivo del reclamo antes de enviarlo.`
  - Reclamo duplicado: `Ya ha presentado un reclamo para este pedido. No es posible presentar más de un reclamo por pedido.`
- **Postcondición**: reclamo `Pendiente` y local notificado.


#### CU-CL10 — Calificar a un Local


- **Actor**: Cliente.
- **Precondición**: cliente realizó al menos un pedido al local y no lo calificó ya.
- **Implementar**:
  1. Acceso desde historial o perfil del local.
  2. Acción `Calificar Local`.
  3. Mostrar formulario con escala y comentario.
  4. Registrar puntaje 1 a 5 y comentario opcional.
  5. Actualizar calificación global del local.
- **Alternativos obligatorios**:
  - Sin pedidos en local: `Solo puede calificar locales en los que haya realizado al menos un pedido.`
  - Ya calificó: `Ya ha calificado a este local. Solo se permite una calificación por local.`
- **Postcondición**: calificación registrada y global del local actualizada.


#### CU-CL11 — Consultar Calificación Global del Cliente


- **Actor**: Cliente.
- **Precondición**: cliente autenticado.
- **Implementar**:
  1. Acceso a perfil o `Mi Calificación`.
  2. Calcular promedio global del cliente.
  3. Mostrar promedio, total de valoraciones y detalle por puntuación.
- **Alternativo obligatorio**: sin calificaciones: `Aún no ha recibido calificaciones de ningún local.`
- **Postcondición**: cliente visualiza su calificación global actualizada.


### 6.5. Inconsistencias del documento que NO se deben resolver silenciosamente


- `CU-CL01` aparece repetido en el índice para crear cuenta y crear cuenta con Google; implementar como un único caso con dos modalidades salvo decisión contraria.
- Face ID aparece como `CU-CL03` en el encabezado, pero como `CU-M01` en el cuerpo.
- `CU-CL03` también aparece como `Eliminar Cuenta de Usuario Propia`.
- El alcance excluye iOS, pero Face ID menciona iOS o equivalente Android; para mobile, preferir abstracción biométrica compatible y pedir confirmación si se requiere iOS real.
- `Plato.categoria` aparece en casos de uso, pero no en el modelo de dominio visible.
- El estado de reclamo del documento de casos de uso usa `Pendiente`/`Atendido`; el modelo enum visible usa `PENDIENTE`, `EN_PROCESO`, `SOLUCIONADO`. No mapear `Atendido` sin decisión explícita.


## 7. Casos de uso críticos y flujos de diagrama


Los casos críticos tienen prioridad porque sostienen el flujo principal de negocio, seguridad, operación comercial y experiencia del cliente. Si uno de estos casos falla, Foodly pierde valor funcional.


### 7.1. Lista crítica obligatoria


- `CU-C01` — Iniciar Sesión.
- `CU-A02` — Aprobar o Rechazar Solicitud de Registro de Local.
- `CU-L01` — Solicitar Registro como Local Habilitado.
- `CU-L02` — Gestionar Platos de Comida.
- `CU-L04/CU-L05` — Registrar Apertura y Cierre del Local.
- `CU-L06` — Buscar y Listar Pedidos Recibidos.
- `CU-L07` — Confirmar Pedido de Cliente.
- `CU-CL01` — Crear Cuenta de Usuario.
- `CU-CL05` — Buscar y Listar Platos y Promociones.
- `CU-CL06` — Realizar un Pedido.


### 7.2. Flujos de diagrama que deben guiar implementación


Estos flujos salen de los diagramas críticos del documento. Para estos casos NO alcanza con leer la tabla textual: también hay que respetar bifurcaciones y bucles del diagrama.


#### Diagrama CU-C01 — Iniciar Sesión


1. Inicio.
2. Usuario accede a pantalla de inicio de sesión.
3. Puede ir por alternativa biométrica Face ID en mobile.
4. Usuario ingresa correo y contraseña.
5. Sistema valida credenciales.
6. Si credenciales son incorrectas, mostrar mensaje A1 y volver a ingreso de credenciales.
7. Si son correctas, verificar cuenta bloqueada.
8. Si cuenta está bloqueada, mostrar mensaje A2 y finalizar.
9. Si no está bloqueada, redirigir al panel correspondiente y finalizar.


#### Diagrama CU-A02 — Aprobar/Rechazar Solicitud de Local


1. Precondición: existe solicitud `Pendiente`.
2. Administrador entra a `Solicitudes de Locales`.
3. Sistema muestra pendientes con correo, dirección, descripción e imágenes.
4. Administrador revisa información.
5. Administrador selecciona aprobar o rechazar.
6. Sistema solicita confirmación.
7. Si cancela, no cambia nada y vuelve al listado.
8. Si confirma, registrar resolución.
9. Si aprobada, habilitar acceso de local.
10. Si rechazada, denegar acceso de local.
11. En ambos casos, enviar correo con resolución y finalizar.


#### Diagrama CU-CL01.A — Registro estándar con correo


1. Precondición: usuario sin cuenta registrada.
2. Usuario selecciona `Registrarse con correo`.
3. Completa datos, correo, contraseña y acepta términos.
4. Sistema valida unicidad y formato.
5. Si datos inválidos, informa error A1-A3 y permite corregir.
6. Si datos válidos, crea cuenta y envía correo de activación.
7. Usuario abre enlace recibido.
8. Si enlace no está vigente, informar expiración A4 y enviar nuevo enlace.
9. Si enlace está vigente, activar cuenta, redirigir al panel principal y finalizar.


#### Diagrama CU-CL01.B — Registro con Google


1. Precondición: cuenta Google activa y correo no registrado.
2. Usuario selecciona `Registrarse con Google`.
3. Sistema inicia OAuth; Google autentica y entrega datos.
4. Si autenticación falla/cancela/error Google, informar B1/B3/B5 y volver a registro.
5. Si autenticación exitosa, verificar correo existente.
6. Si correo ya registrado, informar B2 y finalizar.
7. Si correo nuevo, solicitar datos complementarios y términos.
8. Validar datos complementarios.
9. Si inválidos, informar B4 y permitir corregir.
10. Si válidos, crear cuenta activa inmediata, redirigir a panel principal y finalizar.


#### Diagrama CU-CL05 — Buscar/Listar Platos y Promociones


1. Precondición: cliente autenticado.
2. Cliente accede al módulo de platos/promociones.
3. Sistema muestra platos y promociones activas.
4. Cliente aplica filtros y ordenamientos.
5. Sistema actualiza listado.
6. Si no hay resultados, informar sin coincidencias.
7. Si hay resultados, finalizar mostrando listado.


#### Diagrama CU-CL06 — Realizar Pedido


1. Precondiciones: cliente autenticado y local abierto.
2. Cliente selecciona local abierto.
3. Cliente selecciona platos y cantidades.
4. Validar cantidades.
5. Si cantidad inválida, informar A3 y volver a selección de platos/cantidades.
6. Cliente revisa resumen.
7. Validar que se agregó al menos un plato.
8. Si no hay platos, informar A2 y volver a selección.
9. Cliente confirma `Realizar Pedido`.
10. Sistema revalida que local siga abierto.
11. Si cerró, informar A1 y finalizar sin pedido.
12. Si sigue abierto, registrar pedido `Pendiente`, notificar al local y finalizar.


#### Diagrama CU-L01 — Solicitar Registro como Local Habilitado


1. Precondición: local con cuenta creada pero no habilitada.
2. Local accede a `Solicitar Habilitación`.
3. Completa datos, adjunta imágenes y envía solicitud.
4. Sistema valida campos obligatorios, correo, imágenes y local no registrado.
5. Si faltan campos, informar A1 y volver al formulario.
6. Si correo inválido, informar A2 y volver al formulario.
7. Si imágenes inválidas, informar A3 y volver al formulario.
8. Si todo es válido, registrar solicitud `Pendiente`, notificar administrador y finalizar.


#### Diagrama CU-L04 — Registrar Apertura del Local


1. Precondición: local habilitado, autenticado y cerrado.
2. Local selecciona `Abrir Local`.
3. Sistema verifica si ya está abierto.
4. Si ya está abierto, informar A1 y finalizar.
5. Si no, solicitar confirmación.
6. Local confirma.
7. Sistema registra estado `Abierto` con fecha/hora, vuelve visible el local y finaliza.


#### Diagrama CU-L05 — Registrar Cierre del Local


1. Precondición: local abierto.
2. Local selecciona `Cerrar Local`.
3. Sistema verifica pedidos pendientes de confirmación.
4. Si existen, mostrar advertencia A1 y consultar cierre de todas formas.
5. Si no existen, pasar a confirmación directa.
6. Local confirma cierre.
7. Sistema actualiza estado a `Cerrado`, deja de mostrarlo para nuevos pedidos y finaliza.


#### Diagrama CU-L06 — Buscar/Listar Pedidos Recibidos


1. Precondición: local autenticado.
2. Local accede a `Mis Pedidos`.
3. Sistema muestra pedidos recibidos.
4. Local aplica filtros y ordena.
5. Sistema muestra listado actualizado.
6. Si no hay resultados, informar A1 y finalizar.
7. Si hay resultados, finalizar mostrando listado.


#### Diagrama CU-L07 — Confirmar Pedido de Cliente


1. Precondición: existe pedido `Pendiente`.
2. Local visualiza pedido pendiente y selecciona `Confirmar Pedido`.
3. Local ingresa tiempo estimado de entrega.
4. Si no lo ingresó, informar A1 y volver al ingreso de tiempo.
5. Sistema ejecuta pago electrónico simulado.
6. Si pago falla, informar A2 y finalizar sin confirmar pedido.
7. Si pago correcto, marcar pedido `Confirmado`.
8. Generar factura PDF.
9. Si factura se genera correctamente, enviarla al cliente por correo.
10. Si falla generación de factura, informar A3 y programar reintento automático.
11. Notificar al cliente por correo, interfaz web y push mobile.
12. Finalizar con pedido confirmado y cliente notificado.


