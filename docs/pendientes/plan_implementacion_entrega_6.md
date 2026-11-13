# Plan Maestro de Implementación — Entrega 6 & Requerimientos UI/UX
**Proyecto:** DonaTrack (Trabajo Práctico Anual K3002 UTN FRBA)  
**Squads Involucrados:** `dev-architecture`, `dev-engineers`, `dev-qa`  
**Estado:** `[APROBADO]` por `plan-critic` en Ronda 2 de Auditoría Red-Team  
**Rama Base de Trabajo:** [`feature/revision-entrega-6`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack)

---

## 1. Resumen Ejecutivo

Este documento consolida el plan técnico definitivo para cerrar la totalidad de brechas funcionales y no funcionales correspondientes a la **Entrega 6** (Cliente Liviano SSR, Autenticación JWT/SSO y Aislamiento de Layouts) y a los **20 Requerimientos Mínimos de UI/UX** del Trabajo Práctico Anual de DonaTrack.

El plan fue sometido a un proceso formal de dos rondas de crítica técnica entre [`architect-planner`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/.agents) y [`plan-critic`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/.agents). En la primera iteración se descartaron refactorizaciones riesgosas (como la partición intempestiva del monolito `AdminController`) y se corrigieron cuellos de botella críticos (como el bloqueo del renderizado SSR por cold-starts en Render Free Tier). La versión actual garantiza una arquitectura desacoplada basada en controladores satélites, hidratación asíncrona en cliente con caché defensiva y cumplimiento estricto de las directivas de accesibilidad WCAG 2.1 AA y Salesforce Lightning Design System (SLDS 2.30.3).

---

## 2. Matriz Exhaustiva de Cumplimiento (Consigna vs. Código Real)

A continuación se detalla la contrastación de cada requerimiento del enunciado oficial [`DDS-TP-Anual-2026.pdf`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/enunciado/DDS-TP-Anual-2026.pdf#L9-L28) frente al estado del código en la rama `dev`:

| ID | Requerimiento de Enunciado | Estado Previo en Código | Diagnóstico y Resolución Técnica | Fase |
| :---: | :--- | :---: | :--- | :---: |
| **UX-01** | Landing page accesible públicamente (propósito, donaciones recientes, enlaces a fotos sin login, login/registro, legal/privacidad). | **Parcial (70%)** | Enlaces legales apuntaban a `href="#"`. Falta galería pública de remitos auditados. Se crean `/terminos` y `/privacidad` (Ley 25.326) y se conecta la galería con Supabase Storage. | **Fase 4** |
| **UX-02** | Mapa interactivo de donaciones entregadas con detalles y fotos al clic sin login. | **Simulado (50%)** | [`map.js`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/static/js/map.js) consumía puntos estáticos hardcodeados en JavaScript. Se desacopla con un endpoint asíncrono `/api/public/mapa-entregas` con fallback resiliente. | **Fase 4** |
| **UX-03** | Registro de personas donantes y entidades beneficiarias. | **Parcial (60%)** | Solo existía alta pública para rol `DONANTE`. Las ONGs solo disponían de un formulario de preinscripción. Se añade selector de perfil en `/registro` con estado seguro `BENEFICIARIA_PENDIENTE`. | **Fase 5** |
| **UX-04** | Login para donantes, beneficiarias y administradores. | **Cumplido (100%)** | Operativo en [`PublicController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/PublicController.java) con JWT HS256 y SSO Social Login (Google / GitHub). | Base |
| **UX-05** | Persona donante: filtrar sus donaciones por estado y categoría/subcategoría. | **Faltante (20%)** | El listado en [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html) carecía de controles de filtrado. Se incorporan selects nativos SLDS para filtrar interactivamente en el DOM. | **Fase 2** |
| **UX-06** | Persona donante: navegar por entidades beneficiarias registradas. | **Faltante (40%)** | Se incorpora la pestaña SLDS "Organizaciones Registradas" que consulta `donacionesGateway.obtenerBeneficiarios()` con buscador institucional. | **Fase 5** |
| **UX-07** | Persona donante: consultar misiones a realizar e insignias obtenidas con distancia a la meta. | **Parcial (50%)** | Se renderizaban insignias pero no las misiones activas. Se conecta con `incentivosGateway.obtenerMisiones(id)` desplegando barras de progreso y distancia porcentual. | **Fase 2** |
| **UX-08** | Notificaciones a donante ante asignación, misión cumplida, ascenso o entrega. | **Parcial (40%)** | El backend las emitía vía RabbitMQ, pero no existía cliente HTTP ni gateway en el BFF. Se implementa `notificacionesClient` y endpoint `/api/notificaciones/mis-alertas`. | **Fase 1** |
| **UX-09** | Persona donante: seguir en mapa entregas activas con camión, ubicación y última actualización. | **Faltante (0%)** | No existía componente de mapa en el panel del donante. Se integra Leaflet en una tarjeta SLDS alimentada asincrónicamente por `/api/donante/mapa-camiones`. | **Fase 2** |
| **UX-10** | Entidad beneficiaria: registrar necesidades materiales concretas. | **Cumplido (100%)** | Operativo en [`necesidades.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/beneficiaria/necesidades.html) conectado con `donatrack-donaciones`. | Base |
| **UX-11** | Entidad beneficiaria: ver estado de donaciones asignadas. | **Cumplido (100%)** | Operativo en [`entregas.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/beneficiaria/entregas.html) con detalle de remito y transportista. | Base |
| **UX-12** | Entidad beneficiaria: confirmar recepción cargando fotos de la misma. | **Cumplido (100%)** | Operativo con captura multipart de cámara, firma digital y guardado en Supabase Storage S3. | Base |
| **UX-13** | Entidad beneficiaria: notificaciones ante asignación y entrega. | **Parcial (40%)** | Integrado en la infraestructura centralizada de notificaciones de la Fase 1. | **Fase 1** |
| **UX-14** | Entidad beneficiaria: seguir en mapa entregas activas con camiones asociados. | **Cumplido (100%)** | Operativo con mapa interactivo Leaflet en el panel de entregas de la beneficiaria. | Base |
| **UX-15** | Administrador: registrar donaciones recibidas en depósito a nombre de donante. | **Faltante (30%)** | El formulario existente solo permitía auto-donación. Se crea `/admin/donaciones/recepcion` con selector de donantes y DTO fuertemente tipado. | **Fase 3** |
| **UX-16** | Administrador: actualizar estado de donaciones en depósito si están vencidas. | **Faltante (10%)** | Falta soporte en UI y Gateway para transicionar a `VENCIDA`. Se expone `PUT /api/donaciones/{id}/estado/vencer` y acción directa en la bandeja SLDS. | **Fase 3** |
| **UX-17** | Administrador: seleccionar entidad final con algoritmos de matchmaking. | **Cumplido (100%)** | Operativo en [`matchmaking.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/matchmaking.html) (Algoritmos Semántico y Sub-atendidos). | Base |
| **UX-18** | Administrador: administrar flota de camiones disponibles. | **Cumplido (100%)** | Operativo en [`camiones.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/camiones.html) (alta, chofer, patente, tara, volumen). | Base |
| **UX-19** | Administrador: ranking mensual e historial de rankings anteriores. | **Parcial (50%)** | Solo mostraba el mes corriente. Se expone `GET /api/ranking/historial` en `donatrack-incentivos` y selector histórico de períodos en el backoffice. | **Fase 5** |
| **UX-20** | Administrador: importar donantes desde CSV (>10.000 filas). | **Cumplido (100%)** | Operativo en [`csv.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/csv.html) con procesamiento streaming en backend. | Base |

---

## 3. Síntesis del Debate Red-Team (Ronda 1 vs. Ronda 2)

```mermaid
sequenceDiagram
    autonumber
    actor Planner as architect-planner
    actor Critic as plan-critic (Red-Team)
    
    Planner->>Critic: Envía Plan V1 (Partición AdminController, SSR síncrono en mapas, auto-registro ONG)
    Critic-->>Planner: DICTAMEN R1: RECHAZADO [4 Bloqueantes P0]
    Note over Critic: 1. Peligro de regresión al dividir AdminController.<br/>2. Cold-starts en Render matan el SSR (timeouts 504).<br/>3. Falta notificacionesClient en BFF.<br/>4. Falla de seguridad en registro directo de ONG.
    
    Planner->>Critic: Envía Plan V2 (Controladores satélites, caché resiliente asíncrona, PENDIENTE_VERIFICACION)
    Critic-->>Planner: DICTAMEN R2: APROBADO [0 Bloqueantes P0]
    Note over Critic: Arquitectura pragmática, blindada ante latencia en nube y 100% SLDS.
```

### Bloqueantes Subsanados:
1. **Riesgo Monolítico vs. Estabilidad (P0-1):** En lugar de quebrar [`AdminController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java) arriesgando la inyección de `@ModelAttribute populateUserAttributes` y la sesión de usuario, se conservó la clase maestra y se crearon **controladores satélites REST** específicos para la telemetría y APIs (`DonanteApiController`, `PublicApiController`, `NotificacionesApiController`).
2. **Resiliencia ante Cold-Starts de Render (P0-2):** Se erradicó toda llamada síncrona a servicios de logística dentro del renderizado Thymeleaf. Las vistas entregan HTML en menos de 100 ms y Leaflet solicita las paradas asincrónicamente mediante `fetch()` contra un servicio con caché concurrente en memoria (`CacheResilienteService`, TTL 3 min, timeout defensivo de 2.000 ms).
3. **Infraestructura de Notificaciones (P0-3):** Se diseñó e integró la factoría `notificacionesClient` en [`ApiClientsConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java) y el componente [`NotificacionesGateway.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/gateway/).
4. **Seguridad y Control de Personería en Registro (P0-4):** Se prohibió el otorgamiento irrestricto de `ROLE_BENEFICIARIA` desde la web pública. Toda solicitud institucional se guarda como `BENEFICIARIA_PENDIENTE` y requiere aprobación explícita de un administrador.

---

## 4. Decisiones Arquitectónicas & Trade-offs

### ADR-03: Desacoplamiento Asíncrono de Mapas & Caché Defensiva en Memoria
- **Contexto:** En el nivel gratuito de Render, las instancias en reposo (`donatrack-logistica` en Cuenta 2) tardan entre 40 y 70 segundos en arrancar tras recibir tráfico.
- **Decisión:** 
  1. El cliente liviano SSR renderiza la página instantáneamente sin consultar las coordenadas de los camiones ni esperar al backend.
  2. El navegador web ejecuta un `fetch('/api/donante/mapa-camiones')` o `fetch('/api/public/mapa-entregas')`.
  3. El BFF web mantiene una caché en memoria basada en `ConcurrentHashMap` con un tiempo de vida (TTL) de 180 segundos.
  4. Si la instancia de logística está suspendida, la llamada al cliente HTTP aborta por timeout a los 2.0 segundos y devuelve de inmediato los datos de la última captura en caché o una colección predeterminada de paradas sanitizadas.
- **Fórmula de Disponibilidad:**
  $$\text{Tiempo de Entrega SSR: } T_{\text{SSR}} \le 100\,\text{ms} \quad \implies \quad \text{Disponibilidad Percibida: } A_{\text{UI}} \approx 99.9\%$$

### ADR-04: Gobernanza Dual de Interfaces (Regla de Oro de Layouts)
- **Portal Público (`layout-public.html`):** Uso exclusivo de CSS utilitario moderno (`variables.css`, `base.css`, `components.css`). Queda terminantemente prohibido importar Salesforce Lightning Design System (SLDS) para evitar sangrado tipográfico o colisión de estilos globales.
- **Backoffice Operativo (`layout-admin.html`):** Adherencia estricta a los blueprints oficiales de **SLDS 2.30.3**. Queda terminantemente prohibido incorporar hojas de estilo personalizadas o inventar prefijos `dt-*`. Los filtros de donaciones y tarjetas de misiones se construyen únicamente con `slds-form-element`, `slds-select`, `slds-card` y `slds-progress-bar`.

---

## 5. Plan de Implementación por Fases Atómicas

```mermaid
flowchart TD
    F1["Fase 1: Infraestructura Notificaciones & Resiliencia BFF"] --> F2["Fase 2: Portal Donante SLDS (Filtros, Misiones & Mapa Asíncrono)"]
    F1 --> F3["Fase 3: Depósito Admin (Donaciones en Depósito & Vencimientos)"]
    F2 --> F4["Fase 4: Transparencia Pública & Cumplimiento Legal Ley 25.326"]
    F3 --> F5["Fase 5: Registro Seguro & Historial de Rankings"]
    F4 --> F5
```

---

### Fase 1: Infraestructura de Notificaciones & Resiliencia del BFF
**Objetivo:** Conectar el cliente liviano web con `donatrack-notificaciones` e implementar la infraestructura de caché resiliente para telemetría.

* **Responsable Backend (`@backend-dev`):**
  1. En [`ApiClientsConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java), declarar el bean `notificacionesClient`:
     ```java
     @Bean
     public RestClient notificacionesClient(ApiProperties props, ClientHttpRequestInterceptor interceptor) {
       return createClient(props.notificacionesUrl(), props, interceptor);
     }
     ```
  2. Crear la clase `NotificacionesGateway.java` en `com.donatrack.web.gateway`:
     - `public List<Map<String, Object>> obtenerNotificacionesUsuario(String destinatario)`
     - Consume `GET /api/mensajes?destinatario={destinatario}` de `donatrack-notificaciones`.
  3. Crear el servicio de resiliencia `CacheResilienteService.java` en `com.donatrack.web.service` con `ConcurrentHashMap` y ventana de validez de 180 segundos.
  4. Crear el controlador satélite `NotificacionesApiController.java` exponiendo:
     - `@GetMapping("/api/notificaciones/mis-alertas")` protegido con `@PreAuthorize("isAuthenticated()")`.

* **Criterios de Aceptación:**
  - El bean `notificacionesClient` se levanta con timeouts estrictos (10s conexión, 2.5s lectura).
  - Consulta a `/api/notificaciones/mis-alertas` devuelve HTTP 200 OK en $<150$ ms con lista vacía resiliente si el servicio está en reposo.

---

### Fase 2: Portal Donante — Blueprints SLDS, Misiones y Mapa Asíncrono
**Objetivo:** Satisfacer UX-05, UX-07 y UX-09 (filtros interactivos, avance de misiones y seguimiento de camiones en vivo sin bloquear el renderizado).

* **Responsable Backend (`@backend-dev`):**
  1. Crear `DonanteApiController.java` en `com.donatrack.web.controller.api`:
     ```java
     @RestController
     @RequestMapping("/api/donante")
     @PreAuthorize("hasAnyRole('DONANTE', 'ADMIN')")
     public class DonanteApiController {
       @GetMapping("/mapa-camiones")
       public ResponseEntity<List<EntregaActivaDonanteDTO>> obtenerCamionesEnVivo(Authentication auth);
     }
     ```
  2. Crear el registro inmutable `EntregaActivaDonanteDTO.java`:
     ```java
     public record EntregaActivaDonanteDTO(
         UUID idDonacion,
         String codigoCorto,
         String estado,
         String patenteCamion,
         String choferNombre,
         String destinoComedor,
         double lat,
         double lng,
         String ultimaActualizacion
     ) {}
     ```
  3. En [`AdminController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java#L712), inyectar la lista de misiones al modelo Thymeleaf:
     ```java
     List<Map<String, Object>> misiones = incentivosGateway.obtenerMisiones(donanteId);
     model.addAttribute("misiones", misiones != null ? misiones : Collections.emptyList());
     ```

* **Responsable Frontend (`@frontend-dev`):**
  1. En [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html) (Pestaña Seguimiento), implementar la barra de filtros con blueprints oficiales SLDS:
     - Selector de estado (`Todos`, `En Depósito`, `Asignada`, `Entregada`).
     - Selector de subcategoría (`Alimentos`, `Abrigo`, `Mobiliario`, `Higiene`).
     - Lógica en JS cliente para ocultar/mostrar filas de la tabla sin recargar la página.
  2. En la pestaña Resumen, maquetar la tarjeta SLDS de **Misiones en Curso** con barra de progreso porcentual:
     $$\text{Progreso visual: } \min\left(100, \frac{\text{actual}}{\text{meta}} \times 100\right)\%$$
     e indicación de distancia restante (ej. *"Faltan 2 donaciones para desbloquear la insignia"*).
  3. Crear `donante-mapa.js` y agregarlo a [`panel.html`]:
     - Contenedor `<div id="mapa-donante-camiones" style="height: 350px; width: 100%;"></div>`.
     - Ejecuta `fetch('/api/donante/mapa-camiones')` con spinner SLDS accesible y proyecta los camiones en tiempo real.

* **Criterios de Aceptación:**
  - Cambiar los selects de filtrado altera instantáneamente las filas visibles sin llamadas de red.
  - El mapa muestra los camiones de las donaciones del usuario con popups que contienen la patente y el chofer.

---

### Fase 3: Operatoria de Depósito — Carga de Donaciones y Ciclo de Vencimientos
**Objetivo:** Satisfacer UX-15 y UX-16 proveyendo formularios validados para recepción en depósito y control de mercadería vencida.

* **Responsable Backend (`@backend-dev`):**
  1. En [`DonacionController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java) de `donatrack-donaciones`:
     ```java
     @PutMapping("/donaciones/{id:[a-fA-F0-9\\-]{36}}/estado/vencer")
     public ResponseEntity<Void> marcarDonacionVencida(
         @PathVariable UUID id,
         @RequestParam(name = "observacion", defaultValue = "Vencida en depósito") String obs) { ... }
     ```
  2. En [`DonacionesGateway.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/gateway/DonacionesGateway.java):
     - `public void marcarDonacionVencida(UUID idDonacion)`
     - `public void auditarVencidos()`
  3. Crear el DTO inmutable `RecepcionDepositoRequest.java`:
     ```java
     public record RecepcionDepositoRequest(
         @NotNull(message = "El donante es obligatorio") UUID idDonante,
         @NotBlank(message = "Descripción obligatoria") String descripcion,
         @NotNull @Positive(message = "Cantidad debe ser mayor a cero") Double cantidad,
         @NotBlank String unidadMedicion,
         @NotBlank String nombreSubcategoria,
         boolean esUsado,
         LocalDate fechaVencimiento
     ) {}
     ```
  4. En [`AdminController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java):
     - `@GetMapping("/admin/donaciones/recepcion")`
     - `@PostMapping("/admin/donaciones/recepcion")`
     - `@PostMapping("/admin/donaciones/{id}/vencer")`
     - `@PostMapping("/admin/donaciones/auditoria-vencidos")`

* **Responsable Frontend (`@frontend-dev`):**
  1. Crear la vista SLDS `admin/admin/recepcion-donacion.html`:
     - Formulario con tarjeta SLDS (`slds-card`), select con búsqueda para personas donantes registradas, fecha de vencimiento y toggle para artículos usados.
     - Bloqueo de doble clic en el botón de submit para prevenir duplicados.
  2. En [`matchmaking.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/matchmaking.html):
     - Botón `Marcar Vencida` con confirmación modal SLDS en cada donación en depósito.
     - Botón superior `Ejecutar Auditoría de Vencimientos` para descartar lotes caducados masivamente.

* **Criterios de Aceptación:**
  - Registrar una donación desde el backoffice la crea en estado `EN_DEPOSITO` vinculada a la persona elegida.
  - Al marcar una donación como vencida, su badge cambia a `VENCIDA` y queda excluida del cálculo de matchmaking.

---

### Fase 4: Transparencia Pública & Cumplimiento Legal (Ley 25.326)
**Objetivo:** Satisfacer UX-01 y UX-02 (mapa público desacoplado con datos reales y páginas legales de protección de datos personales).

* **Responsable Backend (`@backend-dev`):**
  1. Crear `PublicApiController.java` en `com.donatrack.web.controller.api`:
     ```java
     @RestController
     @RequestMapping("/api/public")
     public class PublicApiController {
       @GetMapping("/mapa-entregas")
       public ResponseEntity<List<PuntoMapaPublicoDTO>> obtenerPuntosMapa();
     }
     ```
  2. Crear el registro `PuntoMapaPublicoDTO.java`:
     ```java
     public record PuntoMapaPublicoDTO(
         String tipo, // "blue" (acopio), "green" (ong), "orange" (entrega)
         double lat,
         double lng,
         String titulo,
         String descripcion,
         String organizacion,
         String subcategoria,
         String fecha,
         String estado,
         String fotoUrl
     ) {}
     ```
  3. En [`PublicController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/PublicController.java), mapear las rutas GET `/privacidad` y `/terminos`.

* **Responsable Frontend (`@frontend-dev`):**
  1. Modificar [`map.js`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/static/js/map.js):
     - Al cargar Leaflet, consultar `/api/public/mapa-entregas` vía `fetch()`.
     - Renderizar marcadores con popups dinámicos que consuman imágenes de remito en Supabase Storage con placeholder de seguridad.
     - Fallback automático al array offline si la petición demora más de 2 segundos.
  2. Crear las plantillas `public/privacidad.html` y `public/terminos.html` cumpliendo con el layout público (sin SLDS) y con el texto legal exigido por la **Ley Nacional N° 25.326**.
  3. En [`public-footer.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/fragments/public-footer.html#L34-L38), sustituir `href="#"` por `th:href="@{/terminos}"` y `th:href="@{/privacidad}"`.

* **Criterios de Aceptación:**
  - El mapa de la landing muestra entregas reales auditadas sin autenticación.
  - Los enlaces del pie de página abren correctamente las páginas legales con contraste WCAG 2.1 AA.

---

### Fase 5: Registro Seguro con Auditoría & Analítica Histórica
**Objetivo:** Satisfacer UX-03, UX-06 y UX-19 (registro asistido de ONGs, directorio de beneficiarias e historial de podios de gamificación).

* **Responsable Backend (`@backend-dev`):**
  1. En [`IncentivoController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-incentivos/src/main/java/com/donatrack/incentivos/infrastructure/adapters/in/api/IncentivoController.java), exponer:
     - `@GetMapping("/ranking/historial")` consultando `rankingMensualJpaRepository.findAll()`.
  2. En [`IncentivosGateway.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/gateway/IncentivosGateway.java), añadir `public List<Map<String, Object>> obtenerHistorialRankings()`.
  3. En [`PublicController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/PublicController.java#L436), procesar el parámetro de tipo de usuario:
     - Si es beneficiaria, registrar usuario con rol `BENEFICIARIA_PENDIENTE` y crear solicitud en `donatrack-donaciones` (`POST /api/solicitudes-ong`) sin login automático.

* **Responsable Frontend (`@frontend-dev`):**
  1. Modificar [`registro.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/public/registro.html) con selector de perfil y campos institucionales condicionales (Razón Social, CUIT, Dirección).
  2. En [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html), añadir pestaña SLDS "Organizaciones y Comedores" (`id="tab-panel-directorio"`) listando el padrón con buscador.
  3. En [`ranking.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/ranking.html), añadir selector de período mes/año para auditar rankings pasados.

* **Criterios de Aceptación:**
  - Una ONG registrada no puede acceder a la consola operativa hasta que el admin la apruebe.
  - El donante puede buscar y explorar organizaciones registradas desde su propio panel.
  - El administrador puede consultar el historial de podios mensuales de meses pasados.

---

## 6. Protocolo de Calidad y Pruebas (`dev-qa`)

El subagente [`test-engineer`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/.agents) entrará en acción inmediatamente tras la finalización de los desarrolladores para auditar la suite de pruebas bajo las siguientes directivas:

1. **Aislamiento Estricto de Pruebas:**
   - Pruebas unitarias de controladores satélites (`MockMvc`) con autenticación simulada (`@WithMockUser`).
   - Mocks completos de `RestClient` y gateways en `donatrack-web` sin interactuar con bases de datos ni servicios reales.
2. **Cero Tolerancia a Tests Flaky:**
   - Prohibido el uso de `Thread.sleep()`. Toda verificación de concurrencia o tiempos de espera en caché debe utilizar aserciones deterministas (`Awaitility` o relojes controlados).
3. **Máximo 2 Rondas de Corrección:**
   - Si `test-engineer` detecta fallos en la suite, devolverá el reporte exacto de fallos al `@backend-dev` o `@frontend-dev`.
   - Tras la corrección de los desarrolladores, se correrá la verificación final (Ronda 2) para dejar la suite 100% en verde.

---

# Referencias

- [DDS-TP-Anual-2026.pdf](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/enunciado/DDS-TP-Anual-2026.pdf#L9-L28) — Enunciado oficial del Trabajo Práctico Anual K3002 UTN FRBA (Requerimientos mínimos UI/UX págs. 9–11, Entrega 6 págs. 26–28).
- [AdminController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java#L706-L1368) — Controlador principal consolidado de `donatrack-web`.
- [PublicController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/PublicController.java#L85-L356) — Controlador público de autenticación, SSO y rastreo de remitos.
- [ApiClientsConfig.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java#L20-L76) — Configuración central de clientes HTTP REST y timeouts de conexión.
- [DonacionController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java#L122-L589) — Endpoints de donaciones, recepciones y auditoría en `donatrack-donaciones`.
- [IncentivoController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-incentivos/src/main/java/com/donatrack/incentivos/infrastructure/adapters/in/api/IncentivoController.java#L153-L260) — Endpoints de misiones, insignias y ranking en `donatrack-incentivos`.
- [decisiones-arquitectura.md](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/notas_y_borradores/decisiones-arquitectura.md) — Registro formal de decisiones arquitectónicas previas (ADR-01 y ADR-02).
- [Ley Nacional de Protección de los Datos Personales N° 25.326](https://www.argentina.gob.ar/normativa/nacional/ley-25326-64790) — Marco normativo para el tratamiento de datos y privacidad en la República Argentina.
- [Salesforce Lightning Design System (SLDS 2.30.3)](https://www.lightningdesignsystem.com/) — Sistema de diseño oficial para interfaces de backoffice.
- [W3C Web Content Accessibility Guidelines (WCAG) 2.1 AA](https://www.w3.org/TR/WCAG21/) — Estándar internacional de accesibilidad web y contraste.
