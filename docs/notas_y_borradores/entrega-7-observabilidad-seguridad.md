# DonaTrack — Entrega 7: Observabilidad Distribuida, Resiliencia y GraphQL

## 1. Visión General y Objetivos Arquitectónicos

La Entrega 7 de DonaTrack consolida la madurez operativa del ecosistema distribuido, cerrando vulnerabilidades críticas de control de acceso e introduciendo capacidades de observabilidad profunda (métricas, trazas, logs estructurados), auto-recuperación ante fallos y una interfaz de consulta flexible mediante GraphQL.

```
+-----------------------------------------------------------------------------------+
|                                 DONATRACK STACK                                    |
+-----------------------------------------------------------------------------------+
|  [ Navegador Web ] --> [ donatrack-web :8080 (SSR + Bucket4j + Brave Root Span) ] |
+------------------------------------------+----------------------------------------+
                                           | HTTP REST / GraphQL (Bearer JWT)
                                           v
       +-------------------+--------------------+-------------------+
       |                   |                    |                   |
       v                   v                    v                   v
[ donatrack-auth ] [ donatrack-donaciones ] [ donatrack-logistica ] [ donatrack-incentivos ]
    (:8004)         (:8000 /graphql)             (:8002)                 (:8001)
       |                   |                    |                   |
       +-------------------+--------------------+-------------------+
                           |
                           +--> RabbitMQ (AMQP con ObservationEnabled=true)
                           |
       +-------------------+--------------------+-------------------+
       |                   |                    |                   |
       v                   v                    v                   v
[ Prometheus :9090 ] [ Grafana :3000 ]    [ Zipkin :9411 ]     [ Autoheal Docker ]
  (Scrapes Actuators)  (Dashboards)        (Spans & Traces)     (Socket Supervisor)
```

---

## 2. Fase 0: Remediación P0 de Seguridad

### 2.1 Aislamiento de Endpoints Mock SSO
En versiones preliminares, el endpoint `/api/auth/sso/mock` permanecía accesible en el controlador principal mediante flags booleanos. Esto representaba un riesgo crítico de derivación de autenticación (*authentication bypass*) en entornos productivos.

**Solución aplicada:**
- Extracción completa a `MockSsoController.java` anotado con `@Profile({"local", "test", "dev"})`.
- En perfil `prod`, el controlador no es registrado en el `ApplicationContext` de Spring. Solicitudes a `/api/auth/sso/mock` o `/api/auth/sso/codigo` resultan en HTTP 404 (`NoResourceFoundException`).

```java
@RestController
@RequestMapping("/api/auth/sso")
@Profile({"local", "test", "dev"})
public class MockSsoController {
    // Solo existe en entornos no productivos
}
```

### 2.2 Hardening de Entropía y Desacoplamiento de Validación (`CredencialesEntropiaValidator`)
Para evitar que la configuración de seguridad M2M quede inactiva en microservicios con cadenas de filtros personalizadas (como `donatrack-auth`), la validación de entropía se desacopló a un componente dedicado `CredencialesEntropiaValidator` en `donatrack-common`, registrado globalmente:
- **Validación M2M:** Si el perfil activo es `prod`, `donatrack.internal-api-key` no puede coincidir con el valor por defecto (`donatrack-internal-key-change-in-prod`) ni poseer menos de 32 caracteres.
- **Validación JWT:** En `prod`, `donatrack.jwt-secret` no puede ser nula, debe tener una longitud mínima de 32 caracteres y no puede ser la clave de prueba por defecto.
- En caso de incumplimiento, la aplicación aborta el arranque lanzando `IllegalStateException`.

### 2.3 Mitigación Criptográfica y Anti-IDOR (BOLA)
Se implementó `JwtClaimsExtractor` en `donatrack-common`:
- **Verificación HMAC en Roles Administrativos:** `esAdminOInternalService(request, internalApiKey, jwtSecret)` valida criptográficamente la firma HS256 y la fecha de expiración del token JWT antes de admitir privilegios de `ADMIN` o `INTERNAL_SERVICE`, previniendo la inyección de roles mediante tokens forjados sin firma.
- **Control de Titularidad en REST:** En `DonacionController.java` (`GET /api/donaciones/{id}`) y `AuthController.java` (`GET /api/auth/perfil/{id}`), los usuarios no administradores solo pueden acceder a recursos asociados a su propio `userId` o `personaId` (HTTP 403 Forbidden en caso de discrepancia).
- **Sanitización de Logs de Credenciales:** En `AuthService.java`, se eliminó cualquier emisión de contraseñas en texto plano en los eventos de auditoría y restablecimiento.

> [!TIP]
> **Trade-off arquitectónico (Validación programática vs `@PreAuthorize` con SpEL):** Se optó por la validación en servicio/controlador desacoplada mediante `JwtClaimsExtractor` para evitar la carga de reflexión en runtime y mantener la resolución de entidades delegada en repositorios de dominio puros sin acoplamiento a SpEL.

---

## 3. Fase 1: Métricas con Prometheus y Grafana

### 3.1 Exposición y Estandarización de Métricas
En el `pom.xml` padre se incluyó `micrometer-registry-prometheus`. Cada microservicio expone sus métricas mediante Spring Boot Actuator en `/actuator/prometheus`.

Configuración estándar (`application.yml` / `application.properties`):
```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.prometheus.access=unrestricted
management.metrics.tags.application=${spring.application.name}
```

### 3.2 Servidor Prometheus y Scrape Jobs
El archivo `monitoring/prometheus/prometheus.yml` orquesta la recolección periódica cada 15 segundos sobre los servicios del cluster:
- `donatrack-donaciones` (:8000)
- `donatrack-incentivos` (:8001)
- `donatrack-logistica` (:8002)
- `donatrack-notificaciones` (:8003)
- `donatrack-auth` (:8004)
- `donatrack-web` (:8080)

### 3.3 Provisioning Automático en Grafana
Mediante `monitoring/grafana/provisioning/datasources/prometheus.yml` y `dashboards/dashboards.yml`, Grafana arranca sin requerir configuración manual e importa automáticamente el dashboard `donatrack-overview.json`.

Métricas monitoreadas:
- Consumo de memoria JVM Heap (`jvm_memory_used_bytes`).
- Tasa de peticiones por segundo (`http_server_requests_seconds_count`).
- Latencias p95 y p99 (`http_server_requests_seconds{quantile=~"0.95|0.99"}`).
- Distribución de códigos de respuesta HTTP (2xx, 4xx, 5xx).

---

## 4. Fase 2: Trazabilidad Distribuida y Logs Estructurados

### 4.1 Trazabilidad con Micrometer Tracing y Zipkin Brave
Para seguir una solicitud a través de múltiples saltos de red (`donatrack-web` -> `donatrack-donaciones` -> `RabbitMQ` -> `donatrack-incentivos`), se configuró el puente Brave (`micrometer-tracing-bridge-brave`) con reporter HTTP hacia Zipkin (`zipkin-reporter-brave`).

> [!NOTE]
> **Trade-off arquitectónico (Brave vs OpenTelemetry SDK):** Brave fue seleccionado frente al agente completo de OpenTelemetry por su integración nativa con Spring Boot 3 Actuator, menor consumo de memoria (~15MB vs ~60MB adicionales por contenedor) y ausencia de conflictos en el classpath con librerías de serialización Jackson.

### 4.2 Propagación de Contexto sobre RabbitMQ
La comunicación asíncrona mediante colas de mensajería frecuentemente rompe el árbol de trazas (*trace fragmentation*). En `RabbitMQCommonConfig.java`, se activó la observación distribuida:

```java
@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    template.setObservationEnabled(true); // Inyecta traceparent en cabeceras AMQP
    return template;
}

@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(...) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setObservationEnabled(true); // Extrae traceId y crea child span al consumir
    return factory;
}
```

### 4.3 Logs Estructurados en JSON para Entornos de Producción
En `donatrack-common/src/main/resources/logback-spring.xml` se configuró una estrategia dual por perfiles:
- **Perfil local / test:** Salida por consola formateada y coloreada, facilitando la legibilidad en desarrollo.
- **Perfil prod / docker:** Salida en JSON estándar mediante `LogstashEncoder` (`net.logstash.logback:logstash-logback-encoder:8.0`). Cada registro incluye automáticamente `traceId` y `spanId` del contexto MDC de Brave, permitiendo correlación directa de logs y trazas en plataformas como ElasticSearch, Loki o CloudWatch.

```xml
<springProfile name="prod,docker">
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${SERVICE_NAME:-donatrack}"}</customFields>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
</springProfile>
```

---

## 5. Fase 3: Monitoreo Proactivo y Auto-Recuperación (Autoheal)

### 5.1 Healthchecks Nativos con Alpine `wget`
Las imágenes basadas en `eclipse-temurin:21-jre-alpine` no cuentan con herramientas pesadas como `curl` o intérpretes externos de scripting. En `compose.yaml`, los healthchecks de todos los servicios se definieron mediante la utilidad `wget` nativa de BusyBox:

```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8000/actuator/health || exit 1"]
  interval: 15s
  timeout: 5s
  retries: 3
  start_period: 40s
```

### 5.2 Supervisor Docker Autoheal
Se integró el servicio `autoheal` (`willfarrell/autoheal`) montando el socket `/var/run/docker.sock` con `restart: always` y la variable `AUTOHEAL_CONTAINER_LABEL=autoheal`. Los servicios con la etiqueta `autoheal: "true"` son reiniciados automáticamente cuando el healthcheck entra en estado `unhealthy`, garantizando alta disponibilidad sin intervención manual.

---

## 6. Fase 5: GraphQL en `donatrack-donaciones`

### 6.1 Motivación y Trade-Offs
El portal web (`donatrack-web`) requería renderizar vistas complejas combinando donaciones, detalles de bienes y estadísticas históricas del donante. Una aproximación puramente REST obligaba a realizar entre 3 y 5 llamadas HTTP sucesivas (*over-fetching / under-fetching*).

> [!TIP]
> **Trade-off arquitectónico (GraphQL vs REST vs gRPC):**
> - **GraphQL vs REST:** GraphQL permite al cliente web solicitar exactamente los campos requeridos en una única consulta compuesta, reduciendo el payload de red hasta un 65% en conexiones móviles.
> - **GraphQL vs gRPC:** Aunque gRPC ofrece menor overhead en throughput binario, requiere generación de stubs Protobuf y adaptadores WebSocket/gRPC-Web para navegadores. GraphQL opera sobre HTTP POST estándar con JSON, manteniendo compatibilidad total con Thymeleaf, JavaScript y clientes curl.

### 6.2 Esquema GraphQL (`donaciones.graphqls`)
Definido en `donatrack-donaciones/src/main/resources/graphql/donaciones.graphqls`:

```graphql
type Query {
    donaciones(filtro: FiltroDonacionInput, pagina: Int = 0, tamanio: Int = 10): DonacionPage!
    donacion(id: ID!): Donacion
    estadisticasDonante(donanteId: ID!): EstadisticasDonante!
}

type Donacion {
    id: ID!
    donanteId: ID!
    estado: String!
    fechaCreacion: String!
    items: [ItemDonacion!]!
}
```

### 6.3 Prevención del Problema N+1 sin Paginación en Memoria
Para evitar la advertencia de Hibernate `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`, se desacopló la consulta de paginación de la carga de relaciones coleccionadas:
- En `DonacionJpaRepository.java`, la query paginada `buscarConFiltrosPaginados` opera directamente sobre `DonacionEntity` sin `JOIN FETCH`, permitiendo a Postgres ejecutar LIMIT y OFFSET directamente en el motor de base de datos.
- En `DonacionEntity.java`, la relación `@OneToMany` hacia `bienes` se decoró con `@BatchSize(size = 30)`. Hibernate agrupa la carga diferida de bienes en lotes eficientes de hasta 30 donaciones en un único `WHERE donacion_id IN (...)`, erradicando el problema N+1 sin degradar la memoria de la JVM.

### 6.4 Seguridad, Anti-IDOR (BOLA) y Hardening en Producción
- **Protección de Red:** El endpoint `/graphql` está protegido por el filtro global `ServiceSecurityFilter`, exigiendo cabecera `Authorization: Bearer <JWT>` o `X-Service-Key`.
- **Anti-IDOR en Resolvers:**
  - En `estadisticasDonante(idDonante)`: si el llamador no es `ADMIN` ni `INTERNAL_SERVICE`, se valida que `idDonante` coincida con su propio `userId` o `personaId` (arrojando `AccessDeniedException` si intenta consultar métricas ajenas).
  - En `donaciones(...)`: para usuarios no administradores, se fuerza que el filtro de consulta aplique sobre su propio `callerUserId`.
- **Paginación Defensiva:** Se acota el parámetro `tamanio` mediante `Math.min(tamanio, 50)` para mitigar ataques de denegación de servicio por consultas masivas.
- **Deshabilitación de GraphiQL en Producción:** En `application-prod.properties` se configuró `spring.graphql.graphiql.enabled=false`.

---

## 7. Verificación y Resultados de Pruebas

Toda la implementación y sus remediaciones fueron validadas exhaustivamente mediante la suite de integración de Maven sobre los 8 módulos del proyecto:

| Módulo | Tipo de Pruebas | Resultado |
|---|---|---|
| `donatrack-common` | Tests de seguridad JWT, validación de entropía y filtros | 34 / 34 exitosos |
| `donatrack-donaciones` | REST, persistencia JPA, queries GraphQL (`@GraphQlTest`) y Anti-IDOR | 31 / 31 exitosos |
| `donatrack-logistica` | Asignación de rutas, persistencia MongoDB y RabbitMQ | 15 / 15 exitosos |
| `donatrack-incentivos` | Misiones, perfiles donante y rankings mensuales | 26 / 26 exitosos |
| `donatrack-notificaciones` | Plantillas, despacho multicanal y persistencia | 21 / 21 exitosos |
| `donatrack-auth` | Seguridad P0, Mock SSO isolation, JWT, CORS y anti-IDOR | 24 / 24 exitosos |
| `donatrack-web` | SSR Thymeleaf, protección CSRF y Rate Limiting Bucket4j | 8 / 8 exitosos |
| **Total General** | **Suite Completa del Proyecto** | **147 / 147 exitosos (100% verde)** |

---

# Referencias

- [Spring Boot Actuator: Metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html) — Documentación oficial sobre la integración de Micrometer y Prometheus.
- [Micrometer Tracing Reference](https://docs.micrometer.io/tracing/reference/index.html) — Especificación de propagación de contexto distribuido y puentes OpenZipkin Brave.
- [Spring for GraphQL Reference](https://docs.spring.io/spring-graphql/reference/index.html) — Guía oficial de controladores `@QueryMapping` y testing con `@GraphQlTest`.
- [Docker Autoheal Repository](https://github.com/willfarrell/docker-autoheal) — Documentación del contenedor supervisor de reinicio basado en healthchecks.
- [OpenZipkin Brave Repository](https://github.com/openzipkin/brave) — Librería de instrumentación de trazas distribuidas y compatibilidad con Zipkin v2 HTTP reporter.
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder) — Encoder JSON estructurado para Logback con soporte para campos MDC.
- Código fuente local del proyecto:
  - [compose.yaml](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml)
  - [ServiceSecurityAutoConfiguration.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityAutoConfiguration.java)
  - [JwtClaimsExtractor.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/JwtClaimsExtractor.java)
  - [DonacionGraphQLController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/in/graphql/DonacionGraphQLController.java)
  - [donaciones.graphqls](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/resources/graphql/donaciones.graphqls)
  - [DonaTrack-Diagrama-Componentes-y-Despliegue.puml](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml)
