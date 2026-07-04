# Recomendación para Frontend — corrección de pantalla `local-panel/estadisticas`

**Fecha:** 2026-07-03  
**Origen:** análisis verificado contra backend actual  
**Pantalla impactada:** `local-panel/estadisticas`

---

## Objetivo

Dejar una guía clara para frontend sobre cómo corregir los dos problemas reportados por testing en la pantalla de estadísticas del local:

1. las estadísticas deberían iniciar vacías y mostrarse recién después de una búsqueda
2. al limpiar filtros, los resultados anteriores no deberían seguir visibles
3. los textos con tildes no deberían verse con símbolos raros

---

## Resumen ejecutivo

### Recomendación principal

La corrección recomendada es hacerla **primero en frontend**.

¿Por qué?

Porque el backend actual **no está diseñado** para iniciar vacío:

- si frontend no envía filtros, backend usa `MES_ACTUAL` por defecto
- si no hay datos para el período, backend devuelve **error funcional**
- el contrato vigente **no** define un `200` vacío por defecto

Entonces, si negocio/testing quiere una UX de:

- pantalla vacía al entrar
- mostrar resultados solo después de buscar
- limpiar y volver al estado vacío

eso se puede resolver **sin romper el contrato actual** si frontend controla mejor su estado.

---

## Lo verificado en backend

### 1. Hoy existe período por defecto

En `src/main/java/com/example/demo/Logica/Service/LocalService.java`, el método `resolverRangoPeriodo(...)` aplica:

- `MES_ACTUAL` cuando no llega ningún filtro

Eso significa que, si frontend hace la request automáticamente al entrar, **es esperable** que aparezcan estadísticas sin que el usuario haya buscado manualmente.

### 2. Hoy “sin datos” no devuelve respuesta vacía

En el mismo servicio, si no hay pedidos válidos para el período:

- backend lanza `BusinessRuleException`

O sea:

- hoy **no** devuelve `200` con arrays vacíos
- hoy **sí** devuelve error funcional del flujo

### 3. La documentación vigente ya refleja este contrato

Documentos verificados:

- `src/main/Docs/Testing/CU-L11-API-Frontend.md`
- `src/main/Docs/Testing/alineacion-backend-frontend-estadisticas-contrato-actual.md`

Ambos dejan documentado que:

- si no se manda período, backend usa `MES_ACTUAL`
- si no hay ventas en el período, backend responde error de negocio

---

## Diagnóstico de los problemas reportados

## Problema A — “queda siempre la última búsqueda”

### Causa más probable

Esto huele a **estado stale en frontend**.

El síntoma encaja con alguno de estos escenarios:

- frontend guarda el último resultado y no lo limpia al resetear filtros
- frontend dispara búsqueda automática al montar la vista
- frontend no separa el estado de filtros del estado de resultados
- frontend mantiene visible el último payload aunque la búsqueda actual ya no exista

### Recomendación concreta

Frontend debería manejar **cuatro estados separados**:

1. `filtros`
2. `resultado`
3. `error`
4. `yaSeBusco` o equivalente

### Comportamiento recomendado

#### Al entrar a la pantalla

- mostrar pantalla vacía / estado inicial
- **no llamar automáticamente** al endpoint
- `resultado = null`
- `error = null`
- `yaSeBusco = false`

#### Al buscar

- validar filtros elegidos
- llamar al endpoint
- si responde bien:
  - guardar resultado
  - limpiar error
  - `yaSeBusco = true`
- si responde error funcional:
  - limpiar resultado
  - mostrar mensaje correspondiente
  - `yaSeBusco = true`

#### Al limpiar

- resetear filtros
- limpiar resultado
- limpiar error
- volver a `yaSeBusco = false`
- ocultar métricas/tablas/gráficas anteriores

---

## Problema B — “los tildes aparecen con símbolos raros”

### Antes de corregir, hay que ubicar BIEN dónde se rompe

No hay que adivinar. Hay que verificar si el texto roto viene de:

1. **texto fijo de frontend**
   - títulos
   - labels
   - botones
   - placeholders
   - mensajes locales

2. **texto devuelto por backend**
   - mensajes de error
   - nombres de platos
   - otros datos persistidos

### Diagnóstico más probable

Si lo que se ve mal es algo como:

- `Estadísticas`
- `Período`
- `Últimos 7 días`

entonces el problema es **casi seguro frontend** o archivos guardados con encoding incorrecto.

Si lo que se ve mal son:

- nombres de platos
- mensajes que vienen del API

entonces hay que comparar:

1. valor en base de datos
2. response raw del endpoint
3. render final en la UI

Solo así se encuentra el punto exacto de ruptura.

---

## Recomendación específica para el tema de tildes

### Paso 1 — distinguir texto fijo vs texto de datos

Armar una lista rápida de qué textos se rompen exactamente:

- título de pantalla
- texto de preset
- mensaje de error
- nombre de plato
- nombre de eje o gráfica

### Paso 2 — inspeccionar el response raw

Desde DevTools / Network:

- revisar el body exacto de la respuesta
- verificar si el texto ya viene roto o se rompe al renderizar

### Paso 3 — revisar encoding en frontend

Verificar que:

- archivos fuente estén guardados en **UTF-8**
- no existan strings copiadas con mojibake (`Ã¡`, `Ã©`, `Ã³`, etc.)
- si usan i18n, que los archivos de traducción también estén en **UTF-8**

### Paso 4 — revisar datos persistidos si aplica

Si el texto roto corresponde a nombres de platos:

- verificar el valor almacenado en BD
- confirmar si ya estaba mal guardado antes de llegar a la pantalla

---

## Solución recomendada para frontend

## Opción recomendada: corregir UX sin tocar contrato backend

### Qué hacer

1. **No consumir automáticamente** el endpoint al montar la pantalla
2. Mostrar estado inicial vacío hasta que el usuario busque
3. Limpiar completamente el resultado al usar “Limpiar filtros”
4. Separar visualmente:
   - estado inicial
   - estado con resultados
   - estado sin resultados / error funcional
5. Corregir encoding de textos visibles si el problema está en UI

### Ventajas

- no rompe compatibilidad
- no exige cambios backend
- resuelve el reclamo de testing rápido
- deja un flujo más claro para el usuario

### Tradeoff

La UI quedará con una semántica propia:

- backend soporta default `MES_ACTUAL`
- frontend decide no usar ese default automáticamente

Esto es aceptable si queda documentado.

---

## Alternativa si negocio quiere cambiar el contrato oficial

Si el equipo decide que **el sistema completo** debe comportarse como “vacío por defecto”, entonces habría que hacer un cambio coordinado:

### Cambios necesarios

- backend deja de asumir `MES_ACTUAL` automáticamente
- backend exige filtro explícito
- frontend siempre manda criterio de búsqueda elegido por usuario
- se actualizan docs y tests

### Riesgo

Esto sería un **cambio de contrato** y puede romper consumidores existentes si alguien depende del default actual.

Por eso **NO** lo recomiendo como primera medida.

---

## Criterios de aceptación sugeridos para testing

### Estado inicial

- al abrir la pantalla, no se ven métricas ni tablas previas
- no se muestra la última búsqueda persistida visualmente
- el usuario debe accionar “Buscar” para ver estadísticas

### Búsqueda

- al buscar con un período válido, se muestran las estadísticas del período
- al cambiar el período y volver a buscar, se reemplazan los resultados anteriores

### Limpieza

- al presionar “Limpiar”, desaparecen:
  - métricas
  - tablas
  - gráficos
  - mensajes de error previos
- la pantalla vuelve al estado inicial

### Tildes / encoding

- textos fijos de UI se renderizan correctamente
- nombres de platos con tildes se ven correctamente
- mensajes de error visibles al usuario se ven correctamente

---

## Casos de validación recomendados

1. Entrar a la pantalla y confirmar que no dispara búsqueda automática
2. Buscar por preset y ver resultados
3. Buscar por rango libre y ver resultados
4. Limpiar filtros y verificar que desaparezca todo
5. Buscar un período sin datos y verificar manejo visual correcto
6. Probar con platos o textos que tengan:
   - á
   - é
   - í
   - ó
   - ú
   - ñ

---

## Conclusión

La recomendación técnica es:

- **corregir el flujo de visibilidad y limpieza en frontend**
- **no cambiar backend por ahora**
- **investigar el problema de tildes separando texto fijo de datos del API**

En otras palabras:

el problema de “queda la última búsqueda” es, con alta probabilidad, un problema de **manejo de estado en frontend**.  
El problema de tildes, salvo prueba en contrario, apunta más a **UI/encoding frontend** o a **datos cargados incorrectamente**, no al contrato actual del endpoint de estadísticas.

