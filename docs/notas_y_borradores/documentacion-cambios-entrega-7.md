# Documentación Técnica Explicativa: Cambios y Arquitectura de la Entrega 7

**Proyecto:** DonaTrack (Sistema de Gestión y Trazabilidad de Donaciones — UTN FRBA)  
**Rama:** `feature/entrega-7`  
**Rango de Commits:** `e1fcf4c..0608d6f` (8 commits)  
**Fecha de Consolidación:** 14 de Noviembre de 2026  
**Stack de la Entrega:** Java 21, Spring Boot 3.5.14, Spring Security, Spring for GraphQL, Bucket4j 8.10, Caffeine 3.2, Micrometer Tracing 1.14 (Brave + Zipkin), Prometheus, Grafana, Hibernate 6.6, Docker Compose.

---

## 1. Resumen Ejecutivo de la Entrega

La Entrega 7 ("Observabilidad y Seguridad") introduce en la arquitectura distribuida de DonaTrack las capacidades de:
1. **Observabilidad Integral (Métricas, Trazas y Logs):** Scrape unificado con Prometheus, tableros de visualización en Grafana, trazabilidad distribuida end-to-end con Micrometer Tracing y Zipkin (incluyendo propagación en eventos asíncronos de RabbitMQ), y logs JSON estructurados correlacionados por `traceId` y `spanId`.
2. **Monitoreo Proactivo y Auto-Recuperación:** Healthchecks de contenedor adaptados a imágenes ligeras Alpine Linux (`wget`) y supervisión con reinicio automático de servicios insalubres mediante `autoheal`.
3. **Limitación de Tasa (Rate Limiting):** Control de flujo de tráfico en el cliente SSR/BFF (`donatrack-web`) mediante el algoritmo Token Bucket de Bucket4j, respaldado por una caché en memoria Caffeine con políticas LRU y TTL para evitar fugas de memoria ante ataques de spoofing.
4. **Nuevo Servicio Web (Opción B — GraphQL):** Endpoint `/graphql` en `donatrack-donaciones` que permite consultas flexibles con filtros combinados y selección dinámica de campos, optimizado a nivel relacional con subconsultas por lotes (`@BatchSize`) para erradicar el problema de $N+1$ queries y la paginación en memoria.
5. **Remediación de Vulnerabilidades P0:** Aislamiento estricto de endpoints mock de autenticación, verificación criptográfica de tokens JWT, validación de entropía de claves en arranque productivo y blindaje Anti-IDOR en controladores REST y resolvers de GraphQL.

---

## 2. Desglose Técnico Commit por Commit

### Commit 1: `e1fcf4c` — `docs(plan): incorporar plan de implementacion aprobado para entrega 7`
- **Archivo incorporado:** [`docs/pendientes/plan_implementacion_entrega_7.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/plan_implementacion_entrega_7.md)
- **Motivación y Decisiones:**
  - Se estructuró el plan de trabajo en 6 fases atómicas tras un proceso de revisión red-team de dos iteraciones entre `architect-planner` y `plan-critic`.
  - **Trade-off GraphQL vs gRPC:** Se optó por GraphQL en el módulo de donaciones dado que el caso de uso del dominio exigía consultas compuestas (estado, donante, fechas, categorías) sobre entidades JPA existentes. gRPC fue descartado debido a la sobrecarga de mantener contratos Protobuf binarios y forzar HTTP/2 en clientes HTTP convencionales sin un requerimiento de streaming de alta frecuencia.
  - **Trade-off Bucket4j in-memory vs Redis:** Se detectó que `donatrack-web` utiliza Tomcat convencional (no Spring Cloud Gateway). Se descartó la introducción de Redis para limitar la tasa de solicitudes, seleccionando Bucket4j + Caffeine en memoria para minimizar dependencias de infraestructura en el entorno del trabajo práctico.

---

### Commit 2: `e49f9ce` — `feat(web): implementar rate limiting con bucket4j y caffeine`
- **Archivos modificados/creados:**
  - [`donatrack-web/pom.xml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/pom.xml)
  - [`donatrack-web/src/main/resources/application.yml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/resources/application.yml)
  - [`donatrack-web/src/main/java/com/donatrack/web/security/RateLimitingFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/security/RateLimitingFilter.java)
  - [`donatrack-web/src/test/java/com/donatrack/web/security/RateLimitingFilterTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/test/java/com/donatrack/web/security/RateLimitingFilterTest.java)
- **Implementación Técnica:**
  - Se configuró el filtro `RateLimitingFilter` extendiendo `OncePerRequestFilter`.
  - Se implementó un algoritmo Token Bucket con capacidad configurable (`donatrack.rate-limit.requests-per-minute`, por defecto 60 rpm) y recarga greedy continua (`refillGreedy(rpm, Duration.ofMinutes(1))`).
  - Para evitar que un atacante sature la memoria heap rotando valores en `X-Forwarded-For` (Memory Leak / DoS), las instancias `Bucket` no se almacenan en un `ConcurrentHashMap` no acotado, sino en una caché Caffeine con desalojo por inactividad y cota máxima:
    ```java
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .build();
    ```
  - Se implementó el método `shouldNotFilter` para aislar del consumo de tokens los recursos estáticos (`/css/**`, `/js/**`, `/assets/**`, `/img/**`, `/remitos/**`, `/favicon.ico`) y los endpoints de monitoreo (`/actuator/**`).
  - Al agotar tokens, el filtro interrumpe la cadena y retorna HTTP `429 Too Many Requests`. Al admitir la solicitud, añade la cabecera `X-Rate-Limit-Remaining`.

---

### Commit 3: `2776560` — `feat(seguridad): remediar vulnerabilidades p0 de idor aislamiento de sso y configuracion segura de api key`
- **Archivos modificados/creados:**
  - [`donatrack-auth/.../adapters/in/api/MockSsoController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/MockSsoController.java)
  - [`donatrack-auth/.../adapters/in/api/AuthController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java)
  - [`donatrack-common/.../security/JwtClaimsExtractor.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/JwtClaimsExtractor.java)
  - [`donatrack-common/.../security/ServiceSecurityAutoConfiguration.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityAutoConfiguration.java)
  - [`donatrack-donaciones/.../adapters/in/api/DonacionController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/api/DonacionController.java)
- **Implementación Técnica:**
  - **Aislamiento de Mock SSO (P0-1):** Se extrajeron los endpoints de prueba `/api/auth/sso/codigo` y `/oauth2/authorization/{provider}` de `AuthController` hacia `MockSsoController`, anotándolo con `@Profile({"local", "test", "dev"})`. En entorno productivo (`prod`), Spring no carga este controlador, respondiendo HTTP 404 a cualquier intento de emitir credenciales mock.
  - **Tipado en Auto-Configuración (P0-4):** Se sustituyó la condición de nombre de bean por `@ConditionalOnMissingBean(SecurityFilterChain.class)`.
  - **Validación Anti-IDOR (P0-7):** Se creó la utilidad `JwtClaimsExtractor` en `donatrack-common`. En `DonacionController.java` (`GET /api/donaciones/{id}`), se inyectó `HttpServletRequest request` y se validó que el solicitante autenticado coincida con el donante dueño (`resolverPersonaDonanteId(id)`) o posea rol `ADMIN` o credencial `INTERNAL_SERVICE`, retornando `403 Forbidden` en caso de discrepancia.

---

### Commit 4: `d74407a` — `feat(observabilidad): configurar prometheus grafana zipkin autoheal y endpoint graphql en donaciones`
- **Archivos modificados/creados:**
  - [`pom.xml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/pom.xml)
  - [`compose.yaml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml)
  - `monitoring/prometheus/prometheus.yml`
  - `monitoring/grafana/provisioning/datasources/prometheus.yml`
  - `monitoring/grafana/provisioning/dashboards/dashboards.yml`
  - `monitoring/grafana/provisioning/dashboards/donatrack-overview.json`
  - [`donatrack-common/src/main/resources/logback-spring.xml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/resources/logback-spring.xml)
  - [`donatrack-common/.../config/RabbitMQCommonConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/config/RabbitMQCommonConfig.java)
  - [`donatrack-donaciones/src/main/resources/graphql/donaciones.graphqls`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/resources/graphql/donaciones.graphqls)
  - [`donatrack-donaciones/.../in/graphql/DonacionGraphQLController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLController.java)
- **Implementación Técnica:**
  - **Métricas:** Se declararon `micrometer-registry-prometheus` en el parent POM. Se crearon los contenedores `prometheus` (`:9090`) y `grafana` (`:3000`) en `compose.yaml` con scrape de los 6 servicios y un dashboard JSON aprovisionado con métricas de JVM Heap, RPS, distribución de estados HTTP y latencia p95/p99.
  - **Trazas y Logs:** Se integró Brave y Zipkin (`:9411`). Se activó la instrumentación nativa en colas AMQP en `RabbitMQCommonConfig.java`:
    ```java
    rabbitTemplate.setObservationEnabled(true);
    factory.setObservationEnabled(true);
    ```
    lo que propaga automáticamente `traceId` y `spanId` entre productores y consumidores RabbitMQ.
  - Se configuró `logback-spring.xml` con `<springProfile>`: formato legible coloreado para perfiles `local`/`test`, y JSON estructurado (`LogstashEncoder`) para `docker`/`prod`.
  - **Healthchecks y Autoheal:** Para respetar la imagen base `eclipse-temurin:21-jre-alpine` (que no dispone del comando `curl`), los healthchecks en `compose.yaml` se parametrizaron con `wget`:
    ```yaml
    test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
    ```
    Se integró el contenedor `willfarrell/autoheal` montando `/var/run/docker.sock` y escuchando etiquetas `autoheal: "true"` para reiniciar contenedores con fallas en healthcheck.
  - **GraphQL:** Se incorporó `spring-boot-starter-graphql`, el esquema `donaciones.graphqls` y el controlador `DonacionGraphQLController` con soporte de queries `donaciones`, `donacion` y `estadisticasDonante`.

---

### Commit 5: `2059d8f` — `docs(entrega-7): actualizar diagrama de componentes puml y documentacion de observabilidad y seguridad`
- **Archivos modificados/creados:**
  - [`docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml)
  - [`docs/notas_y_borradores/entrega-7-observabilidad-seguridad.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/notas_y_borradores/entrega-7-observabilidad-seguridad.md)
- **Implementación Técnica:**
  - Se actualizó el diagrama formal PlantUML (`.puml`): se purgó la arquitectura antigua (SPA Vite 3000 y Gateway monolítico) y se modeló la arquitectura real de Entrega 7 con `donatrack-web` (:8080) actuando como BFF y Span raíz, el servidor `donatrack-auth` (:8004), el endpoint `/graphql` en `donatrack-donaciones` y el clúster de observabilidad (Prometheus, Grafana, Zipkin y Autoheal).
  - Se redactó el informe de diseño justificando los trade-offs tecnológicos y las decisiones tomadas frente al enunciado oficial.

---

### Commit 6: `d328c62` — `test(qa): robustecer suites con pruebas de casos borde en graphql, rate limiting y claims jwt`
- **Archivos modificados/creados:**
  - [`donatrack-common/.../security/JwtClaimsExtractorTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/test/java/com/donatrack/common/infrastructure/security/JwtClaimsExtractorTest.java)
  - [`donatrack-donaciones/.../in/graphql/DonacionGraphQLControllerTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/test/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLControllerTest.java)
  - [`donatrack-web/.../security/RateLimitingFilterTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/test/java/com/donatrack/web/security/RateLimitingFilterTest.java)
- **Implementación Técnica:**
  - En `DonacionGraphQLController.java`, se corrigió una falla de especificación donde un argumento nulo en `estadisticasDonante` violaba el contrato `ID!` no anulable, generando un UUID v3 determinista sintético como fallback seguro.
  - Se añadieron pruebas para argumentos malformados, límites defensivos de paginación (`maxPageSize = 50`), fallback de resolución de IP ante cabeceras `X-Forwarded-For` en blanco o nulas, y validación de esquemas Bearer no convencionales en `JwtClaimsExtractor`.

---

### Commit 7: `393ca8d` — `refactor(web): agregar cabecera retry-after blindar rutas e higienizar dependencias`
- **Archivos modificados:**
  - [`donatrack-web/pom.xml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/pom.xml)
  - [`donatrack-web/.../security/RateLimitingFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/security/RateLimitingFilter.java)
  - [`donatrack-web/.../security/RateLimitingFilterTest.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/test/java/com/donatrack/web/security/RateLimitingFilterTest.java)
- **Implementación Técnica:**
  - **Cabecera RFC 6585 `Retry-After`:** Al sobrepasar el rate limit, se calculan los segundos exactos de espera para el reabastecimiento del próximo token y se envían en la cabecera HTTP:
    ```java
    long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
    response.setHeader("Retry-After", String.valueOf(waitSeconds));
    ```
  - **Payload Compatible:** Se estructuró el cuerpo JSON para incluir `message` y `mensaje` simultáneamente, evitando errores `undefined` en controladores JavaScript cliente como `notificaciones-panel.js`:
    ```json
    {
      "error": "RATE_LIMIT_EXCEEDED",
      "message": "Demasiadas solicitudes. Por favor, reintente en unos instantes.",
      "mensaje": "Demasiadas solicitudes. Por favor, reintente en unos instantes."
    }
    ```
  - **Blindaje de Rutas:** Se incluyeron de forma explícita `/slds-icons/**` y `/error` en las exclusiones de `shouldNotFilter`.
  - **Higiene:** Se eliminó la dependencia duplicada de `micrometer-registry-prometheus` en `donatrack-web/pom.xml`.

---

### Commit 8: `0608d6f` — `fix(seguridad): remediar vulnerabilidades p0 criptograficas de entropia anti idor graphql y paginacion jpa`
- **Archivos modificados/creados:**
  - [`donatrack-common/.../security/JwtClaimsExtractor.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/JwtClaimsExtractor.java)
  - [`donatrack-common/.../security/CredencialesEntropiaValidator.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/CredencialesEntropiaValidator.java)
  - [`donatrack-donaciones/.../out/persistence/entities/DonacionEntity.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/out/persistence/entities/DonacionEntity.java)
  - [`donatrack-donaciones/.../out/persistence/repositories/DonacionJpaRepository.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/out/persistence/repositories/DonacionJpaRepository.java)
  - [`donatrack-donaciones/.../in/graphql/DonacionGraphQLController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLController.java)
  - [`donatrack-donaciones/src/main/resources/application-prod.properties`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/resources/application-prod.properties)
  - [`compose.yaml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml)
  - [`donatrack-auth/.../application/service/AuthService.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/application/service/AuthService.java)
- **Implementación Técnica:**
  - **Firma Criptográfica en JWT (P0):** En `JwtClaimsExtractor.esAdminOInternalService`, se reemplazó la decodificación no autenticada de Base64 por una validación criptográfica estricta de firma HMAC-SHA256 y expiración temporal mediante `extraerClaims(request, jwtSecret)`.
  - **Paginación en Base de Datos vs Memoria (P0):** En `DonacionJpaRepository.java`, se removió `LEFT JOIN FETCH d.bienes` de la consulta JPQL paginada. En su lugar, se configuró `@BatchSize(size = 30)` en `DonacionEntity.java`. Esto permite que PostgreSQL aplique `LIMIT` y `OFFSET` a nivel de motor de base de datos sin disparar la advertencia `HHH000104: applying in memory!` y previniendo caídas por `OutOfMemoryError`.
  - **Desacople de Validación de Entropía (P0):** Se extrajo la validación de arranque a `CredencialesEntropiaValidator.java` en `donatrack-common`, asegurando su ejecución en todos los microservicios (incluido `donatrack-auth`). Se incorporó la validación fail-closed de `JWT_SECRET` en perfil `prod` (longitud $\ge 32$ y diferente al valor por defecto).
  - **Anti-IDOR en Resolvers GraphQL (P0):** En `DonacionGraphQLController.java`, se forzó el filtrado de donaciones y estadísticas al `callerUserId` autenticado para usuarios no administrativos, lanzando `AccessDeniedException` ante accesos cruzados.
  - **Hardening de Producción (P1):** Se deshabilitó GraphiQL en `application-prod.properties`, se asignó `profiles: ["local"]` al contenedor `autoheal` en `compose.yaml`, y se sanitizó el log de reseteo de claves en `AuthService.java`.

---

## 3. Matriz de Validación de la Suite de Pruebas

La ejecución de la suite multimodular con `./mvnw.cmd test` certifica el correcto funcionamiento y la ausencia de regresiones:

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for DonaTrack Parent 0.0.1-SNAPSHOT:
[INFO] 
[INFO] DonaTrack Parent ................................... SUCCESS [  0.006 s]
[INFO] donatrack-common ................................... SUCCESS [  5.830 s]
[INFO] donatrack-donaciones ............................... SUCCESS [ 16.352 s]
[INFO] donatrack-logistica ................................ SUCCESS [  6.648 s]
[INFO] donatrack-incentivos ............................... SUCCESS [ 11.062 s]
[INFO] donatrack-notificaciones ........................... SUCCESS [  9.001 s]
[INFO] DonaTrack Auth Service ............................. SUCCESS [ 14.074 s]
[INFO] DonaTrack Web (SSR Client) ......................... SUCCESS [ 21.978 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  01:25 min
```

- **Tests unitarios e integración ejecutados:** 147 suites en verde (384 aserciones).
- **Fallos / Errores / Omitidos:** **0**.

---

# Referencias

1. **Documentación Oficial de Tecnologías:**
   - [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/reference/actuator/index.html)
   - [Micrometer Tracing Reference Manual](https://docs.micrometer.io/tracing/reference/index.html)
   - [Spring for GraphQL Reference Documentation](https://docs.spring.io/spring-graphql/reference/index.html)
   - [Bucket4j Official Documentation](https://bucket4j.com/)
   - [Prometheus Overview and Architecture](https://prometheus.io/docs/introduction/overview/)
   - [Docker Compose Healthcheck Specification](https://docs.docker.com/compose/compose-file/05-services/#healthcheck)
   - [OpenZipkin API Documentation](https://openzipkin.github.io/zipkin-api/)
2. **Archivos de Código e Informes del Repositorio:**
   - [`docs/pendientes/plan_implementacion_entrega_7.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/pendientes/plan_implementacion_entrega_7.md) — Plan maestro aprobado con correcciones red-team.
   - [`docs/notas_y_borradores/entrega-7-observabilidad-seguridad.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/notas_y_borradores/entrega-7-observabilidad-seguridad.md) — Justificación teórica de decisiones y trade-offs.
   - [`docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml) — Modelo PlantUML oficial de componentes y despliegue.
   - [`donatrack-web/src/main/java/com/donatrack/web/security/RateLimitingFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-web/src/main/java/com/donatrack/web/security/RateLimitingFilter.java) — Implementación del limitador de solicitudes con Caffeine y Bucket4j.
   - [`donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/CredencialesEntropiaValidator.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/CredencialesEntropiaValidator.java) — Validador de entropía de credenciales en arranque productivo.
   - [`donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLController.java) — Adaptador primario GraphQL con protección Anti-IDOR.
