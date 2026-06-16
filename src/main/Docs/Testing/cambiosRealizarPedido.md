# Cambios realizados en `realizarPedido`

## Objetivo

Documentar por qué se cambió `PedidoService` en el flujo de `realizarPedido` y por qué era necesario corregir el error cuando `detalles` llegaba en `null` o vacío.

---

## Contexto del cambio

El caso de uso `POST /api/v1/pedidos` necesita crear un pedido completo a partir de:

- la cabecera del pedido
- el local
- el cliente
- la lista de detalles

En este flujo, `PedidoService` es la capa correcta para aplicar reglas de negocio. El problema original era que parte importante de la validación y de la construcción del pedido no estaba controlada de forma explícita desde el service, lo que hacía al caso de uso frágil frente a requests incompletos.

---

## Qué se cambió en `PedidoService`

En `src/main/java/com/example/demo/Logica/Service/PedidoService.java` se dejó el flujo de `realizarPedido(...)` centrado en lógica de negocio explícita:

1. validar el request completo antes de persistir
2. buscar y validar `Local`
3. buscar y validar `Cliente`
4. construir los `DetallePedido` desde los datos recibidos
5. recalcular el total en backend
6. persistir primero la cabecera y luego los detalles asociados

Hoy el service:

- valida que exista `dtPedido`
- valida que exista la colección `detalles`
- valida que `detalles` no venga vacía
- valida que cada detalle tenga cantidad válida
- valida que cada detalle tenga `dtPlato.id`
- valida que cada plato exista
- valida que el plato pertenezca al local
- valida que el plato esté disponible
- recalcula el subtotal y el total sin confiar en lo que mande el cliente

---

## Por qué se cambió el service

### 1. Porque la lógica de negocio NO debe quedar implícita en mappers

Ese es un error de diseño MUY común: usar mappers como si fueran la lógica del caso de uso.

Un mapper transforma estructuras.  
Un service decide reglas.

En este caso, reglas como:

- si no hay detalles, el pedido es inválido
- si el plato no pertenece al local, el pedido es inválido
- si la cantidad es menor o igual a cero, el pedido es inválido
- el total debe recalcularse en backend

pertenecen al service, no a un mapper.

### 2. Porque había que controlar mejor errores de entrada

Si el backend deja que el request avance demasiado antes de validar, el fallo termina apareciendo más abajo:

- en un mapper
- en un repositorio
- en un `NullPointerException`
- o en un `500`

Eso es mala arquitectura. El error de negocio debe cortarse lo más arriba posible, con un mensaje claro.

### 3. Porque no se puede confiar en el total ni en los subtotales enviados por el cliente

El cliente puede mandar cualquier valor.  
Por eso el service recalcula:

- `subtotal = precioUnitario * cantidad`
- `total = suma de subtotales`

Esto protege la integridad del pedido y evita inconsistencias.

---

## Error corregido: `detalles` en `null`

### Síntoma

Cuando el request llegaba con:

```json
{
  "dtPedido": { ... },
  "detalles": null
}
```

o incluso sin detalles válidos, el flujo podía romper o quedar mal validado.

### Causa

El problema conceptual era permitir que una estructura incompleta avanzara dentro del caso de uso.

Un pedido SIN detalles no tiene sentido de negocio. No es un pedido “vacío”: es un request inválido.

### Corrección aplicada

En `PedidoService.validarPedidoConDetalles(...)` se agregó esta validación:

```java
if (dtPedidoConDetalles == null
        || dtPedidoConDetalles.getDtPedido() == null
        || dtPedidoConDetalles.getDetalles() == null
        || dtPedidoConDetalles.getDetalles().isEmpty()) {
    throw new IllegalArgumentException(MENSAJE_SIN_PLATOS);
}
```

### Por qué esta corrección es la correcta

Porque corta el problema en la frontera del caso de uso.

En vez de dejar que el sistema falle después por:

- iterar una lista `null`
- mapear detalles inexistentes
- calcular un total inconsistente
- persistir una cabecera sin ítems

el backend responde con una regla de negocio clara:

> `Debe agregar al menos un plato para realizar el pedido.`

Eso es MUCHÍSIMO mejor que un `500`.

---

## Beneficios del cambio

- el caso de uso quedó más robusto
- el service expresa la lógica de negocio de forma explícita
- se evita depender de comportamiento implícito de mappers
- se evita persistir pedidos inválidos
- los errores por request incompleto quedan más cerca de la entrada
- el backend recalcula importes y no confía en datos del cliente

---

## Evidencia en tests

En `src/test/java/com/example/demo/Logica/Service/PedidoServiceTest.java` ya hay cobertura alineada con este comportamiento, por ejemplo:

- `realizarPedidoRechazaCuandoNoHayPlatos()`
- `realizarPedidoRechazaCantidadInvalida()`
- `realizarPedidoRecalculaTotalYPersisteCabeceraYDetalles()`
- `realizarPedidoRechazaPlatoDeOtroLocal()`

Estas pruebas verifican exactamente la intención del cambio:

- fallar temprano ante datos inválidos
- construir correctamente los detalles
- recalcular el total
- persistir solo cuando el pedido es consistente

---

## Archivos relevantes

- `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
- `src/test/java/com/example/demo/Logica/Service/PedidoServiceTest.java`

---

## Conclusión

El cambio en `PedidoService` no fue un “retoque”. Fue una corrección de DISEÑO.

Se movió la responsabilidad al lugar correcto: el service.

Y el problema de `detalles = null` se corrigió donde corresponde: en la validación de entrada del caso de uso, antes de mapear, calcular o persistir nada.

Eso deja `realizarPedido` más predecible, más mantenible y mucho más difícil de romper con requests incompletos.
