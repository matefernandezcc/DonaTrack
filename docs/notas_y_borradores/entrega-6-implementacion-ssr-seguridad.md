# DonaTrack — Documentación Técnica de Implementación: Entrega 6
## Arquitectura de Cliente Liviano (SSR), Seguridad Distribuida & Aislamiento UI

Este documento detalla todas las modificaciones arquitectónicas, mejoras de seguridad, desarrollo de vistas y optimizaciones de resiliencia implementadas en el marco de la **Entrega 6** del sistema distribuido **DonaTrack**.

---

### 1. Arquitectura de Cliente Liviano (SSR) y Separación de Responsabilidades

En cumplimiento estricto con las directrices de diseño distribuido:
- **`donatrack-web` como Cliente Liviano:**
  - El módulo `donatrack-web` se encarga exclusivamente de renderizar vistas Thymeleaf y comunicarse vía HTTP con los microservicios backend utilizando `RestClient`.
  - Se eliminaron las dependencias transitivas de persistencia (`postgresql`, `h2`) del classpath de `donatrack-web`, moviéndolas a `<dependencyManagement>` en el `pom.xml` raíz. `donatrack-web` no mantiene conexión con ninguna base de datos ni ejecuta lógica de negocio.
- **Desacoplamiento de Solicitud de ONG:**
  - Originalmente, `SolicitudOngService` almacenaba datos en memoria (`ConcurrentHashMap`) en el cliente web.
  - Se migró el modelo hacia el microservicio `donatrack-donaciones` creando:
    - Entidad JPA `SolicitudOngEntity` (esquema `donaciones`, tabla `solicitudes_ong`).
    - Repositorio Spring Data JPA `SolicitudOngJpaRepository`.
    - DTO y controlador REST `SolicitudOngController` exponiendo los endpoints `POST /api/solicitudes-ong`, `GET /api/solicitudes-ong` y `PUT /api/solicitudes-ong/{id}/estado`.
    - Sembrado automático de solicitudes iniciales en PostgreSQL para pruebas.
  - `SolicitudOngService` en `donatrack-web` fue refactorizado para consumir la API a través de `DonacionesGateway`.

---

### 2. Seguridad M2M y Flujo de Autenticación (SSO / JWT)

- **Filtro Inter-Servicio `ServiceSecurityFilter` (`X-Service-Key`):**
  - Implementado en `donatrack-common` para validar la autenticidad del tráfico interno entre microservicios o peticiones salientes del cliente web.
  - Valida la presencia de la cabecera `X-Service-Key` con la clave compartida `INTERNAL_API_KEY`, o alternativamente una firma JWT HS256 válida.
  - Se crearon interceptores Feign en `donatrack-donaciones` y `donatrack-incentivos` para inyectar automáticamente `X-Service-Key` en llamadas de servicio a servicio.
  - `ApiClientsConfig` en `donatrack-web` inyecta automáticamente la cabecera `X-Service-Key` y el token Bearer del usuario autenticado en todas las solicitudes REST salientes.
- **SSO Social Login Dual (Google / GitHub con Mock Fallback):**
  - En `donatrack-auth`, el endpoint `GET /api/auth/sso/codigo` permite obtener un código de intercambio de un solo uso para entornos de desarrollo/demo o redireccionar a proveedores OAuth2 configurados.
  - En `donatrack-web`, se implementó el endpoint `@GetMapping("/oauth2/authorization/{provider}")` en `PublicController` que delega la negociación al gateway de autenticación y redirecciona de forma transparente a `/auth/callback?code=...`.
  - La sesión de usuario se establece del lado del servidor en Spring Session con `JwtAuthenticationToken`, sin exponer tokens JWT en cookies no seguras ni en almacenamiento local del cliente.

---

### 3. Aislamiento Estricto de UI/UX (La "Regla de Oro")

- **Layout Público (`layout-public.html` y vistas públicas):**
  - Cero inclusión de archivos, hojas de estilo o clases de Salesforce Lightning Design System (SLDS).
  - Uso exclusivo de CSS utilitario propio (`variables.css`, `base.css`, `components.css`, `sections.css`).
- **Layout Administrativo Backoffice (`layout-admin.html` y vistas `/admin`):**
  - Adherencia total a los blueprints oficiales de SLDS (Salesforce Lightning Design System 2.30.3).
  - Se eliminó el bloque `<style>` con clases custom `.table-scroll-container` en `usuarios.html`, reemplazándolo por el contenedor nativo de SLDS: `slds-scrollable_x slds-border_bottom`.
  - **Menú Responsive Móvil:** Se agregó el botón hamburguesa `#admin-menu-toggle` visible en pantallas `< 768px` (`slds-hide_medium`) que permite colapsar/desplegar la navegación vertical utilizando exclusivamente utilitarios SLDS (`slds-wrap`, `slds-hide`, `slds-show_medium`).
  - **Toasts Centralizados:** En `layout-admin.html`, las notificaciones de éxito (`flashOk`) y error (`flashError`) fueron maquetadas con el blueprint de Toast de SLDS (`slds-notify_toast`, `role="status"` / `role="alert"`, íconos vectoriales SVG y botón de cierre interactivo).
- **Accesibilidad (WCAG 2.5.5 / 2.5.8):**
  - En `sections.css`, los elementos interactivos `.theme-toggle`, `.nav__burger`, `.modal__close` y `.footer__social a` fueron configurados con un área táctil mínima de `48px x 48px`.
  - En `components.css`, `.btn--sm` fue ajustado con `min-height: 44px`.

---

### 4. Nuevas Vistas del Backoffice (Entrega 1 Cumplimentada)

- **Gestión de Choferes (`choferes.html`):**
  - Tarjeta SLDS (`slds-card`), tabla de datos responsive (`slds-table`), formulario modal accesible (`slds-modal`) para dar de alta choferes y acciones directas para eliminar choferes.
  - Endpoints `@GetMapping("/admin/choferes")`, `@PostMapping("/admin/choferes")` y `@PostMapping("/admin/choferes/{legajo}/eliminar")` en `AdminController`.
- **Monitoreo de Rutas de Reparto (`rutas.html`):**
  - Tarjeta SLDS, badges de estado operativo (Iniciada, En Progreso, Completada), información del vehículo y chofer asignado, y desglose de paradas y entregas.
  - Endpoint `@GetMapping("/admin/rutas")` en `AdminController`.

---

### 5. Resiliencia en Despliegue (Render & n8n)

- **Chatbot Reverse Proxy:**
  - Anteriormente `chatbot.js` intentaba conectarse directamente a `http://localhost:5678/webhook/chatbot` desde el navegador del usuario, fallando en producción y por políticas CORS.
  - Se desarrolló el endpoint `@PostMapping("/api/public/chatbot")` en `PublicController` de `donatrack-web`, actuando como reverse proxy hacia n8n con timeout controlado de 2s y mensaje de fallback amable si el motor de automatización no responde.
- **Protección contra Cold-Starts en Render:**
  - Configuración de connection timeouts cortos (10s conexión, 2-3s lectura en endpoints críticos) mediante `JdkClientHttpRequestFactory`.
  - Los métodos del gateway (como `IncentivosGateway.obtenerTop3()`) capturan fallos por microservicios en suspensión y devuelven colecciones vacías en lugar de interrumpir la renderización de la landing page.

---

### 6. Sanitización de Credenciales y Prevención de Fugas

- Se eliminaron todas las contraseñas hardcodeadas en los perfiles `prod` de los microservicios:
  - Supabase: `${SUPABASE_PASSWORD}`
  - CloudAMQP: `${CLOUDAMQP_PASSWORD}`
  - MongoDB Atlas: `${MONGO_ATLAS_URI}`
- Las claves y secretos reales quedaron centralizados en `.env` (excluido en `.gitignore`).
- El archivo `.env.template` fue depurado y no contiene claves ni credenciales reales de ningún servicio externo.
