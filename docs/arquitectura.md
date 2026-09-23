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

El sistema utiliza una única base de datos relacional (PostgreSQL) pero mantiene un **aislamiento lógico estricto mediante esquemas (schemas)**.
- **Esquemas separados**: `logistica`, `donaciones`, `incentivos`, `notificaciones`, `auth`.
- **Estrategia ORM**: Se utiliza JPA/Hibernate. El Modelo Físico (DER) es un mapeo casi directo del modelo de clases de dominio, delegando en JPA la resolución de relaciones (ManyToOne, OneToMany) y las estrategias de herencia (ej. `SINGLE_TABLE` para herencias de `Necesidad` o `JOINED` para roles).
- **Manejo de Archivos**: Los archivos pesados (como fotos de entregas) no se almacenan como blobs en la base de datos, sino que se almacenan en la nube persistiendo únicamente su URL pública en forma de `String` en la BD.

## 3. Patrones de Integración

DonaTrack implementa diferentes patrones para comunicar sus módulos, evitando el acoplamiento duro.

### 3.1. Mensajería Asíncrona (RabbitMQ)
Para evitar bloqueos y acoplamiento temporal, los servicios no se comunican síncronamente cuando el flujo de negocio no lo requiere.
- **Caso de uso principal**: El módulo de Notificaciones es completamente reactivo.
- **Flujo**: Cuando `donaciones` o `incentivos` necesitan notificar algo (ej. una donación asignada o una insignia otorgada), publican un evento de dominio en RabbitMQ (`donaciones.exchange` o `incentivos.exchange`). El módulo de Notificaciones actúa como un consumidor asíncrono puro que procesa estos eventos y despacha los emails.

### 3.2. Broker de Integración de Logística
El módulo de `donaciones` requiere integrarse con `logistica` para solicitar el retiro de bienes. Ya que Logística es un servicio intensivo computacionalmente, se diseñó para poder ser "encendido y apagado" bajo demanda en la nube.
- **Implementación**: Se utiliza el patrón Broker (encapsulado en la clase `LogisticaBrokerAdapter` dentro de `donaciones`).
- **Funcionamiento (Fallback)**: El broker intenta primero contactar al servicio de Logística remoto (en la nube) a través de un cliente HTTP (Feign). Si el servicio no responde (timeout o error), ejecuta un fallback transparente y desvía la petición hacia un servicio de Logística ejecutándose localmente (o en otra zona de disponibilidad).

### 3.3. Integración con Servicios Externos (Planificador y Meta)
- **Planificación de Rutas**: La asignación de rutas es delegada a un motor de optimización externo. Dado que esta operación puede tardar minutos, la comunicación es asíncrona mediante un modelo de petición-respuesta delegada (**Callback**). Logística envía los ítems al planificador y expone un endpoint (`/api/planificacion/callback`) que el planificador llama cuando termina.
- **Notificaciones (n8n/WhatsApp/Telegram)**: En lugar de acoplar directamente el código a SDKs de Meta o Telegram, DonaTrack puede enviar webhooks a una instancia de **n8n** (plataforma de automatización), la cual se encarga de la lógica de enrutamiento hacia WhatsApp, Telegram o SMS.

## 4. Despliegue (Deployment)

Para los ambientes de prueba y producción, la estrategia consiste en tener los módulos principales desplegados en plataformas serverless soportados por Docker.
El servicio de Logística es el único que puede desplegarse independientemente y ser "apagado" cuando no hay campaña activa, dejando el nodo local como sistema de recuperación ante fallos (Disaster Recovery).
