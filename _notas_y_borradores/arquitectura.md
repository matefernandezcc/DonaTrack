# Arquitectura del Sistema DonaTrack

Este documento describe las decisiones arquitectónicas principales para la plataforma **DonaTrack**, que permiten cumplir con los requisitos de mantenibilidad, desacoplamiento, escalabilidad y persistencia definidos por la cátedra.

---

## 1. Estilo Arquitectónico Principal

DonaTrack implementa una **Arquitectura Orientada a Servicios (SOA)** materializada como microservicios lógicos desacoplados, donde cada contexto opera con autonomía de despliegue y modelo de datos propio.

Internamente, cada servicio sigue el patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**. Esta decisión aísla el núcleo de negocio (Dominio y Casos de Uso) de las dependencias externas (bases de datos, brokers de mensajería, frameworks web y APIs de terceros).

### Estructura Hexagonal (por servicio):
- **Dominio (`domain`)**: Entidades de negocio, Value Objects, eventos de dominio y reglas invariantes sin dependencias de frameworks ni librerías de persistencia.
- **Aplicación (`application`)**:
  - `usecases`: Orquestación de la lógica de negocio y transacciones de aplicación.
  - `ports.in`: Contratos de entrada que exponen los casos de uso hacia los adaptadores primarios.
  - `ports.out`: Contratos de salida que definen las operaciones requeridas de infraestructura (repositorios, clientes REST, publicadores de eventos).
- **Infraestructura (`infrastructure`)**:
  - `adapters.in`: Controladores REST, Listeners de RabbitMQ.
  - `adapters.out`: Repositorios JPA / Spring Data Mongo, Clientes HTTP (OpenFeign), Adaptadores AMQP.
  - `config`: Configuración de Spring Boot, seguridad, conversores BSON y beans de infraestructura.

---

## 2. Estrategia de Persistencia Políglota y Bases de Datos (Entrega 5)

En cumplimiento con los requerimientos de la Entrega 5, DonaTrack adoptó una **Estrategia de Persistencia Políglota** cada microservicio persiste su modelo en el motor que mejor se adapta a su dinámica operativa:

| Servicio | Paradigma | Motor de Base de Datos | Tecnología de Mapeo | Esquema / Base de Datos |
|---|---|---|---|---|
| **`donatrack-donaciones`** | Relacional (SQL) | PostgreSQL 16 | ORM (Hibernate / JPA) | Esquema `donaciones` |
| **`donatrack-incentivos`** | Relacional (SQL) | PostgreSQL 16 | ORM (Hibernate / JPA) | Esquema `incentivos` |
| **`donatrack-notificaciones`** | Relacional (SQL) | PostgreSQL 16 | ORM (Hibernate / JPA) | Esquema `notificaciones` |
| **`donatrack-logistica`** | **Documental (NoSQL)** | **MongoDB 7.0** | **ODM (Spring Data MongoDB)** | Base `donatrack_logistica` |

### 2.1. Persistencia Documental en Logística (MongoDB)

#### A. Justificación Técnica del Cambio de Motor (Trade-Offs)
El ciclo de vida del reparto (`RutaDeReparto`) es dinámico, jerárquico y de lectura intensiva. Durante la ejecución de una ruta:
- Un chofer consulta en tiempo real su itinerario completo, requiriendo recuperar la ruta, sus paradas ordenadas, las donaciones asignadas y las direcciones de destino.
- En un esquema relacional normalizado, esta operación exige un `JOIN` sobre 6 tablas (`rutas_reparto`, `paradas`, `entregas`, `comprobantes`, `direcciones`, `coordenadas`), con alto costo de CPU e índices múltiples.
- En **MongoDB**, toda la ruta se persiste y recupera como un único documento sin joins


#### B. Estrategia de Desnormalización y Snapshot Histórico Inmutable
- **Snapshot de Camión y Chofer:** En vez de enlazar por referencia viva a las colecciones `camiones` y `choferes`, la ruta almacena una fotografía inmutable de los datos operativos del vehículo y conductor al momento de la planificación.
  - *Justificación:* Garantiza inmutabilidad legal y operativa. Si un camión cambia su patente o un chofer renueva su legajo en el futuro, las rutas ejecutadas en el pasado preservan exactamente la información con la que salieron a la calle.

#### C. Optimización de Lectura: Índice Compuesto Multikey
Para atender la consulta de trazabilidad `GET /api/entregas/{idDonacion}/estado` (donde un donante consulta en qué ruta viaja su donación), un escaneo secuencial (*collection scan*) sobre los arrays anidados sería $O(N \cdot M)$. Se definió el índice multikey B-Tree:
```java
@CompoundIndex(name = "idx_rutas_entregas_donacion", def = "{'paradas.entregas.idEntrega': 1}")
```
Esto reduce la búsqueda de la ruta contenedora a tiempo logarítmico $O(\log N)$.

#### D. Validación de la Cota BSON (Límite de 16 MB)
MongoDB limita el tamaño de cada documento BSON a 16 MB.
- En DonaTrack, una ruta operativa contiene en promedio entre 15 y 50 paradas, con un promedio de 1 a 2 entregas por parada.
- Cada entrega almacena metadatos y URLs de comprobantes fotográficos (no los binarios en base64, que se sirven externamente).
- Un documento de ruta con 100 entregas promedia un tamaño de **~150 KB**, lo que representa menos del **1% del límite de 16 MB**, garantizando seguridad frente a desbordamientos.

#### E. Mapeo BSON Binary $\leftrightarrow$ Java UUID (`MongoConfig`)
Spring Data MongoDB por defecto almacena los `java.util.UUID` como BSON Binary (Subtipo 4). Para garantizar la deserialización fluida y compatibilidad con scripts nativos de `mongosh` y Docker, se registraron conversores customizados en [`MongoConfig.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/config/MongoConfig.java):
- `BinaryToUuidConverter` (`@ReadingConverter`): Reconstruye el `UUID` a partir de los 16 bytes del BSON.
- `UuidToBinaryConverter` (`@WritingConverter`): Serializa el `UUID` al estándar binario.

#### F. Idempotencia y Doble Mecanismo de Inicialización
Para evitar duplicación de datos en reinicios:
1. **Docker Compose (`database/mongo-init.js`):** Inicializa el contenedor local protegiendo cada inserción con `if (db.<coleccion>.countDocuments() === 0)`.
2. **Spring Boot Runner (`MongoDataInitializer.java`):** Se ejecuta al arrancar el microservicio mediante `CommandLineRunner`. Si detecta que la base está vacía (aplica tanto a Docker local como al cluster en la nube **MongoDB Atlas**), precarga el lote inicial de 6 camiones, 6 choferes, 6 ítems pendientes y 3 rutas de prueba a través de los puertos de dominio.

#### G. Aislamiento Hexagonal Puro
La migración de Logística de PostgreSQL a MongoDB preservó el 100% de la capa de dominio (`RutaDeReparto`, `Parada`, `Entrega`, `Coordenada`, `Direccion`). Ninguna anotación de Spring Data MongoDB contaminó las clases del core; la adaptación se resolvió íntegramente en `infrastructure/adapters/out/persistence` con mappers bidireccionales (`RutaRepartoDocumentMapper`).

---

### 2.2. Persistencia Relacional en Donaciones, Incentivos y Notificaciones (PostgreSQL)

Los microservicios de Donaciones, Incentivos y Notificaciones conservan su almacenamiento en **PostgreSQL 16**:
- **Aislamiento por Esquemas Lógicos:** Se utilizan esquemas segregados (`donaciones`, `incentivos`, `notificaciones`) dentro de la misma instancia relacional. Esto garantiza independencia de tablas sin incurrir en los sobrecostos de aprovisionar múltiples bases físicas independientes en la nube.
- **Identidad Agnóstica a Persistencia:** Las entidades generan sus propios UUIDs en el Dominio al instanciarse (`UUID.randomUUID()`), evitando el antipatrón de identificadores secuenciales autoincrementales (`@GeneratedValue`) dependientes del motor relacional.
- **Estrategias de Herencia JPA:**
  - `JOINED` para la jerarquía de Personas (`personas`, `personas_humanas`, `personas_juridicas`) y Roles (`roles`, `roles_donante`, `roles_beneficiario`, `roles_representante`): Garantiza normalización estricta e integridad referencial, evitando tablas masivas con columnas nulas.
  - `SINGLE_TABLE` para `Necesidad` (`necesidades` con columna discriminadora `dtype`): Las variantes (`NecesidadRecurrente`, `NecesidadExtraordinaria`) comparten el 90% de sus atributos; unificar en una tabla evita joins costosos en lecturas frecuentes.
- **Value Objects Aplanados (Flattening / Embedded):**
  - Los Value Objects del dominio `Contacto`, `DocumentoIdentidad`, `Coordenada`, `Provincia` y `Pais` no poseen identidad propia ni tablas independientes; se aplanan como columnas embebidas en las tablas de sus entidades contenedoras (`personas` y `direcciones`).

---

## 3. Patrones de Integración y Diseño Guiado por el Dominio (DDD)

Las fronteras entre los microservicios fueron modeladas respetando **Domain-Driven Design (DDD)**. Cada microservicio representa un **Bounded Context** con su propio Lenguaje Ubicuo. La integración inter-contextos se resuelve mediante patrones tácticos:

### 3.1. Arquitectura Dirigida por Eventos (EDA) y Consistencia Eventual
Para desacoplar temporalmente los procesos, la comunicación entre microservicios se efectúa publicando **Eventos de Dominio** a través de **RabbitMQ**:
- `RutaIniciadaEvent`: Emitido por Logística; consumido por Donaciones para marcar los bienes en estado `EN_TRASLADO`.
- `EntregaRealizadaEvent`: Emitido por Logística; consumido por Donaciones (transición a `ENTREGADA` y registro de fotos) e Incentivos (suma de puntos y evaluación de insignias).
- `EntregaFallidaEvent`: Emitido por Logística; consumido por Donaciones (replanificación) y Notificaciones (alerta al beneficiario).
- **Trade-off:** Se prioriza Alta Disponibilidad y Consistencia Eventual por sobre transacciones distribuidas ACID de dos fases (2PC), eliminando bloqueos de base de datos entre microservicios.

### 3.2. Capa Anticorrupción (ACL) y Broker de Logística Resiliente
Cuando `donatrack-donaciones` debe derivar una solicitud de retiro hacia Logística, utiliza el patrón **Anticorruption Layer (ACL)** implementado en `LogisticaBrokerAdapter`.
- **Enrutamiento en 3 Niveles con Fallback:**
  1. *Intento 1:* Servidor Nube Primario en Render (`LogisticaRemoto1Client`).
  2. *Intento 2:* Servidor Nube Secundario en Render (`LogisticaRemoto2Client`).
  3. *Intento 3 (Fallback):* Nodo local de respaldo en `localhost:8002` (`LogisticaLocalClient`).
- **Justificación:** Otorga resiliencia frente a pausas por inactividad (*cold-starts*) del plan gratuito de Render o fallas de conectividad en la nube, garantizando que el usuario donante nunca perciba una indisponibilidad.

---

## 4. Despliegue e Infraestructura (Deployment)

El sistema soporta una operación híbrida y desacoplada mediante perfiles de Spring Boot:

```
[Cliente Web / Bruno API]
          |
          v
   [API Gateway / Microservicios]
    |           |               |
    | (JPA)     | (AMQP)        | (Spring Data Mongo)
    v           v               v
[PostgreSQL] [RabbitMQ]     [MongoDB]
 (Docker/     (Docker/       (Docker/
  Supabase)    CloudAMQP)     Atlas Cluster)
```

### 4.1. Perfil Local (`PROFILE=local`)
- **Orquestación con Docker Compose:** Se levantan en una red interna (`donatrack-network`):
  - `postgres-donatrack` (PostgreSQL 16 en puerto `5432`).
  - `rabbitmq-donatrack` (RabbitMQ con Management UI en puertos `5672` y `15672`).
  - `mongo-donatrack` (MongoDB 7.0 en puerto `27017` con volumen persistente `mongo_data`).
  - `n8n-donatrack` (Servidor de automatización en puerto `5678`).
  - Microservicios de aplicación (`:8000`, `:8001`, `:8002`, `:8003`).

### 4.2. Perfil Cloud / Producción (`PROFILE=prod`)
- **Base Relacional Cloud:** **Supabase PostgreSQL** (AWS us-east-1) alojando los esquemas `donaciones`, `incentivos` y `notificaciones`.
- **Base Documental Cloud:** **MongoDB Atlas Cluster** (`donatrack.ofn4ag4.mongodb.net`) alojando la base `donatrack_logistica`.
- **Broker de Mensajería Cloud:** **CloudAMQP** (`jackal.rmq.cloudamqp.com`) con vhost y exchanges dedicados.
- **Alojamiento de Microservicios:** Desplegados en **Render** con soporte de failover gestionado por el Broker de Logística ante desconexiones o *cold starts*.