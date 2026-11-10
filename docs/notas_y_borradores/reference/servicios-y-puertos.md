# 🛠️ Guía de Servicios - DonaTrack (Arquitectura Distribuida)

Este documento detalla los puertos, endpoints de documentación interactiva (Swagger UI / OpenAPI), healthchecks y comandos de ejecución para cada uno de los microservicios y servicios de soporte de la plataforma DonaTrack.

---

## 📌 Puertos y Swagger por Microservicio

| Microservicio | Módulo Maven | Puerto Local | Swagger UI URL | OpenAPI Docs (JSON) | Health Check |
|---|---|---|---|---|---|
| **Donaciones** | `donatrack-donaciones` | **8000** | [http://localhost:8000/swagger-ui/index.html](http://localhost:8000/swagger-ui/index.html) | [http://localhost:8000/v3/api-docs](http://localhost:8000/v3/api-docs) | [http://localhost:8000/actuator/health](http://localhost:8000/actuator/health) |
| **Incentivos** | `donatrack-incentivos` | **8001** | [http://localhost:8001/swagger-ui/index.html](http://localhost:8001/swagger-ui/index.html) | [http://localhost:8001/v3/api-docs](http://localhost:8001/v3/api-docs) | [http://localhost:8001/actuator/health](http://localhost:8001/actuator/health) |
| **Logística** | `donatrack-logistica` | **8002** | [http://localhost:8002/swagger-ui/index.html](http://localhost:8002/swagger-ui/index.html) | [http://localhost:8002/v3/api-docs](http://localhost:8002/v3/api-docs) | [http://localhost:8002/actuator/health](http://localhost:8002/actuator/health) |
| **Notificaciones** | `donatrack-notificaciones` | **8003** | [http://localhost:8003/swagger-ui/index.html](http://localhost:8003/swagger-ui/index.html) | [http://localhost:8003/v3/api-docs](http://localhost:8003/v3/api-docs) | [http://localhost:8003/actuator/health](http://localhost:8003/actuator/health) |
| **Autenticación (Auth)** | `donatrack-auth` | **8004** | [http://localhost:8004/swagger-ui/index.html](http://localhost:8004/swagger-ui/index.html) | [http://localhost:8004/v3/api-docs](http://localhost:8004/v3/api-docs) | [http://localhost:8004/actuator/health](http://localhost:8004/actuator/health) |
| **Cliente Web (SSR)** | `donatrack-web` | **8080** | N/A (Frontend SSR Thymeleaf + SLDS) | N/A | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |

---

## 🗄️ Servicios de Infraestructura y Soporte

| Servicio | Puerto Local | Detalle / Consola | Credenciales por Defecto |
|---|---|---|---|
| **PostgreSQL 15** | `5432` | Schemas relacionales: `donaciones`, `incentivos`, `notificaciones`, `auth` | `postgres` / `postgres` (DB: `donatrack`) |
| **MongoDB 7.0** | `27017` | Base documental NoSQL: `donatrack_logistica` (camiones, choferes, rutas, remitos) | `donatrack` / `donatrack` (Auth DB: `admin`) |
| **RabbitMQ AMQP** | `5672` | Broker de mensajería asíncrona | `donatrack` / `donatrack` |
| **RabbitMQ Management** | `15672` | Consola Web de Administración: [http://localhost:15672](http://localhost:15672) | `donatrack` / `donatrack` |
| **n8n Workflow Engine** | `5678` | Motor de automatización y webhooks: [http://localhost:5678](http://localhost:5678) | Configuración inicial en primer acceso |

---

## 🚀 Comandos de Compilación y Ejecución

### 1. Compilación del Proyecto
DonaTrack es un proyecto Maven multi-módulo. Podés compilarlo y empaquetar todos los artefactos `.jar` de forma centralizada:

```bash
# Compilar todos los módulos con Maven
mvn clean package -DskipTests

# O utilizando el Makefile
make build
```

### 2. Levantar Todo el Stack con Docker
Para compilar imágenes y levantar la plataforma completa (Postgres, Mongo, RabbitMQ, n8n y los 6 microservicios):

```bash
# Levantar stack completo en segundo plano
docker compose up --build -d

# Detener todos los contenedores
docker compose down
```

### 3. Levantar Servicios Individuales (Desarrollo Local con Makefile)
Para levantar servicios de forma aislada apuntando al perfil local (`PROFILE=local` por defecto):

```bash
make donaciones      # Donaciones (Puerto 8000)
make incentivos      # Incentivos (Puerto 8001)
make logistica       # Logística (Puerto 8002)
make notificaciones  # Notificaciones (Puerto 8003)
make auth            # Autenticación (Puerto 8004)
make web             # Cliente Web SSR (Puerto 8080)
```

---

## 🧭 Diagramas de Arquitectura y Clases (PlantUML)
Los modelos visuales formales del proyecto se encuentran en formato UML / PlantUML (`.puml`):
- Diagrama de Componentes y Despliegue: [`docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](docs/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml)
- Diagrama de Clases General: [`docs/diagramas/DonaTrack-Diagrama-Clases.puml`](docs/diagramas/DonaTrack-Diagrama-Clases.puml)
- Diagrama de Comunicación entre Servicios: [`docs/diagramas/DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml`](docs/diagramas/DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml)
