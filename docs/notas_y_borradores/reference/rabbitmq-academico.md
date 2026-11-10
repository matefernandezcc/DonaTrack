# RabbitMQ como Middleware de Mensajería Asincrónica en Arquitecturas Orientadas a Eventos: El Caso de DonaTrack

## Resumen

El diseño de sistemas distribuidos frecuentemente enfrenta el desafío del acoplamiento temporal y espacial entre componentes de software. La implementación de comunicación sincrónica en microservicios o arquitecturas modulares tiende a generar cuellos de botella operativos y cascadas de fallos ante la indisponibilidad de un nodo. El presente documento teórico analiza la adopción de RabbitMQ —un *Message Broker* basado en el protocolo AMQP 0-9-1— como mecanismo de desacoplamiento asincrónico. Se examinan sus fundamentos teóricos, la mecánica de enrutamiento y su integración específica en el ecosistema de DonaTrack bajo el patrón arquitectónico de Puertos y Adaptadores (Hexagonal). Se concluye que, si bien la introducción del *broker* incrementa la complejidad de la infraestructura, garantiza la resiliencia sistémica y el aislamiento del dominio.

---

## 1. Introducción

El acoplamiento arquitectónico representa una de las principales barreras para la escalabilidad en sistemas distribuidos. Cuando un módulo requiere que otro esté disponible en tiempo real para completar una transacción (comunicación sincrónica), el sistema hereda la tasa de disponibilidad más baja de sus componentes. 

RabbitMQ interviene en este escenario operando como un intermediario de mensajería (Message Broker). En lugar de establecer conexiones directas (punto a punto) entre los módulos emisores y receptores, los sistemas delegan los paquetes de información al broker, el cual asume la responsabilidad de almacenarlos y distribuirlos a los consumidores suscritos. Este modelo, fundamentado en la Arquitectura Orientada a Eventos (EDA, por sus siglas en inglés), suprime el acoplamiento temporal y permite escalar emisores y receptores de manera independiente.

## 2. Marco Teórico: Advanced Message Queuing Protocol (AMQP 0-9-1)

RabbitMQ implementa nativamente el protocolo AMQP 0-9-1 (Advanced Message Queuing Protocol), un estándar abierto a nivel de aplicación diseñado para el middleware de mensajería. A diferencia de protocolos de transporte puramente lineales, AMQP define un modelo semántico interno basado en tres entidades fundamentales responsables del enrutamiento de los mensajes (RabbitMQ, 2024b):

1. **Exchange (Intercambiador)**: Entidad receptora primaria. Los productores de mensajes no envían datos directamente a una cola, sino que los publican en un exchange. Su función exclusiva es inspeccionar los atributos del mensaje (como la *Routing Key*) y, basándose en reglas predefinidas, dirigirlo hacia una o múltiples colas (RabbitMQ, 2024a).
2. **Queue (Cola)**: Estructura de datos en disco o memoria que almacena los mensajes bajo una política FIFO (First In, First Out) hasta que un consumidor esté disponible para procesarlos. Proveen tolerancia a fallos garantizando la persistencia de la información ante desconexiones.
3. **Binding (Enlace)**: Regla topológica que vincula un exchange con una queue. Actúa como el criterio de enrutamiento que el exchange evalúa para determinar el destino de cada mensaje (RabbitMQ, 2024a).

## 3. Implementación Arquitectónica en DonaTrack

La arquitectura de DonaTrack requiere la coordinación de múltiples contextos delimitados (*Bounded Contexts*), específicamente: Logística, Donaciones e Incentivos, y Notificaciones. Su integración se realiza mediante RabbitMQ utilizando una estricta separación de responsabilidades derivada de la Arquitectura Hexagonal.

### 3.1. Flujo de Mensajería Asincrónica

La coreografía del sistema evita la orquestación centralizada. El flujo transaccional opera de la siguiente manera:
1. El módulo de **Logística** consolida la entrega física y emite un evento al exchange correspondiente (`logistica.exchange`).
2. El módulo de **Donaciones**, suscrito como consumidor, procesa el evento actualizando el estado de la entidad pertinente y emite de forma reactiva un evento subsecuente en `donaciones.exchange`.
3. El módulo de **Notificaciones** actúa como consumidor final, interceptando los eventos relevantes para despachar alertas asincrónicas sin bloquear la finalización de los subprocesos de negocio.

### 3.2. Aislamiento del Dominio mediante Spring Events

La lógica de negocio (Casos de Uso) de DonaTrack opera con ignorancia absoluta de la infraestructura de mensajería. Para evitar la contaminación del núcleo de dominio con bibliotecas de RabbitMQ, se emplean los eventos internos del framework de aplicación (`ApplicationEventPublisher` de Spring). 

Los *Driven Adapters* (Adaptadores de Salida, como `DonacionesRabbitMQAdapter`) escuchan los eventos emitidos en memoria y ejecutan la traducción técnica hacia el protocolo AMQP. Este aislamiento asegura que las pruebas unitarias del dominio (tests de Casos de Uso) permanezcan agnósticas respecto al *broker*, requiriendo únicamente el mockeo del publicador de eventos local.

## 4. Discusión y Trade-offs

La integración de RabbitMQ resuelve satisfactoriamente el acoplamiento transaccional, garantizando que un fallo en el servidor de notificaciones no impida la culminación exitosa de un flujo logístico. Los mensajes permanecen persistidos en las colas de RabbitMQ hasta la recuperación del servicio afectado (garantía de entrega at-least-once).

No obstante, esta decisión de diseño impone compromisos (*trade-offs*) arquitectónicos insoslayables:
1. **Complejidad Operativa**: Se introduce un nodo de infraestructura adicional (gestionado vía Docker Compose) que requiere monitoreo independiente de disco, memoria y conexiones AMQP.
2. **Consistencia Eventual**: El sistema abandona la consistencia fuerte (ACID distribuido) en favor de una consistencia eventual. Un registro en Logística existirá fracciones de segundo o minutos antes de reflejarse formalmente en el módulo de Donaciones.

## 5. Referencias

- RabbitMQ. (2024a). *AMQP 0-9-1 Model Explained*. RabbitMQ Documentation. https://www.rabbitmq.com/tutorials/amqp-concepts
- RabbitMQ. (2024b). *AMQP 0-9-1 Protocol Specification*. RabbitMQ Documentation. https://www.rabbitmq.com/amqp-0-9-1-reference.html
