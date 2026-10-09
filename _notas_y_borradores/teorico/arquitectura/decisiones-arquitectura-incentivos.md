# Decisiones Arquitectónicas: Microservicio de Incentivos

En este documento se detallan las decisiones de diseño aplicadas en el desarrollo del microservicio de Incentivos, fundamentadas en los principios de **Clean Architecture** (Arquitectura Limpia), **Domain-Driven Design (DDD)** y la segregación en una arquitectura basada en **Microservicios**.

---

## 1. Identidad de Dominio Agnostica a la Persistencia

### El Problema
Tradicionalmente, en aplicaciones acopladas a la base de datos, las entidades JPA (capa de infraestructura) delegan la generación de la identidad al motor de base de datos utilizando anotaciones como `@GeneratedValue`. Esto provoca que un objeto en memoria no posea un ID válido hasta el momento exacto en que es persistido.

### La Solución y el Porqué (Trade-off)
Se ha retirado la anotación `@GeneratedValue` de las entidades JPA (como `RankingMensualEntity`).

En Clean Architecture, el Dominio (el núcleo del negocio) debe dictar las reglas, sin depender de los detalles de implementación (como Hibernate o PostgreSQL). 
Al hacer que el Dominio asigne un identificador único (UUID) en el momento de creación del objeto, garantizamos que:

1. **Aislamiento Total:** El dominio no necesita de una base de datos para funcionar ni para ser testeado. Las entidades nacen con identidad.
2. **Distribución Temprana:** Al tener un ID garantizado desde el instante cero, es posible publicar eventos de dominio (enviar mensajes por RabbitMQ o Webhooks) de forma asíncrona usando ese ID antes o al mismo tiempo que la transacción en la base de datos se completa, evitando cuellos de botella.
3. **Rol del Mapper:** La responsabilidad de mapear el objeto se traslada a clases puente como `PerfilDonanteMapper` o `RankingMensualMapper`. El mapper toma el UUID puro del Dominio y se lo setea explícitamente a la entidad JPA. La base de datos asume un rol pasivo, limitándose a guardar lo que el dominio le indica.

```java
// Ejemplo conceptual en Mapper
RankingMensualEntity entity = new RankingMensualEntity();
// El ID proviene exclusivamente del objeto puro de dominio
entity.setId(dominio.getId()); 
entity.setMes(dominio.getMes());
```

---

## 2. Comunicación Síncrona entre Microservicios (Feign)

### El Problema
El microservicio de **Incentivos** es responsable de calcular puntajes y armar el podio/ranking, así como también de otorgar insignias. Sin embargo, su base de datos (`perfiles_donante`) sólo almacena identificadores (UUIDs) y métricas de los donantes.
Para enviar notificaciones completas por Discord, se requiere mostrar el nombre real del usuario (ej: "Juan Perez"). El microservicio no cuenta con estos datos, ya que están centralizados en el microservicio de **Donaciones**.

### La Solución y el Porqué
Para evitar un anti-patrón de base de datos compartida (donde Incentivos lea la BD de Donaciones), se utiliza **Spring Cloud OpenFeign** para establecer comunicación HTTP síncrona de manera declarativa.

Se creó la interfaz `PersonaClient`, que actúa como cliente REST interno:

```java
@FeignClient(name = "personasClient", url = "${personas.api.url:http://donaciones:8000}")
public interface PersonaClient {

    @GetMapping("/api/personas/{id}")
    PersonaDTO obtenerPersona(@PathVariable("id") UUID id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PersonaDTO(String nombre, String apellido, String razonSocial, String tipo) {}
}
```

**Trade-offs y Beneficios:**
- **Encapsulamiento de Red:** Feign abstrae toda la complejidad de realizar la llamada HTTP a `http://donaciones:8000`.
- **Tolerancia a fallos en Payload:** Gracias a la anotación `@JsonIgnoreProperties(ignoreUnknown = true)`, el microservicio de Incentivos parsea exclusivamente los datos necesarios (`nombre`, `apellido`, etc.). Si Donaciones llega a enviar un payload enorme con múltiples campos (DNI, fecha de nacimiento), Incentivos los ignora de forma segura.
- **Acoplamiento Síncrono:** La contrapartida de este enfoque es que Incentivos requiere que Donaciones esté online para resolver los nombres. En caso de timeout, el diseño se maneja por medio de excepciones silenciosas y un *fallback* a devolver el propio UUID crudo como representación, previniendo que falle toda la generación del ranking.

---

## 3. Notificaciones Asíncronas de Insignias (Diseño y Flujo)

### El Problema
Se necesita notificar al usuario (a través de Discord vía n8n) cuando consigue un nuevo logro o insignia. Sin embargo, no se debe hacer spam ni enviar notificaciones cada vez que el usuario ingresa a consultar su perfil.

### La Solución y el Porqué
Por diseño, la consulta de insignias (`GET /api/donantes/:donanteId/insignias`) es una operación de solo lectura (idempotente). Su única responsabilidad es recuperar el estado actual para que el frontend lo dibuje.

El envío del mensaje a Discord se restringe **estrictamente al momento de otorgamiento de la insignia**. Este momento ocurre únicamente al registrar una nueva donación (`POST /api/donantes/:donanteId/actividad`).
Cuando ingresa una nueva actividad:
1. El Dominio evalúa si el usuario cumple los requisitos para una nueva misión.
2. Si la cumple, se le otorga la insignia y se dispara un evento de dominio (`InsigniaObtenidaEvent`).
3. Un adaptador de infraestructura captura ese evento y envía el payload a n8n para que dispare el mensaje de Discord.

### Cómo probar y forzar una nueva notificación (Paso a Paso)

Si deseas volver a ver el mensaje de "Nueva Insignia" en Discord para un usuario que ya la tiene, debes seguir estos pasos para reiniciar su progreso y simular que la gana por primera vez:

1. **Eliminar las insignias actuales:**
   Conéctate a la base de datos de PostgreSQL del contenedor y borra el progreso del usuario. Por ejemplo, para el usuario `Juan Perez`:
   ```sql
   -- Borrar progreso de misiones y perfil para forzar inicio limpio
   DELETE FROM incentivos.perfiles_donante WHERE perfil_donante_id = 'a1111111-1111-4111-8111-111111111111';
   ```
2. **Generar la actividad requerida:**
   Dirígete a Bruno y ejecuta la request **"Registrar Actividad"** (`POST /api/donantes/:donanteId/actividad`).
   Asegúrate de ejecutarla las veces que sea necesario para cumplir la misión. Por ejemplo, la misión "Buen Inicio" requiere 2 donaciones, así que deberás ejecutar la request 2 veces (cambiando el `idDonacion` en el body para que no dé error de duplicado).
3. **Verificar el resultado:**
   En el momento en que ejecutes la última request que cumple la condición, observarás en los logs del contenedor (`docker logs donatrack-incentivos`) que se envía el evento a n8n, y el mensaje aparecerá inmediatamente en el canal de Discord.

---

# Referencias

- [RankingMensualEntity.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-incentivos/src/main/java/com/donatrack/incentivos/infrastructure/adapters/out/persistence/entities/RankingMensualEntity.java)
- [PersonaClient.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-incentivos/src/main/java/com/donatrack/incentivos/infrastructure/adapters/out/client/PersonaClient.java)
- [IncentivoController.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-incentivos/src/main/java/com/donatrack/incentivos/infrastructure/adapters/in/api/IncentivoController.java)
