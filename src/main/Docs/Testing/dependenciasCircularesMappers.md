# Dependencias circulares en mappers — qué cambiamos y por qué

## Problema detectado

Se detectó un ciclo de dependencias entre beans de Spring relacionado con el mapeo de pedidos:

- `PedidoMapper` dependía de `DetallePedidoMapper`
- `DetallePedidoMapper` dependía de `PedidoMapper`

Eso provocaba un ciclo de creación de beans al iniciar la aplicación:

`PedidoMapper -> DetallePedidoMapper -> PedidoMapper`

Spring lo rechaza por defecto porque las referencias circulares entre beans son una señal de diseño acoplado y frágil.

---

## Causa de fondo

El problema no era solo de Spring. La causa real estaba en el diseño del mapeo.

### Modelo involucrado

- `Pedido` contiene `List<DetallePedido>`
- `DetallePedido` contiene `Pedido`
- `DtPedido` contiene `List<DtDetallePedido>`
- `DtDetallePedido` contenía `DtPedido`

Eso generaba una relación bidireccional completa también en DTOs y mappers.

En la práctica, el sistema intentaba mapear:

- un `Pedido`, que mapea sus `DetallePedido`
- cada `DetallePedido`, que a su vez volvía a mapear su `Pedido`

Resultado:

- dependencia circular entre mappers
- riesgo de recursión infinita
- contratos HTTP innecesariamente grandes
- mayor acoplamiento entre capas

---

## Objetivo del refactor

Romper la circularidad de manera sana, sin usar parches como:

- `@Lazy`
- `spring.main.allow-circular-references=true`

La idea fue corregir la raíz del problema: el diseño del mapeo.

---

## Cambios realizados

## 1. `DtDetallePedido` dejó de contener `DtPedido`

### Antes

`DtDetallePedido` tenía:

```java
private DtPedido dtPedido;
```

### Después

Ese campo fue eliminado.

### Por qué

Porque un detalle ya viaja dentro del pedido padre.

Si `DtPedido` ya contiene una lista de detalles, no tiene sentido que cada detalle vuelva a contener el pedido completo. Eso duplica estructura y reintroduce circularidad.

### Beneficio

- contrato más chico
- menos acoplamiento
- elimina la back-reference innecesaria

---

## 2. `DetallePedidoMapper` dejó de depender de `PedidoMapper`

### Antes

`DetallePedidoMapper` tenía una dependencia directa a `PedidoMapper` y hacía conversiones como:

- `DtDetallePedido -> DetallePedido` reconstruyendo el `Pedido`
- `DetallePedido -> DtDetallePedido` reconstruyendo el `DtPedido`

### Después

`DetallePedidoMapper` pasó a mapear solo el detalle:

- `id`
- `cantidad`
- `precioUnitario`
- `subtotal`
- `plato`

Y dejó de mapear:

- `pedido`
- `dtPedido`

### Por qué

Porque el detalle no debe reconstruir al padre completo. Esa responsabilidad generaba el ciclo y además mezclaba niveles del grafo.

### Beneficio

- se rompe la dependencia circular
- el mapper queda con una responsabilidad clara
- se evita la recursión conceptual

---

## 3. `DetallePedidoMapper` pasó a ser un mapper sin back-reference

Conceptualmente, ahora este mapper transforma:

- `DtDetallePedido -> DetallePedido`
- `DetallePedido -> DtDetallePedido`

pero sin intentar “volver hacia arriba” del árbol.

### Idea de diseño

El hijo no arma al padre.

Eso es importante porque en un agregado como `Pedido`, el contenedor principal debe dominar la composición.

---

## 4. `PedidoService` pasó a construir el `Pedido` de negocio

### Antes

En `realizarPedido(...)`:

- el service validaba
- buscaba `Local`
- buscaba `Cliente`
- construía detalles válidos
- calculaba total

pero luego hacía:

```java
Pedido pedido = pedidoMapper.mapearPedidoDeDt(dtPedido);
```

Eso significaba que el mapper terminaba construyendo la instancia final del pedido.

### Después

El `PedidoService` construye directamente el agregado `Pedido` a partir de:

- `Local` validado
- `Cliente` validado
- `DetallePedido` ya validado
- total recalculado

Además, el service enlaza:

```java
detalle.setPedido(pedido);
```

### Por qué

Porque construir el agregado válido del negocio no es trabajo de un mapper.

Eso implica:

- reglas
- consistencia
- relaciones válidas
- datos recuperados desde repositorio

Y todo eso pertenece al service.

### Beneficio

- mejor separación de responsabilidades
- el agregado se arma con datos ya validados
- el mapper deja de invadir lógica de negocio

---

## 5. `PedidoMapper` quedó acotado al trabajo de mapeo

`PedidoMapper` sigue existiendo, pero ya no es el responsable del caso de uso `realizarPedido`.

Su rol ahora es más sano:

- mapear estructuras de datos
- armar el árbol de pedido dentro del contexto de mapeo
- convertir entre `Pedido` y `DtPedido`

También se ajustó para contemplar correctamente:

- `local`
- `tiempoEstEntrega`

### Por qué

Porque una cosa es mapear datos y otra construir un agregado de negocio real.

El mapper puede armar un árbol de datos.
El service debe armar un agregado consistente del dominio.

---

## 6. Alineación de tests y documentación

Se revisó el impacto del cambio en tests y contratos.

### Qué se encontró

- no había controllers consumiendo `dtPedido` dentro de `DtDetallePedido`
- no había tests dependiendo de ese campo

### Qué se ajustó

- limpieza de `PedidoServiceTest`
- documentación del cambio de contrato en `CU-CL06-Cambios-y-Diagnostico.md`

### Por qué

Porque un refactor sano no termina en el código. También hay que dejar consistentes:

- fixtures
- notas técnicas
- trazabilidad del cambio

---

## Qué problema resolvió este refactor

### Antes

- ciclo entre `PedidoMapper` y `DetallePedidoMapper`
- diseño de mapeo bidireccional completo
- riesgo de recursión
- responsabilidades mezcladas entre mapper y service

### Después

- `DetallePedidoMapper` ya no depende de `PedidoMapper`
- `DtDetallePedido` ya no referencia a `DtPedido`
- `PedidoService` construye el agregado de negocio
- el mapeo quedó más simple y más predecible

---

## Por qué esta solución es mejor que `@Lazy`

Usar `@Lazy` o permitir referencias circulares hubiera sido un parche.

Eso puede hacer que Spring arranque, pero NO corrige:

- el acoplamiento estructural
- la recursión conceptual
- el exceso de datos en DTOs
- la confusión de responsabilidades

La solución aplicada ataca la causa, no el síntoma.

---

## Aprendizaje principal

La lección importante de este cambio es esta:

### Un mapper no debe reconstruir un grafo bidireccional completo sin control.

Y además:

### Un service debe construir el agregado válido del negocio.

Cuando esas dos ideas se mezclan mal:

- aparecen ciclos
- el diseño se vuelve frágil
- los errores de arranque son solo la primera alarma

---

## Resumen corto

Se eliminó la circularidad entre mappers porque el detalle dejó de mapear de vuelta al pedido, el DTO dejó de incluir la referencia completa al padre, y el service pasó a construir el agregado real del negocio.

Eso dejó el diseño:

- más claro
- menos acoplado
- más mantenible
- más coherente con Spring y con separación de responsabilidades
