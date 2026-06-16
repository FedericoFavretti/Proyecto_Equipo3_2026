# Diagnóstico y arreglos realizados en `POST /api/v1/pedidos`

## Contexto

Durante las pruebas del endpoint:

```http
POST /api/v1/pedidos
```

aparecieron varios errores `500` que, al principio, parecían estar relacionados con mappers o persistencia.

Después de revisar código, logs y datos de base, se confirmó que hubo **más de una causa** en momentos distintos.

---

## Síntomas observados

En distintos intentos aparecieron errores como:

- `El plato seleccionado no está disponible`
- `El plato seleccionado no pertenece al local indicado`
- `duplicate key value violates unique constraint "pedido_pkey"`
- `duplicate key value violates unique constraint "detallepedido_pkey"`

Esto obligó a separar el análisis en dos partes:

1. errores de **regla de negocio**
2. errores de **creación del pedido**

---

## Problema real que sí corregimos en código

## 1. El alta del pedido estaba copiando datos que no debían venir del request

En la creación del `Pedido`, originalmente se tomaban desde `DtPedido` campos que no deberían ser responsabilidad del cliente:

- `id`
- `fecha`
- `estado`

Eso era incorrecto para un caso de uso de alta.

### Por qué estaba mal

Porque en un:

```http
POST /api/v1/pedidos
```

el cliente describe **qué quiere pedir**, pero no debería definir:

- la identidad persistente del pedido
- la fecha oficial de creación
- el estado inicial interno del sistema

Si esos valores se copian desde el request, se mezcla un DTO de entrada con una entidad ya persistida.

---

## Arreglo aplicado

Se ajustó `PedidoService` para que, al construir un pedido nuevo:

- **no copie** `dtPedido.getId()`
- genere `fecha` en backend con `new Date()`
- genere `estado` en backend como `EstadoPedido.Pendiente`

### Archivo modificado

`src/main/java/com/example/demo/Logica/Service/PedidoService.java`

### Cambio conceptual

Antes, el alta dependía de información enviada por el cliente que no correspondía al contrato de creación.

Ahora, el backend:

- crea el `Pedido` como entidad nueva
- asigna su estado inicial
- asigna su fecha oficial
- deja que la persistencia genere el `id`

---

## Por qué este arreglo era necesario

Porque el flujo correcto de creación debe ser:

1. validar local y cliente
2. validar platos
3. recalcular subtotales y total
4. construir el pedido en backend
5. persistir pedido y detalles

NO debe ser:

1. recibir un objeto casi persistido desde el cliente
2. confiar en su `id`, `fecha` y `estado`
3. insertarlo como si fuera válido

---

## Qué verificamos además

## 2. `PedidoRepositorioImpl` no inserta `id` manualmente

Se revisó:

`src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

y se confirmó que el `INSERT INTO pedido`:

- no incluye la columna `id`
- deja que PostgreSQL la genere
- recupera luego el valor con `KeyHolder`

### Conclusión

El repositorio no era la fuente directa del problema de diseño del alta.

---

## 3. `DetallePedidoRepositorioImpl` tampoco inserta `id` manualmente

Se revisó:

`src/main/java/com/example/demo/Persistencia/Implementaciones/DetallePedidoRepositorioImpl.java`

y también se confirmó que:

- no inserta `id`
- solo persiste `cantidad`, `precioUnitario`, `subtotal`, `idPlato` e `idPedido`

### Conclusión

Tampoco era el origen directo del problema estructural del alta.

---

## 4. Las secuencias de PostgreSQL quedaron verificadas

Se revisaron:

- `pedido_id_seq`
- `detallepedido_id_seq`

y se confirmó que:

- ambas columnas `id` usan `nextval(...)`
- las secuencias existen
- no había evidencia actual de desalineación en el estado revisado de la base

### Conclusión

La hipótesis de “secuencia rota” perdió fuerza con la evidencia de base revisada.

---

## Error funcional que apareció después

Una vez corregido el problema de creación, los logs mostraron un error distinto:

```text
El plato seleccionado no pertenece al local indicado.
```

Eso NO era un problema de persistencia ni de mapper.

Era una validación correcta de negocio en `PedidoService`:

- el `dtPlato.id` enviado
- no pertenecía al `dtLocal.id` enviado

Cuando se corrigió esa combinación de datos en la prueba, el endpoint respondió correctamente.

---

## Resultado final verificado

La API terminó respondiendo `200 OK` con un body como este:

- `id` generado correctamente
- `fecha` generada en backend
- `estado = Pendiente`
- `total` recalculado correctamente
- `local` y `cliente` resueltos desde repositorio

Eso confirma que el flujo del alta quedó funcional para este caso.

---

## Observación pendiente

Aunque el endpoint ya responde `200`, todavía hay una mejora importante por hacer:

### Falta manejo global de excepciones HTTP

Hoy varias validaciones de negocio salen como `500`, por ejemplo:

- plato no disponible
- plato que no pertenece al local
- local cerrado
- cantidades inválidas

Eso debería mapearse a respuestas 4xx, no 500.

Ejemplos:

- `400 Bad Request`
- `404 Not Found`
- `409 Conflict`

---

## Resumen ejecutivo

El arreglo principal consistió en corregir la creación del pedido para que el backend deje de depender de `id`, `fecha` y `estado` enviados desde `DtPedido`.

Eso era importante porque un alta no debe recibir una entidad casi persistida desde el cliente, sino construir un agregado nuevo y consistente desde el service.

Además, se verificó que:

- `PedidoRepositorioImpl` no inserta `id` manualmente
- `DetallePedidoRepositorioImpl` no inserta `id` manualmente
- las secuencias de base, en el estado revisado, no eran la causa principal

Finalmente, el último error real fue de datos de negocio: el plato usado en la prueba no pertenecía al local enviado. Corregido eso, la API respondió `200`.

