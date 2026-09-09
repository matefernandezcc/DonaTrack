# Resumen de Avance: Entrega 4 (Persistencia, Integración y Despliegue)

Este documento detalla todo el progreso realizado hasta el momento para la **Entrega 4** del sistema DonaTrack, consolidando el trabajo de persistencia de datos, integración entre servicios, diseño arquitectónico y documentación, así como los pendientes necesarios para finalizar esta fase.

---

## 🟢 Lo que ya se completó

### Fase 1: Persistencia y Bases de Datos (JPA)
Se migró todo el modelo de objetos (Mocks en memoria) a una persistencia física utilizando **PostgreSQL** y **Spring Data JPA**. Se mantuvo el patrón de **schemas lógicos aislados** (`logistica`, `donaciones`, `incentivos`, `notificaciones`).
* Se mapearon todas las entidades de negocio de los 4 servicios hacia `@Entity`.
* Se implementaron estrategias de herencia del ORM:
  * `JOINED` para la jerarquía de Personas y Roles en Donaciones.
  * `SINGLE_TABLE` para la jerarquía de Necesidades (Extraordinarias y Recurrentes).
* Se eliminaron todos los MockRepositories.
* **Refactorización de Bugs:** Se arreglaron problemas en el mapeo y de tipo de datos (ej. `altura` en las direcciones) durante la carga del contexto.

### Fase 2: Integración de Microservicios y Mensajería
Se eliminó el acoplamiento síncrono donde no era estrictamente necesario, abrazando una arquitectura orientada a eventos.
* **Integración Asíncrona (RabbitMQ):** 
  * Se configuraron *Topic Exchanges* (`donaciones.exchange`, `incentivos.exchange`).
  * Los módulos de `donaciones` e `incentivos` publican eventos de dominio asíncronos en lugar de llamar síncronamente al servicio de notificaciones.
  * Se actualizaron los Listeners en `donatrack-notificaciones` con `@QueueBinding` explícitos para reaccionar a estos eventos.
* **Broker de Logística:**
  * Se implementó un patrón Broker en `donatrack-donaciones` (`LogisticaBrokerAdapter`).
  * Cuenta con mecanismo de Fallback: intenta pegarle a un servicio remoto (nube) usando Feign, y si falla o da timeout, conmuta automáticamente hacia el servicio de Logística local (`localhost:8002`).
* **Verificación:** Los tests de integración con `mvn clean test` pasan correctamente (BUILD SUCCESS).

### Fase 3: Documentación (Arquitectura y Diagramas)
De acuerdo a las aclaraciones del profesor, se generó la documentación técnica reflejando con exactitud la base de código actual. Todos los documentos fueron alojados en la carpeta `diagramas/`.
* **Documento de Arquitectura (`arquitectura.md`):** Describe la arquitectura orientada a servicios, las decisiones de aislamiento (schemas), uso de RabbitMQ, el broker de integración y las justificaciones correspondientes.
* **Diagrama de Componentes (`DonaTrack-Diagrama-Componentes-y-Despliegue-Entrega4.puml`):** Refleja la topología del backend, los clientes REST (API Gateway vs. Microservicios), las conexiones directas a JPA, el bus de RabbitMQ y las integraciones de SaaS/n8n/Planificador de Rutas.
* **Diagrama de Clases (`DonaTrack-Diagrama-Clases-Entrega4.puml`):** Actualización para marcar explícitamente el uso de `@Entity` y la aplicación de las estrategias de herencia en la capa de datos.
* **Modelos Físicos DER (`DER-*.puml`):** Siguiendo la regla de "mapeo directo del modelo de objetos", se crearon 4 diagramas PlantUML (`DER-Donaciones`, `DER-Logistica`, `DER-Incentivos`, `DER-Notificaciones`) detallando tablas reales, Foreign Keys y tipos de datos SQL.

---

## 🔴 Lo que falta por resolver (Fase 4: Despliegue)

El último gran paso para cerrar la entrega es el **despliegue en la nube**. Con esto se demostrará el funcionamiento del *Broker de Logística* en un escenario real:

1. **Plataforma de Hosting:** 
   * Determinar definitivamente la plataforma a usar (se recomendó **Render** o **Railway** por los *cold starts* gratuitos y el soporte nativo a contenedores Docker).
2. **Despliegue del Servicio de Logística:** 
   * Tomar el módulo `donatrack-logistica` y publicarlo remotamente apoyándose en su `Dockerfile`.
   * Verificar que se comunique correctamente con la BD externa (ej. Supabase) en producción mediante las variables del entorno.
3. **Configuración del Broker:**
   * Actualizar el archivo `application.yml` de `donatrack-donaciones` con la URL real del servicio de Logística remoto.
4. **Colección de Postman/Swagger:**
   * Generar el export final de la colección (o asegurar que el Swagger-UI general quede accesible) con todas las trazas actualizadas.
