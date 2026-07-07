Documento de modelo de dominio
Foodly - Grupo 3
Integrantes:
● Facundo Cabrera
● Simón Corvo
● Federico Favretti
● Federico Laco
● Joaquín Ortíz
● María Nazarena Valiero
● Joaquín Poblete
● Roibeth García
Tutor: Ing. Fernando Arrieta

## Modelo de Dominio................................................................................................................
    1.1 Diagrama de Modelo de Dominio................................................................................
    1.2 Descripción del Modelo de Dominio................................................................................
    Restricciones................................................................................................................................
    2.1. Restricciones de unicidad................................................................................................
    2.2. Restricciones circulares................................................................................................
    2.3. Restricciones obligatorias................................................................................................
    2.4. Restricciones de validación................................................................................................
    2.5. Restricciones de dominio y atributos................................................................................
    2.6. Restricciones de uso.................................................................................................
## 1. Modelo de Dominio................................................................................................................
    1.1 Diagrama de Modelo de Dominio................................................................................
    Para este formato te voy a describir la imagen del modelo de Dominio:

    Usuario: id Long, email String, passwd string, foto string, estado EstadoCuenta, tipo string

    Usuario tiene 3 herencias:
    a-Administrador: nivelAcceso
    b-Local: nombre string, descripcion, string, estado EstadoLocal, CalificacionGlobal Double, estaAbierto boolean, imagenes list <string>
    c-Cliente: documento string, nombre string, apellido string, domicilio direccion, calificacion global double, activo boolean

    plato: id long, nombre string, descripcion string, precio double, imagenes list<string>,disponible boolean.

    pedido: id long, fecha datatime, estado estadopedido, tiempoentrega duration, mediopago string, pagosimulado boolean

    calificacion: id long, puntaje integer (1-5), comentario string, fecha datatime, tipo tipocalificacion

    Promocion: id long, descuento double, fechainicio date, fechafin date, descripcion string

    detallepedido: id long, cantidad integer, preciounitario double, subtotal double

    factura: id long, numero string, monto double, archivo pdf string

    notificacion: id long, tipo tiponotificacion, mensaje string, canal, canalnotificacion, leida boolean, fecha datatime

    relaciones:
    local tiene 3 relaciones:
    nota:en parentesis es el nombre de la relacion, no de la clase
    a-(ofrece, relacion *del lado del local, * del lado del plato) 
    b-(recibe,1 del lado del local, * del lado del pedido) 
    c-(recibe-emite, * en local y * en calificacion)

    cliente tiene 2 relaciones:

    a-(realiza*del lado de pedido,1 del lado del cliente)
    b-(recibe/emite * del lado del cliente y * del lado de calificacion)

    Pedido tiene 3 relaciones:
    a-(contiene, 1 del lado del pedido y 1..* del lado de detallepedido)
    b-(emite, 1 del lado de pedido y 1 del lado de factura)
    c-(genera, 1 del lado de pedido y 0..* del lado del reclamo)
    adicionalmente tiene una relacion de linea punteada hacia notificacion (genera, 1 del lado de pedido, 1 del lado de notificacion)

    plato tiene 2 relaciones:
    a-(tiene, 1 del lado de plato, * del lado de promocion)
    b-(referencia, 1 del lado de plato, * del lado de detallepedido)

    reclamo tiene una relacion de linea punteada(genera, 1 del lado de reclamo, 1 del lado de notificacion)

    datatypes:
    a-direccion: calle string, numero string, ciudad string, codigopostal string
    
    enums:
    a-Estadocuenta: ACTIVO, BLOEQUEADO
    b-EstadoReclamo: PENDIENTE, EN PROCESO, SOLUCIONADO
    c-EstadoLocal: PENDIENTE, HABILITADO, RECHAZADO, BLOQUEADO
    d-CanalNotificacion: MAIL, WEB, PUSH_MOBILE
    e-EstadoPedido: PENDIENTE, CONFIRMADO, RECHAZADO, CANCELADO
    f-TipoCalificacion: CLIENTE_A_LOCAL, LOCAL_A_CLIENTE
    g-TipoNotificacion: PEDIDO, RECLAMO

    1.2 Descripción del Modelo de Dominio................................................................................
    El diagrama del modelo de dominio representa un sistema de gestión de pedidos de
    comida a domicilio, orientado al registro de locales gastronómicos, la realización de
    pedidos por parte de clientes, la gestión de reclamos, calificaciones y el envío de
    notificaciones multicanal.
    En la parte central superior se encuentra la entidad Usuario , que modela a todas las
    personas que interactúan con el sistema. Incluye atributos como correo electrónico,
    contraseña, foto de perfil y estado de la cuenta (activo o bloqueado). De esta entidad
    heredan tres perfiles diferenciados: Administrador , Local y Cliente .
    El Administrador es el perfil encargado de la gestión general del sistema. Puede
    aprobar o rechazar solicitudes de registro de nuevas locales, bloquear y desbloquear
    cuentas de usuarios y listar tanto locales como clientes registrados.
    El Local representa al negocio de comida registrado en el sistema. Posee atributos
    como nombre, dirección física, descripción del tipo de comida ofrecida, estado
    (pendiente de aprobación, habilitado o bloqueado), una calificación global calculada
    en base a las valoraciones de los clientes, y un indicador de si el local se encuentra
    abierto o cerrado para recibir pedidos en el día. Cada local gestiona su propio
    catálogo de Platos , que incluye nombre, descripción, precio, imágenes y
    disponibilidad. Sobre un plato pueden definirse Promociones con descuento por un
    período de tiempo determinado.
    El Cliente es el perfil que realiza pedidos a los locales. Contiene datos personales
    como documento de identidad, nombre, apellido, domicilio y foto de perfil, además
    de una calificación global asignada por los locales con quienes interactuó.
    El Pedido es la entidad central de la operación. Vincula a un cliente con un local, y
    registra la fecha, el estado (pendiente, confirmado, rechazado o cancelado), el
    domicilio de entrega, el tiempo estimado de entrega, el total a pagar y la simulación
    del medio de pago electrónico utilizado. Cada pedido se descompone en uno o más
    DetallePedido , que representa cada ítem seleccionado indicando el plato, la
    cantidad de unidades, el precio unitario al momento del pedido y el subtotal
    correspondiente. Esta separación permite preservar la información del pedido
    aunque el precio del plato cambie posteriormente.
    Al ser confirmado un pedido, el sistema emite automáticamente una factura en
    formato PDF, que contiene el número de comprobante, el monto total y el enlace al
    archivo generado.
Si un cliente queda insatisfecho con un pedido confirmado o entregado, puede abrir un Reclamo ,
    especificando el motivo, el tipo de compensación solicitada (reintegro u otra) y el
    monto a devolver. El reclamo atravesando estados propios: pendiente, en proceso y
    solucionado.

    Tanto el cliente como el local pueden calificarse mutuamente a través de la entidad
    Calificacion , que registra el puntaje (del 1 al 5), un comentario opcional, la fecha y
    el tipo de calificación (cliente a local, o local a cliente). La restricción principal es que
    solo puede calificarse si existió al menos un pedido entre ambas partes.
    Finalmente, ante eventos relevantes del sistema (confirmación o rechazo de un
    pedido, resolución de un reclamo, entre otros), se generan Notificaciones
    asociadas al pedido correspondiente. Cada notificación indica su tipo, el mensaje a
    comunicar, el canal utilizado (correo, interfaz web o push mobile) y si ya fue leída por el
    destinatario.
    Como objeto de valor transversal, Dirección es reutilizada en Múltiples contextos: la
    dirección física del local, el domicilio del cliente y el domicilio de entrega de cada
    pedido. Al ser un objeto de valor, no posee identidad propia y su existencia depende
    de la entidad que la contiene.
    Este modelo busca garantizar una gestión ordenada de los actores del sistema, el
    flujo completo de un pedido desde su realización hasta su facturación, y los
    mecanismos de comunicación, reclamo y evaluación que aseguran la calidad del
    servicio entre clientes y locales.

## 2. Restricciones................................................................................................................
    2.1. Restricciones de unicidad................................................................................................
    ● No hay dos usuarios con el mismo correo electrónico.
    ● No hay dos usuarios con el mismo id.
    ● No hay dos clientes con el mismo documento de identidad.
    ● No hay dos locales con el mismo id.
    ● No hay dos platos con el mismo id dentro de un mismo local.
    ● No hay dos promociones con el mismo id.
    ● No hay dos pedidos con el mismo id.
    ● No hay dos detalles de pedido con el mismo id dentro de un mismo pedido.
    ● No hay dos reclamos con el mismo id.
    ● No hay dos calificaciones con el mismo id.
    ● No hay dos facturas con el mismo id ni con el mismo número de comprobante.
    ● No hay dos notificaciones con el mismo id.
    2.2. Restricciones circulares................................................................................................
    ● Un reclamo debe estar asociado a un pedido que haya sido previamente
confirmado o entregado y que pertenezca al cliente que realiza el reclamo.
    ● Una calificación de un cliente a un local solo puede existir si ese cliente
    realizó al menos un pedido a ese local.
    ● Una calificación de un local a un cliente solo puede existir si ese cliente
    realizó al menos un pedido a ese local.
    ● Un DetallePedido debe referenciar un plato que pertenezca al local al que se
    realizó el pedido.
    ● Una Factura debe estar asociada a un pedido que pertenezca al cliente al que
    se le emite.
    2.3. Restricciones obligatorias................................................................................................
    ● Un Usuario debe tener email, contraseña, foto de perfil y estado de cuenta.
    ● Un Cliente debe tener documento de identidad, nombre, apellido y domicilio.
    ● Un Local debe tener nombre, dirección física, descripción del tipo de comida
    ofrecida y al menos una imagen, además de un estado de aprobación.
    ● Un Plato debe tener nombre, descripción, precio y estar asociado a un local.
    ● Una Promocion debe tener descuento, fecha de inicio, fecha de fin y estar
    asociada a un plato.
    ● Un Pedido debe tener fecha, estado, domicilio de entrega, total y estar
    asociado a un cliente y a un local.
    ● Un DetallePedido debe tener cantidad, precio unitario y subtotal, y estar
    asociado a un pedido y a un plato.
    ● Una Factura debe tener número de comprobante, monto y archivo PDF, y
    estar asociada a un pedido.
    ● Un Reclamo debe tener motivo, tipo de compensación solicitada, estado y
    fecha, y estar asociado a un pedido.
    ● Una Calificacion debe tener puntaje, fecha y tipo, y estar asociada a un cliente
    y a un local.
    ● Una Notificacion debe tener tipo, mensaje, canal y fecha, y estar asociada al
    usuario destinatario.
    2.4. Restricciones de validación................................................................................................
    ● El correo electrónico de un usuario debe tener un formato válido (ejemplo:
    usuario@dominio.com).
    ● La contraseña debe cumplir con un mínimo de seguridad definido (longitud
    mínima e inclusión de caracteres especiales o numéricos).
    ● El documento de identidad de un cliente debe cumplir con el formato válido de
    cédula uruguaya.
    ● El puntaje de una Calificacion debe estar comprendido entre 1 y 5 inclusive.
    ● El descuento de una Promocion debe ser un valor mayor a 0 y menor a 100.
    ● La fecha de fin de una Promocion no puede ser anterior a su fecha de inicio.
    ● El total de un Pedido debe ser igual a la suma de los subtotales de sus
    DetallePedido, aplicando los descuentos de promociones vigentes si
    corresponde.
    ● El subtotal de un DetallePedido debe ser igual al precio unitario multiplicado
    por la cantidad.
    ● El monto de una Factura debe coincidir con el total del pedido al que
    pertenece.
    2.5. Restricciones de dominio y atributos................................................................................
    ● Un pedido solo puede realizarse a un local cuyo estado sea HABILITADO y
    cuyo atributo estaAbierto sea verdadero.
    ● Un local con estado BLOQUEADO no puede recibir nuevos pedidos ni
    registrar apertura del día.
    ● Un usuario con estado BLOQUEADO no puede iniciar sesión ni operar en el
    sistema.
    ● Un local con estado PENDIENTE no puede recibir pedidos hasta ser
    aprobado por el administrador.
    ● El atributo calificacionGlobal de un Local se calcula como el promedio de
    todas las calificaciones de tipo CLIENTE_A_LOCAL recibidas.
    ● El atributo calificacionGlobal de un Cliente se calcula como el promedio de
    todas las calificaciones de tipo LOCAL_A_CLIENTE recibidas.
    ● Un Reclamo solo puede abrirse sobre un pedido cuyo estado sea
    CONFIRMADO.
    2.6. Restricciones de uso.................................................................................................
    ● Un cliente no puede realizar un pedido a un local que se encuentre cerrado
    (estaAbierto = false).
    ● Un pedido solo puede cancelarse si su estado es PENDIENTE; no puede
    cancelarse si ya fue CONFIRMADO o RECHAZADO.
    ● Un cliente no puede calificarse a sí mismo, ni un local puede calificarse a sí
    mismo.
    ● No puede existir más de una calificación del mismo tipo entre el mismo par
    cliente-local (un cliente solo puede calificar una vez a un mismo local, y
    viceversa).
    ● Una Factura solo se genera cuando el pedido pasa al estado CONFIRMADO;
    no existe factura para pedidos rechazados o cancelados.
    ● La eliminación de la cuenta de un Cliente debe dejar información consistente
    en el sistema: sus pedidos, reclamos y calificaciones previas deben
    conservarse o anonimizarse, pero no eliminarse.
    ● Un local no puede registrar apertura del día si su estado no es HABILITADO.
    ● Una notificación debe enviarse por todos los canales definidos (correo, web y
    push mobile) ante los eventos que así lo requieran: confirmación de pedido,
    rechazo de pedido y resolución de reclamo.
