# Revisión Final - Requerimientos Entrega 4

A continuación se detalla el cruce exacto entre los 7 entregables exigidos por la cátedra para la **Entrega 4** (según el archivo `TP-Entrega4.pdf`) y su cumplimiento en el proyecto DonaTrack.

### 1. Modelo de Clases actualizado para cada Servicio
* **Estado:** ✅ Completado.
* **Justificación:** Se reflejó todo el modelo de objetos con JPA (anotaciones `@Entity`, `@OneToMany`, herencias, etc.) asegurando que las clases de dominio estén alineadas al modelo relacional.

### 2. Modelo de Datos (DER Físico)
* **Estado:** ✅ Completado.
* **Justificación:** Existen los diagramas físicos de las 4 bases de datos (Donaciones, Logística, Incentivos, Notificaciones) en la carpeta de diagramas, reflejando claves primarias, foráneas y tablas de herencia.

### 3. Justificaciones de Diseño
* **Estado:** ✅ Completado.
* **Justificación:** Se han documentado las decisiones arquitectónicas principales que explican la razón detrás de cada patrón utilizado.

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
