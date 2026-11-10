# 📋 Lista Maestra Consolidada de Tareas Pendientes — DonaTrack

**Proyecto:** DonaTrack (Trabajo Práctico Anual K3002 — UTN FRBA)  
**Fecha de Consolidación:** 10 de Noviembre de 2026  
**Rama Base:** [`feature/revision-entrega-6`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack)  
**Documentos Fuente Auditados:**
1. [`audit-report-documentation-donatrack.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/audit-report-documentation-donatrack.md)
2. [`AUDITORIA_SEGURIDAD.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/AUDITORIA_SEGURIDAD.md)
3. [`plan_implementacion_entrega_6.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/plan_implementacion_entrega_6.md)

---

## 1. Resumen Ejecutivo y Estado General de la Auditoría Cruzada

Se realizó una auditoría cruzada exhaustiva entre los tres informes de pendientes y el código fuente real del proyecto:

1. **Estado de la Entrega 6 (UI/UX y SSR)**: La totalidad de la lógica de negocio y controladores satélites de las 5 fases planificadas en [`plan_implementacion_entrega_6.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/plan_implementacion_entrega_6.md) se encuentra **implementada en código** y con **284 tests unitarios y de integración pasando en verde**. Restan exclusivamente validaciones funcionales E2E en navegador, auditoría de accesibilidad WCAG y pruebas de resiliencia frente a cold-starts en Render.
2. **Estado de la Documentación**: Se aplicaron todos los *Quick Wins* en el repositorio (`SERVICIOS.md`, corrección de comandos Make, persistencia MongoDB/Postgres en `README.md`, contratos de eventos RabbitMQ, normalización de la colección Bruno). Resta la actualización del modelo PlantUML oficial y la definición sobre los archivos de arquitectura (ADRs) en `docs/notas_y_borradores/`.
3. **Estado de Seguridad (SecOps)**: El reporte [`AUDITORIA_SEGURIDAD.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/AUDITORIA_SEGURIDAD.md) arrojó **8 vulnerabilidades críticas P0** que aún persisten en los controladores de autenticación, configuración de CORS/CSRF, permisos de roles y exposición de credenciales en configuración, requiriendo remediación técnica previa a cualquier despliegue productivo.

---

## 2. Bloque A: Seguridad y SecOps (Auditoría Técnica)

> [!CAUTION]
> Los hallazgos P0 representan vectores de explotación inmediata que permiten evasión de autenticación, escalación vertical a administrador y fuga de credenciales de infraestructura cloud.

### 🔴 Bloqueantes Críticos (P0)

- [ ] **P0-1: Bypass Total de Autenticación SSO en `AuthController`**
  - **Ubicación:** [`donatrack-auth/.../AuthController.java#L43`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java#L43) y [`SecurityConfig.java#L115`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L115).
  - **Problema:** `/api/auth/sso/codigo` permite obtener un código de intercambio para cualquier email arbitrario (incluyendo `admin@donatrack.org`) sin comprobar OAuth2 real, derivando un Bearer token con rol `ADMIN`.
  - **Acción requerida:** Restringir el mock a `@Profile({"local", "test"})` o implementar validación estricta del authorization code con Google/GitHub.

- [ ] **P0-2: Exposición Pública de Endpoints Administrativos (`/admin/usuarios/**`)**
  - **Ubicación:** [`donatrack-auth/.../SecurityConfig.java#L116`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L116).
  - **Problema:** `requestMatchers("/admin/usuarios/**").permitAll()` permite listar usuarios, alterar roles a `ADMIN` (`PUT /admin/usuarios/{id}/rol`) y resetear contraseñas de cualquier cuenta a usuarios anónimos.
  - **Acción requerida:** Modificar a `.requestMatchers("/admin/**").hasRole("ADMIN")`.

- [ ] **P0-3: Credenciales Reales Hardcodeadas y Secretos en Git**
  - **Ubicación:** [`donatrack-logistica/.../application.properties#L24-L25`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/resources/application.properties#L24-L25), `application.yml` en módulos y claves en `.env`.
  - **Problema:** Claves de acceso Supabase S3, connection strings completas de MongoDB Atlas y CloudAMQP expuestas con fallbacks por defecto.
  - **Acción requerida:** Eliminar valores por defecto en `${...}` de las properties, rotar claves en consolas cloud y purgar commits con secretos si el repositorio se hace público.

- [ ] **P0-4: Flag Inseguro `DONATRACK_SECURITY_ENABLED=false` (Fail-Open)**
  - **Ubicación:** [`donatrack-common/.../ServiceSecurityFilter.java#L23`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L23) y [`.env.template#L82`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/.env.template#L82).
  - **Problema:** Los microservicios de negocio (8000–8003) omiten la validación de tokens de seguridad si el flag es `false`.
  - **Acción requerida:** Invertir la política a Fail-Closed por defecto: `@Value("${donatrack.security.enabled:${DONATRACK_SECURITY_ENABLED:true}}")`.

- [ ] **P0-5: Clave Maestra M2M Débil Hardcodeada**
  - **Ubicación:** [`ServiceSecurityFilter.java#L26`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L26) y [`ApiClientsConfig.java#L23`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java#L23).
  - **Problema:** Fallback estático a `"donatrack-internal-key-change-in-prod"`.
  - **Acción requerida:** Forzar validación de entropía en `@PostConstruct` impidiendo el arranque si la clave M2M contiene el valor de ejemplo o tiene menos de 32 caracteres.

- [ ] **P0-6: Desactivación Total de CSRF en Portal Web con Cookies**
  - **Ubicación:** [`donatrack-web/.../WebSecurityConfig.java#L30`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/WebSecurityConfig.java#L30).
  - **Problema:** `http.csrf(AbstractHttpConfigurer::disable)` deja desprotegidas las sesiones basadas en `JSESSIONID` frente a ataques CSRF en formularios de administración y donaciones.
  - **Acción requerida:** Habilitar `CookieCsrfTokenRepository.withHttpOnlyFalse()` o gestión de tokens CSRF en sesión para Thymeleaf.

- [ ] **P0-7: Vulnerabilidades IDOR / BOLA en Donaciones, Solicitudes y Perfiles**
  - **Ubicación:** [`DonacionController.java#L154`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java#L154), [`SolicitudOngController.java#L103`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/SolicitudOngController.java#L103) y [`AuthController.java#L99`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java#L99).
  - **Problema:** No se verifica la coincidencia entre el `userId` autenticado en el token JWT y el recurso solicitado/modificado (`idDonante`, estado de donación, perfil).
  - **Acción requerida:** Validar pertenencia del recurso o rol `ADMIN` en cada endpoint.

- [ ] **P0-8: Inicialización Automática de Administrador con Credenciales Débiles**
  - **Ubicación:** [`donatrack-auth/.../AuthDataSeeder.java#L27`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/AuthDataSeeder.java#L27).
  - **Problema:** Se crean las cuentas `admin/admin` y `donante/donante` incondicionalmente en cualquier entorno.
  - **Acción requerida:** Añadir anotación `@Profile({"local", "dev", "test"})`.

### 🟡 Mejoras de Hardening y Observabilidad (P1)

- [ ] **P1-1: Validación de Magic Bytes en Subida de Remitos a Supabase S3**
  - Restringir la subida en [`SupabaseS3StorageAdapter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/storage/SupabaseS3StorageAdapter.java) validando MIME real y magic bytes (evitar bypass por extensión simulada).
- [ ] **P1-2: Prevención de Open Redirect en `AdminController`**
  - Sanitizar cabecera `Referer` en [`AdminController.java#L140`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java#L140) limitando redirecciones a rutas relativas `/`.
- [ ] **P1-3: Ocultar Diagnósticos Sensibles en Actuator Health**
  - Cambiar `management.endpoint.health.show-details=never` o `when-authorized` para evitar exponer pools Hikari y versiones de software.
- [ ] **P1-4: Cabeceras de Seguridad HTTP (CSP, HSTS, X-Frame-Options)**
  - Configurar Content Security Policy (CSP), HSTS y `frameOptions().deny()` en [`WebSecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/WebSecurityConfig.java).
- [ ] **P1-5: Restricción de CORS Permisivo (`*`)**
  - Configurar dominios específicos en [`SecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L181) mediante `${WEB_URL}` en lugar de permitir comodín universal.
- [ ] **P1-6: Logging Estructurado JSON y Erradicación de `System.out`**
  - Reemplazar trazas directas y `printStackTrace` por Logger parametrizado con `traceId` en los servicios de negocio y notificaciones.
- [ ] **P1-7: Aislamiento de Puertos de Bases de Datos en Docker Compose**
  - En perfiles productivos, no exponer los puertos `5432`, `27017` y `5672` a `0.0.0.0`, manteniéndolos en la red interna de Docker.
- [ ] **P1-8: Endurecimiento de Seguridad en Nodos n8n**
  - Fijar `N8N_BLOCK_ENV_ACCESS_IN_NODE=true` en [`compose.yaml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml#L51) y configurar autenticación por cabecera en los webhooks.

### 🔵 Deuda Técnica y Buenas Prácticas (P2)

- [ ] **P2-1: Estandarización del Parser JWT**
  - Migrar el parser manual por división de strings en [`ServiceSecurityFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L93) hacia una biblioteca estándar como `NimbusJwtDecoder` o `jjwt`.
- [ ] **P2-2: Job de Desalojo de Códigos SSO en Memoria**
  - Implementar limpieza programada (`@Scheduled`) de códigos de un solo uso en [`AuthService.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/application/service/AuthService.java#L26) para evitar fugas de memoria.
- [ ] **P2-3: Row Level Security (RLS) en Supabase Postgres**
  - Habilitar RLS en [`database/init.sql`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/database/init.sql) para proteger las tablas de Donaciones y Usuarios si se expone la API PostgREST.

---

## 3. Bloque B: Documentación Técnica y Diagramas UML/PlantUML

> [!IMPORTANT]
> Conforme a las directivas de gobernanza y normas de la cátedra de la facultad, los diagramas de modelado oficial del proyecto **deben ser en UML / PlantUML (`.puml`)**, descartando cualquier migración destructiva a Mermaid.

- [ ] **Actualización del Diagrama de Componentes y Despliegue PlantUML**
  - **Archivo:** [`docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml).
  - **Tareas:**
    1. Reemplazar la arquitectura antigua (Frontend Vite SPA :3000 + Gateway monolítico) por el cliente SSR Thymeleaf `donatrack-web` (:8080 con Spring Cloud Gateway).
    2. Incorporar el nodo del servidor de identidad centralizada `donatrack-auth` (:8004).
    3. Actualizar la persistencia de Logística para reflejar el motor documental **MongoDB 7.0** y el almacenamiento de objetos **Supabase Storage S3**.
    4. Mapear los 6 microservicios con sus puertos e intercambios RabbitMQ reales.

- [ ] **Resolución y Normalización de ADRs en `docs/notas_y_borradores/`**
  - **Archivos bajo análisis:**
    - [`decisiones-arquitectura.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/notas_y_borradores/decisiones-arquitectura.md) (ADR-01 y ADR-02).
    - `entrega-6-implementacion-ssr-seguridad.md`.
    - `decisiones-arquitectura-incentivos.md`.
  - **Decisión pendiente:** El usuario decidirá si se concentran en una carpeta formal `docs/adr/` con numeración estándar o si se mantienen organizados dentro de `docs/notas_y_borradores/`.
  - **Restricción:** No alterar las carpetas de estudio teórico ([`docs/notas_y_borradores/teorico/bases_de_datos/`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/notas_y_borradores/teorico/bases_de_datos/)).

- [ ] **Documentos de Referencia Pendientes de Creación**
  - [ ] **Catálogo Estático de APIs REST (`docs/reference/api-catalog.md`)**: Inventario Markdown offline con endpoints, parámetros de consulta, DTOs de entrada y códigos HTTP de respuesta.
  - [ ] **Diccionario Exhaustivo de Variables de Entorno (`docs/reference/environment-variables.md`)**: Matriz detallada que documente el propósito, módulo consumidor, valor por defecto y severidad de cada variable definida en [`env.template`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/env.template).
  - [ ] **Guía de Siembra y Datos Semilla (`docs/how-to/database-seeding.md`)**: Manual operativo que documente las credenciales mock de desarrollo, flota de camiones y procedimientos de reseteo con `make db-init` y `make mongo-init`.

- [ ] **Verificación de Diagramas DER Físicos**
  - Revisar las referencias de la Entrega 4 en [`README.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/README.md#L223) para asegurar que los diagramas relacionales físicos existan en `docs/diagramas/` sin enlaces rotos.

---

## 4. Bloque C: Cierre Técnico y QA de la Entrega 6

> [!NOTE]
> Las 5 fases de código de la Entrega 6 (BFF satélites, filtros SLDS, mapa satelital Leaflet, operatoria de depósito y vencimientos, páginas legales Ley 25.326 y registro seguro con rol `BENEFICIARIA_PENDIENTE`) ya fueron implementadas y probadas unitariamente.

- [ ] **Validación Funcional E2E en Navegador y Accesibilidad (WCAG 2.1 AA)**
  - Auditar con navegador real o suite de testing visual los siguientes flujos de usuario:
    1. **Flujo Donante:** Verificación del filtrado en cliente en [`panel.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/donante/panel.html), proyección de misiones e hidratación del mapa Leaflet en vivo.
    2. **Flujo Beneficiaria:** Carga de necesidades materiales y confirmación de recepción con captura de imagen de remito.
    3. **Flujo Administrador:** Formulario de recepción en depósito ([`recepcion-donacion.html`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/templates/admin/admin/recepcion-donacion.html)), modal de marcado de donación vencida y visualización del historial de rankings de gamificación.
    4. **Accesibilidad:** Comprobar contraste mínimo 4.5:1 en componentes SLDS y modo público.

- [ ] **Pruebas de Resiliencia ante Cold-Starts en Render (Nivel Gratuito)**
  - Validar que ante la suspensión de `donatrack-logistica` en Render (demora de 40 a 70 segundos en arrancar), el timeout defensivo de 2.0s y la caché de 180s en [`CacheResilienteService.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/service/CacheResilienteService.java) impidan que el SSR arroje HTTP 504 Gateway Timeout.

- [ ] **Sincronización de CSRF en Vistas Thymeleaf y Peticiones AJAX**
  - Al remediar el hallazgo P0-6 (habilitar CSRF), comprobar que los scripts de JavaScript (`map.js`, `donante-mapa.js`) y los formularios con `th:action` envíen el token CSRF sin provocar respuestas HTTP 403 Forbidden.

---

## 5. Bloque D: Entrega 7 — Observabilidad, Monitoreo y Rendimiento (Fecha: 22/11/2026)

- [ ] **Infraestructura de Observabilidad y Métricas**
  - Incorporar endpoints de métricas Prometheus en Spring Boot Actuator (`micrometer-registry-prometheus`).
  - Configurar recolección de métricas de JVM, latencia HTTP (p95, p99), uso de memoria y estado de RabbitMQ.
  - Diseñar tableros en Grafana para supervisión en tiempo real del clúster de microservicios.

- [ ] **Trazabilidad Distribuida (Distributed Tracing)**
  - Instrumentar correlación de llamadas HTTP y eventos RabbitMQ mediante `Micrometer Tracing` + `Brave`/`OpenTelemetry` para rastrear peticiones extremo a extremo (`traceId` único desde `donatrack-web` hasta la base de datos).

- [ ] **Evaluación de Comunicación Avanzada (gRPC / GraphQL)**
  - Analizar los requerimientos de la consigna de Entrega 7 del enunciado [`DDS-TP-Anual-2026.pdf`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/enunciado/DDS-TP-Anual-2026.pdf) para determinar si se requiere implementar interfaces gRPC o GraphQL entre servicios satélites.

- [ ] **Pruebas de Carga y Concurrencia (Stress Testing)**
  - Diseñar scripts de prueba de carga con k6 o JMeter para simular concurrencia en la landing page y validar el rendimiento del streaming en la importación masiva de CSV (>10.000 filas).

---

## 6. Referencias Obligatorias

1. **Documentación del Proyecto:**
   - [`README.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/README.md) — Matriz principal del sistema y cronograma de entregas.
   - [`SERVICIOS.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/SERVICIOS.md) — Inventario oficial de microservicios, puertos, Swagger y Make targets.
   - [`docs/enunciado/DDS-TP-Anual-2026.pdf`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/enunciado/DDS-TP-Anual-2026.pdf) — Enunciado del Trabajo Práctico Anual K3002 UTN FRBA.
2. **Fuentes Primarias y Estándares de la Industria:**
   - [Spring Security — CSRF Protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
   - [OWASP Top 10 — A01: Broken Access Control & IDOR](https://top10.owasp.org/A01_2021-Broken_Access_Control/)
   - [OWASP Top 10 — A05: Security Misconfiguration](https://top10.owasp.org/A05_2021-Security_Misconfiguration/)
   - [Salesforce Lightning Design System (SLDS 2.30.3)](https://www.lightningdesignsystem.com/)
   - [Planificación K3002 UTN FRBA — Suriweb](https://suriweb.com.ar/web/planificacion.php?curso_id=9)
