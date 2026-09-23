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

## 🌐 Despliegue en la Nube (Entrega 4)

El microservicio de **Logística** se encuentra desplegado de forma independiente en la nube, conectado a una base de datos PostgreSQL en Supabase:

- 🚀 **Swagger UI (Producción):** [`https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html`](https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html)
- 📡 **Health Check:** [`https://donatrack-logistica-50xn.onrender.com/actuator/health`](https://donatrack-logistica-50xn.onrender.com/actuator/health)
- 🗄️ **Base de Datos Cloud:** PostgreSQL en Supabase (Schema `logistica`, Connection Pooler AWS US-East-1)

---

## 🏗️ Arquitectura

El sistema está diseñado como una **arquitectura distribuida de microservicios**:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DonaTrack Platform                          │
├──────────────┬──────────────┬──────────────┬───────────────────────┤
│  Donaciones  │  Incentivos  │  Logística   │   Notificaciones      │
│   :8000      │   :8001      │   :8002      │      :8003            │
├──────────────┴──────────────┴──────────────┴───────────────────────┤
│                     Server / Gateway :8080                          │
├────────────────────────────────┬────────────────────────────────────┤
│     PostgreSQL :5432           │         RabbitMQ :5672             │
│     (schemas por servicio)     │         (Message Broker)           │
├────────────────────────────────┴────────────────────────────────────┤
│                             n8n :5678                               │
│                     (automatización de flujos)                      │
└─────────────────────────────────────────────────────────────────────┘
```

> Para más detalles sobre puertos, URLs de Swagger y comandos de ejecución, consultá [`SERVICIOS.md`](SERVICIOS.md).
> Para el detalle del diseño SOA y arquitectura hexagonal, consultá [`docs/arquitectura.md`](docs/arquitectura.md).

---

## 🛠️ Tech Stack

| Capa | Tecnología | Detalle |
|---|---|---|
| **Lenguaje** | Java 21 | Long-Term Support (LTS) |
| **Framework** | Spring Boot 3.5 | Spring Data JPA, OpenFeign, Springdoc OpenAPI |
| **Build** | Maven (multi-módulo) | Gestión centralizada de dependencias en `pom.xml` padre |
| **API Docs** | SpringDoc OpenAPI | Swagger UI interactivo por microservicio y unificado |
| **Base de Datos** | PostgreSQL 15 / Supabase / H2 | Schemas aislados (`donaciones`, `logistica`, `incentivos`, `notificaciones`) |
| **Mensajería** | RabbitMQ | Topic Exchanges y colas asíncronas |
| **Contenedores** | Docker + Docker Compose | Builds multi-stage optimizados para local y Render |
| **Despliegue** | Render | Docker Web Service en la nube |
| **Automatización** | n8n | Flujos low-code para difusión en redes sociales |
| **Testing** | JUnit 5 + Mockito + H2 | Tests unitarios y de persistencia JPA |

---

## 👥 Integrantes del Equipo

| Integrante | Legajo |
|---|---|
| Fernandez Cruz, Mateo | 209.986-0 |
| Barco Palacios, Ezequiel | 208.891-5 |
| Calabro, Lucio | 222.171-8 |
| Di Giuseppe, Tomas | 176.745-8 |
| Coaquira, Hugo | 222.228-0 |
| Ferrufino, Alan | 222.304-1 |
| Hidalgo, Máximo Juan Manuel | 176.585-1 |
| Vargas, Tomás Ezequiel | 209.874-0 |

---

## 🚀 Entregas del Proyecto

El trabajo práctico se construye de manera evolutiva e incremental a lo largo de 6 entregas (según el cronograma oficial de la cátedra):

---

### ✅ Entrega 1 — Arquitectura y Modelado en Objetos (Parte I)
> 📅 22 de Mayo

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
> 📅 19 de Junio

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
> 📅 3 de Julio

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
1. **Modelo del Dominio**: Diagrama de clases por servicio en [`diagramas/`](diagramas/).
2. **Justificaciones de Diseño**: Revisión detallada en [`_notas_y_borradores/pendientes/entrega_3/revision_entrega_requerimiento.md`](_notas_y_borradores/pendientes/entrega_3/revision_entrega_requerimiento.md).
3. **APIs REST documentadas**: Swagger UI interactivo para cada microservicio y colección en [`docs/donatrack-api`](docs/donatrack-api).
4. **Contenerización Docker**: Dockerfiles multi-stage por módulo y orquestación con [`compose.yaml`](compose.yaml).
5. **Diagrama de Componentes y Despliegue**: Diagrama general de la solución en [`diagramas/`](diagramas/).

</details>

---

### ✅ Entrega 4 — Persistencia, Integración y Despliegue
> 📅 Semana del 14 de Septiembre

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
1. **Modelo de Clases actualizado**: [`DonaTrack-Diagrama-Clases-Entrega4.puml`](diagramas/DonaTrack-Diagrama-Clases-Entrega4.puml).
2. **Modelo de Datos (DER Físicos)**: Diagramas entidad-relación físicos por servicio en [`diagramas/`](diagramas/) (`DER-Donaciones`, `DER-Logistica`, `DER-Incentivos`, `DER-Notificaciones`).
3. **Justificaciones de Diseño y Documento de Arquitectura**: [`docs/arquitectura.md`](docs/arquitectura.md) (justificación de arquitectura SOA, hexagonal, capas y patrones).
4. **Diagrama de Componentes y Despliegue**: [`DonaTrack-Diagrama-Componentes-y-Despliegue-Entrega4.puml`](diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue-Entrega4.puml).
5. **Implementación de requerimientos de integración**: Mensajería con RabbitMQ y Broker de logística con fallback.
6. **Despliegue del Servicio de Logística**: Operativo y accesible vía Swagger en [`https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html`](https://donatrack-logistica-50xn.onrender.com/swagger-ui/index.html).

</details>

---

### ⏳ Entrega 5 — Arquitectura Web MVC
> 📅 Semana del 19 de Octubre

*Los detalles de esta etapa se actualizarán próximamente.*

---

### ⏳ Entrega 6 — Despliegue, Observabilidad y Seguridad
> 📅 Semana del 23 de Noviembre

*Los detalles de esta etapa se actualizarán próximamente.*

---

## ⚡ Quick Start

```bash
# Clonar el repositorio
git clone https://github.com/matefernandezcc/DonaTrack.git
cd DonaTrack

# Compilar todo el proyecto
make build

# Levantar un servicio individual
make donaciones      # Puerto 8000
make incentivos      # Puerto 8001
make logistica       # Puerto 8002
make notificaciones  # Puerto 8003
make server          # Puerto 8080

# Levantar toda la plataforma con Docker (Postgres, RabbitMQ, n8n y servicios)
docker compose up --build -d
```

> 📄 Ver [`SERVICIOS.md`](SERVICIOS.md) para la guía completa de puertos, Swagger y comandos.
> 📄 Ver [`docs/arquitectura.md`](docs/arquitectura.md) para la documentación técnica de arquitectura y diseño.

