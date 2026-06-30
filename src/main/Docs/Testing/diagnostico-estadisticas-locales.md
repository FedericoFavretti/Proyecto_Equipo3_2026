# Diagnóstico y recomendación — Estadísticas de Locales

## Objetivo

Dejar documentado el problema observado en la pantalla de estadísticas del local, su causa raíz verificada en backend y la forma exacta de corregirlo para que otra persona pueda implementarlo.

---

## Resumen ejecutivo

La request de estadísticas **sí está llegando correctamente** al backend con el preset seleccionado.

El error HTTP `400` se produce porque el servicio de estadísticas:

1. resuelve el rango del período,
2. busca pedidos del local en ese rango,
3. y si no encuentra pedidos en estado `Confirmado`, lanza una `BusinessRuleException`.

El problema de fondo es que las estadísticas **solo consideran pedidos `Confirmado`**, pero el sistema luego mueve esos pedidos a `Entregado`. Como consecuencia, un local puede haber vendido y aun así quedar con estadísticas vacías.

---

## Hallazgos verificados

### 1) El endpoint acepta correctamente el filtro

Archivo:

- `src/main/java/com/example/demo/Logica/Controllers/LocalController.java`

Líneas relevantes:

- `112-115`

Qué se verificó:

- El endpoint `GET /estadisticas/{idLocal}` recibe `DtEstadisticasLocalFiltro`.
- No hay evidencia de que el preset `MES_ACTUAL` falle en el binding.

Prueba automatizada existente:

- `src/test/java/com/example/demo/Logica/Controllers/LocalControllerTest.java:43-72`

Esa prueba confirma que el controller acepta presets por query param.

---

### 2) El `400` se genera por regla de negocio, no por error técnico

Archivo:

- `src/main/java/com/example/demo/Logica/Service/LocalService.java`

Líneas relevantes:

- `254-286`
- especialmente `259-264`

Qué ocurre:

- En `259` se resuelve el rango.
- En `260-263` se consulta si existen pedidos confirmados para ese local y período.
- En `264` se lanza la excepción con el mensaje:

`No hay informacion disponible para el periodo seleccionado. Intente con un rango de fechas diferente.`

Transformación a HTTP 400:

- `src/main/java/com/example/demo/Logica/Exceptions/GlobalExceptionHandler.java:28-30`

---

### 3) La validación y los cálculos usan solo estado `Confirmado`

Archivo:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

Líneas relevantes:

- `228-246` → validación de existencia
- `250-271` → platos más pedidos
- `275-291` → ventas confirmadas

Qué se verificó:

- Todos esos métodos filtran por `EstadoPedido.Confirmado`.

---

### 4) Los pedidos luego pasan a `Entregado`

Archivos:

- `src/main/java/com/example/demo/Logica/Service/PedidoService.java`
- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

Líneas relevantes:

- `PedidoService.java:332-337`
- `PedidoRepositorioImpl.java:410-416`

Qué se verificó:

- Existe un scheduler que busca pedidos vencidos en estado `Confirmado`.
- Luego los actualiza a `Entregado`.

---

## Causa raíz

La pantalla de estadísticas queda sin datos cuando, para el período consultado, los pedidos del local **ya no están en `Confirmado` sino en `Entregado`**.

Eso genera un falso negativo:

- sí hubo ventas,
- sí hubo platos pedidos,
- pero el backend las excluye del cálculo.

---

## Recomendación principal

## Incluir `Entregado` además de `Confirmado` en estadísticas

Esta es la recomendación principal porque una pantalla de estadísticas debe reflejar ventas reales del período, no solo pedidos todavía en tránsito operativo.

### Motivo

`Confirmado` describe un estado operativo intermedio.  
`Entregado` también representa una venta válida y completada.

Si solo se cuenta `Confirmado`, el dato histórico se degrada con el paso del tiempo.

---

## Implementación exacta esperada

## 1) Cambiar la validación de existencia de pedidos

Archivo:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

Método:

- `existePedidoConfirmadoEnPeriodo`

Rango de líneas actual:

- `228-246`

Qué debe hacer la persona que implemente:

- Mantener el filtro por `idlocal` y por rango de fechas.
- Dejar de filtrar por un único estado.
- Cambiar la condición para aceptar **dos estados válidos para estadísticas**:
  - `Confirmado`
  - `Entregado`

Puntos exactos a revisar:

- línea `233`
- línea `242`

Resultado esperado:

- la validación debe responder verdadero si existe al menos un pedido del local en ese período que esté en `Confirmado` **o** `Entregado`.

---

## 2) Cambiar el cálculo de platos más pedidos

Archivo:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

Método:

- `obtenerPlatosMasPedidosConfirmadosEnPeriodo`

Rango de líneas actual:

- `250-271`

Qué debe hacer la persona que implemente:

- Mantener el join actual con `detallepedido`.
- Mantener el agrupamiento por plato.
- Mantener el orden descendente por cantidad total.
- Cambiar el filtro de estado para incluir:
  - `Confirmado`
  - `Entregado`

Puntos exactos a revisar:

- línea `256`
- línea `268`

Resultado esperado:

- el ranking de platos debe considerar todos los pedidos válidos del período, no solo los aún confirmados.

---

## 3) Cambiar el cálculo de ventas

Archivo:

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`

Método:

- `obtenerVentasConfirmadasEnPeriodo`

Rango de líneas actual:

- `275-291`

Qué debe hacer la persona que implemente:

- Mantener la suma de `p.total`.
- Mantener el filtro por local y por rango de fechas.
- Cambiar el filtro de estado para incluir:
  - `Confirmado`
  - `Entregado`

Puntos exactos a revisar:

- línea `280`
- línea `289`

Resultado esperado:

- el total de ventas debe incluir ventas confirmadas y entregadas del período.

---

## 4) Alinear nombres de métodos (recomendado)

Esto no bloquea la corrección funcional, pero es una mejora importante de claridad.

Archivo:

- `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`

Métodos a revisar:

- `existePedidoConfirmadoEnPeriodo`
- `obtenerPlatosMasPedidosConfirmadosEnPeriodo`
- `obtenerVentasConfirmadasEnPeriodo`

Recomendación:

- Renombrar esos métodos para que el nombre refleje la nueva regla.

Ejemplo conceptual:

- pasar de “Confirmados” a algo como “Pedidos validos para estadisticas”.

Motivo:

- hoy el nombre del método mentiría si empieza a incluir `Entregado`.

Si se renombran:

1. ajustar interfaz,
2. ajustar implementación,
3. ajustar invocaciones en:
   - `src/main/java/com/example/demo/Logica/Service/LocalService.java:260-280`

---

## 5) Actualizar documentación funcional

Archivo:

- `src/main/Docs/Guia/GuiaCasosDeUso.md`

Líneas relevantes:

- `349-353`

Qué debe cambiar conceptualmente:

- hoy la guía indica: “Calcular ventas y platos más pedidos usando únicamente pedidos en estado `Confirmado`”.
- debe actualizarse para reflejar la nueva regla de estadísticas.

Recomendación funcional:

- indicar que estadísticas consideran pedidos válidos cerrados operativamente para el período.
- si el equipo quiere máxima precisión, dejar explícito: `Confirmado` y `Entregado`.

---

## 6) Actualizar pruebas automatizadas

### Pruebas del servicio

Archivo:

- `src/test/java/com/example/demo/Logica/Service/LocalStatisticsServiceTest.java`

Casos mínimos a ajustar/agregar:

1. verificar que estadísticas aceptan pedidos `Confirmado`,
2. verificar que estadísticas también aceptan pedidos `Entregado`,
3. verificar que el mensaje “sin datos” solo aparece cuando no existe ninguno de los dos estados.

Líneas actualmente relevantes:

- `159-170`

### Pruebas del controller

Archivo:

- `src/test/java/com/example/demo/Logica/Controllers/LocalControllerTest.java`

No parece requerir cambios por esta corrección, porque el binding ya está cubierto.  
Solo tocar si el contrato del endpoint cambia.

---

## Alternativa secundaria (NO recomendada como solución principal)

## Devolver `200` con métricas vacías en vez de `400`

Archivo:

- `src/main/java/com/example/demo/Logica/Service/LocalService.java`

Línea crítica:

- `264`

Qué implicaría:

- eliminar la excepción de “sin datos”,
- devolver una respuesta válida con:
  - `ventasConfirmadas = 0`,
  - `platosMasPedido = []`,
  - `fechaDesde` y `fechaHasta` resueltas.

Por qué NO se recomienda como primer cambio:

- mejora UX, sí,
- pero NO corrige la causa raíz si se siguen excluyendo pedidos `Entregado`.

Esta alternativa solo tiene sentido después de resolver la regla de negocio principal o si el equipo decide explícitamente que “sin datos” no es un error.

---

## Checklist de implementación para otra persona

1. Revisar `PedidoRepositorioImpl.java`.
2. Cambiar los 3 queries de estadísticas para incluir `Confirmado` + `Entregado`.
3. Verificar si conviene renombrar métodos en repositorio/interfaz.
4. Ajustar llamadas en `LocalService.java` si hubo rename.
5. Actualizar `GuiaCasosDeUso.md`.
6. Ajustar/agregar tests en `LocalStatisticsServiceTest.java`.
7. Confirmar que el mensaje de “sin datos” solo se dispare cuando realmente no existan pedidos válidos del período.

---

## Decisión recomendada

La implementación recomendada es:

> **mantener la lógica general del endpoint, pero cambiar la definición de pedido válido para estadísticas para incluir `Confirmado` y `Entregado`.**

Esto corrige el problema real sin alterar innecesariamente el flujo del controller ni el manejo general del período.

---

## Archivos impactados

- `src/main/java/com/example/demo/Persistencia/Implementaciones/PedidoRepositorioImpl.java`
- `src/main/java/com/example/demo/Persistencia/Repositorios/PedidoRepositorio.java`
- `src/main/java/com/example/demo/Logica/Service/LocalService.java`
- `src/test/java/com/example/demo/Logica/Service/LocalStatisticsServiceTest.java`
- `src/main/Docs/Guia/GuiaCasosDeUso.md`

---

## Estado actual del diagnóstico

Diagnóstico verificado contra código existente del proyecto el **2026-06-29**.
