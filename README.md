<div align="center">

# 📦 DonaTrack

**Sistema de Gestión y Trazabilidad de Donaciones**

*Trabajo Práctico Anual Integrador — Diseño de Sistemas (K3002) — 2026*

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Multi--Module-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)](https://swagger.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Render](https://img.shields.io/badge/Render-Deploy%20Live-46E3B7?logo=render&logoColor=black)](https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html)

</div>

---

## 📖 Contexto

**DonaTrack** es una solución digital desarrollada para [**UTN Solidaria**](https://www.frba.utn.edu.ar/), orientada a organizar, registrar y monitorear donaciones de bienes materiales desde su recepción en el depósito hasta su entrega a entidades beneficiarias.

La plataforma resuelve los desafíos de gestión y trazabilidad que enfrenta la organización, mejorando la distribución de recursos y fortaleciendo la transparencia frente a donantes y beneficiarios.

---

## 🌐 Despliegue en la Nube

Los microservicios se encuentran preparados y desplegados de forma distribuida en Render, vinculados a infraestructura gestionada:

- 🚀 **Logística Swagger UI:** [`https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html`](https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html)
- 📡 **Health Check:** [`https://donatrack-logistica-50xn.onrender.com/actuator/health`](https://donatrack-logistica-50xn.onrender.com/actuator/health)
- 🗄️ **Persistencia Cloud:** PostgreSQL en Supabase (schemas `donaciones`, `incentivos`, `notificaciones`, `auth`), MongoDB Atlas (`donatrack_logistica`) y Supabase S3 Storage para remitos.

---

## 🏗️ Arquitectura

El sistema está diseñado como una **arquitectura distribuida de microservicios**:

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   DonaTrack Platform                                   │
├──────────────┬──────────────┬──────────────┬───────────────────┬───────────────────────┤
│  Donaciones  │  Incentivos  │  Logística   │  Notificaciones   │  Autenticación (Auth) │
│    :8000     │    :8001     │    :8002     │       :8003       │         :8004         │
├──────────────┴──────────────┴──────────────┴───────────────────┴───────────────────────┤
│                         Cliente Web SSR (Thymeleaf + SLDS) :8080                       │
├────────────────────────────────┬───────────────────────────────────────────────────────┤
│      PostgreSQL 15 :5432       │                  MongoDB 7.0 :27017                   │
│   (donaciones, incentivos,     │                 (donatrack_logistica)                 │
│      notificaciones, auth)     │                                                       │
├────────────────────────────────┴───────────────────────────────────────────────────────┤
│                             RabbitMQ :5672 (Message Broker)                            │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                         n8n :5678 (automatización de flujos)                           │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

> Para más detalles sobre puertos, URLs de Swagger y comandos de ejecución, consultá [`SERVICIOS.md`](SERVICIOS.md).
> Para la guía de entornos (`local` vs `prod`), configuración de persistencia y seguridad M2M, consultá [`docs/PERFILES_Y_ENTORNOS.md`](docs/PERFILES_Y_ENTORNOS.md).
> Para el detalle del diseño SOA, arquitectura hexagonal y persistencia políglota, consultá [`docs/notas_y_borradores/arquitectura.md`](docs/notas_y_borradores/arquitectura.md).
> Para los diagramas formales en PlantUML, consultá [`docs/diagramas/`](docs/diagramas/).

---

## 🛠️ Tech Stack

| Capa | Tecnología | Detalle |
|---|---|---|
| **Lenguaje** | Java 21 | Long-Term Support (LTS) |
| **Framework** | Spring Boot 3.5 | Spring Data JPA, Spring Data MongoDB, OpenFeign, Spring Security, Springdoc OpenAPI |
| **Build** | Maven (multi-módulo) | Gestión centralizada de dependencias en `pom.xml` padre |
| **API Docs** | SpringDoc OpenAPI | Swagger UI interactivo por microservicio y unificado |
| **Base de Datos Relacional** | PostgreSQL 15 / Supabase / H2 | Schemas aislados (`donaciones`, `incentivos`, `notificaciones`, `auth`) |
| **Base de Datos NoSQL** | MongoDB 7.0 / Mongo Atlas | Persistencia documental para Logística (`donatrack_logistica`) |
| **Almacenamiento de Archivos** | Supabase S3 Storage | Almacenamiento de remitos de entrega y comprobantes |
| **Mensajería** | RabbitMQ | Topic Exchanges y colas asíncronas (`donaciones.exchange`, `logistica.exchange`) |
| **Contenedores** | Docker + Docker Compose | Builds multi-stage optimizados para local y Render |
| **Despliegue** | Render | Docker Web Services en la nube con perfiles independientes |
| **Automatización** | n8n | Flujos low-code para difusión en Discord y WhatsApp / Twilio |
| **Frontend SSR** | Thymeleaf + SLDS 2.30 | Salesforce Lightning Design System con layouts dinámicos (Público y Backoffice) |
| **Testing** | JUnit 5 + Mockito + H2 | Tests unitarios y de integración |

---

## 👥 Integrantes del Equipo

| Integrante | Legajo |
|---|---|
| Fernandez Cruz, Mateo | 209.986-0 |
| Hidalgo, Máximo Juan Manuel | 176.585-1 |

---

## 🚀 Entregas del Proyecto

El trabajo práctico se construye de manera evolutiva e incremental a lo largo de **7 entregas** (según la planificación oficial de la cátedra de Diseño de Sistemas - UTN BA):

---

### ✅ Entrega 1 — Arquitectura y Modelado en Objetos (Parte I)
> 📅 Viernes 22 de Mayo (Modalidad Presencial)

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Servicio de Donaciones** — Gestión de Donantes y Donaciones.
- **Servicio de Donaciones** — Importación masiva de donantes mediante archivo CSV.
- **Servicio de Donaciones** — Gestión de Entidades Beneficiarias y Necesidades.
- **Bocetos de interfaz de usuario.**

#### Dominio abordado
- Modelado de personas donantes (humanas y jurídicas) con medios de contacto.
- Registro de donaciones con segmentación automática por subcategoría.
- Entidades beneficiarias con necesidades recurrentes y extraordinarias.
- Importación masiva de donantes desde CSV (+10.000 filas).
- Bocetos de las principales interfaces de usuario.

#### Entregables
1. Modelo del Dominio (diagrama de clases por servicio).
2. Diagramas de Arquitectura (despliegue y componentes).
3. Justificaciones de Diseño Iniciales.
4. Diagrama General de Casos de Uso.
5. Bocetos de interfaz de usuario.
6. Implementación de los requerimientos.

</details>

---

### ✅ Entrega 2 — Arquitectura y Modelado en Objetos (Parte II)
> 📅 Viernes 19 de Junio (Modalidad Presencial)

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Servicio de Donaciones** — Trazabilidad de estados de las donaciones.
- **Servicio de Donaciones** — Asignación de donaciones a entidades beneficiarias.
- **Servicio de Incentivos** — Analítica de donantes, misiones e insignias.
- **Servicio de Incentivos** — Difusión de insignias y ranking mensual.
- **Servicio de Notificaciones** — Integración con medios de notificación (email, SMS, WhatsApp).
- **Exposición REST** de los servicios (Parte I).

#### Dominio abordado
- Máquina de estados de donaciones (En Depósito → Asignada → Lista para entregar → En traslado → Entregada / Vencida / Entrega fallida).
- Algoritmos de asignación: Compatibilidad Semántica y Prioridad a Sub-atendidos (ejecución asincrónica en horarios de baja carga).
- Sistema de incentivos con categorías (Colaborador, Sostenedor, Transformador), misiones secuenciales e insignias.
- Integración con n8n para publicación automática en redes sociales al obtener insignias.
- Ranking mensual de donantes más activos.
- Servicio de Notificaciones con envío simulado por email, SMS y WhatsApp.
- Eventos de notificación: inactividad de donante (+20 días), asignación de donación, cumplimiento de misión, cambio de categoría.

#### Entregables
1. Modelo del Dominio (diagrama de clases).
2. Diagrama de despliegue y componentes.
3. Justificaciones de Diseño y Diagramas Complementarios.
4. Implementación de requerimientos.
5. Implementación del flujo automatizado de publicación y difusión de insignias (n8n).

</details>

---

### ✅ Entrega 3 — Arquitectura y Modelado en Objetos (Parte III)
> 📅 Viernes 3 de Julio (Modalidad Presencial)

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Servicio de Logística** — Flota de camiones, choferes, planificación externa de rutas y trazabilidad de entregas.
- **Servicio de Donaciones** — Eventos de logística y notificaciones a donantes/entidades.
- **Revisión y maduración de Entregas 1 y 2** — Refactorización con patrones de diseño y desacoplamiento.
- **Exposición REST & Contenerización** — Documentación Swagger completa y Dockerización de cada servicio.

#### Dominio abordado
- **Flota y Logística**: Modelado de camiones (patente, capacidad en volumen m³, altura m, capacidad de carga kg) y choferes con legajo.
- **Planificación de Rutas Asíncrona**: Integración con componente externo en lotes de hasta 100 donaciones mediante endpoint de **Callback** (`/api/planificacion/callback`).
- **Trazabilidad de Entregas**: Ciclo de vida de la entrega (`Pendiente` → `En traslado` → `Entregada` / `No recibida`).
- **Comprobante de Recepción**: Registro de fecha/hora, camión responsable y fotos de la donación recibida.
- **Eventos Logísticos**: Notificaciones por inicio de ruta con enlace a mapa interactivo, entrega realizada exitosamente con comprobante, y reporte de entregas fallidas con justificación.

#### Maduración y Patrones aplicados (E1 y E2)
- **Segmentación clara**: Separación entre `DonacionOriginal` y donaciones segmentadas (`Donacion`), delegada mediante fachada (`ProcesadorCargaInicial`) y Strategy de segmentación.
- **Necesidades Recurrentes**: Modelo temporal con `PeriodoNecesidad` (`ABIERTA`, `SATISFECHA`) y renovación periódica automática (`RenovacionPeriodosJob`).
- **Importador CSV**: Detección y actualización de donantes duplicados por email (+10.000 filas) probado con tests unitarios.
- **Matchmaking**: Asignación con patrón Strategy (`CompatibilidadSemantica` y `PrioridadASubAtendidos`).
- **Incentivos y Gamificación**: Manejo de reset de progreso en misiones por inactividad ("Racha") e impacto directo de donaciones en el progreso.
- **Notificaciones**: Despacho multicanal con patrón Strategy (`EmailStrategy`, `SmsStrategy`, `WhatsAppStrategy`).

#### Entregables
1. **Modelo del Dominio**: Diagrama de clases por servicio en [`docs/diagramas/`](docs/diagramas/).
2. **Justificaciones de Diseño**: Revisión detallada en [`docs/notas_y_borradores/decisiones-arquitectura.md`](docs/notas_y_borradores/decisiones-arquitectura.md).
3. **APIs REST documentadas**: Swagger UI interactivo para cada microservicio y colección en [`docs/donatrack-api`](docs/donatrack-api).
4. **Contenerización Docker**: Dockerfiles multi-stage por módulo y orquestación con [`compose.yaml`](compose.yaml).
5. **Diagrama de Componentes y Despliegue**: Diagrama general de la solución en [`docs/diagramas/`](docs/diagramas/).

</details>

---

### ✅ Entrega 4 — Persistencia, Integración y Despliegue
> 📅 Viernes 9 de Octubre (Modalidad Sincrónica)

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Persistencia Relacional y ORM (JPA / Hibernate)** — Mapeo de entidades de dominio de todos los servicios a PostgreSQL con esquemas aislados (`donaciones`, `logistica`, `incentivos`, `notificaciones`).
- **Integración Asíncrona (RabbitMQ)** — Publicación y consumo de eventos desacoplados (`donaciones.exchange`, `logistica.exchange`) para notificaciones automáticas y trazabilidad.
- **Broker de Integración con Logística** — Componente `LogisticaBrokerAdapter` con patrón Broker y fallback automático entre servicio remoto en la nube y copia local.
- **Despliegue del Servicio de Logística en la Nube** — Desplegado y operativo en Render con conexión a PostgreSQL en Supabase.
- **Documentación de Arquitectura y DER Físicos** — Documento explicativo de arquitectura SOA/Hexagonal y diagramas de clases, componentes, despliegue y DER físicos por módulo.

#### Dominio y Arquitectura abordados
- Migración de todo el modelo de objetos a JPA con estrategias de herencia (`JOINED` para roles/personas y `SINGLE_TABLE` para necesidades).
- Aislamiento estricto por schemas PostgreSQL y soporte multi-entorno (`local`, `prod`, `test` con H2).
- Comunicación asíncrona mediante Topic Exchanges en RabbitMQ para eventos como inicio de ruta, entregas exitosas, cambios de estado y recompensas.
- Patrón Broker en `donatrack-donaciones` (`LogisticaBrokerAdapter`) con cliente Feign hacia la nube y fallback transparente a `localhost:8002`.
- Despliegue independiente del microservicio `donatrack-logistica` en Render conectado a Supabase.

#### Entregables
1. **Modelo de Clases actualizado**: [`DonaTrack-Diagrama-Clases.puml`](docs/diagramas/DonaTrack-Diagrama-Clases.puml).
2. **Modelo de Datos (DER Físicos)**: Diagramas entidad-relación físicos por microservicio en [`docs/diagramas/`](docs/diagramas/): [`DER-Donaciones`](docs/diagramas/donaciones/DER-Donaciones.puml), [`DER-Incentivos`](docs/diagramas/incentivos/DER-Incentivos.puml), [`DER-Logistica`](docs/diagramas/logistica/DER-Logistica.puml) y [`DER-Notificaciones`](docs/diagramas/notificaciones/DER-Notificaciones.puml).
3. **Justificaciones de Diseño y Documento de Arquitectura**: [`docs/notas_y_borradores/arquitectura.md`](docs/notas_y_borradores/arquitectura.md) (justificación de arquitectura SOA, hexagonal, capas y patrones).
4. **Diagrama de Componentes y Despliegue**: [`DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml).
5. **Implementación de requerimientos de integración**: Mensajería con RabbitMQ y Broker de logística con fallback.
6. **Despliegue del Servicio de Logística**: Operativo y accesible vía Swagger en [`https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html`](https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html).

</details>

---

### ✅ Entrega 5 — Persistencia (Persistencia Políglota)
> 📅 Viernes 30 de Octubre (Modalidad Sincrónica)

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Persistencia Políglota** — Desacoplamiento del esquema relacional monolítico y adopción de motores especializados según la naturaleza de cada Bounded Context.
- **Persistencia Documental en Logística (MongoDB 7.0)** — Migración completa de `donatrack-logistica` hacia MongoDB Atlas / local (`donatrack_logistica`) utilizando Spring Data MongoDB (ODM).
- **Modelo Jerárquico de Rutas sin JOINs** — Agregado raíz `RutaDeReparto` con paradas, entregas, comprobantes y coordenadas embebidas ($O(1)$ de lectura en consultas del chofer).
- **Snapshot Histórico Inmutable** — Almacenamiento desnormalizado de datos del chofer y vehículo en la ruta para garantizar auditoría legal inalterable.
- **Persistencia Relacional (PostgreSQL 15 / JPA)** — Schemas aislados (`donaciones`, `incentivos`, `notificaciones`, `auth`) en Supabase / local.
- **Almacenamiento de Archivos** — Integración con Supabase S3 Storage para remitos y fotos de entrega.
- **Mapeo BSON Binary $\leftrightarrow$ Java UUID** — Conversores `@ReadingConverter` y `@WritingConverter` en `MongoConfig.java`.

#### Entregables
1. **Modelo de Dominio y Datos Actualizado**: Diagrama de Clases [`DonaTrack-Diagrama-Clases.puml`](docs/diagramas/DonaTrack-Diagrama-Clases.puml) y Comunicación entre Servicios [`DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml`](docs/diagramas/DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml).
2. **Modelos de Persistencia (DDL y Scripts)**: Schemas relacionales en [`database/init.sql`](database/init.sql), datos semilla en [`database/local-seed-data.sql`](database/local-seed-data.sql) y colecciones NoSQL en [`database/mongo-init.js`](database/mongo-init.js).
3. **Memoria Técnica y Justificaciones**: [`docs/notas_y_borradores/arquitectura.md`](docs/notas_y_borradores/arquitectura.md) y matriz [`Justificación Decisiones - DonaTrack - Grupo 7.xlsx`](docs/notas_y_borradores/Justificación%20Decisiones%20-%20DonaTrack%20-%20Grupo%207.xlsx).
4. **Diagrama de Componentes**: Actualización con persistencia políglota y Supabase S3 Storage.
5. **Implementación de Código y Contenedores**: Servicios `postgres_donatrack` y `mongo_donatrack` en [`compose.yaml`](compose.yaml).

</details>

---

### 🚀 Entrega 6 — Interfaz de Usuario y Arquitectura MVC
> 📅 Viernes 13 de Noviembre (Modalidad Sincrónica) — *En curso / Próxima defensa*

<details open>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Cliente Liviano Desacoplado (`donatrack-web`)** — Aplicación web SSR independiente con Spring Boot 3.5 y Thymeleaf, consumiendo APIs REST del backend mediante `RestClient` y Bearer JWT sin acoplamiento a base de datos.
- **Servicio de Autenticación Centralizado (`donatrack-auth`)** — Servidor de identidad perimetral con Spring Security 6, emisión de JWT (HS256) y soporte de Single Sign-On (SSO / Social Login con Google y GitHub OAuth2).
- **Aislamiento Dual de Layouts (Diseño UI/UX)**:
  - **Layout Público (`layout-public.html`)**: Landing page moderna, amigable y accesible, catálogo de ONGs, mapa interactivo de donaciones entregadas y postulación de nuevas organizaciones.
  - **Layout Administrativo (`layout-admin.html`)**: Backoffice para personas administradoras construido íntegramente sobre blueprints oficiales de **Salesforce Lightning Design System (SLDS 2.30.3)** (Global Header, Vertical Navigation, Data Tables, Cards, Modals, Toasts) sin clases CSS inventadas.
- **Módulos y Portales por Rol**:
  - **Público**: Landing page con impacto mensual, mapa interactivo de entregas, registro y login.
  - **Portal Donante (`/app/donante`)**: Gamificación con insignias y progreso de misiones, cálculo de impacto ecológico/social, historial y alta de donaciones.
  - **Portal Beneficiaria (`/app/beneficiaria`)**: Declaración de necesidades materiales (recurrentes y extraordinarias), seguimiento de camiones en mapa y confirmación de recepción con fotos.
  - **Backoffice Administrador (`/app/admin`)**: Gestión de camiones y choferes con SLDS Data Table, confirmación de matchmaking y asignación de donaciones, auditoría de vencimientos en depósito, ranking mensual e importación batch de donantes por CSV (+10.000 filas).
- **Despliegue Distribuido Multi-Cuenta (Render + SaaS)**:
  - Cuenta 1: Core de datos (`donatrack-donaciones`, `donatrack-incentivos`, `donatrack-notificaciones`).
  - Cuenta 2: Capa de interacción y logística (`donatrack-web`, `donatrack-auth`, `donatrack-logistica`).
- **Accesibilidad y Usabilidad**: Cumplimiento de heurísticas de Nielsen y pautas de accesibilidad **WCAG 2.1 AA** (soporte ARIA, navegación por teclado, contraste y responsive design).

#### Entregables
1. **Cliente Liviano Desacoplado**: Módulo [`donatrack-web`](donatrack-web) renderizando dinámicamente vistas Thymeleaf.
2. **Servicio de Autenticación**: Módulo [`donatrack-auth`](donatrack-auth) con endpoints `/auth/login`, `/auth/register`, `/auth/exchange` y `/auth/profile`.
3. **Registro de Decisiones de Arquitectura (ADR)**: [`docs/notas_y_borradores/decisiones-arquitectura.md`](docs/notas_y_borradores/decisiones-arquitectura.md) (ADR-01 Desacoplamiento SSR, ADR-02 Dual Layouts, ADR-03 JWT & SSO, ADR-04 Despliegue Render).
4. **Memoria Técnica de Implementación E6**: [`docs/notas_y_borradores/entrega-6-implementacion-ssr-seguridad.md`](docs/notas_y_borradores/entrega-6-implementacion-ssr-seguridad.md).
5. **Guía de Demostración E2E y Pruebas**: [`docs/DEMO-PASO-A-PASO.md`](docs/DEMO-PASO-A-PASO.md) con validación paso a paso de flujos, comandos curl, consultas DBeaver/Mongo y troubleshooting.
6. **Colección de APIs Bruno**: [`docs/donatrack-api/bruno-collection`](docs/donatrack-api/bruno-collection) para testing automatizado con entornos `dev` y `prod`.

</details>

---

### ⏳ Entrega 7 — Observabilidad y Seguridad
> 📅 Viernes 4 de Diciembre (Modalidad Presencial) — *Próxima entrega*

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Herramientas de Observabilidad** — Recolección, visualización y análisis unificado de métricas, trazas distribuidas y logs centralizados para monitorear el estado y comportamiento de los microservicios.
- **Monitoreo Proactivo y Resiliencia** — Supervisión continua de salud de los servicios con políticas de autorestart ante fallos.
- **Seguridad y Control de Tráfico** — Implementación de Rate Limiting (limitación de solicitudes) para regular el tráfico entrante y prevenir abusos o sobrecargas en los microservicios.
- **Evolución de Integración de Servicios**:
  - *Opción 1:* Protocolo **gRPC** en reemplazo de HTTP para comunicaciones de alta performance y baja latencia entre servicios en tiempo real.
  - *Opción 2:* Interfaz **GraphQL** en el Servicio de Donaciones o Logística para permitir consultas dinámicas con selección granular de campos y filtros avanzados.

#### Entregables
1. **Diagrama de Componentes y Despliegue de la solución actual**.
2. **Implementación de Herramientas de Observabilidad, Monitoreo, Seguridad y Control**.
3. **Implementación de Web Service utilizando gRPC o GraphQL**.

</details>

---

## ⚡ Quick Start

```bash
# 1. Clonar el repositorio
git clone https://github.com/matefernandezcc/DonaTrack.git
cd DonaTrack

# 2. Configurar variables de entorno (plantilla oficial)
cp .env.template .env

# 3. Compilar todo el proyecto
make build

# 4. Levantar un microservicio individual en local (Makefile)
make donaciones      # Donaciones (Puerto 8000)
make incentivos      # Incentivos (Puerto 8001)
make logistica       # Logística (Puerto 8002)
make notificaciones  # Notificaciones (Puerto 8003)
make auth            # Autenticación (Puerto 8004)
make web             # Cliente Web SSR (Puerto 8080)

# O levantar toda la plataforma distribuida con Docker Compose:
docker compose up --build -d
```

> 📄 Ver [`SERVICIOS.md`](SERVICIOS.md) para la guía completa de puertos, Swagger interactivo, healthchecks y comandos.
> 📄 Ver [`docs/PERFILES_Y_ENTORNOS.md`](docs/PERFILES_Y_ENTORNOS.md) para la guía completa de perfiles (`local` vs `prod`), Docker y cloud.
> 📄 Ver [`docs/notas_y_borradores/arquitectura.md`](docs/notas_y_borradores/arquitectura.md) para la documentación técnica de arquitectura y diseño.
> 📄 Ver [`docs/diagramas/`](docs/diagramas/) para los diagramas de arquitectura y clases en formato PlantUML (`.puml`).

