# Plan Maestro de Implementación (Versión 2) · [APROBADO]
## Optimización de Latencia, Navegación Instantánea y Feedback Visual SLDS en DonaTrack Web

---

### Resumen Ejecutivo
Tras completar el ciclo estricto de revisión y auditoría Red-Team entre `@architect-planner` y `@plan-critic` (Squad `dev-architecture`), este documento representa la **Versión 2 Definitiva y Aprobada** del plan de implementación para erradicar las causas de latencia y congelamiento de navegación en **DonaTrack Web**, incorporando a la vez un sistema de feedback visual profesional alineado con **Salesforce Lightning Design System (SLDS v2.30.3)**.

El plan resuelve los tres bloqueantes críticos identificados:
1. **Propagación del `SecurityContextHolder`** en hilos asíncronos mediante un bean `DelegatingSecurityContextAsyncTaskExecutor` para evitar errores `401 Unauthorized` hacia los microservicios y evitar la saturación de `ForkJoinPool.commonPool()`.
2. **Persistencia visual de perfil mediante `HttpSession`**, desacoplando el `@ModelAttribute` global de llamadas de red sin perder los cambios editados por el usuario.
3. **Resiliencia individual por microservicio** con `.exceptionally()` antes del `CompletableFuture.allOf().join()`, impidiendo que la caída de un servicio secundario derribe la página completa con error HTTP 500.
4. **Navegación instantánea en cliente ($<10\,\text{ms}$)** interceptando clics de pestañas en `layout-admin.html` cuando el usuario ya se encuentra en `/app/donante` y sincronizando el historial del navegador mediante `pushState` y `popstate`.
5. **Lazy Mounting de Leaflet**, postergando la inicialización del mapa y su consulta de camiones hasta que el usuario acceda de forma explícita a la pestaña "Mis Donaciones (Seguimiento)".
6. **Feedback visual nativo SLDS** con una Top Progress Bar superior (`slds-progress-bar`), Skeleton Loaders utilitarios (`.slds-box.slds-theme_shade`) y bloqueo defensivo contra doble envío de formularios con `slds-spinner_brand`.

---

## 1. Detección de Stack & Contexto Arquitectónico
- **Lenguaje y Runtime**: Java 21 LTS (OpenJDK 64-Bit).
- **Framework Backend**: Spring Boot 3.5.x, Spring MVC, Spring Security 6.x con OAuth2 Resource Server & JWT Nimbus.
- **Motor de Renderizado**: Thymeleaf 3 con dialecto Spring Security (`spring.thymeleaf.cache: ${SPRING_THYMELEAF_CACHE:true}`).
- **Frontend Core**: Vanilla JavaScript moderno modular (ES6+), Leaflet 1.9.4 para cartografía OSM y **Salesforce Lightning Design System (SLDS v2.30.3)** como estándar oficial de diseño sin dependencias CSS externas.
- **Topología**: Monorepo Maven con frontend SSR / BFF (`donatrack-web`) comunicándose con 5 microservicios (`donatrack-auth`, `donatrack-donaciones`, `donatrack-incentivos`, `donatrack-logistica`, `donatrack-notificaciones`).

---

## 2. Métricas Objetivo y Speedup de Rendimiento

- **Alternancia de Pestañas en Cliente**: $T_{\text{tab}} < 10\,\text{ms}$ (intercambio instantáneo en memoria sobre el DOM ya renderizado).
- **Carga Inicial SSR (BFF)**: $T_{\text{ssr}} < 400\,\text{ms}$ en red local / pruebas.
- **Fórmula de Concurrencia con Fallbacks Defensivos**:
  $$T_{\text{secuencial}} = T_{\text{auth\_sync}} + \sum_{i=1}^{k} T_{\text{gateway}_i} \approx 200\,\text{ms} + 5 \times 150\,\text{ms} = 950\,\text{ms}$$
  $$T_{\text{paralelo}} = \max_{i \in [1..k]}(T_{\text{gateway}_i}) + \epsilon_{\text{pool}} \approx 160\,\text{ms} + 5\,\text{ms} = 165\,\text{ms}$$
  $$\text{Speedup } S = \frac{T_{\text{secuencial}}}{T_{\text{paralelo}}} \approx \frac{950}{165} \approx 5.75\times$$

---

## 3. Matriz de Mitigación de Hallazgos Red-Team (Ronda 1 $\to$ Ronda 2)

| ID | Hallazgo en Ronda 1 | Solución Arquitectónica en Plan V2 | Estado |
| :--- | :--- | :--- | :---: |
| **P0.1** | Pérdida de JWT en `bearerTokenInterceptor` por ejecución en `ForkJoinPool.commonPool()` e I/O starvation. | Se introduce el bean `@Bean(name = "securityTaskExecutor")` utilizando `DelegatingSecurityContextAsyncTaskExecutor` sobre un `ThreadPoolTaskExecutor` acotado (8/16 hilos, cola de 100) en [ApiClientsConfig.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java). Inyección con `@Qualifier` en [AdminController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java). | **RESUELTO** |
| **P0.2** | Regresión funcional en perfil: Al desacoplar `@ModelAttribute`, la inmutabilidad del token JWT provocaba que la edición no se reflejara en la UI. | Implementación de capa intermedia en `HttpSession`. `@ModelAttribute` consulta primero la sesión; `actualizarPerfil` persiste en `authGateway` y refresca inmediatamente `session.setAttribute(...)`. Cero consultas HTTP en navegación regular y persistencia visual inmediata. | **RESUELTO** |
| **P0.3** | Caída en cascada de `/app/donante` por excepción en `CompletableFuture.allOf().join()` ante falla de 1 microservicio. | Encadenamiento de `.exceptionally(...)` individual con fallbacks seguros (`emptyList`, `emptyMap`, defaults calculados) previo al `.allOf().orTimeout(3, TimeUnit.SECONDS).join()`. Degradación graceful garantizada. | **RESUELTO** |
| **P1.1** | Riesgo de romper el aislamiento de diseño SLDS con skeleton loaders CSS inventados. | Se utilizan componentes nativos oficiales de SLDS v2.30.3: `slds-progress-bar slds-progress-bar_x-small` para loader superior, `slds-spinner_container` con `.slds-spinner_brand`, y utilidades `.slds-box.slds-theme_shade` para placeholders. | **RESUELTO** |
| **P1.2** | Desincronización del historial del navegador (`popstate`). | Se suscribe un listener global a `window.addEventListener('popstate', ...)` que extrae el parámetro `tab` de la URL y conmute la vista sin recarga de página. | **RESUELTO** |
| **P1.3** | Desalineación de Leaflet y llamadas innecesarias al inicio. | Lazy Mounting con flag `mapaDonanteInicializado = false`. El mapa solo se monta cuando `#tab-panel-seguimiento` es visible, y en cambios posteriores solo llama a `mapInstance.invalidateSize()`. | **RESUELTO** |
| **P1.4** | Riesgo de doble submit en formularios críticos. | Utilidad unificada `DonaTrackForms.registrarProteccion(form, submitBtn)` aplicada a recepción de donaciones, modal de perfil y formulario de donaciones del donante. | **RESUELTO** |
| **P1.5** | Inflexibilidad de hot-reload con Thymeleaf cache hardcodeado. | `spring.thymeleaf.cache: ${SPRING_THYMELEAF_CACHE:true}` en [application.yml](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/application.yml). | **RESUELTO** |
| **P2.1** | Múltiples instancias de `HttpClient` en `ApiClientsConfig`. | `@Bean public HttpClient sharedHttpClient()` compartido entre los 5 clientes REST. | **RESUELTO** |
| **P2.2** | Detección incorrecta del tab activo en sidebar con query params. | Evaluación canónica con `window.location.pathname + window.location.search`. | **RESUELTO** |
| **P2.3** | Convención de commits en español. | Obligatorio `tipo(ámbito): descripción en español` (ej. `feat(web): implementar lazy mounting y navegacion instantanea`). | **RESUELTO** |

---

## 4. Fases de Implementación Atómica

### FASE 1: Concurrencia Segura y Resiliencia en Backend
- **Archivos**:
  - [`ApiClientsConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java)
  - [`AdminController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java)
  - [`application.yml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/application.yml)
- **Implementaciones Clave**:
  1. Configuración de `sharedHttpClient` con `connectTimeout` de 2000ms y `readTimeout` de 3000ms.
  2. Creación del bean `securityTaskExecutor` como `DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor)`.
  3. Desacoplamiento de `@ModelAttribute populateUserAttributes` mediante lectura directa de claims Nimbus en memoria y consulta de respaldo en `HttpSession`. Al actualizar el perfil en `/perfil/actualizar`, se escribe en `authGateway` y se actualiza `session.setAttribute("perfil_descripcion", ...)` y `session.setAttribute("perfil_ubicacion", ...)`.
  4. Ejecución en paralelo con `CompletableFuture.supplyAsync(..., securityTaskExecutor)` en `adminResumen` (3 llamadas) y en `donantePanel` (5 llamadas), cada una protegida con su handler `.exceptionally(...)` individual que retorna listas o mapas vacíos para evitar la caída de la página ante fallos secundarios.

---

### FASE 2: Navegación Instantánea Frontend ($<10\,\text{ms}$)
- **Archivos**:
  - [`layout-admin.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/layout/layout-admin.html)
  - [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html)
  - [`donante-mapa.js`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/static/js/donante-mapa.js)
- **Implementaciones Clave**:
  1. Intercepción de clics en `#admin-nav`: si la página actual es `/app/donante` y el enlace apunta a `/app/donante?tab=...`, se ejecuta `e.preventDefault()`, se llama a `cambiarTabDonante(targetTab, true)` en memoria y se actualiza `history.pushState`.
  2. Listener de `popstate`: al pulsar los botones Atrás o Adelante del navegador, se detecta el parámetro `tab` de la URL y se conmuta el DOM localmente sin recargar la página.
  3. Evaluación canónica del menú lateral comparando `pathname + search` para resaltar la pestaña correcta con `slds-is-active`.
  4. Lazy Mounting de Leaflet en `donante-mapa.js`: bandera `mapaDonanteInicializado = false`. No se ejecutan `cargarCamiones()` ni la inicialización de Leaflet si la pestaña `#tab-panel-seguimiento` no está activa. Al seleccionarla, se monta una sola vez y en visitas subsiguientes se llama a `mapInstance.invalidateSize()`.

---

### FASE 3: Feedback Visual Profesional SLDS (v2.30.3)
- **Archivos**:
  - [`layout-admin.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/layout/layout-admin.html)
  - [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html)
  - [`recepcion-donacion.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/recepcion-donacion.html)
  - [`donaciones.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/donaciones.html)
- **Implementaciones Clave**:
  1. Top Progress Bar (`DonaTrackLoader`): elemento superior delgado con `slds-progress-bar slds-progress-bar_x-small` y `role="progressbar"`. Se activa automáticamente al navegar entre páginas completas.
  2. Skeleton Loaders utilitarios SLDS: maquetados con `.slds-card.slds-box.slds-theme_shade` sin CSS inventado.
  3. Spinner modal SLDS y protección de formularios (`DonaTrackForms`): al enviar formularios críticos de recepción, perfil o donación, se valida el formulario, se inhabilita el botón con `disabled = true` y `slds-is-disabled`, y se despliega el contenedor `slds-spinner_container` con `.slds-spinner_brand`.

---

### FASE 4: QA, Tests Unitarios y Regresión
- **Archivos**:
  - [`AdminControllerTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/test/java/com/donatrack/web/AdminControllerTest.java)
  - [`DonanteFlowTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/test/java/com/donatrack/web/DonanteFlowTest.java)
- **Implementaciones Clave**:
  1. Test MockMvc verificando que las peticiones a `/app/donante` y `/app/admin` no invocan `authGateway.obtenerPerfil` (`verifyNoInteractions(authGateway)`).
  2. Test de persistencia de perfil verificando que `/app/perfil/actualizar` actualiza los atributos en `MockHttpSession`.
  3. Test de resiliencia en `DonanteFlowTest` simulando excepciones en gateways secundarios y comprobando respuesta HTTP 200 con la plantilla renderizada.
  4. Verificación de accesibilidad WCAG 2.1 AA (`role="tab"`, `aria-selected`, `aria-controls`, `role="status"`).
  5. Ejecución completa de la suite Maven: `mvn test -pl donatrack-web` verificando que los 112 tests existentes más los nuevos finalicen al 100% en verde.

---

# Referencias
- [Salesforce Lightning Design System v2.30.3 - Component Blueprints](https://www.lightningdesignsystem.com/)
- [Spring Security Documentation - DelegatingSecurityContextAsyncTaskExecutor](https://docs.spring.io/spring-security/reference/features/integrations/concurrency.html)
- [Java 21 CompletableFuture & Exception Handling Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- [AdminController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java)
- [ApiClientsConfig.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java)
- [layout-admin.html](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/layout/layout-admin.html)
- [panel.html](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html)
- [donante-mapa.js](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/static/js/donante-mapa.js)

