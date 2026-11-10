# 📖 Guía Canónica de Perfiles y Entornos de Ejecución (DonaTrack)

DonaTrack implementa una **arquitectura distribuida de microservicios con persistencia políglota**, orientada a desacoplar responsabilidades funcionales y optimizar los motores de almacenamiento según las necesidades de cada *Bounded Context*:
* **Donaciones, Incentivos, Notificaciones y Autenticación:** PostgreSQL (relacional con esquemas dedicados).
* **Logística:** MongoDB (documental pura / ODM Spring Data Mongo).
* **Mensajería Asíncrona:** RabbitMQ (AMQP Topic Exchanges).
* **Automatización y Webhooks:** n8n Workflow Engine.
* **Presentación e Interacción:** Cliente Web SSR (Spring Boot + Thymeleaf + SLDS).

Este documento especifica de forma unificada la filosofía, configuración técnica y modos de operación de los dos perfiles principales del sistema: **`local`** y **`prod`**.

---

## 1. Filosofía de los Perfiles: Agilidad vs Seguridad Estricta

```
┌────────────────────────────────────────────────────────────────────────┐
│                        DonaTrack Profile Philosophy                    │
├──────────────────────────────────┬─────────────────────────────────────┤
│        Perfil LOCAL              │          Perfil PROD                │
├──────────────────────────────────┼─────────────────────────────────────┤
│  ⚡ Agilidad y Cero Fricción     │  🛡️ Seguridad Estricta Fail-Closed   │
│  📦 Docker Local (Postgres/Mongo)│  ☁️ Cloud SaaS (Supabase/Atlas/AMQP)│
│  🔓 M2M Security: false (Relaxed)│  🔒 M2M Security: true (Mandatory)  │
│  🛠️ Hibernate ddl-auto: update   │  🚫 Hibernate ddl-auto: none        │
│  🧪 Mock SSO: Habilitado         │  ⛔ Mock SSO: Deshabilitado (403)   │
│  🌱 Auto-Seeding de Usuarios     │  🧱 Esquemas estables y auditables  │
└──────────────────────────────────┴─────────────────────────────────────┘
```

### 1.1. Perfil `local` (Desarrollo y Testing Ágil)
* **Objetivo:** Permitir a cualquier desarrollador levantar el ecosistema completo o servicios aislados en su propia máquina de manera rápida y sin requerir cuentas cloud ni tokens externos.
* **Características:**
  * **Persistencia Local:** Se conecta a PostgreSQL en `localhost:5432`, MongoDB en `localhost:27017` y RabbitMQ en `localhost:5672` (orquestados mediante `compose.yaml`).
  * **Evolución Dinámica del Esquema:** `spring.jpa.hibernate.ddl-auto=update` crea y adapta las tablas automáticamente sin intervención manual.
  * **Seguridad M2M Relajada:** `donatrack.security.enabled=false`. Los microservicios permiten peticiones directas vía cURL, Postman o Bruno sin exigir cabeceras `X-Service-Key` ni JWT Bearer.
  * **Mock Social Login:** `donatrack.auth.mock-sso.enabled=true`. El endpoint `/api/auth/sso/codigo` y la ruta `/oauth2/authorization/{provider}` simulan el flujo OAuth2 de Google y GitHub instantáneamente, permitiendo pruebas sin configurar una app en Google Cloud Console.
  * **Datos Semilla:** `AuthDataSeeder` garantiza la disponibilidad de usuarios estándar (`admin/admin`, `donante/donante`, `beneficiaria/beneficiaria`).

### 1.2. Perfil `prod` (Producción en la Nube y Blindaje)
* **Objetivo:** Operar con máxima resiliencia, rendimiento predecible y cumplimiento de políticas de seguridad estricta en entornos distribuidos (Render y servicios SaaS).
* **Características:**
  * **Infraestructura Cloud Cifrada:** PostgreSQL en Supabase Session Pooler con TLS, MongoDB Atlas NoSQL con TLS, CloudAMQP con puerto SSL `5671` y Supabase S3 Storage para comprobantes.
  * **Integridad Estricta de Esquemas:** `spring.jpa.hibernate.ddl-auto=none`. Hibernate no realiza escaneos de modificación sobre la base productiva, reduciendo tiempos de arranque en frío y previniendo alteraciones accidentales.
  * **Seguridad M2M Obligatoria (Fail-Closed):** `donatrack.security.enabled=true`. Toda llamada inter-servicio que no pertenezca a rutas públicas explícitas (`/actuator/**`, `/v3/api-docs/**`, `/auth/**`) debe contener una cabecera `X-Service-Key` válida o un token JWT (HS256) emitido legítimamente.
  * **Bypass de Autenticación Bloqueado:** `donatrack.auth.mock-sso.enabled=false`. El endpoint `/api/auth/sso/codigo` responde con `403 Forbidden` y no permite la emisión de tokens sin pasar por proveedores OAuth2 reales.
  * **Pool de Conexiones Optimizado:** HikariCP configurado con `maximum-pool-size: 3` por instancia para respetar los límites de conexiones globales de Supabase Free Tier.

---

## 2. Matriz de Persistencia y Configuración por Servicio

| Microservicio | Puerto | Motor de Persistencia | Esquema / Colección (Local) | Esquema / Colección (Prod) | Seguridad M2M (Local / Prod) | DDL Auto (Local / Prod) |
|---|---|---|---|---|---|---|
| **donatrack-auth** | `8004` | PostgreSQL (JPA) | `donatrack` (schema `auth`) | Supabase (schema `public`) | `false` / `true` | `update` / `none` |
| **donatrack-donaciones** | `8000` | PostgreSQL (JPA) | `donatrack` (schema `donaciones`) | Supabase (schema `donaciones`) | `false` / `true` | `update` / `none` |
| **donatrack-incentivos** | `8001` | PostgreSQL (JPA) | `donatrack` (schema `incentivos`) | Supabase (schema `incentivos`) | `false` / `true` | `update` / `none` |
| **donatrack-logistica** | `8002` | MongoDB (ODM) + S3 | `donatrack_logistica` (Local) | `donatrack_logistica` (Atlas) + Supabase S3 | `false` / `true` | Auto-index |
| **donatrack-notificaciones** | `8003` | PostgreSQL (JPA) | `donatrack` (schema `notificaciones`) | Supabase (schema `notificaciones`) | `false` / `true` | `update` / `none` |
| **donatrack-web** | `8080` | Stateless (SSR) | N/A (Cliente REST con JWT) | N/A (Cliente REST con JWT) | Emite `X-Service-Key` | N/A |

---

## 3. Variables de Entorno Clave (`.env`)

La alternancia entre perfiles se controla principalmente mediante dos variables en `.env`:

```env
# 1. Perfil activo en las aplicaciones Spring Boot ("local" o "prod")
SPRING_PROFILES_ACTIVE=local

# 2. Perfil de Docker Compose:
#    - "local": Levanta contenedores de Postgres, Mongo, RabbitMQ y n8n locales.
#    - "" (vacío): Excluye los contenedores locales para usar directamente Supabase, Atlas y CloudAMQP.
COMPOSE_PROFILES=local
```

### Tabla de Parámetros de Configuración

| Variable de Entorno | Valor Local Típico | Valor Prod Típico | Propósito |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | `prod` | Activa las propiedades `application-local` o `application-prod`. |
| `COMPOSE_PROFILES` | `local` | `""` | Define si Docker Compose instancia bases de datos locales. |
| `DONATRACK_SECURITY_ENABLED`| `false` | `true` | Gobierna el filtro de cabecera `X-Service-Key` M2M. |
| `INTERNAL_API_KEY` | `donatrack-internal-key...` | *(Secreto robusto)* | Clave precompartida para comunicación entre servicios backend. |
| `JWT_SECRET` | *(Clave HS256 >= 32 bytes)* | *(Secreto robusto)* | Firma simétrica de tokens JWT. |
| `POSTGRES_HOST` | `localhost` / `postgres-donatrack` | *(No aplica en prod)* | Host del PostgreSQL local. |
| `SUPABASE_HOST` | *(Opcional en local)* | `aws-1-us-east-1.pooler...` | Host del Session Pooler de Supabase. |
| `MONGO_HOST` | `localhost` / `mongo-donatrack` | *(No aplica en prod)* | Host del MongoDB local. |
| `MONGO_ATLAS_URI` | *(Opcional en local)* | `mongodb+srv://...` | Cadena de conexión cifrada a MongoDB Atlas. |
| `RABBITMQ_HOST` | `localhost` / `rabbitmq` | *(No aplica en prod)* | Broker RabbitMQ local (puerto 5672). |
| `CLOUDAMQP_HOST` | *(Opcional en local)* | `jackal.rmq.cloudamqp.com` | Broker CloudAMQP (puerto 5671 con TLS). |

---

## 4. Modos de Ejecución Operativa

### Modo 1: Local Completo con Docker Compose (Recomendado para Desarrollo General)
Levanta todos los motores de base de datos, colas y los 6 microservicios en contenedores locales:

```bash
# 1. Copiar plantilla de entorno
cp .env.template .env

# 2. Asegurar que .env tenga:
# SPRING_PROFILES_ACTIVE=local
# COMPOSE_PROFILES=local

# 3. Inicializar entorno y bases de datos con Makefile
make setup

# O alternativamente con Docker Compose:
docker compose up --build -d
```

### Modo 2: Microservicios Aislados en Local (Para Depuración en IDE)
Levanta únicamente los motores de persistencia en Docker y corre un microservicio específico desde la terminal o IDE:

```bash
# Iniciar contenedores de soporte
docker compose --profile local up -d postgres-donatrack mongo-donatrack rabbitmq

# Iniciar un servicio específico (ej: Donaciones en puerto 8000)
make donaciones

# O con Maven directamente:
mvn spring-boot:run -pl donatrack-donaciones -Dspring-boot.run.profiles=local
```

### Modo 3: Híbrido (Ejecución Local conectada a Servicios Cloud)
Ideal para verificar comportamientos contra Supabase o MongoDB Atlas sin necesidad de desplegar en Render:

```bash
# 1. Configurar .env con credenciales Cloud y COMPOSE_PROFILES vacío:
# SPRING_PROFILES_ACTIVE=prod
# COMPOSE_PROFILES=

# 2. Ejecutar el servicio deseado con perfil prod:
make server PROFILE=prod
make logistica PROFILE=prod

# O con Maven directamente:
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run -pl donatrack-web
```

### Modo 4: Despliegue en Render (Producción)
En Render, cada servicio se despliega como un servicio web Docker independiente configurando `SPRING_PROFILES_ACTIVE=prod` en las variables de entorno del panel de control. Docker Compose no interviene en Render; los contenedores leen las variables directamente del entorno del sistema operativo.

---

## 5. Verificación de Salud de los Servicios (`Health Checks`)

Todos los microservicios exponen endpoints de Spring Boot Actuator para auditar su estado:

| Servicio | URL Health Check | Componentes Inspeccionados |
|---|---|---|
| **Web SSR** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | Estado del servidor web y conectividad general. |
| **Auth** | [http://localhost:8004/actuator/health](http://localhost:8004/actuator/health) | Conexión relacional PostgreSQL (`auth.usuarios` o `public.usuarios`). |
| **Donaciones** | [http://localhost:8000/actuator/health](http://localhost:8000/actuator/health) | Conexión PostgreSQL (schema `donaciones`) y RabbitMQ. |
| **Incentivos** | [http://localhost:8001/actuator/health](http://localhost:8001/actuator/health) | Conexión PostgreSQL (schema `incentivos`) y RabbitMQ. |
| **Logística** | [http://localhost:8002/actuator/health](http://localhost:8002/actuator/health) | Conexión documental MongoDB (`mongo` status UP) y RabbitMQ. |
| **Notificaciones** | [http://localhost:8003/actuator/health](http://localhost:8003/actuator/health) | Conexión PostgreSQL (schema `notificaciones`) y RabbitMQ. |

Respuesta JSON esperada (`HTTP 200 OK`):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": { "database": "PostgreSQL" }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 6. Resolución de Problemas Frecuentes (Troubleshooting)

1. **Error: `relation "auth.usuarios" does not exist`**:
   * *Causa:* Se ejecutó el perfil `local` contra la base de Supabase (donde las tablas de Auth residen en el esquema `public`).
   * *Solución:* Usar `SPRING_PROFILES_ACTIVE=prod` al conectarse a Supabase, o levantar el PostgreSQL local con `docker compose --profile local up -d postgres-donatrack`.
2. **Error `401 Unauthorized: Acceso no autorizado: se requiere cabecera X-Service-Key o Bearer JWT valido`**:
   * *Causa:* Se está invocando directamente un microservicio backend teniendo activo el perfil `prod` (donde la seguridad M2M está encendida).
   * *Solución:* Incluir la cabecera `X-Service-Key: <INTERNAL_API_KEY>` o realizar las pruebas a través del cliente Web SSR (`donatrack-web`), el cual inyecta automáticamente esta cabecera mediante `ApiClientsConfig.java`.
3. **Error `403 Forbidden` al invocar `/api/auth/sso/codigo`**:
   * *Causa:* El perfil activo es `prod`. Por razones de seguridad, el bypass de autenticación por mock SSO está deshabilitado en producción.
   * *Solución:* Utilizar el flujo de login normal por credenciales (`/auth/login`) o cambiar a perfil `local` si se desea simulación rápida de cuentas.

---

## # Referencias

- [`donatrack-common/.../ServiceSecurityFilter.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/security/ServiceSecurityFilter.java)
- [`donatrack-auth/.../AuthController.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/java/com/donatrack/auth/infrastructure/adapters/in/api/AuthController.java)
- [`donatrack-auth/src/main/resources/application.yml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-auth/src/main/resources/application.yml)
- [`donatrack-donaciones/src/main/resources/application-prod.properties`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/resources/application-prod.properties)
- [`donatrack-logistica/src/main/resources/application-prod.properties`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/resources/application-prod.properties)
- [`compose.yaml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/compose.yaml)
- [Documentación Oficial de Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Documentación Oficial de Supabase Connection Pooling](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler)
