# Arquitectura del Sistema DonaTrack

Este documento describe las decisiones arquitectónicas principales para la plataforma **DonaTrack**, que permiten cumplir con los requisitos de mantenibilidad, desacoplamiento y escalabilidad solicitados.

## 1. Estilo Arquitectónico Principal

DonaTrack utiliza una **Arquitectura Orientada a Servicios (SOA)** implementada como un conjunto de servicios (módulos o microservicios lógicos) débilmente acoplados.

Internamente, cada servicio sigue el patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**. 
Esta decisión aisla la lógica de negocio (Dominio y Casos de Uso) de las dependencias externas (bases de datos, colas de mensajes, frameworks web y servicios de terceros).

### Estructura Hexagonal (por servicio):
- **Dominio (`domain`)**: Entidades de negocio sin dependencias externas.
- **Aplicación (`application`)**:
  - `usecases`: Lógica de negocio orquestada.
  - `ports.in`: Interfaces de entrada (casos de uso que exponen).
  - `ports.out`: Interfaces de salida (contratos que deben cumplir las infraestructuras, como repositorios o clientes externos).
- **Infraestructura (`infrastructure`)**:
  - `adapters.in`: Controladores REST, Listeners de RabbitMQ.
  - `adapters.out`: Repositorios JPA, Clientes Feign, Adaptadores de envío de mensajes.
  - `config`: Configuraciones de Spring, RabbitMQ, etc.

## 2. Persistencia y Bases de Datos

El sistema utiliza una **estrategia de persistencia híbrida** (PostgreSQL) dependiendo del entorno de despliegue, manteniendo en ambos casos un **aislamiento lógico estricto mediante esquemas (schemas)**.
- **Base de Datos Local (Docker)**: Clúster principal para los servicios locales (`donaciones`, `incentivos`, `notificaciones` y el nodo fallback de `logistica`).
- **Base de Datos Cloud (Supabase)**: Instancia en la nube dedicada exclusivamente a los servidores de `logistica` desplegados en Render (dado que no pueden acceder a la red local).
- **Esquemas separados**: `logistica`, `donaciones`, `incentivos`, `notificaciones`, `auth`.
  * *Justificación:* Mantener bases de datos físicas separadas en la nube aumenta drásticamente los costos. Utilizar schemas lógicos garantiza el aislamiento exigido por los microservicios, evitando el antipatrón de Base de Datos Compartida, pero de forma económica.
- **Identidad Agnóstica a Persistencia**: Se eliminó el uso de secuencias autogeneradas por la base de datos (`@GeneratedValue`) en favor de asignar UUIDs explícitamente desde el Dominio.
  * *Justificación:* Permite que las entidades nazcan con identidad antes de interactuar con la base de datos, habilitando la publicación inmediata de eventos asincrónicos y reduciendo el acoplamiento con la tecnología de persistencia.
- **Estrategia ORM (Mapeo Objeto-Relacional)**: Se utiliza JPA/Hibernate con una clara separación entre `Domain Entities` y `JPA Entities` mediante Mappers. 
  * *Justificación de Herencias:* Se eligió `JOINED` para la jerarquía de Personas (para normalizar y evitar tablas nulas masivas) y `SINGLE_TABLE` para Necesidades (ideal porque las variaciones agregan pocos campos y evita JOINs costosos).

## 3. Patrones de Integración y Diseño Guiado por el Dominio (DDD)

Las fronteras de integración entre los microservicios fueron definidas utilizando **Domain-Driven Design (DDD)**. Cada servicio representa un **Bounded Context** (Contexto Delimitado) con su propio modelo de lenguaje (*Ubiquitous Language*). Para comunicarlos sin corromper sus modelos, se implementan patrones de *Context Mapping*.

### 3.1. Arquitectura Dirigida por Eventos (EDA) y Eventos de Dominio
Para evitar bloqueos y acoplamiento temporal, los Bounded Contexts se comunican publicando **Eventos de Dominio** (patrón táctico de DDD) mediante colas (RabbitMQ/CloudAMQP) para flujos reactivos.
- **Justificación y Trade-off:** Se abandona la consistencia fuerte (ACID) en favor de una consistencia eventual. Si el servicio de Notificaciones o Logística cae temporalmente, el flujo de Donaciones no colapsa; los mensajes quedan encolados (tolerancia a fallos). Además, el núcleo del dominio emite *Spring Events* en memoria que son capturados por un adaptador secundario, evitando contaminar la lógica de negocio con librerías de infraestructura AMQP.

### 3.2. Capa Anticorrupción (ACL) y Broker de Logística
El módulo de `donaciones` debe entregar datos a `logistica`. Para evitar que el modelo de Donaciones se contamine con la lógica externa y protegerse de la inestabilidad de la nube, se utiliza el patrón **Anticorruption Layer (ACL)** de DDD combinado con un broker inteligente.
- **Implementación**: Se encapsula la lógica de traducción y enrutamiento en el `LogisticaBrokerAdapter`.
- **Justificación**: Provee Alta Disponibilidad (HA). El broker intenta derivar la petición sincrónica primero al Servidor de Nube Primario (Render). Si falla (timeout o 503 por pausa), hace un desvío transparente al Servidor Nube Secundario, y como último recurso, a la instancia de `localhost`. Esto satisface la necesidad de elegir entre múltiples proveedores logísticos.

### 3.3. Comunicación Síncrona Específica (Feign Clients)
El servicio de `incentivos` requiere consultar datos nominales a `donaciones` para generar el ranking y las notificaciones por Discord.
- **Justificación**: Se utiliza **Spring Cloud OpenFeign** en lugar de leer directamente la base de datos de Donaciones (lo cual sería un severo anti-patrón de integración). Se implementa con tolerancia a fallos (`@JsonIgnoreProperties`) para ignorar cambios indeseados en el payload del servicio emisor, y si la red falla, el sistema maneja excepciones silenciosamente para no frenar la lógica de cálculo principal.

### 3.4. Integración con Servicios Externos (SaaS y Callbacks)
- **Notificaciones Low-Code (n8n)**: En lugar de acoplar directamente el código Java a SDKs de Meta (WhatsApp) o Discord, DonaTrack dispara *Webhooks* hacia **n8n**. Esto simplifica el backend y permite modificar los canales de notificación gráficamente sin redesplegar.
- **Planificación Externa (Callback)**: La asignación de camiones tarda minutos. La comunicación es asíncrona mediante un webhook inverso (`/api/planificacion/callback`), permitiendo que el hilo principal del servidor quede libre mientras el optimizador externo trabaja.

## 4. Despliegue (Deployment)

Para los ambientes de producción (nube) y desarrollo (local), se tomaron las siguientes decisiones de infraestructura:

- **Ecosistema Docker (Inmutabilidad)**: Todo el conjunto (PostgreSQL, RabbitMQ local, n8n) se orquesta vía `docker-compose`. 
  * *Justificación:* Garantiza que todos los desarrolladores operen en un entorno inmutable. Se incluye un script de *Seeding* que puebla la base de datos automáticamente al inicio para facilitar el testing.
- **Migración a Servicios Administrados (CloudAMQP y Supabase)**: 
  * *Justificación:* Para poder desplegar la aplicación en Render y tener una integración distribuida real, fue necesario externalizar RabbitMQ (CloudAMQP) y la base de datos (Supabase), ya que los microservicios en la nube no pueden enrutar peticiones a contenedores dentro de un `localhost`.
- **Estrategia Free-Tier (Manejo de "Cold-Starts")**: Los servicios desplegados en Render (como Logística) se apagan automáticamente ("spin-down") tras inactividad para reducir costos.
  * *Justificación Técnica:* Para evitar que el conjunto de tests automáticos o manuales falle estrepitosamente debido a los tiempos de encendido en la nube (hasta 50 segundos), se introdujeron scripts dinámicos de Pre-Request en Bruno que evalúan la latencia y derivan la comunicación al Broker Local si la instancia principal no está operativa.
