# Guía Práctica: ¿Qué es PostgreSQL?

Esta guía explica qué es PostgreSQL, por qué es uno de los motores de bases de datos más elegidos en la industria y cómo está configurado dentro de los distintos entornos de DonaTrack.

## 1. ¿Qué es PostgreSQL?

**PostgreSQL** (frecuentemente llamado "Postgres") es un sistema de gestión de bases de datos relacionales orientado a objetos (ORDBMS) de código abierto. Tiene más de 30 años de desarrollo activo y es mundialmente reconocido por su robustez, rendimiento y estricto cumplimiento del estándar SQL.

### Características Clave:
*   **Relacional y algo más:** Si bien almacena los datos de forma tradicional (tablas, filas, claves primarias y foráneas), también soporta características orientadas a objetos, como tipos de datos personalizados (ej. soporte nativo para JSON/JSONB), lo que lo hace tan flexible como una base de datos NoSQL sin perder la estructura relacional.
*   **Transaccional (ACID):** Garantiza que las operaciones de la base de datos se procesen de forma segura, incluso tras fallos de energía o caídas del sistema.
*   **Open Source:** Es 100% gratuito y mantenido por una enorme comunidad global, sin depender de los caprichos comerciales de una sola corporación.

---

## 2. PostgreSQL en DonaTrack (Entornos y Configuración)

En la arquitectura de DonaTrack, PostgreSQL actúa como la fuente de verdad persistente de todos los microservicios, pero su comportamiento cambia dependiendo del entorno de ejecución (perfil de Spring).

Para respetar el aislamiento de contextos (Bounded Contexts) de la Arquitectura Hexagonal, **cada microservicio maneja su propio esquema (`schema`) dentro de la misma base de datos**, evitando que las tablas se mezclen.

### A. Entorno Local (Desarrollo)
En tu entorno local (`application-local.properties`), la aplicación se conecta a una instancia de Postgres levantada mediante **Docker**.
Cada módulo se conecta usando parámetros (con valores por defecto) hacia `localhost` apuntando a su esquema específico.

Ejemplo en el módulo de **Logística**:
```properties
# /donatrack-logistica/src/main/resources/application-local.properties
spring.datasource.url=jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:donatrack}?currentSchema=logistica
spring.datasource.username=${POSTGRES_USER:postgres}
spring.datasource.password=${POSTGRES_PASSWORD:postgres}
```

### B. Entorno de Producción (Supabase)
Para producción (`application-prod.properties`), DonaTrack delega la infraestructura de Postgres a un proveedor Cloud Serverless llamado **Supabase**. Se conecta mediante un *connection pooler* (puerto 5432 en Supabase) para manejar alta concurrencia.

```properties
# /donatrack-incentivos/src/main/resources/application-prod.properties
spring.datasource.url=jdbc:postgresql://${SUPABASE_HOST:aws-1-us-east-1.pooler.supabase.com}:${SUPABASE_PORT:5432}/postgres?currentSchema=incentivos
```

### C. Entorno de Pruebas (Tests Unitarios)
Curiosamente, **Postgres NO se usa para correr los tests**. Para que las pruebas sean veloces y no requieran levantar Docker, DonaTrack utiliza una base de datos en memoria llamada **H2**, pero configurada en "Modo Postgres" para que simule su comportamiento y acepte su sintaxis SQL.

```properties
# /donatrack-donaciones/src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS donaciones
```

---

## 3. Trade-offs de usar PostgreSQL

**¿Por qué elegirlo?**
*   Soporte nativo impecable para buscar dentro de estructuras de datos complejas como JSON, lo que lo hace súper versátil.
*   Excelente manejo de la concurrencia (MVCC), evitando bloqueos innecesarios cuando muchos usuarios acceden a la vez.

**Desventajas**
*   Suele consumir un poco más de memoria RAM base (por conexión) comparado con motores más ligeros como MySQL o MariaDB.
*   Configurar la replicación de alta disponibilidad o un escalado horizontal (*sharding*) requiere herramientas externas y un nivel de experiencia DevOps intermedio-alto (de ahí la decisión de usar Supabase en producción para delegar esa tarea).

---

## Referencias

- PostgreSQL. (2024). *About PostgreSQL*. Documentación Oficial. https://www.postgresql.org/about/
- Archivos locales de configuración de DonaTrack: `application.properties` (perfiles `local`, `test` y `prod`).
