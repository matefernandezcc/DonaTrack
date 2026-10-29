# 📖 Manual de Operación: Perfiles de Entorno (Local vs Producción)

DonaTrack implementa una **arquitectura de persistencia políglota / multi-motor**:
* **Donaciones, Incentivos y Notificaciones:** PostgreSQL (relacional con esquemas dedicados).
* **Logística:** MongoDB (base de datos documental pura / ODM).
* **Mensajería:** RabbitMQ (AMQP).

Este manual describe cómo alternar de forma limpia entre el entorno **local** (Docker) y el entorno de **producción** (Supabase + MongoDB Atlas + CloudAMQP).

---

## 1. Variables de Configuración Centrales

La configuración se gestiona a través del archivo `.env`:

| Variable | Local | Producción | Descripción |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | `prod` | Perfil activo de Spring Boot en los microservicios |
| `COMPOSE_PROFILES` | `local` | `""` *(o sin declarar)* | Perfiles de Docker Compose para levantar o excluir infraestructura local |

---

## 2. Modo Desarrollo Local

En este modo, toda la infraestructura de base de datos y mensajería corre en contenedores Docker locales:
* **PostgreSQL:** `localhost:5432` (`postgres:15-alpine`)
* **MongoDB:** `localhost:27017` (`mongo:7-jammy`)
* **RabbitMQ:** `localhost:5672` (`rabbitmq:4.3.2-management-alpine`)
* **n8n:** `localhost:5678` (`n8n:2.39.7`)

### Paso 1: Configurar el `.env`
Verificar que tu archivo `.env` tenga:
```env
SPRING_PROFILES_ACTIVE=local
COMPOSE_PROFILES=local
```

### Paso 2: Levantar Infraestructura Local
Gracias a `COMPOSE_PROFILES=local`, Docker Compose iniciará automáticamente los contenedores de base de datos y broker:
```bash
# Opción A: Mediante Makefile (inicializa BD e importa workflows de n8n)
make setup

# Opción B: Directo con Docker Compose
docker compose up -d
```

### Paso 3: Ejecutar los Servicios
Podés correr cualquier servicio individualmente o el servidor general:
```bash
# Iniciar Logística (puerto 8002 - conecta a Docker Mongo)
make logistica

# Iniciar Donaciones (puerto 8000 - conecta a Docker Postgres)
make donaciones

# O iniciar el Monolito/Gateway Donatrack Server (puerto 8080)
make server
```

---

## 3. Modo Producción (Cloud Services)

En modo producción, los servicios se conectan a los motores de persistencia en la nube:
* **PostgreSQL:** Supabase Pooler (`aws-1-us-east-1.pooler.supabase.com:5432`)
* **MongoDB:** MongoDB Atlas (`donatrack.ofn4ag4.mongodb.net`)
* **RabbitMQ:** CloudAMQP (`jackal.rmq.cloudamqp.com:5671`)

### Opción A: Ejecución Local apuntando a Cloud (Híbrido)
Útil para probar o depurar contra las bases de datos de producción desde tu máquina.

1. **Definir el perfil al invocar el comando:**
   ```bash
   # Logística conectando a MongoDB Atlas
   make logistica PROFILE=prod

   # Servidor general conectando a Supabase y MongoDB Atlas
   make server PROFILE=prod

   # O directamente con Maven
   SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run -pl donatrack-logistica
   ```

2. **(Opcional) Cambiar el default en `.env`:**
   ```env
   SPRING_PROFILES_ACTIVE=prod
   ```

---

### Opción B: Ejecución en Docker Compose modo Producción (App-Only)
Si deseás levantar los microservicios en contenedores Docker locales pero consumiendo los servicios Cloud:

1. **Ajustar `.env`:**
   ```env
   SPRING_PROFILES_ACTIVE=prod
   COMPOSE_PROFILES=
   ```
2. **Levantar los servicios:**
   ```bash
   docker compose up -d
   ```
   * Como `COMPOSE_PROFILES` está vacío, Docker Compose **NO** levantará los contenedores de Postgres, Mongo ni RabbitMQ locales.
   * Gracias a que las dependencias tienen `required: false`, los servicios de aplicación (`donaciones`, `logistica`, etc.) arrancarán sin errores y se conectarán directamente a Supabase, Atlas y CloudAMQP.

---

## 4. Verificación de Salud de los Servicios

Cada servicio expone el endpoint de Spring Boot Actuator para inspeccionar el estado de sus conexiones:

### 1. Logística (`http://localhost:8002/actuator/health`)
* **En Local:**
  ```json
  {"status":"UP","components":{"mongo":{"status":"UP"}}}
  ```
* **En Producción (Atlas):**
  ```json
  {"status":"UP","components":{"mongo":{"status":"UP","details":{"maxWireVersion":25}}}}
  ```
  *(Nota: Logística no reporta componente `db` relacional ya que opera 100% sobre persistencia documental).*

### 2. Donaciones (`http://localhost:8000/actuator/health`)
* Reporta conexión `db` (PostgreSQL de Docker en local o Supabase en prod).

---

## 5. Resumen de Comandos Rápidos

| Acción | Comando |
|---|---|
| Inicializar entorno local completo | `make setup` |
| Bajar todos los contenedores | `docker compose down` |
| Ejecutar tests de todos los módulos | `mvn test` |
| Ejecutar tests de Logística | `make test-logistica` |
| Iniciar Logística en modo Local | `make logistica` |
| Iniciar Logística en modo Prod (Atlas) | `make logistica PROFILE=prod` |
| Iniciar Servidor en modo Local | `make server` |
| Iniciar Servidor en modo Prod | `make server PROFILE=prod` |
