# CU-CL06 — Cambios realizados para poder testear `Realizar Pedido`

## Objetivo

Documentar los problemas encontrados al testear `CU-CL06` (`POST /api/v1/pedidos`), los cambios aplicados y la razón técnica de cada uno.

---

## Request usada para probar

```json
{
  "domicilioEntrega": {
    "calle": "Av. Italia",
    "numero": "1234",
    "ciudad": "Montevideo",
    "codigoPostal": "11600"
  },
  "medioDePago": "Efectivo",
  "pagoSimulado": false,
  "dtLocal": {
    "id": 6
  },
  "dtCliente": {
    "id": 4,
    "activo": false
  },
  "detalles": [
    {
      "cantidad": 2,
      "dtPlato": {
        "id": 2
      }
    }
  ]
}
```

---

## Problemas detectados y cambios aplicados

### 1. Error 400 por deserialización de primitivos

#### Síntoma

Spring devolvía errores como:

- `Cannot map null into type boolean`
- `Cannot map null into type long`

#### Causa

Los DTOs de entrada usaban primitivos (`boolean`, `long`, `int`) y el parser JSON fallaba cuando algún campo llegaba como `null` o incompleto.

#### Cambios aplicados

Se cambiaron tipos primitivos por wrappers en DTOs de entrada:

- `DtPedido.pagoSimulado`: `boolean` -> `Boolean`
- `DtCliente.activo`: `boolean` -> `Boolean`
- `DtDetallePedido.id`: `long` -> `Long`
- `DtDetallePedido.cantidad`: `int` -> `Integer`

#### Razón

En DTOs de entrada HTTP conviene aceptar `null` durante la deserialización y validar después. Si se usan primitivos, el parseo rompe antes de llegar a la lógica del caso de uso.

---

### 2. Error 500 por `Cliente` sin `id`

#### Síntoma

El flujo fallaba al guardar el pedido con:

- `pedido.getCliente().getId()` en `null`

#### Causa

`ClienteRepositorioImpl.buscarPorId(...)` y `listarTodos()` construían objetos `Cliente` sin asignar el `id` heredado de `Usuario`.

#### Cambio aplicado

Se creó un mapper interno en:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/ClienteRepositorioImpl.java`

Ese mapper ahora:

- construye el `Cliente`
- asigna `cliente.setId(rs.getLong("id"))`

#### Razón

El repositorio estaba devolviendo un objeto de dominio incompleto. El pedido necesita `idCliente` para persistirse correctamente.

---

### 3. Error 500 por columnas SQL inexistentes en `pedido`

#### Síntoma

PostgreSQL devolvía errores como:

- `column "mediodepago" does not exist`
- `column "mp_preferencia_id" does not exist`

#### Causa

`PedidoRepositorioImpl` usaba nombres SQL desalineados con el esquema real de la tabla `pedido`.

También intentaba persistir columnas de Mercado Pago que no existen en la tabla actual:

- `mp_preferencia_id`
- `mp_init_point`

#### Cambios aplicados

En:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

se hicieron estos cambios:

1. Se eliminaron del `INSERT`, `UPDATE` y mapeo las columnas inexistentes de Mercado Pago.
2. `actualizarDatosMp(...)` pasó a lanzar `UnsupportedOperationException` para dejar explícito que el esquema actual no soporta esos campos.
3. Se alinearon todos los nombres SQL con la tabla real:
   - `tiempoestentrega`
   - `codigopostal`
   - `mediopago`
   - `pagosimulado`
   - `idlocal`
   - `idcliente`

#### Razón

El código JDBC debe reflejar EXACTAMENTE el esquema real de PostgreSQL. Inventar columnas o depender de nombres camelCase deja el repositorio frágil y rompe en runtime.

---

### 4. Error 500 por incompatibilidad entre `Duration` y columna `TIME`

#### Síntoma

PostgreSQL devolvía:

- `column "tiempoestentrega" is of type time without time zone but expression is of type bigint`

#### Causa

El modelo Java usa:

- `Duration tiempoEstEntrega`

pero la base usa:

- `time without time zone`

El repositorio intentaba:

- guardar minutos con `setLong(...)`
- leer la columna con `getLong(...)`

Eso está mal porque `TIME` no es un `LONG`.

#### Cambio aplicado

En `PedidoRepositorioImpl` se implementó una solución puente:

- al leer: `rs.getTime("tiempoestentrega")` y conversión a `Duration`
- al guardar/actualizar:
  - si hay duración -> `java.sql.Time`
  - si no hay valor -> `NULL`

#### Razón

Se eligió una solución mínima compatible con el esquema actual sin migrar la base. A futuro conviene rediseñar este campo para representar una duración real.

---

### 5. Error 500 al recuperar la clave generada del pedido

#### Síntoma

Spring devolvía:

- `The getKey method should only be used when a single key is returned`

#### Causa

`GeneratedKeyHolder` estaba recibiendo múltiples columnas al hacer el `INSERT`, y luego el código llamaba:

```java
keyHolder.getKey()
```

Eso solo funciona si el driver devuelve una única clave.

#### Cambio aplicado

En `PedidoRepositorioImpl.guardar(...)`, el `PreparedStatement` pasó de:

```java
Statement.RETURN_GENERATED_KEYS
```

a:

```java
new String[]{"id"}
```

#### Razón

Así PostgreSQL devuelve explícitamente solo la PK `id`, y `keyHolder.getKey()` vuelve a ser válido.

---

### 6. Respuesta JSON gigante por referencia circular

#### Síntoma

El `POST /api/v1/pedidos` devolvía miles de líneas en el JSON.

#### Causa

Se estaba serializando el dominio directamente:

- `Pedido` contiene `detalles`
- `DetallePedido` contiene `pedido`
- eso genera recursión:
  - `Pedido -> DetallePedido -> Pedido -> ...`

Además, se exponían objetos demasiado ricos para una API.

#### Cambio aplicado — Fase 1 del fix limpio

Se dejó de devolver `Pedido` directamente desde el controller.

Se agregaron DTOs de salida:

- `DtPedidoResponse`
- `DtDetallePedidoResponse`
- `DtPlatoResumenResponse`
- `DtLocalResumenResponse`
- `DtClienteResumenResponse`

Se agregó además:

- `PedidoResponseMapper`

Y el endpoint:

- `POST /api/v1/pedidos`

pasó a responder:

- `ResponseEntity<DtPedidoResponse>`

#### Razón

La API no debe exponer el grafo completo del dominio. Debe devolver un contrato HTTP controlado, sin recursión y sin campos internos innecesarios.

---

## Archivos modificados

- `src/main/java/com/example/demo/Logica/DataTypes/DtPedido.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtCliente.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtDetallePedido.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/ClienteRepositorioImpl.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`
- `src/main/java/com/example/demo/Logica/Controllers/PedidoController.java`
- `src/main/java/com/example/demo/Logica/Interfaces/iPedidoController.java`
- `src/main/java/com/example/demo/Logica/Service/PedidoResponseMapper.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtPedidoResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtDetallePedidoResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtPlatoResumenResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtLocalResumenResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtClienteResumenResponse.java`

---

## Estado actual del caso de uso

Con estos cambios:

- el request ya deserializa correctamente
- el pedido ya puede persistirse
- el repositorio quedó alineado con el esquema real de la tabla `pedido`
- la respuesta del `POST` dejó de serializar recursivamente el dominio

---

## Deuda técnica pendiente

### 1. Modelado de `tiempoEstEntrega`

Hoy se usa una solución puente para mapear:

- `Duration` <-> `TIME`

Eso no es ideal. A futuro conviene:

- cambiar la columna a un tipo que represente duración (`INTEGER`, `INTERVAL`, etc.)
  o
- cambiar el modelo Java si el negocio realmente quiere una hora del día.

### 2. Extender DTOs de salida al resto de endpoints de pedidos

Por ahora el fix limpio se aplicó al:

- `POST /api/v1/pedidos`

Conviene aplicar el mismo criterio a:

- confirmar pedido
- listar pedidos
- otros endpoints del módulo

### 3. Revisar mapeo de `imagenes` en `Local`

Sigue habiendo señales de mapeo incorrecto en el contenido de `imagenes`, que aparece serializado como cadenas deformadas.

---

## Conclusión

Los problemas para testear `CU-CL06` no vinieron de un único bug, sino de una cadena de desalineamientos entre:

- DTOs de entrada
- mapeo de repositorios
- nombres de columnas SQL
- tipos Java vs tipos PostgreSQL
- y diseño de respuesta HTTP

El trabajo realizado dejó el caso de uso mucho más estable y, sobre todo, dejó evidencia clara de QUÉ estaba mal y POR QUÉ.
