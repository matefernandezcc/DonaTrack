# Estado y Resumen de Avance: Entrega 4 (Persistencia, Integración y Despliegue)

Este documento consolida el estado actual, los avances logrados y las notas de la cátedra para la **Entrega 4** del sistema DonaTrack.

---

## 📌 Aclaraciones Clave del Profesor (Clase)
* **El DER es un mapeo directo:** Las tablas salen directamente de las clases de dominio. Debe ser FÍSICO y COMPLETO (con PKs, FKs, campos de control, etc.).
* **Broker de Logística:** No es un microservicio separado, sino una clase/componente dentro de Donaciones que decide si llamar a la logística local o a la desplegada en la nube si la principal cae.
* **Estrategia de Despliegue:** Desplegar logística en la nube (Render/Railway), pero apagarlo para no gastar. Tener copia local de backup (Broker permite esto).
* **Documento de Arquitectura:** Es un único documento general para todo el sistema, detallando el diagrama de componentes actualizado y las justificaciones.
* **Imágenes/Archivos:** Se guardan como URL, no como binarios en la BD.
* **Frontend:** No se requiere para la Entrega 4 (se pide en la 5). Por ahora, probar con Swagger o Postman/Bruno.

---

## 🟢 Lo que ya se completó

### Fase 1: Persistencia y Bases de Datos (JPA)
Se migró todo el modelo de objetos a **PostgreSQL** y **Spring Data JPA** manteniendo schemas aislados (`logistica`, `donaciones`, `incentivos`, `notificaciones`).
* [x] Mapeo de entidades de negocio a `@Entity`.
* [x] Estrategias de herencia: `JOINED` (Personas/Roles) y `SINGLE_TABLE` (Necesidades).
* [x] **Fidelidad DER ↔ JPA ↔ SQL:** Las 36 tablas están 100% alineadas.
* [x] Eliminación de Mocks y refactorización de bugs (ej. `PersonaEntity` sin ID autogenerado requirió UUID manual en tests).
* [x] Perfiles de conexión (`local`, `prod`, `test`) y tests con H2 in-memory.

### Fase 2: Integración de Microservicios y Mensajería
* [x] **RabbitMQ (Asíncrono):** Topic Exchanges para `donaciones` e `incentivos`. Los servicios publican eventos (ej. inicio de ruta) y `notificaciones` consume.
* [x] **Broker de Logística:** Implementado en `donatrack-donaciones` (`LogisticaBrokerAdapter`). Usa Feign con Fallback al servicio local (`localhost:8002`).

### Fase 3: Arquitectura y Documentación
* [x] **Doc Arquitectura:** Creado en `docs/arquitectura.md` (SOA + Hexagonal).
* [x] **Diagramas de Componentes y Clases:** Actualizados en `diagramas/` reflejando persistencia y RabbitMQ.
* [x] **DER Físicos:** 4 diagramas (`DER-Donaciones`, `DER-Logistica`, `DER-Incentivos`, `DER-Notificaciones`).
* [x] **Swagger y Bruno:** Configuraciones listas para pruebas.

### Fase 3.5: Infraestructura Docker
* [x] `compose.yaml` (Postgres, RabbitMQ, n8n, 5 servicios Java) y Dockerfiles actualizados.

---

## 🟢 Fase 4: Despliegue en la Nube y Testing (Completada)

El despliegue en la nube y la integración avanzada con el Broker han sido completados exitosamente cumpliendo todos los requisitos:

1. **Despliegue de Logística:** 
   * [x] Publicar `donatrack-logistica` remotamente en Render: `https://dona-logistica.onrender.com/swagger-ui/index.html`.
   * [x] Conectar con BD Supabase PostgreSQL (schema `logistica` con pooler AWS US-East-1).
2. **Configuración del Broker de Integración (Donaciones -> Logística):**
   * [x] Implementar soporte multicapa en `LogisticaBrokerAdapter`.
   * [x] Intento 1: Servidor propio en la nube (`LogisticaRemoto1Client` -> `logistica.url.remota1`).
   * [x] Intento 2 (Compañero): Servidor alternativo en la nube (`LogisticaRemoto2Client` -> `logistica.url.remota2`).
   * [x] Intento 3 (Fallback): Logística local (`LogisticaLocalClient` -> `localhost:8002`) por si la nube falla o está pausada.
3. **Pruebas y Documentación:**
   * [x] Configuración de Pre-Request Scripts inteligentes en Bruno para enrutar el test al servidor productivo que esté vivo, ignorando los caídos.
   * [x] Comprobación de que las requests hacia Base de Datos (GET/POST CRUD) operan con código HTTP 200 en Render.
   * [x] Nota técnica documentada: Las requests que usan mensajería asíncrona dan 500 en la nube por falta de clúster RabbitMQ, pero caen elegantemente en Fallback local al usarlas a través del Broker.
   * [x] Verificación de suite de tests unitarios y de persistencia JPA pasando al 100%.
