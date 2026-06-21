-CU-A02 Aprobar o rechazar solicitud de registro de local
        Estado actual
        Funcional a nivel backend, con bandeja de pendientes y resolucion administrativa especifica.

        API implementada
        - GET /api/v1/admins/solicitudes-locales/pendientes
        - PUT /api/v1/admins/solicitudes-locales/{idLocal}

        Cobertura validada
        - listado de solicitudes pendientes con id, email, nombre, direccion, descripcion e imagenes
        - aprobacion de solicitud pendiente via body JSON con estadoObjetivo
        - rechazo de solicitud pendiente via body JSON con estadoObjetivo
        - rechazo por estado objetivo invalido
        - 400 si falta estadoObjetivo
        - 404 si el local no existe
        - 409 si la solicitud ya fue resuelta
        - validacion de invocacion a RegistroLocalNotificador al resolver

        Ajustes aplicados
        - se reemplazo el contrato generico basado en DtLocal por DtResolverSolicitudLocalRequest
        - se agrego DtSolicitudLocalPendienteResponse para la bandeja administrativa
        - AdminService ya no reaplica el mismo estado actual; ahora cambia realmente a Habilitado o Rechazado
        - aprobar deja estadoLocal=Habilitado y usuario.estado=Activo
        - rechazar deja estadoLocal=Rechazado y usuario.estado=Bloqueado
        - se agrego actualizarEstado(...) en UsuarioRepositorio para persistir explicitamente usuario.estado
        - LocalRepositorioImpl ahora hace LEFT JOIN con usuario para hidratar email y otros datos del agregado
        - LocalRepositorioImpl usa ResultSet#getArray para parsear imagenes sin llaves sobrantes de PostgreSQL
        - AdminService valida idLocal, estado Pendiente y correo asociado antes de notificar

        Verificacion manual por API
        - GET /api/v1/admins/solicitudes-locales/pendientes devolvio la lista de pendientes de CU-A02
        - PUT /api/v1/admins/solicitudes-locales/{idLocal} devolvio 204 No Content al resolver correctamente

        Que falta para cerrar del todo el caso de uso
        - validar manualmente en base de datos que local.estado y usuario.estado queden sincronizados luego de aprobar/rechazar
        - agregar test de repositorio o integracion para LocalRepositorioImpl (email + imagenes + JOIN usuario)
        - ajustar tests de AdminService al nuevo contrato de persistencia con actualizarEstado(...)
        - verificar y decidir que hacer con datos legacy sin fila consistente en usuario
        - validar entrega real de notificacion por correo; hoy esta cubierta la invocacion del notificador, no la entrega efectiva

-Solicitar Hab local
        Estado actual
        Corregido para que la solicitud cree usuario + local de forma consistente con la base actual.

        Ajustes aplicados
        - ahora exige passwd
        - encodea la password antes de persistir usuario
        - fija defaults de dominio en backend: estadoCuenta=Pendiente, tipo=local, estadoLocal=Pendiente, calificacionGlobal=0.0, estaAbierto=false
        - ignora estados internos enviados por el cliente

        Que falta para cerrar del todo el caso de uso
        - decidir si el requerimiento de passwd en esta etapa esta alineado de verdad con la guia o si conviene rediseñar el flujo para separar solicitud y cuenta
-CU-L02 Gestionar platos de comida alta
        Estado actual
        Cubierto a nivel de negocio en LocalServiceTest:
        - alta de plato para local habilitado
        - rechazo por nombre vacio
        - rechazo por precio invalido
        - rechazo por imagen invalida
        - rechazo si el local no esta habilitado
        - modificacion de plato existente
        - rechazo si el plato no existe
        - permite mantener el mismo nombre del propio plato
        - rechazo si el plato pertenece a otro local
        - baja logica: desactiva el plato en lugar de borrarlo
-CU-L02 Gestionar platos de comida 
        - Modificacion de plato 

        Verificacion manual por API
        - modificacion OK via PUT /api/v1/locales/platos/{idPlato}
        - baja OK via DELETE /api/v1/locales/platos/{idPlato}
        - se confirmo que el 404 previo venia de usar mal la ruta /api/v1/pedidos/locales/platos/{idPlato}; la correcta para CU-L02 es /api/v1/locales/platos/{idPlato}
        - se confirmo que la baja no elimina el registro del plato: lo deja persistido con disponible=false

        Ajustes aplicados
        - LocalService ahora valida nombre, precio e imagenes del plato con mensajes especificos.
        - La modificacion valida existencia del plato, pertenencia al local y duplicados excluyendo el propio id.
        - La baja ya no hace delete fisico; deja disponible=false y persiste el cambio.

        Pendiente fuera de esta fase
        - autenticacion/autorizacion real del actor Local segun la precondicion del CU
        - manejo HTTP consistente de errores de negocio
-CU-L04 / CU-L05 Apertura y cierre de local
        Estado actual
        Apertura OK y cierre OK sin pedidos pendientes.
        Tambien quedaron cubiertos los alternativos basicos:
        - no abrir si ya estaba abierto
        - no cerrar si ya estaba cerrado
        - no cerrar si hay pedidos pendientes

        Bugs corregidos
        - LocalRepositorioImpl no mapeaba el id del Local y eso rompia apertura/cierre al actualizar.
        - PedidoRepositorioImpl consultaba la tabla equivocada (pedidos en vez de pedido) para validar pendientes.
        - docker-compose no estaba pasando variables MP_* al contenedor app y la aplicacion no iniciaba.

        Que falta para cerrar del todo el caso de uso
        - confirmacion explicita de apertura/cierre segun el caso de uso
        - advertencia funcional con cantidad de pedidos pendientes antes de cerrar
        - manejo HTTP correcto de errores de negocio (hoy varias validaciones terminan en 500)
-Crear Cliente (puede mejorarse con los puntos subsiguientes):
        2) Qué hay que corregir en ClienteService.registrarUsuario
        Archivo: src/main/java/com/example/demo/Logica/Service/ClienteService.java
        
        ya funciona activar cuenta
-

-CU-L07 Confirmar Pedido de Cliente
        Estado actual
        Confirmacion OK via POST /api/v1/pedidos/{idPedido}/confirmar enviando body JSON con tiempoEstimadoEntregaMinutos.
        Se verifico correctamente sobre pedido existente en estado Pendiente.

        Request utilizada
        {
          "tiempoEstimadoEntregaMinutos": 35
        }

        Cobertura validada
        - carga de tiempo estimado desde request
        - cambio de estado a Confirmado
        - pagoSimulado en true
        - generacion/persistencia de factura asociada al pedido

        Ajustes necesarios para que la prueba funcionara
        - se cambio el contrato HTTP del endpoint para recibir DtConfirmarPedidoRequest
        - PedidoService dejo de depender de tiempoestentrega precargado en base y ahora lo setea desde el request
        - se separaron responsabilidades en PagoSimuladoService, FacturaService y NotificacionPedidoService
        - se corrigio FacturaRepositorioImpl.actualizar, que tenia un parentesis sobrante en SQL

        Que falta para cerrar del todo el caso de uso
        - manejo HTTP correcto de errores de negocio; hoy varias validaciones todavia terminan en 500
        - implementar de verdad el alternativo de error PDF con reintento automatico
        - implementar notificacion real por web y push mobile

-CU-L08 Rechazar Pedido de Cliente
        Estado actual
        Rechazo OK via POST /api/v1/pedidos/{idPedido}/rechazar enviando body JSON con motivo.
        Se verifico correctamente sobre pedido existente en estado Pendiente, sin tocar el modelo/schema de Pedido.

        API implementada
        - POST /api/v1/pedidos/{idPedido}/rechazar

        Request utilizada
        {
          "motivo": "No contamos con disponibilidad para prepararlo"
        }

        Cobertura validada
        - motivo obligatorio con mensaje funcional del CU
        - cambio de estado a Rechazado
        - persistencia del motivo en Notificacion.mensaje asociado al pedido
        - envio de correo al cliente con el motivo de rechazo
        - 404 si el pedido no existe
        - 409 si el pedido ya no esta en estado Pendiente

        Ajustes aplicados
        - se creo DtRechazarPedidoRequest como contrato HTTP especifico para CU-L08
        - PedidoController e iPedidoController ahora reciben body JSON con motivo en /rechazar
        - PedidoService implementa la validacion de motivo, existencia del pedido y estado Pendiente antes de rechazar
        - el pedido se persiste con estado Rechazado y luego se delega la comunicacion del rechazo
        - se reutilizo NotificacionPedidoService para crear una Notificacion de tipo Pedido con el motivo dentro de mensaje
        - se eligio guardar el motivo en Notificacion.mensaje porque el modelo de dominio actual no contempla motivo dentro de Pedido
        - NotificacionRepositorioImpl quedo corregido para setear realmente los parametros del INSERT/UPDATE; antes la notificacion podia parecer guardada pero no persistirse bien
        - se agregaron tests de controller y service para motivo vacio, pedido inexistente y rechazo exitoso

        Verificacion manual por API
        - GET /api/v1/pedidos/locales/{idLocal}?estado=Pendiente permite ubicar pedidos pendientes para probar el CU
        - POST /api/v1/pedidos/{idPedido}/rechazar con body JSON valido devolvio 200 OK
        - POST /api/v1/pedidos/{idPedido}/rechazar con motivo vacio devolvio 400 Bad Request con el mensaje del CU
        - se debe verificar en base de datos:
          - pedido.estado = Rechazado
          - existencia de fila en notificacion
          - existencia de relacion en pedido_notificacion

        Que falta para cerrar del todo el caso de uso
        - autenticacion/autorizacion real del actor Local; hoy el rechazo sigue resolviendose solo por idPedido
        - notificacion real por web y push mobile; por ahora queda persistencia + correo y logs para los otros canales
        - decidir si el motivo debe seguir viviendo como mensaje de notificacion o si mas adelante el dominio necesitara un modelado estructurado del rechazo
        - agregar prueba de integracion real contra BD para confirmar insercion en notificacion + pedido_notificacion
        - revisar si el correo al cliente debe considerarse obligatorio o si necesita politica de reintento cuando falle infraestructura de mail

-CU-L06 Buscar y Listar Pedidos Recibidos
        Estado actual
        Listado rediseñado via GET /api/v1/pedidos/locales/{idLocal} siguiendo camino B.
        La API ya no reutiliza DtPedido para este caso de uso y ahora responde con DTO de listado especifico.

        Ajustes aplicados
        - se creo DtPedidoListadoResponse como contrato de salida especifico para CU-L06
        - se creo DtPedidoListadoFiltro para filtros por estado, fechaDesde, fechaHasta, ordenarPor y direccion
        - PedidoController e iPedidoController ahora aceptan query params de listado y delegan con el nuevo contrato
        - PedidoService dejo de usar PedidoMapper para CU-L06 y ahora usa un flujo especifico de listado
        - se agrego PedidoListadoMapper para mapear proyecciones livianas a la response del caso de uso
        - PedidoRepositorio/PedidoRepositorioImpl incorporaron listarRecibidosPorLocal(...) con SQL especifico
        - el repositorio ahora devuelve cliente resumen y cantidadItems, sin hidratar el agregado Pedido completo
        - se elimina la causa del 500 previo por detalles = null al listar

        Cobertura validada
        - listado resumido con filtro
        - rechazo por campo de orden invalido
        - rechazo por rango de fechas invalido
        - controller mapping sin rutas duplicadas

        Shape actual de la respuesta
        - id
        - fecha
        - estado
        - total
        - tiempoEstEntrega
        - cliente { id, nombre, apellido }
        - cantidadItems

        Que falta para cerrar del todo el caso de uso
        - test de controller para validar query params y shape JSON
        - manejo HTTP correcto de errores de negocio; hoy local inexistente sigue dependiendo de RuntimeException y deberia mapear a 404
        - confirmar con frontend si el alternativo "sin resultados" se resuelve mostrando mensaje sobre 200 + []

-CU-CL05 Buscar y Listar Platos y Promociones
        Estado actual
        Busqueda funcional via POST /api/v1/clientes/busqueda con retorno combinado de platos y promociones.
        Se verifico manualmente que la busqueda por nombre devuelve platos existentes y que promociones retorna [] cuando no hay promociones vigentes cargadas.

        API implementada
        - POST /api/v1/clientes/busqueda

        Cobertura validada
        - busqueda por nombre
        - filtro por local mediante dtLocal.id
        - orden por precio ascendente/descendente
        - orden alfabetico
        - filtro por promocionActiva
        - 400 con mensaje de "sin resultados" cuando platos y promociones quedan vacios

        Verificacion manual por API
        - con body { "nombre": "Hamburguesa" } devolvio 200 OK y el plato esperado
        - se confirmo que promocionActiva=true deja la busqueda sin resultados si la tabla promocion no tiene filas vigentes
        - se confirmo que filtrar por dtLocal.id incorrecto tambien produce el alternativo de sin resultados

        Que falta para cerrar del todo el caso de uso
        - categorias: la guia del CU las pide como criterio de filtro, pero hoy no existe categoria en DtFiltro, en el dominio Plato ni en la persistencia
        - popularidad: la guia del CU la menciona como criterio de orden, pero hoy no existe ningun campo, calculo ni ordenamiento implementado para eso
        - definir el contrato final del alternativo "sin resultados"; hoy se responde 400, pero funcionalmente podria requerirse 200 + listas vacias segun decision de UX/frontend
        - agregar tests de integracion de repositorio para validar SQL real, combinacion de filtros y ordenamientos

        Problemas detectados en la devolucion de la API
        - dtLocal expone mas informacion de la necesaria para este CU porque arrastra campos heredados de DtUsuario como email, tipo, estadoCuenta, foto y passwd=null
        - el shape del local no esta especializado para CU-CL05; hoy se devuelve el DTO amplio en lugar de un resumen orientado al caso de uso
        - local.imagenes se esta devolviendo con serializacion corrupta/ensuciada en algunos registros, con llaves y escapes sobrantes en lugar de una lista limpia de strings
        - promociones hoy retorna correctamente [], pero falta validar con datos reales cargados el shape final cuando existan promociones activas
        - revisar si corresponde ocultar datos sensibles o irrelevantes del local antes de exponer la respuesta al cliente
        - filtrar explicitamente platos disponibles/visibles si esa es la regla funcional esperada del catalogo cliente

-CU-CL06 Realizar un Pedido
        Estado actual
        Alta OK via POST /api/v1/pedidos.
        Se verifico registro de pedido pendiente con recalculo de total y persistencia de cabecera + detalles.

        Cobertura validada
        - rechaza cuando no hay platos
        - rechaza cantidad invalida
        - rechaza plato que no pertenece al local
        - revalida que el local este abierto antes de registrar
        - recalcula total desde los platos persistidos, sin confiar en el total enviado por request

        Que falta para cerrar del todo el caso de uso
        - notificacion al local por correo y web segun GuiaCasosDeUso.md
        - validar/expresar mejor errores HTTP de negocio (hoy terminan como RuntimeException generica)
        - cubrir explicitamente el alternativo de local cerrado con test dedicado y mensaje documentado

-CU-C01 Login
    Funciona

-CU-C05 Editar Datos de Cuenta de Usuario
        Estado actual
        Parcialmente funcional a nivel backend via PUT /api/v1/usuarios/perfil con autenticacion JWT.
        Se verifico que la edicion parcial funciona al menos para cambios simples que no tocan credenciales, por ejemplo actualizar solo `nombre`.

        API implementada
        - PUT /api/v1/usuarios/perfil

        Contrato actual verificado
        - Header obligatorio: Authorization: Bearer {token}
        - Content-Type: multipart/form-data
        - Cliente puede editar: nombre, apellido, email, password, direccion.calle, direccion.numero, direccion.ciudad, direccion.codigoPostal y foto
        - Local puede editar: nombre, descripcion, email, password, direccion.calle, direccion.numero, direccion.ciudad, direccion.codigoPostal y foto
        - Admin puede editar: email y password

        Ajustes aplicados
        - se centralizo el CU en un endpoint unico dentro de UsuarioController
        - UsuarioService ahora aplica whitelist por actor y actualizacion parcial segura sin reconstruir ciegamente el agregado
        - UsuarioRepositorioImpl.actualizar(...) ya persiste primero la tabla base `usuario` y luego la tabla especifica (`cliente`, `local` o `administrador`)
        - se agrego soporte real de persistencia para Administrador con AdministradorRepositorio y AdministradorRepositorioImpl
        - ClienteRepositorioImpl y LocalRepositorioImpl ahora hidratan correctamente datos base del usuario (email, passwd, foto, estado, tipo) para no romper autenticacion ni updates posteriores
        - si cambia email o password, el backend intenta invalidar el token actual para forzar relogin y evitar inconsistencias del JWT
        - se instrumentaron logs en JwtAuthenticationFilter para distinguir mejor errores de parsing/validacion del token durante las pruebas

        Verificacion manual por API
        - login OK via POST /api/v1/usuarios/login devolviendo JWT
        - edicion OK de solo `nombre` via PUT /api/v1/usuarios/perfil con Bearer token valido
        - al intentar editar multiples campos incluyendo `email`, la request llega al service pero termina en 500 por problema de infraestructura en base

        Error detectado actualmente
        - cuando la edicion incluye `email` o `password`, UsuarioService intenta invalidar la sesion actual guardando el token en `token_blacklist`
        - la base actual no tiene creada la tabla `token_blacklist`
        - el error observado es:
          - `ERROR: relation "token_blacklist" does not exist`
          - `BadSqlGrammarException: INSERT INTO token_blacklist (token, expiracion) VALUES (?, ?) ON CONFLICT DO NOTHING`
        - por eso hoy:
          - cambiar solo campos no credenciales (ej. `nombre`) funciona
          - cambiar credenciales o combinaciones que incluyan `email`/`password` rompe con 500

        Como solucionarlo
        - crear la tabla `token_blacklist` en PostgreSQL para que la invalidacion de tokens funcione como fue implementada
        - estructura minima esperada por el repositorio actual:
          - `token` TEXT PRIMARY KEY
          - `expiracion` TIMESTAMP NOT NULL
        - SQL sugerido:
          - `CREATE TABLE token_blacklist (token TEXT PRIMARY KEY, expiracion TIMESTAMP NOT NULL);`
        - despues de crearla, reintentar:
          - login
          - PUT /api/v1/usuarios/perfil cambiando `email` o `password`
          - verificar que responda correctamente y que luego obligue a relogin

        Que falta para cerrar del todo el caso de uso
        - crear y versionar formalmente la tabla `token_blacklist` en el esquema/migracion del proyecto para no depender de ajustes manuales
        - validar manualmente el flujo completo de cambio de `email` y de `password` con relogin posterior
        - revisar si login debe bloquear cuentas no activas o pendientes; hoy la implementacion actual no parece exigir explicitamente estado Activo
        - mapear mejor errores de infraestructura/autenticacion para no exponer 500 genericos cuando falten dependencias de base
        - confirmar con frontend el armado definitivo del multipart/form-data y el manejo UX cuando el token se invalida por cambio de credenciales

-CU-CL08 Buscar y Listar Historial de Pedidos Propios
        Estado actual
        Funcional a nivel backend de forma temporal via GET /api/v1/pedidos/clientes/{idCliente}.
        Se puede listar historial propio por idCliente con filtros por estado, fecha e idLocal, reutilizando el contrato de listado de pedidos.

        API implementada
        - GET /api/v1/pedidos/clientes/{idCliente}

        Cobertura validada
        - listado de historial del cliente con local resumido en la respuesta
        - filtro por estado
        - filtro por rango de fechas
        - filtro por idLocal
        - ordenamiento por fecha, total y estado
        - mensaje de sin pedidos
        - mensaje de filtros sin resultados
        - adaptacion del DTO compartido de listado sin crear DTO nuevo

        Ajustes aplicados
        - DtPedidoListadoResponse ahora soporta ambos contextos: cliente o local, con serializacion NON_NULL
        - PedidoListadoView se extendio para soportar datos de local ademas de cliente
        - PedidoListadoMapper ahora mapea cliente o local segun el contexto del listado
        - DtPedidoListadoFiltro se extendio con idLocal
        - PedidoRepositorio/PedidoRepositorioImpl incorporaron listarHistorialPorCliente(...) y existePedidoPorCliente(...)
        - se corrigio un bug de PostgreSQL tipando NULL::bigint y NULL::varchar en la query compartida para evitar 500 por conversion de tipos

        Shape actual de la respuesta
        - id
        - fecha
        - estado
        - total
        - tiempoEstEntrega
        - local { id, nombre }
        - cantidadItems

        Que falta para cerrar del todo el caso de uso
        - reemplazar el endpoint temporal /clientes/{idCliente} por una resolucion real del cliente autenticado, alineada con la precondicion "cliente autenticado"
        - mover el acceso a un endpoint semantico del actor, por ejemplo /api/v1/pedidos/mis-pedidos, cuando se resuelva autenticacion
        - agregar test de integracion o verificacion manual completa sobre SQL real con datos de base para combinaciones de filtros
        - revisar manejo HTTP de cliente inexistente; hoy depende de RuntimeException y deberia resolverse con contrato mas explicito
        - confirmar con frontend/UX si el alternativo de "sin resultados" debe mantenerse como 400 o si corresponde 200 con lista vacia + mensaje

-CU-L14 Consultar Calificacion Global del Local
        Estado actual
        Funcional a nivel backend con dos accesos:
        - endpoint final autenticado via GET /api/v1/calificaciones/local/mi-calificacion
        - endpoint temporal de desarrollo via GET /api/v1/calificaciones/local/{idLocal}/mi-calificacion-dev para probar por Postman sin JWT

        API implementada
        - GET /api/v1/calificaciones/local/mi-calificacion
        - GET /api/v1/calificaciones/local/{idLocal}/mi-calificacion-dev

        Cobertura validada
        - calcula promedio global del local a partir de las calificaciones recibidas de tipo Cliente_a_local
        - devuelve total de valoraciones
        - devuelve detalle por puntuacion de 1 a 5
        - informa el alternativo "Su local todavía no ha recibido calificaciones de los clientes."
        - sincroniza local.calificacionGlobal como dato derivado/cache al consultar y al guardar nuevas calificaciones del cliente al local

        Ajustes aplicados
        - CalificacionRepositorio/CalificacionRepositorioImpl ahora pueden listar calificaciones recibidas por local
        - la persistencia de calificaciones se corrigio para asociar cliente y local cuando ambos extremos vienen informados
        - CalificacionService expone resumen como Map<String,Object> para respetar la restriccion de no crear DTO nuevo
        - se agrego consultarCalificacionGlobalDelLocalPorId(...) para reutilizar la logica del endpoint temporal DEV

        Importante: endpoint temporal
        - GET /api/v1/calificaciones/local/{idLocal}/mi-calificacion-dev es SOLO para desarrollo/prueba manual sin JWT
        - no responde a la precondicion real del CU ("local autenticado")
        - debe eliminarse cuando quede cerrada la autenticacion real del actor

        Hallazgo de seguridad verificado
        - este endpoint temporal NO es el unico caso donde el actor Local se resuelve por path variable
        - tambien existe GET /api/v1/pedidos/locales/{idLocal} en PedidoController para CU-L06, que lista pedidos del local usando idLocal en la URL en vez de Authentication
        - eso esta MAL respecto a la guia, porque la precondicion dice "local autenticado" y no "cualquier caller que conozca el idLocal"
        - ademas, hoy SecurityConfig tiene authorizeHttpRequests(...).anyRequest().permitAll(), por lo que Spring Security no esta exigiendo autenticacion/autorizacion real en las rutas
        - tambien hay autenticacion manual duplicada en controllers: por ejemplo CalificacionController.consultarCalificacionGlobalDelLocal(...) y UsuarioController.editarDatosDeCuentaDeUsuario(...) verifican a mano `authentication == null || !authentication.isAuthenticated()` y devuelven 401 desde el propio controller

        Que falta para cerrar del todo el caso de uso
        - eliminar el endpoint temporal /local/{idLocal}/mi-calificacion-dev una vez que la prueba con JWT quede estable
        - reemplazar la resolucion por idLocal en el endpoint final y en endpoints similares del actor Local por resolucion desde Authentication/JWT
        - corregir SecurityConfig para dejar de usar anyRequest().permitAll() y proteger explicitamente las rutas segun actor/perfil
        - revisar endpoints vecinos del perfil Local, en especial GET /api/v1/pedidos/locales/{idLocal}, PUT /api/v1/locales/{idLocal}/apertura y PUT /api/v1/locales/{idLocal}/cierre, porque hoy siguen exponiendo identidad del actor por path
- cuando Spring Security quede bien configurado, retirar los chequeos manuales de 401 en controllers y dejar que la seguridad resuelva acceso/autorizacion antes de entrar al endpoint
- aun despues de eso, varios endpoints seguiran necesitando Authentication/Principal, pero solo para identificar al usuario autenticado; no para duplicar la validacion de acceso dentro del controller

-CU-CL03 Eliminar Cuenta de Usuario Propia
        Estado actual
        Implementado a nivel backend con anonimización segura y endpoint temporal de desarrollo mientras la autenticación real del actor queda pendiente.

        API implementada
        - DELETE /api/v1/usuarios/clientes/{idCliente}/cuenta-dev

        Cobertura validada
        - anonimiza datos personales del cliente
        - bloquea la cuenta y desactiva el perfil cliente
        - rechaza eliminación si existen pedidos activos
        - rechaza eliminación si existen reclamos pendientes de resolución
        - oculta clientes anonimizados de listados operativos basados en ClienteRepositorio.listarTodos()

        Ajustes aplicados
        - UsuarioService incorpora eliminarCuentaDeUsuarioPropia(...) con regla de anonimización en vez de delete físico
        - PedidoRepositorio/PedidoRepositorioImpl incorporan existePedidoActivoPorCliente(...) considerando Pendiente y Confirmado como pedidos en curso
        - ReclamoRepositorio/ReclamoRepositorioImpl incorporan existeReclamoPendientePorCliente(...)
        - ClienteRepositorioImpl.listarTodos() ahora excluye clientes con activo=false para evitar “basura funcional” en listados operativos
        - la anonimización deja email técnico no reutilizable, password inválida, documento neutral, dirección neutral y estado Bloqueado


        Verificacion manual por API
        - se confirmo que la ruta temporal existe y debe invocarse con metodo DELETE
        - al probar la misma URL con PUT devolvio 405 Method Not Allowed, consistente con el @DeleteMapping implementado
        - endpoint de prueba: DELETE /api/v1/usuarios/clientes/{idCliente}/cuenta-dev
        Decisión/limitación documentada
        - el dominio/documentación de reclamos menciona estado propio, pero la persistencia visible actual no lo modela explícitamente en la clase/repositorio
        - por eso, en esta iteración, cualquier reclamo asociado al cliente se considera pendiente de resolución para no arriesgar inconsistencia funcional antes del despliegue

        Que falta para cerrar del todo el caso de uso
        - reemplazar el endpoint temporal /clientes/{idCliente}/cuenta-dev por resolución real desde Authentication/JWT
        - definir formalmente si todos los pedidos Confirmado deben considerarse “en curso” o si hará falta un estado final adicional en el dominio
        - completar el modelado persistente del estado de Reclamo para distinguir Pendiente / En_proceso / Solucionado sin aproximaciones
        - validar manualmente por API y base de datos que la anonimización no rompa consultas históricas de pedidos y reclamos

