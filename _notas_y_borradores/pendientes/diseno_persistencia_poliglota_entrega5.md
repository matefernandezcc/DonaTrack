# Entrega 5: Persistencia Políglota y Estrategia de Desnormalización

**Sistema:** DonaTrack — Plataforma de Gestión y Trazabilidad de Donaciones  
**Cátedra:** Diseño de Sistemas (K3002) — UTN FRBA (2026)  
**Módulo afectado:** `donatrack-logistica`  
**Rama de desarrollo:** `feature/entrega-5-persistencia-poliglota`  
**Estado:** Documento Integral de Avance y Diseño Técnico (Fases 1, 2, 3 y 4 completadas)

---

## 🚦 Tablero de Estado de Avance — Entrega 5

| Fase | Alcance | Estado | Evidencia / Detalle |
|---|---|:---:|---|
| **Fase 1** | **Infraestructura y Dependencias** | ✅ Completada | `compose.yaml` (servicio `mongo-donatrack`), `.env.template`, `pom.xml` (`spring-boot-starter-data-mongodb`) y properties configuradas. |
| **Fase 2** | **Diseño Conceptual NoSQL y Desnormalización** | ✅ Completada | Modelo de 4 colecciones, esquemas BSON, estrategia de embebido de paradas/entregas y desnormalización de camión/chofer. |
| **Fase 3** | **Implementación en Código (`donatrack-logistica`)** | ✅ Completada | 10 Documentos BSON, 5 Repositorios Mongo, 5 Mappers y 6 Adaptadores de persistencia implementando los puertos de salida. |
| **Fase 4** | **Tests y Validación de Integración** | ✅ Completada | Suite `LogisticaMongoPersistenceTest` creada. **26/26 tests pasando al 100%**. |
| **Fase 5** | **Diagramas y Entregables Formales** | ⏳ Próxima | Diagrama NoSQL / Documental y actualización del Diagrama de Componentes para la defensa. |

---

## 1. Contexto y Objetivos de la Entrega 5

Según el enunciado oficial (`enunciado/DDS-TP-Anual-2026.pdf`), los requerimientos centrales de la Entrega 5 son:
1. **Persistencia Políglota:**
   - El microservicio de **Logística** debe persistirse en una **base de datos documental (NoSQL) mediante un ODM (Object-Document Mapper)**, utilizando **MongoDB** y **Spring Data MongoDB**.
   - Los demás microservicios (`donatrack-donaciones`, `donatrack-incentivos`, `donatrack-notificaciones`) permanecen en una base de datos relacional (**PostgreSQL**) gestionada con un **ORM (Hibernate/JPA)** bajo esquemas independientes.
2. **Estrategia de Desnormalización:**
   - Diseñar e implementar estrategias de desnormalización deliberadas para optimizar consultas de lectura frecuentes (Read-Heavy) y soportar el acceso atómico a agregados de dominio.
3. **Independencia en Arquitectura Hexagonal:**
   - La migración de persistencia debe limitarse exclusivamente a la capa de infraestructura (`adapters/out/persistence`), preservando intactas las entidades de dominio y los puertos de aplicación.

---

## 2. Fase 1: Infraestructura y Dependencias

Se preparó el entorno para soportar la persistencia políglota sin afectar los servicios relacionales existentes:

1. **Docker Compose (`compose.yaml`):**
   - Se añadió el contenedor `mongo-donatrack` (`mongo:7-jammy`), puerto `27017` y persistencia en volumen dedicado `mongo_data`.
   - Se configuró el servicio `logistica` para depender de `mongo-donatrack` mediante la variable `MONGO_HOST=mongo-donatrack`.
2. **Variables de Entorno (`.env.template`):**
   - Se agregaron las variables de conexión: `MONGO_HOST`, `MONGO_PORT`, `MONGO_DATABASE`, `MONGO_USERNAME`, `MONGO_PASSWORD`.
3. **Dependencias Maven (`donatrack-logistica/pom.xml`):**
   - Se incorporó `spring-boot-starter-data-mongodb` gestionado centralmente por el POM padre.
4. **Configuración de Spring Boot (`application.properties` y `application-local.properties`):**
   - Parámetros `spring.data.mongodb.*` con `auto-index-creation=true` para generación automática de índices al arranque.

---

## 3. Fase 2: Diagnóstico del Modelo Previo y Diseño Conceptual NoSQL

### 3.1. Diagnóstico del Modelo Relacional Previo (JPA)
En la Entrega 4, la logística requería 7 tablas con claves foráneas en PostgreSQL:
* **Sobrecarga de JOINs:** Consultar la hoja de ruta completa de un chofer demandaba 5 `JOIN`s (`rutas_reparto` + `camiones` + `choferes` + `paradas` + `entregas`).
* **Impedancia en datos semi-estructurados:** Las fotos del comprobante de recepción (`List<String> fotos`) requerían tipos nativos de PostgreSQL (`varchar[]`) o tablas intermedias.
* **Pérdida de snapshot histórico:** Modificaciones posteriores a los datos del camión o chofer en los maestros alteraban el historial de viajes pasados.

### 3.2. Modelo Documental en MongoDB (`donatrack_logistica`)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        COLECCIÓN: rutas_reparto                        │
│   (Documento Raíz Agregado - Contiene Paradas, Entregas y Comprobante) │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
      ┌─────────────────────────────┴─────────────────────────────┐
      ▼                                                           ▼
Desnormalización Camión                                    Desnormalización Chofer
(patente, capacidades)                                     (nombre, legajo)
[Snapshot histórico inmutable]                             [Snapshot histórico inmutable]
```

#### A. Colección Principal: `rutas_reparto` (Agregado Raíz)
Es la colección de mayor tráfico de lectura. Embebe paradas y entregas, y desnormaliza los datos del camión y chofer asignados:

```json
{
  "_id": "d3b07384-d113-4a11-9a28-97193a0dc951",
  "fechaOperativa": "2026-10-23",
  "iniciada": true,

  /* --- CAMPOS DESNORMALIZADOS (Lectura O(1) y snapshot inmutable) --- */
  "camionId": "c1111111-1111-4111-8111-111111111111",
  "camionPatente": "AA123BB",
  "camionCapacidadCarga": 4000.0,
  "camionCapacidadVolumen": 20.0,
  "camionAltura": 3.2,

  "choferId": "f1111111-1111-4111-8111-111111111111",
  "choferLegajo": "CH-101",
  "choferNombre": "Roberto Gómez",

  /* --- SUBDOCUMENTOS EMBEBIDOS (Composición de Agregado) --- */
  "paradas": [
    {
      "orden": 1,
      "direccion": {
        "calle": "Av. Corrientes",
        "altura": "1234",
        "localidad": "CABA"
      },
      "coordenada": {
        "latitud": -34.6037,
        "longitud": -58.3816
      },
      "entregas": [
        {
          "idEntrega": "e1111111-1111-4111-8111-111111111111",
          "estado": "ENTREGADA",
          "pesoEstimado": 45.0,
          "volumenEstimado": 0.5,
          "comprobanteRecepcion": {
            "fechaHora": "2026-10-23T10:15:30",
            "camionPatente": "AA123BB",
            "fotos": [
              "https://storage.donatrack.org/remitos/foto1.jpg",
              "https://storage.donatrack.org/remitos/foto2.jpg"
            ]
          }
        }
      ]
    }
  ]
}
```

* **Índices estratégicos:**
  - `fechaOperativa`: Búsqueda de hojas de ruta por día.
  - `choferId`: Rutas asignadas a un chofer.
  - `paradas.entregas.idEntrega` (**Multikey Index**): Búsqueda directa en $O(1)$ de la ruta contenedora a partir del ID de la entrega/donación.

#### B. Colecciones Maestras / Catálogos
* `camiones`: Catálogo de flota (`_id`, `patente` único, `capacidadVolumen`, `altura`, `capacidadCarga`).
* `choferes`: Catálogo de conductores (`_id`, `legajo` único, `nombre`).

#### C. Colecciones de Proceso Asincrónico
* `solicitudes_planificacion`: Solicitudes enviadas al componente de ruteo externo (`_id`, `fechaSolicitud`, `estado`, `idsDonaciones`).
* `items_planificacion`: Donaciones listas en depósito en espera de conformar el próximo lote de ruteo.

---

## 4. Matriz de Justificaciones de Diseño y Trade-Offs

| Decisión de Diseño | Justificación Técnica | Trade-off Asumido y Mitigación |
|---|---|---|
| **Persistencia Políglota (MongoDB en Logística)** | El módulo de Logística presenta patrones de acceso agregados y transaccionales acotados a la ruta. No requiere transacciones distribuidas ACID complejas multi-tabla. | **Trade-off:** Complejidad operativa al administrar dos motores de base de datos (PostgreSQL + MongoDB).<br>**Mitigación:** Orquestación centralizada en `compose.yaml` y aislamiento a nivel de microservicio. |
| **Embeber Paradas y Entregas en `rutas_reparto`** | Modela fielmente la relación de composición. La parada y la entrega no tienen sentido de negocio ni ciclo de vida fuera de su ruta asignada. | **Trade-off:** Crecimiento del tamaño del documento BSON.<br>**Mitigación:** La consigna restringe los lotes de ruteo a un máximo de 100 donaciones. Un documento de 100 entregas pesa menos de 80 KB, muy por debajo del límite de 16 MB de MongoDB. |
| **Desnormalización de Camión y Chofer en la Ruta** | Permite que la app del chofer y el mapa interactivo lean la hoja de ruta completa en $O(1)$ sin realizar queries complementarias a las colecciones maestras. Además, preserva un snapshot fidedigno de qué camión y chofer efectuaron el reparto en esa fecha. | **Trade-off:** Duplicación de datos. Si se corrige el nombre de un chofer en el maestro, no se propaga a rutas pasadas.<br>**Mitigación:** Esto es deseable para fines de auditoría histórica. |
| **Comprobante de Recepción con Array BSON de Fotos** | MongoDB maneja arrays primitivos (`fotos: [ "url1", "url2" ]`) de forma nativa sin requerir normalizaciones artificiales ni extensiones complejas. | Ninguno. Es el caso de uso canónico para documentos BSON. |

---

## 5. Fase 3: Implementación en Código (`donatrack-logistica`)

Se construyó la nueva capa de persistencia documental sin tocar una sola línea del dominio ni de la capa de aplicación:

### 5.1. Clases de Documentos (`documents/`)
* [`CamionDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/CamionDocument.java)
* [`ChoferDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/ChoferDocument.java)
* [`RutaDeRepartoDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/RutaDeRepartoDocument.java) (con `@CompoundIndex` en `paradas.entregas.idEntrega`)
* [`ParadaDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/ParadaDocument.java)
* [`EntregaDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/EntregaDocument.java)
* [`ComprobanteRecepcionDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/ComprobanteRecepcionDocument.java)
* [`DireccionDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/DireccionDocument.java)
* [`CoordenadaDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/CoordenadaDocument.java)
* [`SolicitudPlanificacionDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/SolicitudPlanificacionDocument.java)
* [`ItemPlanificacionDocument.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/documents/ItemPlanificacionDocument.java)

### 5.2. Repositorios Spring Data MongoDB (`repositories/mongo/`)
* [`CamionMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/CamionMongoRepository.java): `findByPatente`, `deleteByPatente`.
* [`ChoferMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/ChoferMongoRepository.java): `findByLegajo`, `deleteByLegajo`.
* [`RutaDeRepartoMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/RutaDeRepartoMongoRepository.java): Query especializada `@Query("{ 'paradas.entregas.idEntrega': ?0 }")` para recuperar la ruta por ID de entrega.
* [`EntregaMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/EntregaMongoRepository.java)
* [`SolicitudPlanificacionMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/SolicitudPlanificacionMongoRepository.java)
* [`ItemPlanificacionMongoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/repositories/mongo/ItemPlanificacionMongoRepository.java)

### 5.3. Mappers ODM (`mappers/mongo/`)
* [`CamionMongoMapper.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mappers/mongo/CamionMongoMapper.java)
* [`ChoferMongoMapper.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mappers/mongo/ChoferMongoMapper.java)
* [`RutaDeRepartoMongoMapper.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mappers/mongo/RutaDeRepartoMongoMapper.java): Implementa la desnormalización hacia BSON y la reconstrucción completa del grafo de objetos hacia el dominio.
* [`SolicitudPlanificacionMongoMapper.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mappers/mongo/SolicitudPlanificacionMongoMapper.java)
* [`ItemPlanificacionMongoMapper.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mappers/mongo/ItemPlanificacionMongoMapper.java)

### 5.4. Adaptadores de Salida ODM (`mongo/`)
Implementan los puertos de salida de la aplicación y están marcados con `@Primary @Repository` para que Spring los inyecte automáticamente en todos los casos de uso:
* [`MongoCamionRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoCamionRepository.java) implements `CamionRepositoryPort`
* [`MongoChoferRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoChoferRepository.java) implements `ChoferRepositoryPort`
* [`MongoRutaDeRepartoRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoRutaDeRepartoRepository.java) implements `RutaDeRepartoRepositoryPort`
* [`MongoEntregaRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoEntregaRepository.java) implements `EntregaRepositoryPort`
* [`MongoSolicitudPlanificacionRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoSolicitudPlanificacionRepository.java) implements `SolicitudPlanificacionRepositoryPort`
* [`MongoItemPlanificacionRepository.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/mongo/MongoItemPlanificacionRepository.java) implements `ItemPlanificacionRepositoryPort`

---

## 6. Fase 4: Tests y Validación de la Persistencia NoSQL

Se implementó una nueva suite de pruebas unitarias y de integración para validar la persistencia documental:
* [`LogisticaMongoPersistenceTest.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/test/java/com/donatrack/logistica/infrastructure/adapters/out/persistence/LogisticaMongoPersistenceTest.java):
  - Inserción y búsqueda de Camión en MongoDB.
  - Inserción y búsqueda de Chofer en MongoDB.
  - Validación de la desnormalización de Camión y Chofer dentro de `RutaDeRepartoDocument`.
  - Búsqueda y actualización atómica de una `Entrega` embebida dentro de la ruta agregada.

### Resultado de la ejecución:
```
[INFO] Running com.donatrack.logistica.infrastructure.adapters.out.persistence.LogisticaMongoPersistenceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.245 s
[INFO] 
[INFO] Results:
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
**Total de tests en `donatrack-logistica`: 26/26 exitosos (100%).**
