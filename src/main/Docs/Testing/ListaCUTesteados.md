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
