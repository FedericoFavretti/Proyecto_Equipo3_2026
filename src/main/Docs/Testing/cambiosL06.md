# CU-L06 — Cambios aplicados

## Caso de uso

**CU-L06 — Buscar y Listar Pedidos Recibidos**

Endpoint involucrado:

```http
GET /api/v1/pedidos/locales/{idLocal}
```

---

## Problema detectado antes del cambio

El endpoint de listado estaba reutilizando `DtPedido` y el flujo terminaba dependiendo del agregado completo `Pedido`.

Eso generaba varios problemas:

- el contrato de salida no era propio de un listado
- se exponían más datos de los necesarios
- el mapper intentaba recorrer `detalles` aunque el repositorio no los hidrataba
- al listar pedidos de un local existente podía aparecer `500` por `NullPointerException`

Error observado en logs:

```text
Cannot invoke "java.util.List.stream()" because "detalles" is null
```

---

## Decisión tomada

Se aplicó el **camino B**:

- CU-L06 pasa a resolverse como **endpoint de listado**
- se deja de reutilizar `DtPedido`
- se crea un **DTO específico para respuesta de listado**
- se crea un **DTO específico para filtros**
- se usa una **proyección liviana de persistencia** en lugar de mapear `Pedido` completo

Esto evita acoplar CU-L06 al detalle completo del pedido.

---

## Contrato nuevo de CU-L06

### Query params soportados

- `estado`
- `fechaDesde`
- `fechaHasta`
- `ordenarPor`
- `direccion`

Ejemplo:

```http
GET /api/v1/pedidos/locales/6?estado=Pendiente&fechaDesde=2026-06-01&fechaHasta=2026-06-14&ordenarPor=total&direccion=asc
```

### Response nueva

El endpoint ahora devuelve:

```java
List<DtPedidoListadoResponse>
```

Con shape resumido:

- `id`
- `fecha`
- `estado`
- `total`
- `tiempoEstEntrega`
- `cliente`
  - `id`
  - `nombre`
  - `apellido`
- `cantidadItems`

---

## Archivos modificados

### Modificados

- `src/main/java/com/example/demo/Logica/Controllers/PedidoController.java`
- `src/main/java/com/example/demo/Logica/Interfaces/iPedidoController.java`
- `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
- `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`
- `src/test/java/com/example/demo/Logica/Service/PedidoServiceTest.java`

### Nuevos

- `src/main/java/com/example/demo/Logica/DataTypes/DtPedidoListadoFiltro.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtPedidoListadoResponse.java`
- `src/main/java/com/example/demo/Logica/DataTypes/DtClientePedidoResumenResponse.java`
- `src/main/java/com/example/demo/Logica/Mappers/PedidoListadoMapper.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoListadoView.java`

---

## Cambios por capa

### Controller

`PedidoController` ahora:

- mantiene la ruta `GET /api/v1/pedidos/locales/{idLocal}`
- recibe filtros opcionales por query params
- construye `DtPedidoListadoFiltro`
- delega al service de listado

### Interface

`iPedidoController` fue actualizado para reflejar:

- nuevo DTO de salida
- nuevos parámetros de filtro

### Service

`PedidoService.listarPedidos(...)` ahora:

- valida que el local exista
- valida filtros y ordenamiento
- delega a un método específico de repositorio para CU-L06
- mapea con `PedidoListadoMapper`

Ya no usa `PedidoMapper` para este caso.

### Persistencia

`PedidoRepositorioImpl` ahora incorpora una consulta SQL específica de listado:

- filtra por `idLocal`
- puede filtrar por `estado`
- puede filtrar por rango de fechas
- puede ordenar por `fecha`, `total` o `estado`
- devuelve proyección liviana `PedidoListadoView`

También calcula `cantidadItems` desde `detallepedido`.

---

## Cobertura agregada

Se agregaron pruebas de servicio para CU-L06:

- retorna listado resumido con filtro
- rechaza campo de orden inválido
- rechaza rango de fechas inválido

Pruebas ejecutadas:

```powershell
.\mvnw.cmd -q "-Dtest=PedidoServiceTest,ControllerMappingUniquenessTest" test
```

Resultado: OK

---

## Beneficios del cambio

- elimina el `500` causado por `detalles = null`
- evita exponer datos internos innecesarios
- alinea mejor la API con `GuiaCasosDeUso.md`
- separa listado de detalle
- deja base limpia para agregar filtros y ordenamientos reales

---

## Pendiente

Aunque CU-L06 quedó más limpio, todavía faltan mejoras:

- test de controller para validar binding HTTP y shape JSON
- manejo HTTP global de errores (`404`, `400`, etc.)
- definir si el caso “sin resultados” se expresa solo como `200 + []` o con mensaje de apoyo en frontend
