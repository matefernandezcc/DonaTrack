# Guía de Arquitectura Hexagonal - DonaTrack

Este documento detalla la estructura de directorios adoptada para los microservicios del proyecto **DonaTrack**, basada en el patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**. El objetivo primordial es aislar las reglas de negocio (Dominio) de los componentes tecnológicos externos (Infraestructura), facilitando la mantenibilidad, escalabilidad y la realización de pruebas automatizadas aisladas.

---

## Estructura de Directorios General

    src
    └── main
        └── java
            └── ar.edu.utn.donatrack
                ├── domain
                │   ├── entities
                │   ├── models
                │   └── exceptions
                │
                ├── application
                │   ├── ports
                │   │   ├── in
                │   │   └── out
                │   └── usecases
                │
                └── infrastructure
                    ├── adapters
                    │   ├── in
                    │   └── out
                    └── config

---

## Explicación Detallada de Capas y Carpetas

### 1. Capa de Dominio (`domain`)
Representa el corazón del sistema, el núcleo de la lógica de negocio pura. 
* **Regla de Oro:** Esta capa es completamente agnóstica de frameworks o tecnologías externas. No debe importar ninguna librería de persistencia (como `@Entity` o `@Table` de JPA/Hibernate), infraestructura web, ni anotaciones de frameworks (como Spring). Es código Java puro e inmutable ante cambios tecnológicos.

* **`entities/`**: Contiene las entidades principales de negocio. Tienen un ciclo de vida definido y una identidad única (`id: UUID`). 
  * *Ejemplo en Servicio de Logística:* `Entrega`, `RutaDeReparto`, `Camion`, `Chofer`.
* **`models/`** (o `valueobjects/`): Agrupa los Objetos de Valor (*Value Objects*). Son clases que encapsulan datos y reglas de validación estructural, pero no poseen identidad única. Son inmutables (no tienen *setters*). También se incluyen aquí los tipos enumerados (`enums`).
  * *Ejemplo en Servicio de Logística:* `Direccion`, `Coordenada`, `EstadoEntrega`, `EstadoPlanificacion`.
* **`exceptions/`**: Centraliza las excepciones explícitas y semánticas del negocio. Permite que el dominio maneje errores conceptuales antes de que lleguen a la infraestructura.
  * *Ejemplo:* `RutaYaIniciadaException`, `EntregaInvalidaException`.

---

### 2. Capa de Aplicación (`application`)
Actúa como la capa de orquestación y flujo de control. Define *qué* puede hacer el sistema (casos de uso) y *qué interfaces necesita* para comunicarse con el mundo exterior. Al igual que el dominio, no tiene conocimiento sobre bases de datos ni protocolos web.

* **`ports/in/` (Puertos de Entrada):** Interfaces de Java que exponen las operaciones que el mundo exterior puede solicitarle a nuestra aplicación. Son la API del core del negocio.
  * *Ejemplo:* `PlanificarRutasPort`, `IniciarRutaPort`, `ConfirmarEntregaPort`.
* **`ports/out/` (Puertos de Salida):** Interfaces de Java que definen lo que nuestra aplicación necesita del mundo exterior para cumplir su cometido. Son abstracciones de la infraestructura (bases de datos, clientes de APIs externas, mensajería).
  * *Ejemplo:* `RutaRepositoryPort` (necesidad de persistir), `ProveedorRutasExternoPort` (necesidad de enviar el lote de 100 al servicio de ruteo externo).
* **`usecases/`**: Clases concretas que implementan los puertos de entrada (`ports/in`). Representan el "pegamento" de la arquitectura. Su función es coordinar el flujo: inyectar los puertos de salida, recuperar las entidades del dominio, ejecutar los métodos de negocio de dichas entidades y guardar los resultados a través de los puertos de salida.
  * *Ejemplo:* `PlanificarRutasUseCase` (implementa `PlanificarRutasPort` e interactúa con `ProveedorRutasExternoPort`).

---

### 3. Capa de Infraestructura (`infrastructure`)
Es la capa externa que contiene los detalles técnicos de implementación. Aquí es donde se "ensucia" el código con frameworks (Spring Boot), drivers de bases de datos, protocolos de red (HTTP/REST, JSON), librerías de mapeo (JPA, Jackson), herramientas de documentación (Swagger) y contenedores (Docker).

* **`adapters/in/` (Adaptadores de Entrada):** Componentes técnicos que reciben estímulos del mundo exterior (una petición HTTP, un evento en una cola de mensajería, la ejecución de una tarea cron) y traducen esa información para invocar a un Puerto de Entrada de la aplicación.
  * *Ejemplo:* Controladores REST (`LogisticaController`), Listeners de mensajería (RabbitMQ), o un programador de tareas (*Scheduler* de baja carga).
* **`adapters/out/` (Adaptadores de Salida):** Implementaciones técnicas concretas de los Puertos de Salida (`ports/out`). El caso de uso llama a la interfaz sin saber cómo funciona, y este adaptador resuelve el detalle tecnológico real.
  * *Ejemplo:* Repositorios JPA con bases de datos SQL (`JpaRutaRepositoryAdapter`), clientes HTTP externos (usando Feign, WebClient o RestTemplate para golpear al proveedor de ruteo externo), o productores de eventos hacia colas de mensajería.
* **`config/`**: Contiene las clases de configuración específicas del framework. Aquí se configuran los beans de Spring (`@Configuration`), la inyección de dependencias que conecta los adaptadores con los casos de uso, configuraciones de CORS, beans de Swagger/OpenAPI y seguridad perimetral.

---

## Ventajas de este Enfoque para DonaTrack

1. **Bounded Context Aislados:** Cada servicio (Logística, Donaciones, Incentivos) gestiona su propia arquitectura de forma independiente, evitando acoplamientos rígidos a nivel código.
2. **Facilidad de Mockeo y Testing:** Es posible probar toda la lógica de un caso de uso (`usecases`) mockeando únicamente las interfaces de los `ports/out`, sin necesidad de levantar bases de datos ni servidores web reales.
3. **Independencia Tecnológica:** Si en el futuro se decide cambiar la base de datos (ej. de PostgreSQL a MongoDB) o el framework web, los cambios se limitarán exclusivamente a la capa de `infrastructure`, dejando el negocio (`domain` y `application`) intacto.