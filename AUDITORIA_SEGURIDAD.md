# Reporte de Auditoría Integral de Seguridad y Observabilidad — Plataforma DonaTrack

**Fecha:** 10 de Noviembre de 2026  
**Auditor:** Especialista SecOps & Observabilidad (`secops-engineer`), Squad dev-architecture  
**Alcance:** DonaTrack Multi-Módulo (Java 21 / Spring Boot 3 multi-módulo): `donatrack-auth`, `donatrack-common`, `donatrack-donaciones`, `donatrack-logistica`, `donatrack-incentivos`, `donatrack-notificaciones`, `donatrack-web`, `database`, Docker/Compose y workflows `n8n`.

---

## 1. Resumen Ejecutivo

La plataforma DonaTrack presenta múltiples vectores críticos de explotación inmediata que permiten el compromiso total del sistema: derivación arbitraria de tokens de administrador sin autenticación previa mediante el endpoint SSO, exposición pública de rutas de administración de usuarios y cambio forzado de roles, y credenciales reales de infraestructura cloud (Supabase PostgreSQL y S3, MongoDB Atlas, CloudAMQP, Twilio y Discord) expuestas tanto en archivos de configuración rastreados como en el historial de Git. Es mandatario aplicar remediación inmediata en los filtros de seguridad, eliminar credenciales de código fuente, rotar las claves en los proveedores externos y activar autenticación estricta por defecto antes de cualquier pase a producción.

---

## 2. Nivel de Riesgo Global

# `[CRÍTICO]`

| Categoría | Severidad | Cantidad de Hallazgos | Estado |
|---|:---:|:---:|:---:|
| **P0 - Bloqueantes de Seguridad** | 🔴 CRÍTICO / ALTO | 8 | Requiere acción inmediata previa a despliegue |
| **P1 - Hardening & Observabilidad** | 🟡 MEDIO | 8 | Prioridad alta para producción |
| **P2 - Convenciones & Buenas Prácticas** | 🔵 BAJO | 4 | Mantenibilidad y deuda técnica |

---

## 3. Bloqueantes (P0): Vulnerabilidades Críticas y Altas

### 🔴 P0-1: Bypass Total de Autenticación y Robo de Cuentas (SSO Code Generation sin Validación)
- **Riesgo:** [CRÍTICO] (CVSS 9.8 - CWE-306, CWE-287)
- **Archivos:**
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java#L42-L60) (Líneas 42–60)
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L115) (Línea 115)
- **Vector de Ataque:**  
  La ruta `/api/auth/sso/codigo` se encuentra configurada bajo `.permitAll()`. El endpoint acepta un parámetro `email` arbitrario provisto por el cliente, busca o crea el usuario sin requerir firma OAuth2, token de Google/GitHub ni comprobación de identidad, genera un código de intercambio de un solo uso (`authService.generarCodigoIntercambio(usuario)`) y lo devuelve directamente en la respuesta JSON. Cualquier atacante no autenticado puede ejecutar:
  ```http
  GET /api/auth/sso/codigo?provider=google&email=admin@donatrack.org HTTP/1.1
  Host: localhost:8004
  ```
  La API responde con `{"code": "<uuid>"}`. Seguidamente, el atacante invoca `POST /auth/token/exchange` con dicho código y obtiene un Bearer JWT válido con rol `ADMIN`.
- **Impacto:** Robo de cuenta (Account Takeover) completo e instantáneo de cualquier usuario registrado en la plataforma, incluyendo el superusuario administrador.
- **Propuesta de Parche:**
  ```java
  // ANTES (AuthController.java)
  @GetMapping("/api/auth/sso/codigo")
  public ResponseEntity<Map<String, String>> obtenerCodigoSocial(
      @RequestParam("provider") String provider,
      @RequestParam(value = "email", required = false) String emailParam,
      @RequestParam(value = "nombre", required = false) String nombreParam) {
    ...
    Usuario usuario = authService.obtenerOCrearUsuarioSocial(email, nombre, provider.toUpperCase());
    String code = authService.generarCodigoIntercambio(usuario);
    return ResponseEntity.ok(Map.of("code", code));
  }
  
  // DESPUÉS (AuthController.java)
  // Eliminar por completo el endpoint mock o restringirlo exclusivamente al perfil local de pruebas:
  @Profile({"local", "test"})
  @GetMapping("/api/auth/sso/codigo")
  public ResponseEntity<Map<String, String>> obtenerCodigoSocialLocal(
      @RequestParam("provider") String provider,
      @RequestParam(value = "email", required = false) String emailParam) {
    // Solo disponible en entornos aislados de desarrollo
    ...
  }
  ```
  En producción, el flujo social debe ejecutarse estrictamente a través del protocolo OAuth2 Authorization Code Grant validado por Spring Security contra el proveedor de identidad oficial.

---

### 🔴 P0-2: Exposición Pública de Endpoints Administrativos y Escalada de Privilegios
- **Riesgo:** [CRÍTICO] (CVSS 9.8 - CWE-284, CWE-862)
- **Archivos:**
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L116) (Línea 116)
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java#L76-L97) (Líneas 76–97)
- **Vector de Ataque:**  
  En `SecurityConfig.java`, la directiva `auth.requestMatchers("/admin/usuarios/**").permitAll()` desprotege todas las operaciones de administración. Esto permite que cualquier usuario anónimo sin credenciales pueda:
  1. `GET /admin/usuarios`: Listar todos los usuarios del sistema, sus correos, nombres, IDs y roles (OWASP A01).
  2. `PUT /admin/usuarios/{id}/rol`: Modificar el rol de cualquier usuario a `ADMIN` enviando `{"rol": "ADMIN"}`.
  3. `POST /admin/usuarios/{id}/reset-password`: Resetear la contraseña de cualquier cuenta (incluida la del administrador) fijándola en el valor estático `"password123"`.
- **Impacto:** Escalada vertical completa de privilegios y compromiso de confidencialidad de la base de datos de usuarios.
- **Propuesta de Parche:**
  ```java
  // ANTES (SecurityConfig.java)
  auth.requestMatchers(
          "/auth/login",
          "/auth/register",
          "/auth/token/exchange",
          "/api/auth/perfil/**",
          "/api/auth/sso/**",
          "/admin/usuarios/**",  // <-- VULNERABLE: Abierto a todo el mundo
          "/healthcheck",
          ...
  
  // DESPUÉS (SecurityConfig.java)
  auth.requestMatchers(
          "/auth/login",
          "/auth/register",
          "/auth/token/exchange",
          "/healthcheck",
          "/error"
      ).permitAll()
      .requestMatchers("/admin/**").hasRole("ADMIN")
      .requestMatchers("/api/auth/perfil/**").authenticated()
      .anyRequest().authenticated();
  ```

---

### 🔴 P0-3: Claves Cloud S3, Base de Datos y Secretos Hardcodeados en Repositorio y Git
- **Riesgo:** [CRÍTICO] (CVSS 9.1 - CWE-798, CWE-312)
- **Archivos:**
  - [`donatrack-logistica/src/main/resources/application.properties`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/resources/application.properties#L24-L25) (Líneas 24–25)
  - [`donatrack-auth/src/main/resources/application.yml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/resources/application.yml#L25) (Línea 25)
  - [`donatrack-web/src/main/resources/application.yml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/application.yml#L14) (Línea 14)
  - [`donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L26-L31) (Líneas 26–31)
  - Historial Git (commit `f61240e`) y `.env` en la raíz.
- **Detalle de Credenciales Expuestas:**
  - **Supabase Storage S3 Key ID:** `9e4a5c1c13943732a986fb065085deb9`
  - **Supabase Storage S3 Secret Key:** `177633d57022b9b617e0fae23079dc562741789ab07009fcfbc6e1df0ac63f93`
  - **Supabase Postgres Password:** `DonatrackSecreto2026!`
  - **CloudAMQP RabbitMQ Password:** `yo4h1mr_quNyULw_99BnAPnbg9YLqVty`
  - **MongoDB Atlas URI:** `mongodb+srv://mfernandezcruz_db_user:vHwUHMZDZBblzyQ6@...`
  - **JWT Secret default:** `SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!`
- **Vector de Ataque:** Cualquier persona con acceso de lectura al repositorio Git o a sus clones puede inspeccionar `application.properties` o ejecutar `git log -p` para extraer credenciales válidas y tomar control total de los almacenes de datos y almacenamiento de objetos S3.
- **Propuesta de Parche:**
  1. Rotar inmediatamente todas las claves en las consolas de Supabase, CloudAMQP, Twilio y MongoDB Atlas.
  2. Eliminar los valores por defecto hardcodeados en los archivos de configuración:
  ```properties
  # ANTES (donatrack-logistica/.../application.properties)
  supabase.s3.access-key-id=${SUPABASE_S3_ID:9e4a5c1c13943732a986fb065085deb9}
  supabase.s3.secret-access-key=${SUPABASE_S3_KEY:177633d57022b9b617e0fae23079dc562741789ab07009fcfbc6e1df0ac63f93}

  # DESPUÉS
  supabase.s3.access-key-id=${SUPABASE_S3_ID}
  supabase.s3.secret-access-key=${SUPABASE_S3_KEY}
  ```
  3. Purgar el historial de Git utilizando `git-filter-repo` o `BFG Repo-Cleaner` para erradicar las contraseñas comprometidas.

---

### 🔴 P0-4: Desactivación de Seguridad en Microservicios por Flag Inseguro (`DONATRACK_SECURITY_ENABLED=false`)
- **Riesgo:** [CRÍTICO] (CVSS 9.4 - CWE-656, CWE-1188)
- **Archivos:**
  - [`donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L23-L24) (Líneas 23–24, 38–41)
  - [`.env.template`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/.env.template#L82) (Línea 82)
- **Vector de Ataque:**  
  `ServiceSecurityFilter` es el único filtro de seguridad en los microservicios `donaciones`, `logistica`, `incentivos` y `notificaciones` (ninguno incluye `spring-boot-starter-security`). El filtro evalúa:
  ```java
  if (!securityEnabled) {
    filterChain.doFilter(request, response);
    return;
  }
  ```
  Al estar inicializado con `@Value("${donatrack.security.enabled:${DONATRACK_SECURITY_ENABLED:false}}")` y figurar `DONATRACK_SECURITY_ENABLED=false` en las plantillas y en `compose.yaml`, todo el tráfico HTTP ingresa sin ninguna autenticación a los microservicios expuestos en los puertos 8000–8003.
- **Impacto:** Invocación directa y no autorizada de endpoints de negocio críticos: modificación de donaciones, ejecución de asignaciones batch, consulta de datos de beneficiarios, alteración de rutas y despacho masivo de notificaciones.
- **Propuesta de Parche:**
  ```java
  // ANTES (ServiceSecurityFilter.java)
  @Value("${donatrack.security.enabled:${DONATRACK_SECURITY_ENABLED:false}}")
  private boolean securityEnabled;

  // DESPUÉS: Fail-Closed por defecto (la seguridad DEBE ser obligatoria)
  @Value("${donatrack.security.enabled:${DONATRACK_SECURITY_ENABLED:true}}")
  private boolean securityEnabled;
  ```
  En `.env.template` y variables de despliegue, fijar `DONATRACK_SECURITY_ENABLED=true`.

---

### 🔴 P0-5: Fallback de Clave Maestra M2M Débil (`donatrack-internal-key-change-in-prod`)
- **Riesgo:** [CRÍTICO] (CVSS 9.1 - CWE-798, CWE-321)
- **Archivos:**
  - [`donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L26-L27) (Líneas 26–27)
  - [`donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/ApiClientsConfig.java#L23-L26) (Líneas 23–26)
  - [`donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/config/FeignConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/config/FeignConfig.java#L13-L16) (Líneas 13–16)
- **Vector de Ataque:**  
  La validación M2M permite paso total si la cabecera `X-Service-Key` coincide con `internalApiKey`. Debido al fallback estático en `@Value`, si la variable de entorno `INTERNAL_API_KEY` no se define en un contenedor o entorno, el sistema utiliza `"donatrack-internal-key-change-in-prod"`. Cualquier cliente que envíe dicha cabecera obtiene autorización completa e irrestricta.
- **Impacto:** Evasión completa de filtros perimetrales y autorización universal en todos los microservicios.
- **Propuesta de Parche:**
  ```java
  // ANTES
  @Value("${INTERNAL_API_KEY:${donatrack.internal-api-key:donatrack-internal-key-change-in-prod}}")
  private String internalApiKey;

  // DESPUÉS: Fallar de inmediato al arrancar si no se configuró una clave M2M segura
  @Value("${INTERNAL_API_KEY:#{null}}")
  private String internalApiKey;

  @PostConstruct
  public void validateKey() {
    if (securityEnabled && (internalApiKey == null || internalApiKey.length() < 32 || internalApiKey.contains("change-in-prod"))) {
      throw new IllegalStateException("CRITICAL: INTERNAL_API_KEY debe estar configurada con alta entropía (mínimo 32 caracteres)!");
    }
  }
  ```

---

### 🟠 P0-6: Desactivación Total de CSRF en Aplicación Web con Sesiones de Cookies
- **Riesgo:** [ALTO] (CVSS 8.1 - CWE-352)
- **Archivos:**
  - [`donatrack-web/src/main/java/com/donatrack/web/config/WebSecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/WebSecurityConfig.java#L30) (Línea 30)
- **Vector de Ataque:**  
  `donatrack-web` es un portal SSR basado en Spring MVC / Thymeleaf que utiliza sesiones estándar HTTP mediante la cookie `JSESSIONID`. La línea `http.csrf(AbstractHttpConfigurer::disable)` desactiva la protección CSRF en todo el portal. Un sitio externo controlado por un atacante puede inducir a un usuario logueado (o administrador) a visitar una página trampa que envíe peticiones automáticas POST a endpoints como:
  - `POST /app/perfil/actualizar`
  - `POST /app/donante/donaciones`
  - `POST /app/admin/camiones/{patente}/eliminar`
  - `POST /app/admin/choferes/{legajo}/eliminar`
  - `POST /app/admin/usuarios/{id}/rol`
- **Impacto:** Ejecución de acciones sensibles, eliminación de activos de logística o asignación de privilegios en nombre de la víctima.
- **Propuesta de Parche:**
  ```java
  // ANTES (WebSecurityConfig.java)
  http.csrf(AbstractHttpConfigurer::disable)
  
  // DESPUÉS (WebSecurityConfig.java)
  // Habilitar CSRF con repositorio de tokens en cookie o sesión para Thymeleaf
  http.csrf(csrf -> csrf
      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .ignoringRequestMatchers("/api/public/**", "/healthcheck")
  )
  ```
  *(Thymeleaf inyecta automáticamente el token CSRF en los formularios con `th:action`).*

---

### 🟠 P0-7: Broken Object Level Authorization (BOLA / IDOR) en Donaciones, Perfiles y Solicitudes
- **Riesgo:** [ALTO] (CVSS 8.5 - CWE-639, CWE-285)
- **Archivos:**
  - [`donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java#L154-L243) (Líneas 154–243, 561–573)
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java#L99-L109) (Líneas 99–109)
  - [`donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/SolicitudOngController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/SolicitudOngController.java#L103-L155) (Líneas 103–155)
- **Vector de Ataque:**  
  1. `GET /api/donaciones`: Si no se provee el query parameter `idDonante`, retorna **todas** las donaciones del sistema incluyendo correos del donante y del beneficiario. Si se provee `idDonante`, no verifica si el usuario autenticado es dueño de dicho ID, permitiendo enumerar donaciones de cualquier persona.
  2. `PUT /api/donaciones/{id}/estado`: Permite forzar el cambio de estado de cualquier donación sin corroborar permisos ni roles.
  3. `GET /api/auth/perfil/{id}` y `PUT /api/auth/perfil/{id}`: Permite consultar y sobrescribir información de perfil de cualquier usuario sin autenticación.
  4. `PUT /api/solicitudes-ong/{id}/estado`: Permite aprobar o rechazar solicitudes de ONGs sin verificar rol de administrador.
- **Impacto:** Fuga masiva de datos personales (PII) y manipulación ilegítima del estado operativo de donaciones y entidades.
- **Propuesta de Parche:**
  Implementar validación estricta de pertenencia en la capa de aplicación:
  ```java
  // Ejemplo en DonacionController.java
  @GetMapping("/donaciones")
  public ResponseEntity<?> listarDonaciones(
      @RequestParam(name = "estado", required = false) String estado,
      @RequestParam(name = "idDonante", required = false) UUID idDonante,
      @RequestHeader(name = "Authorization", required = false) String bearer) {
      
      UUID authenticatedUserId = SecurityUtils.extractCurrentUserId(bearer);
      boolean isAdmin = SecurityUtils.isCurrentUserAdmin(bearer);
      
      if (!isAdmin && (idDonante == null || !idDonante.equals(authenticatedUserId))) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(Map.of("error", "No autorizado para consultar donaciones de otros usuarios"));
      }
      ...
  }
  ```

---

### 🟠 P0-8: Inserción Incondicional de Administrador con Credenciales por Defecto (`admin / admin`)
- **Riesgo:** [ALTO] (CVSS 8.0 - CWE-1188, CWE-521)
- **Archivos:**
  - [`donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/AuthDataSeeder.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/AuthDataSeeder.java#L27-L51) (Líneas 27–51)
- **Vector de Ataque:**  
  `AuthDataSeeder` implementa `CommandLineRunner` sin anotación `@Profile({"local", "dev"})`. En cualquier despliegue sobre una base de datos nueva (incluso en producción), inicializa los usuarios:
  - `admin` / `admin` (Rol: ADMIN)
  - `donante` / `donante` (Rol: DONANTE)
  - `beneficiaria` / `beneficiaria` (Rol: BENEFICIARIA)
- **Impacto:** Acceso no autorizado inmediato a funciones de administración mediante credenciales públicas y predecibles.
- **Propuesta de Parche:**
  ```java
  // ANTES
  @Component
  public class AuthDataSeeder implements CommandLineRunner {

  // DESPUÉS: Restringir a perfiles no productivos
  @Component
  @Profile({"local", "dev", "test"})
  public class AuthDataSeeder implements CommandLineRunner {
  ```

---

## 4. Mejoras Recomendadas (P1): Hardening y Observabilidad

### 🟡 P1-1: Subida de Archivos Sin Validación de Tipos Ni Sanitización (XSS Almacenado / Traversal)
- **Ubicación:** [`SupabaseS3StorageAdapter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/storage/SupabaseS3StorageAdapter.java#L43-L50) y [`LogisticaController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/in/api/LogisticaController.java#L293-L312)
- **Diagnóstico:** Se extrae la extensión directamente de `file.getOriginalFilename()` sin comprobar lista blanca (`.webp`, `.jpg`, `.png`), tamaño de payload, ni validación de número mágico (magic bytes). Si se sube un archivo `.html` o `.svg` con scripts incrustados al bucket público de Supabase, se produce un Stored XSS ejecutable en el navegador.
- **Remediación:**
  1. Validar extensiones permitidas: solo `image/jpeg`, `image/png`, `image/webp`.
  2. Forzar regeneración del nombre con UUID aleatorio y extensión estricta (`.webp` o `.jpg`), descartando el nombre original del cliente.
  3. Comprobar magic bytes con Apache Tika o validación de cabecera binaria.

### 🟡 P1-2: Redirección Abierta (Open Redirect) en Portal Web
- **Ubicación:** [`donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/controller/AdminController.java#L140-L141) (Líneas 140–141)
- **Diagnóstico:** `actualizarPerfil` obtiene `String referer = request.getHeader("Referer");` y ejecuta `return "redirect:" + (referer != null ? referer : "/app/donante");`. Si un atacante induce una petición con cabecera `Referer: https://phishing-donatrack.com`, el controlador redirige a la víctima al dominio malicioso (CWE-601).
- **Remediación:** Validar que el `Referer` comience con `/` o coincida exactamente con el host configurado de la aplicación (`webUrl`).

### 🟡 P1-3: Exposición de Diagnósticos Sensibles en Actuator Health
- **Ubicación:** `application.properties` y `application.yml` en los 6 servicios (`management.endpoint.health.show-details=always`).
- **Diagnóstico:** El endpoint público `/actuator/health` expone la versión de la base de datos, detalles del pool Hikari, queries de validación, espacio en disco del host y estado de RabbitMQ a usuarios anónimos.
- **Remediación:** Cambiar a `management.endpoint.health.show-details=when-authorized` o `never` en perfiles productivos.

### 🟡 P1-4: Ausencia de Cabeceras de Seguridad HTTP (CSP, HSTS, Permissions-Policy)
- **Ubicación:** [`WebSecurityConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/config/WebSecurityConfig.java)
- **Diagnóstico:** No se definen políticas de Content Security Policy (CSP), ni Strict-Transport-Security (HSTS), ni Permissions-Policy.
- **Remediación:**
  ```java
  http.headers(headers -> headers
      .contentSecurityPolicy(csp -> csp
          .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; img-src 'self' data: https:; font-src 'self' https://fonts.gstatic.com;"))
      .httpStrictTransportSecurity(hsts -> hsts
          .includeSubDomains(true)
          .maxAgeInSeconds(31536000))
      .frameOptions(frame -> frame.deny())
      .contentTypeOptions(ct -> {})
  );
  ```

### 🟡 P1-5: CORS Permisivo con Wildcard (`*`)
- **Ubicación:** [`SecurityConfig.java:181`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/config/SecurityConfig.java#L181) y [`compose.yaml:45`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml#L45)
- **Diagnóstico:** `configuration.setAllowedOrigins(List.of("*"))` en `donatrack-auth` y `N8N_CORS_ALLOWED_ORIGINS=*` en `compose.yaml`. Permite que cualquier origen web envíe solicitudes CORS.
- **Remediación:** Restringir a los dominios autorizados especificados mediante `${WEB_URL}`.

### 🟡 P1-6: Logging de Datos Personales (PII) y Uso de Salida No Estructurada
- **Ubicación:** [`AdaptadorEmail.java:22-23`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorEmail.java#L22-L23), [`AdaptadorSMS.java:22-23`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorSMS.java#L22-L23), [`AdaptadorWhatsApp.java:22-23`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorWhatsApp.java#L22-L23), [`GlobalExceptionHandler.java:198`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/config/GlobalExceptionHandler.java#L198)
- **Diagnóstico:**
  - Se loguean correos electrónicos, números telefónicos y cuerpos de mensajes completos en texto plano en nivel `INFO`.
  - Se utiliza `ex.printStackTrace()` y `System.out.println` en múltiples clases de negocio, perdiendo el formato JSON y la correlación de trazas (`traceId` / `requestId`).
- **Remediación:**
  1. Enmascarar PII: registrar únicamente identificadores ofuscados (ej. `j***z@donatrack.org`, `+54****5678`).
  2. Reemplazar `printStackTrace()` y `System.out` por `log.error("...", ex)` estructurado.

### 🟡 P1-7: Exposición de Puertos de Bases de Datos y Servicios a `0.0.0.0` en Docker Compose
- **Ubicación:** [`compose.yaml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml#L14)
- **Diagnóstico:** Los puertos de PostgreSQL (`5432`), MongoDB (`27017`), RabbitMQ (`5672`, `15672`) y los microservicios (`8000-8004`) están publicados directamente al host (`0.0.0.0`). En un entorno de producción o servidor expuesto, esto permite que atacantes externos alcancen directamente las bases de datos o servicios backend saltándose el frontend.
- **Remediación:** En entornos productivos, mapear únicamente el puerto web (`8080:8080`) o un Reverse Proxy (Nginx/Traefik con SSL) y mantener los demás servicios en una red interna privada de Docker (`networks: internal`).

### 🟡 P1-8: Configuración Insegura en Contenedor n8n
- **Ubicación:** [`compose.yaml:51`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml#L51)
- **Diagnóstico:** `N8N_BLOCK_ENV_ACCESS_IN_NODE=false` permite que cualquier script JavaScript ejecutado dentro de los nodos Code de n8n acceda a `process.env`, exponiendo los tokens de Twilio y webhooks de Discord. Además, los webhooks carecen de autenticación (Header Auth / HMAC).
- **Remediación:** Cambiar a `N8N_BLOCK_ENV_ACCESS_IN_NODE=true` y configurar cabecera de autenticación (`Header Auth`) en los webhooks de n8n.

---

## 5. Nitpicks y Deuda Técnica (P2)

1. **Parser Artesanal de Tokens JWT:**  
   [`ServiceSecurityFilter.java:93-136`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java#L93-L136) realiza un split por puntos y busca la cadena `"exp":` mediante indexOf. Si el token no incluye el claim `"exp"`, retorna `true` indefinidamente. Se recomienda sustituir por `NimbusJwtDecoder` o `jjwt`.
2. **Caché SSO en Memoria sin Desalojo:**  
   [`AuthService.java:26`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/application/service/AuthService.java#L26) almacena códigos de intercambio en un `ConcurrentHashMap` en memoria sin job periódico de limpieza de claves vencidas.
3. **Discrepancia en Archivos de Entorno:**  
   Existe `.env.template` en la raíz pero `.gitignore` lista `.env.example`. Se sugiere estandarizar a `.env.example` para seguir las convenciones de la industria.
4. **Falta de Políticas Row Level Security (RLS) en Postgres:**  
   [`database/init.sql`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/database/init.sql) no activa RLS (`ALTER TABLE ... ENABLE ROW LEVEL SECURITY`). Al ejecutarse sobre Supabase, las tablas en el esquema `public` quedan desprotegidas ante consultas vía API Data/PostgREST si ésta se encuentra habilitada.

---

## 6. Referencias Técnicas Investigadas

- [Spring Security Reference - CSRF Protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [OWASP Top 10 - A01: Broken Access Control & IDOR](https://top10.owasp.org/A01_2021-Broken_Access_Control/)
- [OWASP Top 10 - A05: Security Misconfiguration](https://top10.owasp.org/A05_2021-Security_Misconfiguration/)
- [OWASP Top 10 - A07: Identification and Authentication Failures](https://top10.owasp.org/A07_2021-Identification_and_Authentication_Failures/)
- [Supabase Storage Security & S3 Protocol Guide](https://supabase.com/docs/guides/storage/s3/authentication)
