# Guía de Servicios - DonaTrack (Arquitectura Distribuida)

Este documento detalla los puertos y URLs de Swagger para cada uno de los microservicios distribuidos de DonaTrack, así como las instrucciones de construcción y despliegue.

## Puertos y Swagger por Servicio

| Nombre de Servicio | Módulo Maven | Puerto Local | Swagger UI URL | OpenAPI JSON (JSON docs) |
|---|---|---|---|---|
| **Donaciones** | `donatrack-donaciones` | **8000** | [http://localhost:8000/swagger-ui/index.html](http://localhost:8000/swagger-ui/index.html) | [http://localhost:8000/v3/api-docs](http://localhost:8000/v3/api-docs) |
| **Incentivos** | `donatrack-incentivos` | **8001** | [http://localhost:8001/swagger-ui/index.html](http://localhost:8001/swagger-ui/index.html) | [http://localhost:8001/v3/api-docs](http://localhost:8001/v3/api-docs) |
| **Logística** | `donatrack-logistica` | **8002** | [http://localhost:8002/swagger-ui/index.html](http://localhost:8002/swagger-ui/index.html) | [http://localhost:8002/v3/api-docs](http://localhost:8002/v3/api-docs) |
| **Notificaciones** | `donatrack-notificaciones` | **8003** | [http://localhost:8003/swagger-ui/index.html](http://localhost:8003/swagger-ui/index.html) | [http://localhost:8003/v3/api-docs](http://localhost:8003/v3/api-docs) |
| **Server (General/Gateway)** | `donatrack-server` | **8080** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

### Servicios de Terceros
- **n8n**: `http://localhost:5678` (Plataforma de automatización de workflows)
- **Base de Datos PostgreSQL**: `localhost:5432` (Mismo contenedor de base de datos compartida, utilizando diferentes esquemas en base a la variable `currentSchema`).
- **RabbitMQ**:
  - Puerto de AMQP (mensajería): `5672`
  - Consola de Administración (Management UI): [http://localhost:15672](http://localhost:15672) (Credenciales por defecto: `donatrack` / `donatrack`)

---

## Cómo compilar todo el proyecto

DonaTrack es un proyecto Maven multi-módulo. Puedes compilar y empaquetar todos los módulos de una sola vez desde la raíz del proyecto sin necesidad de entrar a cada carpeta individualmente:

```bash
mvn clean package -DskipTests
```

Esto generará los archivos ejecutables `.jar` en la carpeta `target/` de cada módulo (ej. `donatrack-logistica/target/donatrack-logistica-0.0.1-SNAPSHOT.jar`).

---

## Cómo levantar todos los servicios con Docker

Una vez que hayas empaquetado los archivos `.jar` localmente, puedes compilar las imágenes y levantar el stack completo de contenedores de Docker en segundo plano ejecutando:

```bash
# 1. Compilar los jars
mvn clean package -DskipTests

# 2. Levantar la base de datos, n8n y todos los servicios de DonaTrack
docker compose up --build -d
```

Este proceso:
- Utiliza la base de datos PostgreSQL (`db_donatrack`) compartida.
- Configura de forma transparente en cada contenedor la variable de entorno `SPRING_DATASOURCE_URL` apuntando a su respectivo esquema de la base de datos (`donaciones`, `incentivos`, `logistica`, `notificaciones` y `public`).

---

## Cómo levantar un servicio específico de forma local (Desarrollo)

Si estás desarrollando y quieres levantar un único servicio de forma local sin usar Docker, puedes hacerlo ejecutando los atajos de `make`:

```bash
# Para Donaciones (Puerto 8000)
make donaciones

# Para Incentivos (Puerto 8001)
make incentivos

# Para Logística (Puerto 8002)
make logistica

# Para Notificaciones (Puerto 8003)
make notificaciones

# Para Server (Puerto 8080)
make server
```

