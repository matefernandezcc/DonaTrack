# Informe Completo de Implementación y Diseño — Entrega 5 (Persistencia Políglota)

**Proyecto:** DonaTrack — Plataforma de Gestión y Trazabilidad de Donaciones  
**Cátedra:** Diseño de Sistemas (K3002) — UTN FRBA (2026)  
**Fecha de Actualización:** 28 de Octubre de 2026  
**Rama:** `feature/entrega-5-persistencia-poliglota`  
**Ubicación:** `_notas_y_borradores/informe_completo_entrega5_persistencia_poliglota.md`

---

## 📌 Resumen Ejecutivo de Tareas Realizadas

En esta etapa se completaron de punta a punta todos los requerimientos y ajustes pendientes de la **Entrega 5 (Persistencia Políglota)**:

1. **Actualización del Diagrama de Componentes y Despliegue (`DonaTrack-Diagrama-Componentes-y-Despliegue.puml`):**
   * Se rediseñó por completo tomando como referencia visual la arquitectura de 4 microservicios en el backend y el doble perfil de infraestructura:
     * **Profile: local (Docker):** PostgreSQL (`:5432`), RabbitMQ (`:5672/:15672`), MongoDB (`:27017`).
     * **Profile: prod (Cloud):** Supabase PostgreSQL (AWS us-east-1), CloudAMQP (`jackal.rmq.cloudamqp.com`), MongoDB Atlas (`donatrack.ofn4ag4`).
   * Se reflejaron exactamente los protocolos de conexión: `JPA/Postgres` (Donaciones, Incentivos, Notificaciones), `AMQP` (todos los microservicios) y `Spring Data Mongo` (exclusivo para Logística).

2. **Actualización de Diagramas en `diagramas/` para la Entrega 5:**
   * **`logistica/DER-Logistica.puml`:** Migrado del antiguo esquema relacional de PostgreSQL a un **Modelo de Agregados y Colecciones Documentales NoSQL (MongoDB)**, explicitando el Agregado Raíz `rutas_reparto`, los subdocumentos embebidos (`ParadaDocument`, `EntregaDocument`, `ComprobanteRecepcionDocument`, `DireccionDocument`, `CoordenadaDocument`), los campos desnormalizados con snapshot histórico de camión y chofer, y los índices compuestos.
   * **`secuencias/Secuencia-EntregaExitosa.puml` y `Secuencia-EntregaFallida.puml`:** Se corrigieron los endpoints para reflejar fielmente los controllers de Spring Boot: `POST /api/entregas/{idDonacion}/confirmar` y `POST /api/entregas/{idDonacion}/falla`.

3. **Precarga Automática de Datos Semilla Enriquecidos para MongoDB:**
   * **Docker Compose (`database/mongo-init.js` montado en `compose.yaml`):** Script de inicialización nativo para Mongo que inserta 6 camiones, 6 choferes, 6 items de planificación pendientes y 3 rutas de reparto con diversos estados (Pendiente, En Curso con comprobante y fotos, y Finalizada).
   * **Spring Boot Component (`MongoDataInitializer.java`):** Implementación de `CommandLineRunner` en `donatrack-logistica` que detecta si la base de datos documental está vacía (tanto en Docker local como en MongoDB Atlas en la nube) y auto-puebla los datos a través de los puertos de dominio DDD.
   * **Mapeo BSON Binary $\leftrightarrow$ UUID (`MongoConfig.java`):** Configuración de conversores bidireccionales en Spring Data MongoDB para garantizar compatibilidad universal con los datos insertados por `mongosh` y Docker.

4. **Auditoría y Chequeo Integral de Conexiones:**
   * Se verificaron las configuraciones de properties de los 4 microservicios (`donaciones`, `incentivos`, `notificaciones`, `logistica`):
     * Módulos relacionales conectan con `JPA/PostgreSQL` a Docker local o Supabase en la nube con esquemas segregados (`donaciones`, `incentivos`, `notificaciones`).
     * Módulo de logística conecta con `Spring Data MongoDB` a Docker Mongo (`localhost:27017`) o MongoDB Atlas (`donatrack.ofn4ag4.mongodb.net`).
     * Mensajería asíncrona unificada vía RabbitMQ local o CloudAMQP en la nube.
   * Se actualizó `.env.template` agregando el bloque de configuración documentado para CloudAMQP.

5. **Matriz Formal de Justificaciones de Diseño (Formato Cátedra):**
   * Creado el documento [`_notas_y_borradores/justificaciones_diseno_entrega5.md`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/_notas_y_borradores/justificaciones_diseno_entrega5.md) respetando la tabla de 5 columnas del Excel de la cátedra (`docs/Justificación Decisiones - DonaTrack - Grupo 7.xlsx`), abarcando las 10 decisiones estructurales de persistencia políglota, agregado raíz, desnormalización, snapshot histórico, cota BSON de 16 MB e índices multinivel.

---

## 🗄️ Mecanismo de Precarga, Idempotencia y Arquitectura NoSQL

### 1. ¿Por qué el archivo es `mongo-init.js` y no existe una extensión `.nosql`?
Una duda recurrente al trabajar con bases no relacionales es por qué no usamos un archivo `.nosql`. La razón radica en cómo se estructuran ambos mundos:

* **SQL es un estándar:** SQL (*Structured Query Language*) está estandarizado internacionalmente por ISO y ANSI. Por ello, PostgreSQL, MySQL y Oracle procesan scripts con la extensión universal `.sql`.
* **NoSQL es un término paraguas arquitectónico:** No es un lenguaje ni una especificación unificada. Agrupa familias tecnológicas incompatibles en su sintaxis:
  * *Documentales:* MongoDB (BSON / JSON).
  * *Grafos:* Neo4j (Cypher / `.cql`).
  * *Clave-Valor:* Redis (comandos de memoria), DynamoDB.
  * *Columnares:* Cassandra (CQL / `.cql`).
* **MongoDB es nativo en JavaScript:**
  * El motor de comandos oficial de MongoDB (`mongosh`) está programado sobre el motor **V8 de Node.js / Google Chrome**.
  * Cada instrucción (`db.coleccion.insertOne()`, `db.coleccion.find()`) es **código JavaScript real**.
  * Un script `.js` permite programación imperativa: validaciones `if`, bucles `for`, fechas dinámicas (`new Date()`) y UUIDs BSON nativos (`UUID(...)`), superando las limitaciones estáticas de un simple archivo `.json`.
  * La imagen oficial de Docker (`mongo:7-jammy`) busca específicamente archivos `.js` o `.sh` dentro de `/docker-entrypoint-initdb.d/`.

---

### 2. Capa de Infraestructura: Roles de `MongoConfig` y `MongoDataInitializer`

En el paquete [`com.donatrack.logistica.infrastructure.config`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/config/) se agregaron dos componentes clave:

#### A. [`MongoConfig.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/config/MongoConfig.java) ➡️ Traductor BSON Binary $\leftrightarrow$ Java UUID
* **El Problema:** En MongoDB BSON, los UUIDs se almacenan como un tipo binario de 16 bytes (`org.bson.types.Binary`). Al consultar los endpoints de camiones o rutas, Spring Data arrojaba `ConverterNotFoundException: No converter found capable of converting from type [org.bson.types.Binary] to type [java.util.UUID]`.
* **La Solución:** `MongoConfig` registra dos conversores customizados en Spring:
  1. `BinaryToUuidConverter` (`@ReadingConverter`): Desempaqueta los 16 bytes del BSON y reconstruye el `java.util.UUID`.
  2. `UuidToBinaryConverter` (`@WritingConverter`): Serializa los `java.util.UUID` de Java al formato `Binary` estándar de Mongo (Subtipo 4).
* **Beneficio:** Garantiza interoperabilidad transparente entre la base (local o en la nube) y la API REST.

#### B. [`MongoDataInitializer.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/config/MongoDataInitializer.java) ➡️ Sembrador Automático de Spring Boot
* **El Problema:** `database/mongo-init.js` solo se dispara dentro del contenedor local de Docker. Si el desarrollador o docente arranca la aplicación conectándose a **MongoDB Atlas en la nube (`PROFILE=prod`)**, la base remota estaría vacía.
* **La Solución:** Implementa `CommandLineRunner`, ejecutándose inmediatamente al finalizar el arranque de Spring Boot. Si detecta que no hay camiones cargados (`camionRepository.obtenerTodos().isEmpty()`), puebla la base usando los propios puertos de dominio de Logística.

---

### 3. Garantía de Idempotencia (Sin Duplicados)
El sistema cuenta con **3 barreras que impiden la duplicación de datos**:
1. **Guarda en `mongo-init.js`:** Cada colección está protegida por `if (db.<coleccion>.countDocuments() === 0)`. Si ya existen datos, saltea la inserción sin arrojar error.
2. **Guarda en `MongoDataInitializer.java`:** `if (camionRepository.obtenerTodos().isEmpty())`. Si hay registros, emite un log informativo y omite la precarga.
3. **Ciclo de vida de Docker:** El directorio `/docker-entrypoint-initdb.d/` solo se procesa en el *primer* arranque del volumen (`mongo_data`). En reinicios posteriores (`docker compose up -d`), Docker detecta la base existente y omite la carpeta de inicialización.

---

### 4. Detalle del Dataset Precargado

| Colección | Cantidad | Elementos destacados |
|---|:---:|---|
| **`camiones`** | 6 | `AA123BB` (4.000 kg), `AF456CD` (2.500 kg), `AC789EE` (8.000 kg), `AD102ZX` (3.200 kg), `AE304KL` (5.500 kg), `AG555OP` (1.800 kg). Variedad de capacidades y alturas (2.5m a 3.8m). |
| **`choferes`** | 6 | `CH-101` Roberto Gómez, `CH-102` Laura Fernández, `CH-103` Carlos Atlas, `CH-104` Martín Benítez, `CH-105` Sofía Morales, `CH-106` Esteban Rossi. |
| **`items_planificacion`** | 6 | Donaciones pendientes en Balvanera (Corrientes 1234), La Plata (Calle 50), Palermo (Santa Fe 3200), Quilmes (Belgrano 450), San Isidro (Centenario 1820) y San Telmo (Defensa 800). |
| **`rutas_reparto`** | 3 | **Ruta 1 (Pendiente):** Para iniciar por Roberto Gómez (`AA123BB`).<br>**Ruta 2 (En Curso):** Laura Fernández (`AF456CD`), con Parada 1 ENTREGADA (con comprobante y fotos) y Parada 2 EN_TRASLADO.<br>**Ruta 3 (Finalizada):** Histórica de ayer por Carlos Atlas (`AC789EE`) en La Plata. |

---

## 🎨 Guía de Importación para Lucidchart

Para reflejar estos diagramas en tu documento de **Lucidchart** (`https://lucid.app/lucidchart/9c714d54-d357-4b30-bec0-bf22dbca2003/edit`), Lucidchart permite importar código PlantUML directamente en cada pestaña:

### ¿Cómo importar PlantUML en Lucidchart?
1. En Lucidchart, creá una **Nueva Pestaña** (botón `+` abajo a la izquierda).
2. Andá a **File (Archivo)** $\rightarrow$ **Import Data (Importar datos)** o abrí el menú de formas y seleccioná **Insert (Insertar)** $\rightarrow$ **PlantUML**.
3. Pegá el contenido del archivo `.puml` correspondiente y hacé clic en **Generate (Generar)**.

### Mapeo de Pestañas recomendadas:

| Pestaña en Lucidchart | Archivo Fuente en el Repositorio | Descripción |
|---|---|---|
| **Componentes y Despliegue E5** | [`diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml) | Arquitectura políglota con perfiles Local (Docker) y Prod (Cloud Supabase/Atlas/CloudAMQP). |
| **Modelo NoSQL Logística (DER)** | [`diagramas/logistica/DER-Logistica.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/logistica/DER-Logistica.puml) | Agregado raíz MongoDB (`rutas_reparto`), subdocumentos embebidos e índices. |
| **Clases General y Dominio** | [`diagramas/DonaTrack-Diagrama-Clases.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/DonaTrack-Diagrama-Clases.puml) | Diagrama de clases general con entidades de todos los módulos. |
| **Comunicación Inter-Servicios** | [`diagramas/DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/DonaTrack-Diagrama-Clases-Comunicacion-Entre-Servicios.puml) | Event-Driven Architecture (EDA) con publicadores y listeners de eventos. |
| **Secuencia: Inicio de Ruta** | [`diagramas/secuencias/Secuencia-InicioRuta.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/secuencias/Secuencia-InicioRuta.puml) | Inicio de recorrido, publicación a RabbitMQ y actualización de donaciones. |
| **Secuencia: Entrega Exitosa** | [`diagramas/secuencias/Secuencia-EntregaExitosa.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/secuencias/Secuencia-EntregaExitosa.puml) | Confirmación con comprobante fotográfico, eventos a Donaciones e Incentivos. |
| **Secuencia: Entrega Fallida** | [`diagramas/secuencias/Secuencia-EntregaFallida.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/secuencias/Secuencia-EntregaFallida.puml) | Reporte de fallo, replanificación automática y notificación por email. |
| **Casos de Uso Donaciones** | [`diagramas/donaciones/CU-Donaciones.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/donaciones/CU-Donaciones.puml) | Casos de uso de donantes, administradores y beneficiarios. |
| **Estados Donaciones** | [`diagramas/donaciones/Estados-Donaciones.puml`](file:///home/maximo/Documentos/Dise%C3%B1o%20de%20Sistemas/DonaTrack/diagramas/donaciones/Estados-Donaciones.puml) | Máquina de estados de una donación (creación, ruteo, entrega). |

---

## 🛠️ Comandos de Verificación y Testing

* **Inicialización rápida desde Makefile:**
  ```bash
  make mongo-init    # Precarga solo MongoDB
  make db-init       # Inicializa Postgres y MongoDB simultáneamente
  ```
* **Prueba de endpoints REST de Logística (puerto 8002):**
  ```bash
  curl -s http://localhost:8002/api/camiones | jq '. | length'                # 6 camiones
  curl -s http://localhost:8002/api/choferes | jq '. | length'                # 6 choferes
  curl -s http://localhost:8002/api/planificacion/pendientes | jq '. | length'# 6 items
  curl -s http://localhost:8002/api/rutas | jq '. | length'                   # 3 rutas
  ```
* **Compilación y Tests Unitarios:**
  ```bash
  mvn test -pl donatrack-logistica   # 24/24 tests pasando exitosamente (BUILD SUCCESS)
  ```
