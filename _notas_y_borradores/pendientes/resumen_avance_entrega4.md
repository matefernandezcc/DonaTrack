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

## 🔴 Lo que falta por resolver (Fase 4: Despliegue)

El último gran paso para cerrar la entrega es el **despliegue en la nube** para demostrar el funcionamiento del Broker:

1. **Despliegue de Logística:** 
   * [ ] Publicar `donatrack-logistica` remotamente (Render/Railway).
   * [ ] Conectar con BD Supabase.
2. **Configuración del Broker:**
   * [ ] Actualizar `application.yml` de `donaciones` con la URL del servicio remoto.
3. **Pruebas Finales:**
   * [ ] Generar export de colección de Postman/Bruno final para pruebas.
