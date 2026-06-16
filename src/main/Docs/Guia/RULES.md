# RULES.md - Guía de implementación de Foodly


Este archivo define cómo se debe implementar Foodly en este repositorio. La prioridad es que el código refleje los documentos del proyecto, no preferencias personales ni soluciones inventadas.


## 1. Rol del asistente/desarrollador


Actuar como desarrollador de software senior con 15+ años de experiencia:


- Verificar antes de afirmar. Si una regla, flujo o nombre no está claro, revisar documentos/código antes de responder.
- Corregir decisiones técnicas débiles con fundamento. CONCEPTOS > CÓDIGO.
- No aceptar atajos que rompan arquitectura, dominio, seguridad o trazabilidad.
- Proponer alternativas con tradeoffs cuando una decisión tenga impacto real.
- Si falta información o hay contradicción entre documentos, detenerse y preguntar antes de implementar.


## 2. Fuentes oficiales del proyecto


Documentos revisados para redactar estas reglas:


1. /Users/roibethgarcia/Downloads/Documento de Casos de Uso.docx (1).pdf
2. /Users/roibethgarcia/Downloads/Documento de Modelo de dominio.pdf
3. /Users/roibethgarcia/Downloads/Documento de Arquitectura (1).pdf
4. /Users/roibethgarcia/Downloads/Documento de diseño.pdf
5. /Users/roibethgarcia/Downloads/Documento de Alcance (1).pdf


### Prioridad cuando se implemente


1. *Casos de uso*: mandan sobre flujo, precondiciones, validaciones funcionales, mensajes, postcondiciones y comportamiento esperado.
2. *Modelo de dominio*: manda sobre nombres de entidades, atributos, enumeraciones, datatypes y restricciones del dominio.
3. *Arquitectura*: manda sobre capas, módulos, tecnología y responsabilidades.
4. *Diseño*: guía servicios, controladores, diagramas de secuencia e interacción entre componentes.
5. *Alcance*: define qué entra y qué queda fuera de esta versión.


Si dos documentos se contradicen, NO resolver silenciosamente. Documentar la contradicción y pedir decisión.


## 3. Idioma y nombres del código


### Regla principal


Todo nombre de función o método de negocio debe estar en español y representar explícitamente el caso de uso que implementa.


Ejemplos correctos:


- iniciarSesion
- cerrarSesion
- cambiarContrasena
- recuperarContrasenaPorCorreoElectronico
- buscarYListarUsuariosRegistrados
- aprobarSolicitudRegistroLocal
- rechazarSolicitudRegistroLocal
- solicitarRegistroComoLocalHabilitado
- registrarAperturaDelLocal
- registrarCierreDelLocal
- buscarYListarPedidosRecibidos
- confirmarPedidoDeCliente
- rechazarPedidoDeCliente
- realizarPedido
- cancelarPedido
- realizarReclamo
- calificarLocal
- calificarCliente


### Convenciones Java


- Usar camelCase para métodos y atributos.
- Usar PascalCase para clases, entidades, DTOs y enums.
- No usar tildes ni ñ en identificadores Java: usar contrasena, promocion, calificacion.
- No traducir nombres definidos por el modelo de dominio sin aprobación.
- Se permiten sufijos técnicos estándar de Spring/Java cuando correspondan: Controller, Service, Repository, DTO, Mapper, Exception.
- Los endpoints REST deben usar recursos claros y preferentemente en español: /api/usuarios, /api/locales, /api/pedidos, /api/reclamos.


## 4. Modelo de dominio obligatorio


Las entidades, datatypes y enumeraciones siguientes vienen del Documento de Modelo de dominio. Implementarlas con estos nombres salvo decisión explícita del equipo.


### Entidades y atributos


#### Usuario


- id: Long
- email: String
- passwd/password/contrasena: String - el diagrama muestra passwd/password; si se normaliza a español, usar contrasena y documentar la decisión.
- foto: String
- estado: EstadoCuenta
- tipo: String


#### Administrador extends Usuario


- nivelAcceso: String


#### Local extends Usuario


- nombre: String
- direccion: Direccion
- descripcion: String
- estado: EstadoLocal
- calificacionGlobal: Integer
- estaAbierto: Boolean
- imagenes: List<String>


#### Cliente extends Usuario


- documento: String
- nombre: String
- apellido: String
- domicilio: Direccion
- calificacionGlobal: Double
- activo: Boolean


#### Plato


- id: Long
- nombre: String
- descripcion: String
- precio: Double
- imagenes: List<String>
- disponible: Boolean


Nota: el caso de uso CU-L02 menciona categoria, pero el modelo de dominio visible no la incluye. No agregarla al dominio sin confirmación.


#### Promocion


- id: Long
- descuento: Double
- fechaInicio: Date
- fechaFin: Date
- descripcion: String


#### Pedido


- id: Long
- fecha: DateTime
- estado: EstadoPedido
- tiempoEstEntrega: time
- total: Double
- domicilioEntrega: Direccion
- medioPago: String
- pagoSimulado: Boolean


#### DetallePedido


- id: Long
- cantidad: Integer
- precioUnitario: Double
- subtotal: Double


#### Factura


- id: Long
- numero: String
- monto: Double
- archivoPDF: String


#### Reclamo


- id: Long
- motivo: String
- tipoCompensacion: String
- estado: EstadoReclamo
- montoReintegro: Double
- fecha: DateTime


#### Calificacion


- id: Long
- puntaje: Integer entre 1 y 5
- comentario: String
- fecha: DateTime
- tipo: TipoCalificacion


#### Notificacion


- id: Long
- tipo: TipoNotificacion
- mensaje: String
- canal: CanalNotificacion
- leida: Boolean
- fecha: DateTime


#### Direccion datatype


- calle: String
- numero: String
- ciudad: String
- codigoPostal: String


### Enumeraciones


#### EstadoCuenta


- ACTIVO
- BLOQUEADO


#### EstadoLocal


- PENDIENTE
- HABILITADO
- BLOQUEADO
-RECHAZADO


#### EstadoPedido


- PENDIENTE
- CONFIRMADO
- RECHAZADO
- CANCELADO


#### EstadoReclamo


- PENDIENTE
- EN_PROCESO
- SOLUCIONADO


#### CanalNotificacion


- MAIL
- WEB
- PUSH_MOBILE


#### TipoCalificacion


- CLIENTE_A_LOCAL
- LOCAL_A_CLIENTE


#### TipoNotificacion


- PEDIDO
- RECLAMO


## 5. Restricciones de dominio que NO se pueden omitir


- No puede haber dos usuarios con el mismo email.
- No puede haber dos clientes con el mismo documento.
- No puede haber dos facturas con el mismo id ni con el mismo numero.
- Un usuario debe tener email, contraseña, foto de perfil y estado de cuenta.
- Un cliente debe tener documento, nombre, apellido y domicilio.
- Un local debe tener nombre, dirección física, descripción, al menos una imagen y estado de aprobación.
- Un plato debe tener nombre, descripción, precio y estar asociado a un local.
- Una promoción debe tener descuento, fecha de inicio, fecha de fin y estar asociada a un plato.
- Un pedido debe tener fecha, estado, domicilio de entrega, total, cliente y local.
- Un detalle de pedido debe referenciar un plato que pertenezca al local del pedido.
- Una factura debe estar asociada a un pedido del cliente al que se emite.
- Un reclamo solo puede existir para un pedido confirmado y del cliente que reclama.
- Solo se puede calificar si existió al menos un pedido entre cliente y local.
- El puntaje de una calificación debe estar entre 1 y 5 inclusive.
- La fecha de fin de una promoción no puede ser anterior a su fecha de inicio.
- El documento de identidad del cliente debe respetar formato válido de cédula uruguaya.




## 8. Arquitectura obligatoria


El Documento de Arquitectura define Foodly como una arquitectura multicapa/3-tier:


1. *Capa de presentación Web*: React para Administrador, Local y Cliente.
2. *Capa de presentación Mobile*: Flutter Android para Cliente.
3. *Capa de negocio/servicios*: backend Spring Boot, API centralizada y reglas de negocio.
4. *Capa de datos*: PostgreSQL.
5. *Servicios externos desacoplados*: correo electrónico, push mobile, generación de PDF y pagos simulados.


### Backend Spring Boot


El backend debe centralizar reglas críticas:


- Autenticación y autorización por roles.
- Aprobación de locales.
- Gestión de pedidos.
- Reclamos.
- Calificaciones.
- Notificaciones.
- Facturación PDF.


No mover reglas de negocio críticas al frontend o mobile.


### Módulos lógicos esperados


Organizar por dominio/feature, no por carpetas gigantes puramente técnicas:


- autenticacion
- usuarios
- administracion
- locales
- catalogo
- pedidos
- reclamos
- calificaciones
- notificaciones
- facturacion
- persistencia o infraestructura equivalente


Dentro de cada módulo se pueden usar subcapas como api, aplicacion, dominio, infraestructura, siempre que no contradigan la arquitectura multicapa del documento.


## 9. Reglas Spring Boot


- Usar inyección por constructor.
- Declarar dependencias como private final.
- No exponer entidades JPA directamente desde controladores: usar DTOs.
- Validar requests con Bean Validation (@Valid, @NotNull, @Size, etc.).
- Mantener lógica de negocio en servicios, no en controladores.
- Usar @Transactional en métodos de servicio que modifican estado.
- Usar repositorios Spring Data JPA para persistencia.
- Implementar manejo global de errores con @ControllerAdvice.
- No hardcodear secretos, claves JWT, credenciales de correo ni URLs sensibles.
- Usar logs parametrizados; nunca loguear contraseñas, tokens ni datos sensibles completos.

### Convenciones para DTOs / DataTypes

- Request: entra desde controller.
- Response: sale hacia API.
- Summary: representa vistas resumidas o listados.
- Shared: piezas reutilizables, pequeñas y neutras.
- No usar DTOs genéricos si representan más de un caso de uso.


## 10. Seguridad y autenticación


La arquitectura menciona JWT. Por lo tanto:


- Validar tokens siempre del lado servidor.
- No aceptar tokens expirados ni algoritmos inseguros como none.
- No guardar información sensible en el payload del JWT.
- Usar expiración corta para access tokens.
- Si se usan refresh tokens, rotarlos y permitir revocación.
- Al bloquear un usuario, invalidar sus sesiones/tokens activos según CU-A03.
- Al cerrar sesión, invalidar el token/sesión activo según CU-C02.
- Las contraseñas deben almacenarse con BCrypt o mecanismo fuerte equivalente, nunca en texto plano.
- Para cambio de contraseña, respetar CU-C03: código 2FA numérico de 6 dígitos, expiración de 10 minutos, máximo 3 intentos y bloqueo temporal de 15 minutos.
- Para recuperación de contraseña, respetar CU-C04: enlace con expiración de 30 minutos y mensaje genérico si el correo no existe.


## 11. Reglas específicas de pedidos


- Un cliente solo puede realizar pedidos a locales en estado abierto.
- Al confirmar el pedido, volver a validar que el local siga abierto.
- Un pedido nuevo se registra en estado PENDIENTE.
- El cliente puede cancelar solo pedidos PENDIENTE.
- El local puede confirmar o rechazar solo pedidos PENDIENTE.
- Al confirmar un pedido, registrar tiempo estimado de entrega, generar factura PDF y notificar al cliente.
- Al rechazar o cancelar un pedido, notificar al actor correspondiente por los canales definidos.
- DetallePedido.precioUnitario debe guardar el precio al momento del pedido para preservar histórico aunque cambie el precio del plato.


## 12. Notificaciones y facturación


- Canales documentados: MAIL, WEB, PUSH_MOBILE.
- No implementar SMS ni WhatsApp: están fuera de alcance.
- La factura debe ser PDF y enviarse por correo al cliente cuando el pedido sea confirmado.
- No integrar facturación real externa: está fuera de alcance. Implementar generación/simulación local según documentos.


## 13. Fuera de alcance explícito


No implementar en esta versión salvo orden explícita y actualización del alcance:


- Perfil de repartidor.
- Seguimiento en tiempo real sobre mapa.
- Aplicación iOS.
- Chat en tiempo real.
- Geolocalización avanzada o asignación automática de reparto.
- Programa de fidelización, puntos o cupones complejos.
- Soporte multilenguaje.
- BI/panel analítico avanzado.
- SMS o WhatsApp.
- Integración real con sistemas externos de facturación.


## 14. Protocolo antes de escribir código


Antes de implementar cualquier funcionalidad:


1. Identificar el caso de uso exacto (CU-*).
2. Leer el flujo principal, alternativos, precondiciones y postcondiciones.
3. Identificar entidades, atributos y enums afectados en el modelo de dominio.
4. Revisar si el caso toca seguridad, notificaciones, facturación, pedidos o estados.
5. Definir nombres de métodos en español alineados al caso de uso.
6. Implementar primero el comportamiento mínimo documentado. No inventar funcionalidades.
7. Agregar pruebas del caso de uso y de los flujos alternativos relevantes.
8. No ejecutar build. Si se necesita verificar, usar pruebas específicas, no empaquetado/build completo.


## 15. Definición de terminado


Una tarea solo se considera terminada si:


- El código implementa el caso de uso documentado.
- Se respetan precondiciones, flujos alternativos y postcondiciones.
- Se usan nombres de dominio correctos.
- No se agregan funcionalidades fuera de alcance.
- Hay validaciones de dominio donde corresponde.
- Hay control de autorización por rol donde corresponde.
- Los errores devuelven mensajes consistentes con el caso de uso cuando estén definidos.
- No se expone información sensible.
- No se ejecutó build.


## 16. Commits y colaboración


- Usar conventional commits.
- Nunca agregar Co-Authored-By ni atribución a IA.
- No hacer commits sin instrucción explícita del usuario.
- No hacer cambios destructivos sin confirmación.


## 17. Contradicciones detectadas que requieren decisión del equipo


Estas inconsistencias ya fueron observadas y deben resolverse antes de implementar esas partes:


1. CU-CL01 aparece duplicado en el índice de cliente.
2. CU-CL03 aparece como Face ID y también como eliminar cuenta; además el detalle de Face ID dice CU-M01.
3. El alcance excluye iOS, pero el caso de Face ID menciona Face ID en iOS o equivalente Android.
4. Plato.categoria aparece en CU-L02/CU-CL05, pero no en el modelo de dominio visible.
5. El modelo indica descuento de promoción mayor a 0 y menor a 100; el caso de uso habla de rango 1% a 100%.
6. El modelo usa Double para dinero (precio, total, monto). Técnicamente BigDecimal sería más correcto para dinero, pero cambiarlo rompe la literalidad del modelo y requiere aprobación.
7. El diagrama muestra passwd/password en Usuario; el equipo debe decidir si el atributo Java final será contrasena por coherencia idiomática o si se mantiene el nombre del diagrama.
