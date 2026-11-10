# 🎓 Tutorial: Onboarding y Primeros Pasos en DonaTrack

Bienvenido al equipo de desarrollo de **DonaTrack**. Este tutorial te guiará paso a paso para clonar, configurar, compilar y ejecutar todo el entorno localmente en tu estación de trabajo.

---

## 📋 Requisitos Previos

Asegurate de contar con las siguientes herramientas instaladas en tu sistema:
- **Java 21 LTS** (Eclipse Temurin, OpenJDK o Corretto)
- **Apache Maven 3.9+** (o utilizar `./mvnw` incluido en el repositorio)
- **Docker Desktop** (con soporte para Compose v2)
- **Git**
- *(Opcional)* **Make** (para utilizar los atajos del `Makefile`)

---

## 🚀 Paso 1: Clonar el Repositorio

Abrí tu terminal y cloná el proyecto:

```bash
git clone https://github.com/matefernandezcc/DonaTrack.git
cd DonaTrack
```

---

## ⚙️ Paso 2: Configurar las Variables de Entorno

DonaTrack incluye una plantilla oficial preconfigurada para ejecución local sin requerir credenciales externas:

```bash
# En Linux / macOS / Git Bash:
cp .env.template .env

# En Windows PowerShell:
Copy-Item .env.template .env
```

Por defecto, `.env` viene con:
- `SPRING_PROFILES_ACTIVE=local`
- Conexiones a PostgreSQL (`postgres_donatrack:5432`) y MongoDB (`mongo_donatrack:27017`).
- RabbitMQ local (`donatrack` / `donatrack`).

---

## 📦 Paso 3: Compilar los Microservicios

Compilá y empaquetá todos los submódulos Maven de forma centralizada:

```bash
mvn clean package -DskipTests
```

O si disponés de `make`:

```bash
make build
```

Esto generará los artefactos `.jar` en el subdirectorio `target/` de cada microservicio.

---

## 🐳 Paso 4: Levantar la Plataforma Completa

Iniciá todos los contenedores de infraestructura y los microservicios:

```bash
docker compose up --build -d
```

Verificá el estado de los contenedores:

```bash
docker compose ps
```

Deberás ver 8 servicios en ejecución:
- `postgres_donatrack` (:5432)
- `mongo_donatrack` (:27017)
- `rabbitmq_donatrack` (:5672, UI: :15672)
- `n8n_donatrack` (:5678)
- `donatrack-donaciones` (:8000)
- `donatrack-incentivos` (:8001)
- `donatrack-logistica` (:8002)
- `donatrack-notificaciones` (:8003)
- `donatrack-auth` (:8004)
- `donatrack-web` (:8080)

---

## 🔍 Paso 5: Verificación y Smoke Tests

Abrí tu navegador web e ingresá a los siguientes puntos de acceso:

1. **Cliente Web SSR (Aplicación Principal):**
   [http://localhost:8080](http://localhost:8080)
   - Acceso con usuario de prueba: `admin` / `admin` o `donante` / `donante`.
2. **Swagger UI de Donaciones:**
   [http://localhost:8000/swagger-ui/index.html](http://localhost:8000/swagger-ui/index.html)
3. **Consola de RabbitMQ:**
   [http://localhost:15672](http://localhost:15672) (Usuario: `donatrack`, Clave: `donatrack`)
4. **Health Check de Logística:**
   [http://localhost:8002/actuator/health](http://localhost:8002/actuator/health)

---

## 🧭 Próximos Pasos

- Para ver todos los puertos y endpoints: consultá [`../reference/servicios-y-puertos.md`](../reference/servicios-y-puertos.md) o [`../../SERVICIOS.md`](../../SERVICIOS.md).
- Para comprender el diseño en capas: consultá [`../architecture/hexagonal.md`](../architecture/hexagonal.md).
- Para ver los diagramas UML formales: consultá [`../../diagramas/`](../../diagramas/).
