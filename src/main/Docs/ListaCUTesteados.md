-Solicitar Hab local(crea el local siempre como habilitado, faltaria crear el caso contrario)
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
        Listado OK via GET /api/v1/pedidos/locales/{idLocal}.
        Se verifico respuesta 200 con pedidos del local, incluyendo:
        - id, fecha, tiempoEstEntrega, total y domicilioEntrega
        - medioDePago, pagoSimulado y estado
        - local y cliente embebidos

        Respuesta observada
        - estado devuelto correctamente como Confirmado
        - tiempoEstEntrega serializado como Duration ISO-8601, por ejemplo PT35M
        - detalles llega en null al listar

        Error detectado en la API
        - el endpoint expone de mas porque devuelve List<Pedido> directo en vez de un DTO de salida
        - se filtran campos internos de Local y Cliente que no deberian salir en una API publica, por ejemplo:
          - passwd
          - tipo
          - foto
          - estado
        - el campo imagenes del local aparece mal serializado / deformado, lo que sugiere problema de mapeo o doble serializacion

        Que falta para cerrar del todo el caso de uso
        - implementar DTO de salida especifico para CU-L06
        - dejar de exponer datos internos/sensibles del dominio en la respuesta
        - corregir el mapeo/serializacion de imagenes del local
        - implementar filtros por estado y fecha segun GuiaCasosDeUso.md
        - implementar ordenamientos por fecha, monto u otro criterio
        - manejo HTTP correcto de errores de negocio; hoy local inexistente probablemente termina en 500 y no en 404

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