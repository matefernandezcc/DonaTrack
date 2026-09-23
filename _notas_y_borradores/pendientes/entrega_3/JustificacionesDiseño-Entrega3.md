# Justificaciones de Diseño — Entrega 3

## 1. Arquitectura de comunicación inter-servicios

### Decisión

La comunicación entre los servicios sigue un modelo **Event-Driven** (orientado a eventos) utilizando **RabbitMQ** como Message Broker. El flujo principal es:

```
Logística → RabbitMQ → Donaciones → RabbitMQ → Notificaciones
```

### Justificación

La consigna establece dos restricciones explícitas (Requerimientos de implementación 3 y 4):

> _"El servicio de logística **no debe invocar** los servicios de donaciones ni incentivos, sino dejar disponible la información."_
>
> _"El servicio de logística **no debe comunicarse** con el servicio de notificaciones."_

Esto hace inviable una comunicación sincrónica (REST directo) entre Logística y los demás servicios. La alternativa elegida es **mensajería asincrónica con RabbitMQ** por las siguientes razones:

1. **Desacoplamiento temporal**: Logística no necesita que Donaciones ni Notificaciones estén disponibles al momento de publicar un evento. Si algún servicio está caído, los mensajes se encolan y se procesan cuando se recupera.

2. **Desacoplamiento de conocimiento**: Logística no conoce la existencia de Donaciones ni Notificaciones. Solo publica eventos en un exchange (`logistica_exchange`) con routing keys descriptivos (`ruta.iniciada`, `entrega.realizada`, `entrega.fallida`). Quién consume esos eventos es transparente para Logística.

3. **Cumplimiento de la restricción**: Logística "deja disponible la información" publicando eventos. No invoca endpoints REST de otros servicios.

### Trade-off

- **Consistencia eventual**: Al no tener comunicación sincrónica, el estado entre servicios es eventualmente consistente. Una entrega confirmada en Logística puede tardar milisegundos en reflejarse como `ENTREGADA` en el servicio de Donaciones. Para el dominio de DonaTrack esto es aceptable: las transiciones de estado no son críticas en tiempo real.
- **Complejidad operativa**: RabbitMQ agrega un componente de infraestructura que necesita monitoreo, pero el compose.yaml ya lo gestiona como contenedor.


## 2. Donaciones como intermediario de notificaciones

### Decisión

El servicio de Donaciones actúa como intermediario entre Logística y Notificaciones. Donaciones consume los eventos de Logística, actualiza el estado de las donaciones y luego re-emite nuevos eventos enriquecidos hacia Notificaciones.

### Justificación

Logística no tiene acceso a la información de contacto de los donantes ni de las entidades beneficiarias. Esos datos pertenecen al dominio de Donaciones (entidades `Persona`, `Contacto`, `Donante`, `Beneficiario`).

El flujo completo para un inicio de ruta es:

1. **Logística** publica `RutaIniciadaEvent` (contiene: `rutaId`, `patenteCamion`, `nombreChofer`, lista de `idsDonaciones`).
2. **Donaciones** consume ese evento via `RutaIniciadaRabbitMQListener`, ejecuta `ProcesarInicioRutaUseCase`:
   - Cambia las donaciones a estado `EN_TRASLADO`.
   - Busca los contactos de los donantes y entidades beneficiarias en sus repositorios.
   - Publica `NotificacionInicioRutaEvent` con los contactos ya resueltos.
3. **Notificaciones** consume el evento enriquecido y envía las notificaciones por el medio configurado (email, SMS o WhatsApp).

### Alternativa descartada

Se consideró que Logística publique directamente los eventos de notificación, pero esto requeriría que Logística acceda a datos de contacto que no le pertenecen, violando el principio de separación de responsabilidades y la restricción explícita de la consigna.


## 3. Procesamiento en lotes de 100 y callback asíncrono

### Decisión

`PlanificacionRutasUseCase` particiona los ítems pendientes en sublistas de máximo 100 elementos. Por cada lote se crea una `SolicitudPlanificacion` con estado `PENDIENTE` que se persiste.

### Justificación

La consigna lo establece como restricción del proveedor externo:

> _"El sistema deberá realizar las solicitudes al proveedor en lotes puesto que, por restricciones del proveedor, cada ejecución procesa como máximo 100 donaciones a entregar."_

La implementación usa un bucle simple con `subList`:

```java
for (int i = 0; i < totalItems; i += MAX_DONACIONES_POR_LOTE) {
    List<ItemPlanificacion> lote = itemsPendientes.subList(i, endIndex);
    // Crear solicitud, procesar lote
}
```

La `SolicitudPlanificacion` persiste el estado del lote para poder ser actualizada cuando el proveedor responda vía callback.

### Callback del proveedor externo

La consigna requiere:

> _"El sistema deberá exponer una URL de callback donde el componente externo podrá notificar el resultado de la planificación."_

Se implementó `POST /api/planificacion/callback` que recibe un `CallbackPlanificacionRequest` con el `idSolicitud` y la lista de rutas generadas. El `ProcesarCallbackPlanificacionUseCase` persiste las rutas y actualiza la solicitud a `PROCESADA`.

### Trade-off

El procesamiento es secuencial (lote a lote). Para volúmenes mucho mayores podría paralelizarse, pero la consigna no lo requiere y la simplicidad favorece la mantenibilidad.


## 4. Algoritmo de planificación (Bin Packing simplificado)

### Decisión

Se implementó un algoritmo de **First Fit Decreasing (FFD) simplificado** para distribuir entregas en camiones según peso y volumen.

### Justificación

El algoritmo recorre los ítems de planificación y los asigna al camión actual hasta que se excede su capacidad de peso o volumen. Cuando se excede:
- Se guarda la ruta actual.
- Se rota al siguiente camión/chofer disponible (con módulo circular).
- Se crea una nueva ruta.

Las entregas con el mismo destino (dirección) se agrupan en la misma `Parada`, evitando visitas duplicadas.

### Trade-off

- **Simplicidad vs. optimalidad**: El bin packing es NP-hard en su forma general. El algoritmo FFD simplificado no garantiza la solución óptima, pero produce resultados razonables en tiempo lineal (`O(n)`). Para un sistema universitario que prioriza la claridad del código sobre la optimización, es una decisión justificada.
- **Sin reordenamiento**: No se reordena por peso/volumen descendente (que sería FFD puro). Los ítems se procesan en el orden que llegan. Esto simplifica la implementación sin sacrificar la lógica fundamental.
- **Rotación circular de recursos**: Si hay más entregas que capacidad total de la flota, los camiones se reutilizan. En un sistema real esto requeriría validación adicional (jornadas laborales, descansos).


## 5. Máquina de estados de donaciones y su relación con entregas

### Decisión

Los estados de una donación siguen una máquina de estados lineal con bifurcación:

```
EN_DEPOSITO → ASIGNADA → LISTA_PARA_ENTREGAR → EN_TRASLADO → ENTREGADA
                                                           ↘ ENTREGA_FALLIDA → (replanificación) → ASIGNADA
EN_DEPOSITO → VENCIDA
```

### Justificación

- `EN_DEPOSITO`: Estado inicial al recibir la donación. La donación permanece en depósito hasta ser asignada.
- `ASIGNADA`: Se asignó a una entidad beneficiaria mediante el matchmaking. Lista para ser incluida en una planificación de ruta.
- `EN_TRASLADO`: Transición automática disparada cuando el chofer inicia su ruta (evento `RutaIniciadaEvent`).
- `ENTREGADA`: La entidad beneficiaria confirmó la recepción. Se registran fotos como evidencia.
- `ENTREGA_FALLIDA`: La entrega no pudo concretarse. Puede ser replanificada por un administrador, volviendo a `ASIGNADA`.
- `VENCIDA`: El job de auditoría (`AuditoriaDepositoJob`) detectó que la donación superó su fecha de vencimiento.

### Relación con estados de entrega (Logística)

El servicio de Logística maneja su propio estado de entrega (`EstadoEntrega: PENDIENTE, EN_TRASLADO, ENTREGADA, FALLIDA`). Estos estados se sincronizan con los de Donaciones a través de eventos:

| Evento de Logística | Estado Entrega | Estado Donación resultante |
|---|---|---|
| `RutaIniciadaEvent` | `EN_TRASLADO` | `EN_TRASLADO` |
| `EntregaRealizadaEvent` | `ENTREGADA` | `ENTREGADA` |
| `EntregaNoSatisfactoriaEvent` | `FALLIDA` | `ENTREGA_FALLIDA` |

Cada servicio mantiene su propia representación del estado, evitando acoplamiento de modelos de dominio. La consistencia se logra a través de eventos.


## 6. Patrón de comunicación: Event-Driven con Spring Events + RabbitMQ

### Decisión

Se usa un patrón de dos niveles:
1. **Eventos locales** (Spring `ApplicationEventPublisher`): Los use cases publican eventos de dominio dentro del mismo proceso.
2. **Adaptadores de mensajería** (`LogisticaRabbitMQAdapter`, `DonacionesRabbitMQAdapter`): Escuchan los eventos locales con `@EventListener` y los re-publican en RabbitMQ.

### Justificación

Esta separación sigue la **Arquitectura Hexagonal** (Ports & Adapters):

- Los **use cases** no conocen RabbitMQ. Solo publican eventos de dominio usando el puerto `ApplicationEventPublisher` de Spring.
- Los **adaptadores de infraestructura** son los únicos que conocen RabbitMQ. Si mañana se cambia el broker por Kafka, solo hay que cambiar los adaptadores.

Esto permite que los **tests unitarios** funcionen sin RabbitMQ: los use cases se testean con mocks del `ApplicationEventPublisher`, verificando que se publican los eventos correctos sin necesidad de infraestructura.
