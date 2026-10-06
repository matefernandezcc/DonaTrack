# Revisión Final - Requerimientos Entrega 4

A continuación se detalla el cruce exacto entre los 7 entregables exigidos por la cátedra para la **Entrega 4** (según el archivo `TP-Entrega4.pdf`) y su cumplimiento en el proyecto DonaTrack.

### 1. Modelo de Clases actualizado para cada Servicio
* **Estado:** ⚠️ Pendiente de revisión grupal / Ajustes requeridos.
* **Justificación:** Se reflejó todo el modelo de objetos con JPA (anotaciones `@Entity`, `@OneToMany`, herencias, etc.) asegurando que las clases de dominio estén alineadas al modelo relacional.
* **Resultado de la Auditoría:** Se detectaron discrepancias entre el diagrama de clases conceptual (`DC-Donaciones.puml`) y la implementación física en JPA. El código realiza un aplanamiento de clases (`Contacto`, `DocumentoIdentidad`, `Provincia`, `Pais`, `Coordenada`) y cambia Enums por Strings (`EstadoDonacion`), que no están reflejados en el diagrama de clases. Se debe discutir con el equipo si se adapta el diagrama al código o viceversa.

### 2. Modelo de Datos (DER Físico)
* **Estado:** ✅ Completado y Auditado con éxito.
* **Justificación:** Existen los diagramas físicos de las 4 bases de datos (Donaciones, Logística, Incentivos, Notificaciones) en la carpeta de diagramas, reflejando claves primarias, foráneas y tablas de herencia.
* **Resultado de la Auditoría:** Aprobado al 100%. Se cruzó exhaustivamente los diagramas físicos (`DER-Donaciones.puml`, etc.) con el código real de las entidades JPA. Los diagramas DER son un reflejo exacto y fiel de la implementación, capturando correctamente el aplanamiento de objetos embebidos, los tipos de datos primitivos desnormalizados y las estrategias de herencia (`JOINED` y `SINGLE_TABLE`).

### 3. Justificaciones de Diseño
* **Estado:** ✅ Completado.
* **Justificación Detallada:** Se han documentado las decisiones arquitectónicas principales que explican la razón detrás de cada patrón utilizado. Entre las justificaciones clave se encuentran:
  1. **Comunicación Event-Driven (RabbitMQ):** Se justifica el desacoplamiento temporal y la restricción de que Logística no debe invocar sincrónicamente a Donaciones ni Notificaciones.
  2. **Broker Multicapa para Logística:** Justificación de la resiliencia en la nube frente a entornos pausados (Free Tier).
  3. **Aislamiento de Persistencia:** Uso de esquemas lógicos separados para PostgreSQL dentro de Supabase.
* ⚠️ **ACCIÓN PENDIENTE (RECORDATORIO):** Tal como se indicó en los borradores de la Entrega 3, toda esta sección de justificaciones **debe ser exportada al archivo Excel** para mantener persistencia institucional y luego acoplarse definitivamente a los archivos de justificación de arquitectura.

### 4. Diagrama de Componentes actualizado
* **Estado:** ✅ Completado.
* **Justificación:** El diagrama refleja las colas de mensajes de RabbitMQ para la comunicación asíncrona y el componente del Broker de Integración entre Donaciones y Logística.

### 5. Documento de Arquitectura (Patrones y Capas)
* **Estado:** ✅ Completado.
* **Justificación:** El archivo `docs/arquitectura.md` incluye la justificación de la arquitectura Hexagonal (Puertos y Adaptadores), Arquitectura Orientada a Servicios (SOA) y la explicación detallada del **Broker Multicapa** (que implementa una cascada de 3 niveles: Render Propio -> Render Compañero -> Localhost). 

### 6. Implementación de requerimientos de integración
* **Estado:** ✅ Completado.
* **Justificación:** 
  * **Colas de Mensajes:** Implementación con RabbitMQ para aislar de forma asíncrona al Servicio de Notificaciones.
  * **Broker de Logística:** Implementado mediante el patrón Broker (`LogisticaBrokerAdapter`) haciendo uso de Feign Clients y control de fallos (Fallback).

### 7. Despliegue del servicio de logística
* **Estado:** ✅ Completado.
* **Justificación:** El microservicio de Logística fue desplegado exitosamente en Render y conectado a una base de datos PostgreSQL en la nube (Supabase).
* **Nota Técnica de Pausa:** La consigna indica explícitamente: *"El despliegue puede encontrarse pausado para reducir consumos hasta que sea presentado en la defensa"*. Para gestionar inteligentemente esta restricción, se construyó un *Pre-Request Script* dinámico en Bruno que verifica qué servidor de la nube está activo y sano, evitando fallas por *cold-starts* o instancias suspendidas de Render.

---

## 🔎 Anexo: Auditoría de Código (Cumplimiento de Integración)

Para garantizar la veracidad técnica de los puntos de integración, se realizó una auditoría directa sobre el código fuente de los microservicios:

### 1. Cola de mensajes para notificaciones (Asincronismo)
**Requerimiento:** *La integración con el Servicio de Notificaciones debe ser asincrónica mediante colas para no afectar disponibilidad.*
* **Auditoría:** En `donatrack-donaciones`, la clase `NotificacionesMessagingAdapter.java` no realiza peticiones HTTP bloqueantes. Utiliza `RabbitTemplate` para despachar mensajes (tipo `NotificacionRequest`) hacia el exchange `donaciones.exchange`.
* **Consumidor:** En `donatrack-notificaciones`, las clases `DonacionesRabbitMQListener.java` e `IncentivosRabbitMQListener.java` utilizan la anotación `@RabbitListener` para consumir asincrónicamente los eventos.
* **Veredicto:** ✅ El diseño es 100% asincrónico y tolerante a fallos de disponibilidad en el servicio de Notificaciones.

### 2. Broker de Integración con Logística (Soporte Multi-Servicio)
**Requerimiento:** *Implementar un broker que permita seleccionar entre más de 1 servicio de logística disponible (el propio y otro externo).*
* **Auditoría:** En `donatrack-donaciones`, la clase `LogisticaBrokerAdapter.java` implementa el patrón estructural Broker orquestando clientes Feign.
* **Estrategia (Fallback en Cascada):** El bloque `try-catch` del Broker intenta enviar los datos a `LogisticaRemoto1Client` (nube propia). Ante una desconexión o timeout (503 Service Unavailable por pausa gratuita), redirige a `LogisticaRemoto2Client` (nube del compañero). Como último recurso, aterriza en `LogisticaLocalClient` (`localhost:8002`).
* **Veredicto:** ✅ Se cumple sobradamente la consigna, agregando 3 niveles de resiliencia frente a caídas.

### 3. Requerimiento de Despliegue (Pausa por Consumos)
**Requerimiento:** *El servicio de logística deberá ser desplegado vía web. El despliegue puede encontrarse pausado para reducir consumos hasta que sea presentado en la defensa.*
* **Auditoría:** El servicio `donatrack-logistica` se encuentra productivo en el dominio `https://dona-logistica.onrender.com`. Al estar hospedado en el *Free Tier* de Render, entra en suspensión (*Spin Down*) automáticamente tras 15 minutos sin tráfico, evitando costos.
* **Solución de Ingeniería:** Para mitigar el tiempo de *"cold-start"* de Render (que puede demorar más de 50 segundos y causar fallas en las pruebas), se implementó un **Pre-Request Script** en la colección de Bruno. Este script intercepta la solicitud, hace un *ping* a `/api/camiones` para verificar el estado del servidor, y en caso de que esté pausado o caído, permite alertar al usuario y habilitar el salto transparente hacia el Broker local sin romper la experiencia.
* **Veredicto:** ✅ Estrategia de despliegue 100% cumplida y complementada con automatización de pruebas para soportar la restricción económica.
