Casos de uso críticos que SÍ están realizados
Estos son los únicos críticos que hoy puedo considerar realizados de verdad:

1) CU-L01 — Solicitar Registro como Local Habilitado
Estado: Implementado
API: POST /api/v1/locales/solicitudes-habilitacion
{
  "email": "local@foodly.com",
  "passwd": "Clave123",
  "nombre": "La Cocina",
  "direccion": {
    "calle": "Av. Italia",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11600"
  },
  "descripcion": "Comida casera",
  "imagenes": ["fachada.jpg", "producto.png"]
}
2) CU-L02 — Gestionar Platos de Comida
Estado: Implementado
Alta
API: POST /api/v1/locales/platos
{
  "nombre": "Milanesa al pan",
  "descripcion": "Milanesa con lechuga y tomate",
  "precio": 350,
  "imagenes": ["milanesa.jpg"],
  "disponible": true,
  "dtLocal": {
    "id": 1
  }
}
Modificación
API: PUT /api/v1/locales/platos/{idPlato}
{
  "nombre": "Milanesa completa",
  "descripcion": "Milanesa con fritas",
  "precio": 420,
  "imagenes": ["milanesa2.jpg"],
  "disponible": true,
  "dtLocal": {
    "id": 1
  }
}
Baja
API: DELETE /api/v1/locales/platos/{idPlato}
Lista completa, pero SOLO de casos de uso críticos
CU crítico	Estado	API
CU-C01 Iniciar sesión	Parcial	POST /auth/login
CU-A02 Aprobar/Rechazar solicitud de local	Parcial	POST /api/v1/admins
CU-L01 Solicitar registro como local habilitado	Implementado	POST /api/v1/locales/solicitudes-habilitacion
CU-L02 Gestionar platos	Implementado	POST/PUT/DELETE /api/v1/locales/platos...
CU-L04 Registrar apertura del local	Parcial	PUT /api/v1/locales/{idLocal}/apertura
CU-L05 Registrar cierre del local	Parcial	PUT /api/v1/locales/{idLocal}/cierre
CU-L06 Buscar y listar pedidos recibidos	Parcial	GET /api/v1/pedidos/locales/{idLocal}
CU-L07 Confirmar pedido	Parcial	POST /api/v1/pedidos/{idPedido}/confirmar
CU-CL01 Crear cuenta de usuario	Parcial	POST /api/v1/clientes + GET /api/v1/usuarios/activar?email=...
CU-CL05 Buscar y listar platos y promociones	No implementado	existe GET /api/v1/clientes/{filtro}, pero service devuelve null
CU-CL06 Realizar pedido	Parcial	POST /api/v1/pedidos
Por qué los demás críticos NO los conté como realizados
CU-C01 — Iniciar sesión
Existe API, pero NO alcanza:

no veo validación clara de cuenta bloqueada
no resuelve el flujo por rol del caso de uso
sigue desalineado con el registro de contraseña
CU-A02 — Aprobar o Rechazar Solicitud de Registro de Local
El service soporta aprobar y rechazar, pero el controller actual solo aprueba.
Eso significa que el caso crítico no está cerrado.

CU-L04 / CU-L05
Cambian el estado abierto/cerrado, sí.
Pero faltan alternativos importantes del caso de uso:

“ya está abierto”
cierre con pedidos pendientes
CU-L06
Lista pedidos por local, pero sin filtros/ordenamientos del caso crítico.

CU-L07
Confirma el pedido, pero le faltan piezas esenciales:

ingreso correcto del tiempo estimado desde API
pago simulado del flujo
PDF
envío de factura
notificaciones
CU-CL01
El registro estándar existe, pero el flujo completo todavía no está sólido:

Google sigue sin implementar
activación/integración auth todavía inconsistente
CU-CL05
No implementado. El service sigue devolviendo null.

CU-CL06
Crea pedido pendiente si el local está abierto, pero faltan:

detalle de platos
validación de cantidades
validación de “al menos un plato”
notificación al local
Conclusión
Si me obligas a responder con rigor técnico:

Casos de uso críticos realizados hoy:

CU-L01
CU-L02
Todos los demás críticos están parciales o no implementados.