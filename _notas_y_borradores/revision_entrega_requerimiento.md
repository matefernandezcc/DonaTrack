# Revisión de Requerimientos - Entrega 1

Este documento contiene el seguimiento de la revisión de los requerimientos de la primera entrega. 
Cada requerimiento será analizado para verificar su correcta implementación en el código, documentando el resultado, las evidencias y las correcciones necesarias si las hubiera.

## Estado de la Revisión

### Requerimiento 1: Separación de donaciones y responsabilidad de segmentación

**Descripción:** Que tengan claramente separada las donaciones originales y las donaciones segmentadas y que la segmentación sea una responsabilidad de la donación o una fachada (entrega 1).

**Estado:** ✅ **Cumplido**

**Evidencia / Análisis:**
- **Separación de conceptos:** Existe la clase `DonacionOriginal` que representa la donación global tal como ingresa, y la clase `Donacion` que representa las fracciones ya segmentadas (por ejemplo, bienes de una misma subcategoría). Queda evidenciado en que `DonacionOriginal` contiene un atributo `List<Donacion> donacionesSegmentadas`.
- **Responsabilidad de segmentación:** La clase `DonacionOriginal` implementa el método `segmentarBienes(...)`, el cual delega la lógica compleja a una fachada/caso de uso (`ProcesadorCargaInicial`). Además, se puede observar en el paquete `segmentador` que se utiliza un patrón *Strategy* (`SegmentarPorSubcategoria`, `SegmentarPorEstado`, etc.) para resolver las distintas formas de segmentación. Esto satisface plenamente la consigna de que recaiga en la donación o en una fachada.

**Correcciones necesarias:** Ninguna. Todo en orden.

---
### Requerimiento 2: Definición de Necesidades Recurrentes

**Descripción:** Que tengan bien definidas las cuestiones asociadas a necesidades recurrentes (entrega 1).

**Estado:** ✅ **Cumplido**

**Evidencia / Análisis:**
- **Modelo de dominio sólido:** La recurrencia está correctamente abstraída. Existe la clase `NecesidadRecurrente` (hereda de `Necesidad`) que define la configuración global (`cantidadObjetivo`, `tipoPeriodo`, estado `activa`).
- **Manejo por Períodos:** Se delegó el seguimiento temporal a la clase `PeriodoNecesidad`. Cada período tiene su propia `fechaInicio`, `fechaFin`, su estado (`ABIERTA`, `SATISFECHA`) y mantiene su propia lista de `donacionesAsignadas`.
- **Gestión del ciclo de vida:** `NecesidadRecurrente` mantiene punteros claros: un `periodoActual` y una lista de `historialPeriodos`. Dispone del método `cerrarPeriodoYCrearSiguiente()` para avanzar el ciclo sin perder historial.
- **Cálculo de progreso:** Es `PeriodoNecesidad` quien encapsula la lógica para saber si las donaciones acumuladas alcanzan el objetivo (`cantidadAcumulada()`, `estaCubierta()`).
- **Renovación automática:** Se observa la presencia de un adaptador `RenovacionPeriodosJob` que se encarga de disparar la transición de los períodos automáticamente.

**Correcciones necesarias:** Ninguna. Muy buen diseño orientado a objetos.

---
### Requerimiento 3: Importación Masiva de Donantes en CSV

**Descripción:** Que funcione la importación masiva de donantes en CSV (entrega 1).

**Estado:** ✅ **Cumplido**

**Evidencia / Análisis:**
- **Implementación dedicada:** Existe la clase `ImportadorCSV` (dentro del paquete `roles.strategyAdministrador.importador`) que implementa la lógica principal.
- **Procesamiento de datos:** El importador lee el archivo en memoria, descarta el encabezado y procesa línea por línea.
- **Manejo inteligente de duplicados:** Utiliza el email como clave de identificación (`buscarPorEmail`). Si el donante ya existe, se invoca a `actualizarInformacion()` para no duplicar datos; caso contrario, se crea utilizando `PersonaFactory.crearDesdeCSV()`.
- **Demostración de funcionamiento:** Se incluye la clase de pruebas `ImportadorCSVTest` con casos de uso completos: inserción de datos nuevos (PersonaHumana y PersonaJuridica), actualización de datos existentes y manejo de archivos nulos o vacíos.

**Correcciones necesarias:** Ninguna. Funcionalidad completa y testeada.

---
### Requerimiento 4: Proceso de asignación de donaciones (Matchmaking)

**Descripción:** Que tengan un proceso de asignación de las donaciones a entidades beneficiarias y que utilicen strategy o similar (entrega 2).

**Estado:** ✅ **Cumplido**

**Evidencia / Análisis:**
- **Uso de Patrón Strategy:** Se constató el uso riguroso del patrón Strategy para el proceso de asignación. El sistema cuenta con la interfaz `AlgoritmoAsignacion` en el paquete `strategyAdministrador.asignador`, la cual define el contrato `recomendarNecesidades(...)`.
- **Múltiples algoritmos:** Existen varias implementaciones concretas, tales como `CompatibilidadSemantica` y `PrioridadASubAtendidos`, permitiendo que el criterio de asignación pueda variar dinámicamente.
- **Clase Contexto:** La clase `AsignadorDonaciones` opera como el contexto del patrón (recibiendo la estrategia inyectada en su constructor) y exponiendo el método `ejecutarMatchmaking(...)`.
- **Orquestación:** Se pudo verificar la existencia de un `MatchmakerService` y un `AsignacionBatchJob` que se encargan de ejecutar de manera masiva este proceso para enlazar efectivamente las donaciones con los beneficiarios.

**Correcciones necesarias:** Ninguna. Diseño excelente.

---
### Requerimiento 5: Endpoints del Servicio de Donaciones

**Descripción:** Que esté correcta la exposición de las operaciones del servicio de donaciones (entrega 2).

**Estado:** ✅ **Cumplido**

**Evidencia / Análisis:**
- **Controlador REST:** El archivo `DonacionController.java` maneja correctamente la capa de presentación.
- **Endpoints Semánticos:** Se observa un excelente uso de los verbos HTTP (GET, POST, PUT, DELETE) para acciones bien definidas:
  - `POST /api/recepciones` para cargar la donación bruta inicial.
  - `PUT /api/donaciones/{id}/estado/...` para transiciones de estado puntuales (en depósito, asignada, entregada).
  - `GET /api/donaciones/{id}/matchmaking` para solicitar sugerencias.
  - `POST /api/donaciones/asignacion-batch` y `/auditoria/vencidos` para disparar procesos masivos/batch.
- **Patrón DTO:** Las operaciones no acoplan la base de datos o el dominio directamente a la respuesta web, sino que intermedian la información a través de DTOs (`CargaBienesRequestDTO`, `DonacionResponseDTO`, etc.).
- **Documentación Viva:** Se aplican decoradores de Swagger/OpenAPI (`@Operation`, `@ApiResponse`, `@Tag`) en cada endpoint, lo que permite que la documentación de la API se autogenere correctamente.

**Correcciones necesarias:** Ninguna. Cumple con los estándares de diseño de APIs.

---
### Requerimiento 6: Pérdida de misiones por inactividad (Racha)

**Descripción:** Que manejen la pérdida de misiones. Por ejemplo "racha" se pierde si no realiza ninguna donación durante un mes completo (entrega 2).

**Estado:** ✅ **Cumplido** *(bug corregido)*

**Evidencia / Análisis:**
- **Detección de inactividad:** Se implementó la detección automática mediante un Cron Job diario (`VerificarInactividadJob`) que ejecuta `procesarInactividad(...)` en cada `PerfilDonante`. La condición es: si pasan **más de 30 días** desde la última donación, se guarda `fechaCorteRacha` en `MetricasDonante`.
- **Bug detectado y corregido:** La evaluación de `MESES_CONSECUTIVOS` en `Mision.java` usaba `obtenerTodasLasDonaciones()`, tomando el historial completo y nunca consultando `fechaCorteRacha`. Esto podía hacer que la misión devuelva racha intacta aunque el Job ya hubiera detectado el corte.
- **Corrección aplicada:**
  1. Se agregó el método `obtenerDonacionesDesdeCorte()` en `MetricasDonante`, que filtra y descarta toda donación anterior a `fechaCorteRacha` (si no es null, retorna el historial completo).
  2. Se actualizó el `case MESES_CONSECUTIVOS` en `Mision.java` para usar ese nuevo método.
- **Verificación:** Se corrieron los 33 tests del módulo `donatrack-incentivos` → `BUILD SUCCESS`, 0 fallos.

**Correcciones necesarias:** Ninguna. Bug resuelto.

---
### Requerimiento 7: Integración con Redes Sociales vía N8N

**Descripción:** Que ejecuten la integración con redes sociales vía N8N (entrega 2).

**Estado:** ⚠️ **Cumplido Parcialmente (Requiere Atención)**

**Evidencia / Análisis:**

**Lado Java (Productor del evento):** El módulo `donatrack-incentivos` implementa el adaptador `N8nDifusionAdapter`, que escucha el evento interno `InsigniaObtenidaEvent` (mediante `@EventListener`) y hace un HTTP POST al webhook de N8N con el nombre de la insignia y el ID del donante. El URL del webhook es configurable mediante la propiedad `n8n.webhook.url` y tiene tolerancia a fallos (no rompe el flujo si N8N no responde).

**Workflows N8N implementados:**
1. **DonaTrack - Insignias** (Webhook → Discord): Recibe el evento Java, formatea el mensaje y publica en un Discord Webhook usando la URL configurada en `$env.DISCORD_WEBHOOK_URL`. Este workflow **está completo y correctamente encadenado**.
2. **DonaTrack - Ranking** (Scheduler → Discord): Se dispara automáticamente, calcula el ranking y formatea un mensaje con podio (🥇🥈🥉). Sin embargo, tiene **dos puntos débiles**: (a) usa **datos mockeados** como fuente en lugar de llamar a la API real del servicio de incentivos, y (b) el nodo de publicación final también apunta a `https://httpbin.org/post` (un endpoint de prueba) en vez del webhook real de Discord.
3. **Chatbot**: Workflow adicional que responde consultas por webhook, extra valorable.

**Correcciones necesarias:**
1. En el workflow **DonaTrack - Ranking**, reemplazar el nodo `Mock Data Source` por una llamada HTTP real a la API del servicio (`/incentivos/ranking` o similar).
2. En el mismo workflow, el nodo `Publish to Discord Webhook` debe usar la variable `$env.DISCORD_WEBHOOK_URL` (consistente con el de Insignias) en lugar de `httpbin.org/post`.

---
*(Aquí se irán agregando los próximos requerimientos evaluados)*
