# Guía Teórica y Práctica de RabbitMQ

Esta documentación explica qué es RabbitMQ, sus conceptos principales y **por qué y cómo lo usamos dentro de la arquitectura de DonaTrack** para lograr una comunicación asincrónica eficiente.

---

## 1. ¿Qué es RabbitMQ?

RabbitMQ es un **Message Broker** (agente de mensajes) de código abierto. Funciona como un intermediario o "servicio postal" que recibe mensajes de una aplicación (el productor) y los enruta hacia una o más aplicaciones (los consumidores). 

Utiliza principalmente el protocolo estándar **AMQP** (Advanced Message Queuing Protocol), lo que lo hace altamente interoperable, rápido y seguro.

### ¿Para qué se usa?
- **Desacoplamiento de Servicios**: Evita que el Servicio A tenga que llamar directamente (sincrónicamente vía HTTP/REST) al Servicio B. 
- **Procesamiento Asincrónico**: Permite que las operaciones lentas (como enviar emails o generar reportes) se pongan en una "lista de tareas" y se procesen en segundo plano sin bloquear al usuario.
- **Tolerancia a fallos**: Si el servicio que consume los mensajes está caído, RabbitMQ guarda los mensajes en una cola hasta que vuelva a estar disponible, evitando la pérdida de información.

---

## 2. Conceptos Clave (Glosario)

- **Producer (Productor)**: La aplicación o servicio que envía (publica) el mensaje.
- **Consumer (Consumidor)**: La aplicación o servicio que recibe y procesa el mensaje.
- **Queue (Cola)**: El buzón donde se almacenan los mensajes hasta que son consumidos. Operan bajo el principio FIFO (First In, First Out).
- **Exchange (Intercambiador)**: Es el "clasificador" de correos. El productor no envía el mensaje directamente a una cola, sino a un Exchange. El Exchange decide a qué cola(s) enviar el mensaje según ciertas reglas.
- **Binding (Enlace)**: La regla o vínculo que conecta un Exchange con una Queue. Utiliza "Routing Keys" para filtrar qué mensajes van a qué cola.

---

## 3. RabbitMQ en DonaTrack

En DonaTrack aplicamos un modelo **Event-Driven (orientado a eventos)**. Dado que contamos con múltiples módulos (Donaciones, Incentivos, Logística, Notificaciones), una llamada sincrónica REST entre todos ellos crearía cuellos de botella y acoplamiento duro.

### El Flujo de Comunicación (Ejemplo Práctico)
Cuando un camión entrega una donación:
1. **Logística (Productor)** publica un evento en RabbitMQ (`logistica.exchange`).
2. **Donaciones (Consumidor)** escucha ese evento, actualiza el estado de la donación a "Entregada" y a su vez publica otro evento (`donaciones.exchange`).
3. **Notificaciones (Consumidor)** escucha el evento de Donaciones y se encarga de enviarle el email al usuario.

Todo esto ocurre en fracciones de segundo, pero de manera totalmente desacoplada.

### Integración con Arquitectura Hexagonal
Para no "ensuciar" nuestra lógica de negocio con dependencias de RabbitMQ, usamos el siguiente patrón:

1. **Casos de Uso (Core)**: No saben qué es RabbitMQ. Cuando ocurre algo importante, publican un **Evento de Dominio** genérico en memoria usando `ApplicationEventPublisher` de Spring.
2. **Adaptadores de Salida (Driven Adapters)**: Clases como `DonacionesRabbitMQAdapter` o `LogisticaRabbitMQAdapter` escuchan ese evento de memoria de Spring (con `@EventListener`) y son los encargados de traducirlo y publicarlo físicamente en RabbitMQ.
3. **Adaptadores de Entrada (Driving Adapters)**: Clases Listener con anotaciones como `@RabbitListener` que escuchan las colas físicas, reciben el mensaje en JSON, lo convierten en un comando y se lo pasan a los casos de uso para que lo procesen.

> [!TIP]
> **Beneficio directo de este patrón:** Nuestros tests unitarios de la lógica de negocio (Casos de Uso) funcionan sin necesitar que RabbitMQ esté levantado, ya que solo mockeamos el publicador de eventos de Spring.

---

## 4. ¿Cómo lo usamos a nivel código? (Spring Boot)

### 4.1 Configuración de Infraestructura (Docker)
RabbitMQ se levanta junto con toda la infraestructura del proyecto a través de Docker Compose:
```bash
docker compose up -d rabbitmq
```
*(Nota: Expone el puerto `5672` para conexiones AMQP y el `15672` para la consola de administración web).*

### 4.2 Ejemplo de Productor (Adaptador)
```java
@Component
public class DonacionesRabbitMQAdapter {

    private final RabbitTemplate rabbitTemplate;

    public DonacionesRabbitMQAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Escucha el evento genérico de Spring y lo manda a RabbitMQ
    @EventListener
    public void handleDonacionEntregada(DonacionEntregadaEvent event) {
        rabbitTemplate.convertAndSend(
            "donaciones.exchange",          // Exchange
            "notificacion.entrega.exitosa", // Routing Key
            event                           // Payload (se serializa a JSON)
        );
    }
}
```

### 4.3 Ejemplo de Consumidor (Listener)
```java
@Component
public class NotificacionesListener {

    // Se conecta automáticamente a la cola configurada y escucha mensajes
    @RabbitListener(queues = "notificaciones.donaciones.queue")
    public void procesarEntregaExitosa(DonacionEntregadaEvent event) {
        // Llama al caso de uso de Notificaciones para mandar el email
        enviarNotificacionUseCase.ejecutar(event.getEmail(), event.getMensaje());
    }
}
```

---

## 5. Resumen: Cuándo usarlo y cuándo no

| ¿Cuándo usar RabbitMQ? | ¿Cuándo NO usar RabbitMQ? |
|-------------------------|--------------------------|
| Procesos en segundo plano (emails, PDFs, cálculos). | Operaciones donde el usuario necesita la respuesta de manera inmediata en la pantalla (ej. Validar login). |
| Cuando un evento interesa a múltiples servicios a la vez (Pub/Sub). | Si la arquitectura es un monolito simple sin procesos pesados (es overkill agregar infraestructura). |
| Cuando necesitamos garantía de entrega (si el receptor cae, el mensaje queda guardado). | Consultas simples de lectura (GET) de una API a otra. Para eso usar REST (o gRPC). |

> [!NOTE]
> Para más detalles arquitectónicos, puedes revisar el archivo [`docs/arquitectura.md`](../../docs/arquitectura.md) y las [`Justificaciones de Diseño`](../pendientes/JustificacionesDiseño-Entrega3.md).
