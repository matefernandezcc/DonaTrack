# Análisis de la Entrega 4: Persistencia, Integración y Despliegue

## Resumen de Aclaraciones del Profesor (Clase)

De la transcripción de la clase se extraen estos puntos clave que ajustan cómo abordar la entrega:

> [!IMPORTANT]
> **El DER es un mapeo directo del modelo de objetos.** El profesor fue enfático: "el modelo de datos sea simplemente un mapeo de objetos, que yo no construya el DER desde cero sino que hagan el mapping". Es decir, las tablas salen directamente de las clases de dominio que ya tienen.

> [!IMPORTANT]
> **El DER debe ser FÍSICO y COMPLETO.** Un alumno preguntó cuánto detalle poner, y el profesor respondió: "Sí, bien físico, bien diseño, cualquier cosa, sean de negocio o no sean de negocio." Incluye campos de control de concurrencia, campos técnicos, URLs para archivos (en vez de guardar binarios), metadatos, etc.

> [!IMPORTANT]
> **El Broker NO es un componente separado sino algo más simple.** El profesor dijo: "No tiene que ser otro componente, tiene que ser algo que cumpla con ese rol". Es una clase/componente dentro de Donaciones que decide a cuál servicio de logística llamar. La idea es: tener logística local + logística desplegada, y el broker elige cuál usar (si el principal está apagado, usa el secundario).

> [!TIP]
> **Estrategia de despliegue: prender y apagar.** El profesor sugiere: desplegar logística en la nube, pero apagarlo cuando no se usa para no gastar cuota. Tener una copia local como backup. El broker permite switchear entre ambos. "Prendan y apáganlo. Para eso tengan dos servicios de logística."

> [!NOTE]
> **El foco ahora es el diagrama de componentes, no el de clases.** El profesor dijo: "Ya no nos importa el modelo de objetos. Lo que nos importa es el diagrama de componentes." La visión de arquitectura de alto nivel es lo principal de acá en adelante.

> [!NOTE]
> **El documento de arquitectura es uno general.** Un alumno preguntó si es uno por servicio o uno general. El profesor confirmó que es uno general. Quiere que incluya los debates de arquitectura reales del equipo.

> [!NOTE]
> **Los archivos (fotos/imágenes) se guardan como URL, no como binario.** El profesor dijo: "guardamos la URL, el metadato para acceder". No almacenar las imágenes en la base.

---

## Estado Actual vs. Lo que Pide la Entrega

| # | Entregable | Estado | Esfuerzo |
|---|-----------|--------|----------|
| 1 | Modelo de Clases actualizado por servicio | 🟡 Parcial | 🔥 Bajo |
| 2 | DER físico por servicio | 🔴 Falta | 🔥🔥 Medio |
| 3 | Justificaciones de Diseño (doc + diagramas) | 🟡 Parcial | 🔥🔥 Medio |
| 4 | Diagrama de Componentes actualizado | 🟡 Parcial | 🔥 Bajo |
| 5 | Documento de arquitectura | 🔴 Falta | 🔥🔥 Medio |
| 6a | Cola de mensajes para Notificaciones | 🟢 Casi listo | 🔥 Bajo |
| 6b | Broker de Integración con Logística | 🔴 Falta | 🔥🔥 Medio |
| 7 | Despliegue de logística | 🔴 Falta | 🔥🔥 Medio |

---

## Análisis Detallado por Entregable

### 1. Modelo de Clases Actualizado por Servicio

**Estado actual:**
- Existe [DonaTrack-Diagrama-Clases.puml](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/diagramas/DonaTrack-Diagrama-Clases.puml) general.
- Existe [ServicioLogistica.puml](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/diagramas/logistica/ServicioLogistica.puml) por servicio.
- Carpetas `donaciones/`, `incentivos/`, `notificaciones/` existen en diagramas.

**Pendiente:**
- [ ] Actualizar diagramas de clases reflejando las entities JPA (capas de persistencia).
- [ ] Incluir las clases de integración (listeners RabbitMQ, broker, adapters).

---

### 2. DER Físico por Servicio

> [!CAUTION]
> Completamente faltante. El profesor enfatizó que es un DER **físico**: tipos de dato, PKs, FKs, campos técnicos (timestamps, versiones, etc.), todo lo que está en la base real.

**El enfoque correcto (según el profesor):** el DER sale como mapeo directo del modelo de objetos. Tomar cada clase → tabla, cada atributo → columna, resolver herencias y relaciones.

**Lo que falta:**
- [ ] DER físico para **Logística** (schema `logistica`) — Ya tiene entities JPA, es el más fácil de generar
- [ ] DER físico para **Donaciones** (schema `donaciones`)
- [ ] DER físico para **Incentivos** (schema `incentivos`)
- [ ] DER físico para **Notificaciones** (schema `notificaciones`)

**Decisiones a tomar para los DERs:**
- Estrategia de herencia para `Necesidad` (RecurrenteExtraordinaria): `SINGLE_TABLE`, `JOINED`, o `TABLE_PER_CLASS`
- Estrategia de herencia para `Persona` (Humana/Jurídica) si aplica
- Archivos/fotos: guardar como URL (string) según lo que dijo el profesor
- Decidir qué campos técnicos incluir (created_at, updated_at, version, etc.)

---

### 3. Justificaciones de Diseño

**Estado actual:** Existe [revision_entrega_requerimiento.md](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/_notas_y_borradores/revision_entrega_requerimiento.md) como revisión de entregas anteriores, pero no es un documento de justificaciones de diseño para la entrega 4.

**Lo que falta:**
- [ ] Documento que justifique las decisiones de diseño específicas de esta entrega:
  - ¿Por qué RabbitMQ y no otro mecanismo para las notificaciones?
  - ¿Por qué un broker para logística y cómo funciona la selección?
  - ¿Qué estrategias de herencia ORM se eligieron y por qué?
  - ¿Por qué PostgreSQL con schemas separados?
- [ ] Diagramas complementarios de secuencia para los flujos de integración

---

### 4. Diagrama de Componentes Actualizado

> [!IMPORTANT]
> El profesor fue MUY enfático: **"Todo tiene que estar en el modelo de componentes."** Incluye: los 4 servicios, RabbitMQ, PostgreSQL, n8n, APIs externas (Twilio, SendGrid, Meta), el broker de logística, el componente externo de planificación de rutas. "Yo miro el modelo de componentes y es lo primero que tengo que entender."

**Estado actual:**
- Existe [DonaTrack-Diagrama-Componentes-y-Despliegue.puml](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml) — bastante completo, pero le faltan cosas.

**Lo que falta agregar:**
- [ ] El **Broker de Integración** entre Donaciones y Logística
- [ ] El **componente externo de planificación de rutas** + callback
- [ ] Diferenciar: nodo local de logística vs. nodo desplegado de logística
- [ ] Verificar que estén **todas** las integraciones reflejadas (n8n, Discord webhooks, APIs de notificación)

---

### 5. Documento de Arquitectura

> [!CAUTION]
> Completamente faltante. El profesor confirmó que es **un documento general** (no uno por servicio). Quiere que sea sobre los componentes, comunicaciones, y justificaciones técnicas.

**Lo que falta:**
- [ ] Crear `docs/arquitectura.md` que cubra:
  - **Estilo arquitectónico**: Arquitectura orientada a servicios con Hexagonal por servicio
  - **Patrones de integración**: 
    - Cola de mensajes (RabbitMQ): para desacoplar servicios de dominio ↔ notificaciones
    - Broker de integración: para seleccionar proveedor de logística
    - Callback: para recibir resultados del planificador externo
  - **Persistencia**: PostgreSQL con schemas aislados, ORM con JPA/Hibernate
  - **Justificaciones** de cada decisión arquitectónica
  - El profesor dijo: "Discutan todo lo que hay que discutir, qué componentes hay y cómo se están comunicando"

---

### 6a. Cola de Mensajes para Notificaciones ✅ (Casi listo)

**Estado actual — Ya implementado:**
- RabbitMQ en [compose.yaml](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/compose.yaml) ✅
- Logística → RabbitMQ → Donaciones (eventos de ruta/entrega) ✅
- Donaciones → RabbitMQ → Notificaciones (notificación inicio ruta, entrega OK, entrega fallida) ✅
- Notificaciones consume vía listeners RabbitMQ ✅

**Pendiente menor:**
- [ ] Verificar que **Incentivos → Notificaciones** también use RabbitMQ (actualmente parece usar REST directo vía `IncentivosNotificacionAdapter` / `NotificacionClient`)
- [ ] Verificar que **Donaciones → Notificaciones** para notificaciones de inactividad (20 días sin donar) también use RabbitMQ

---

### 6b. Broker de Integración con Logística 🔴 (Falta)

> [!WARNING]
> No hay ninguna implementación del broker. Pero según el profesor, no necesita ser un componente externo complicado. Es una clase dentro de Donaciones (o entre Donaciones y Logística) que decide a cuál servicio de logística dirigir la comunicación.

**Lo que el profesor explicó:**
- Tener logística corriendo localmente (localhost:8002) 
- Tener logística desplegada en la nube (URL externa)
- El broker decide: si el servicio principal (nube) está disponible → lo usa. Si está caído → usa el local (o viceversa)
- "Métan acá algo que haga de broker, que decida cuál servicio uso"

**Lo que falta implementar:**
- [ ] Crear interfaz `LogisticaServiceBroker` con un método que encapsule las llamadas a logística
- [ ] Implementar la lógica de selección (health check, configuración, fallback)
- [ ] Configurar las URLs de ambos servicios de logística (local y remoto)
- [ ] Integrar el broker en los puntos donde Donaciones necesita comunicarse con Logística

---

### 7. Despliegue del Servicio de Logística

**Estado actual:**
- [Dockerfile](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/Dockerfile) existe ✅
- [application-prod.properties](file:///home/maximo-hidalgo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-logistica/src/main/resources/application-prod.properties) con config Supabase ✅
- Docker Compose funcional ✅

**Lo que falta:**
- [ ] Elegir plataforma de despliegue (Render, Railway, Fly.io, etc.) — Investigar cuál ofrece tier gratuito para Java
- [ ] Desplegar el servicio de logística allí
- [ ] Obtener la URL pública del servicio
- [ ] Verificar que los endpoints responden (Swagger UI accesible)
- [ ] Configurar la base de datos remota (Supabase u otro)
- [ ] Documentar las URIs de acceso

> [!TIP]
> El profesor dijo: "No lo mantengan, apréndanlo y apáganlo." No necesita estar corriendo 24/7, solo funcionar cuando lo demuestren o cuando otro grupo lo pruebe.

---

## Persistencia JPA — Estado por Servicio

| Servicio | Entities JPA | Repos JPA | Repos Mock | Estado |
|----------|-------------|-----------|------------|--------|
| **Logística** | 7 entities + 3 embeddables | 2 | 6 | 🟡 Parcial |
| **Donaciones** | 0 | 1 | 4 | 🔴 Sin persistencia |
| **Incentivos** | 0 | 0 | 1 | 🔴 Sin persistencia |
| **Notificaciones** | 0 | 0 | 0 | 🟡 Podría justificarse |

### Trabajo de persistencia pendiente:

#### Logística (poco trabajo):
- [ ] Migrar `MockCamionRepository` → JPA (entity `CamionEntity` ya existe)
- [ ] Migrar `MockChoferRepository` → JPA (entity `ChoferEntity` ya existe)
- [ ] Migrar `MockEntregaRepository` → JPA (entity `EntregaEntity` ya existe)
- [ ] Migrar `MockRutaDeRepartoRepository` → JPA (entity `RutaDeRepartoEntity` ya existe)
- [ ] Verificar los mappers domain ↔ entity existentes

#### Donaciones (trabajo pesado):
- [ ] Crear entities JPA para todas las clases de dominio: `Donacion`, `DonacionOriginal`, `Bien`, `Categoria`, `Subcategoria`, `Persona`, `Contacto`, `Donante`, `Beneficiario`, `Representante`, `Necesidad`, `NecesidadRecurrente`, `NecesidadExtraordinaria`, `PeriodoNecesidad`, `HistorialEstado`, `Foto`, `Archivo`
- [ ] Resolver herencias JPA (Necesidad, Persona, Rol)
- [ ] Crear JPA repositories
- [ ] Crear mappers domain ↔ entity
- [ ] Migrar Mocks a JPA

#### Incentivos:
- [ ] Crear entities JPA para: `PerfilDonante`, `MetricasDonante`, `Mision`, `Insignia`, `RegistroDonacion`, `CategoriaDonante`
- [ ] Crear JPA repositories
- [ ] Migrar `MockPerfilDonanteRepository` → JPA

#### Notificaciones:
- [ ] Evaluar si necesita persistencia (puede justificarse que solo procesa y despacha)
- [ ] Si se persiste: crear entities para `Notificacion` y `Evento`

---

## Plan de Priorización Sugerido

### Fase 1 — Persistencia (lo más pesado)
1. **Completar JPA de Logística** (las entities ya existen, solo faltan repos)
2. **JPA de Donaciones** (el más complejo, es el core)
3. **JPA de Incentivos**

### Fase 2 — Integración
4. **Implementar Broker de Logística** (clase simple dentro de Donaciones)
5. **Migrar Incentivos → Notificaciones a RabbitMQ** (si aún usa REST)

### Fase 3 — Documentación
6. **Generar DER físicos** (se generan desde las entities JPA creadas en Fase 1)
7. **Documento de arquitectura** (un doc general)
8. **Actualizar diagrama de componentes** (agregar broker, planificador externo)
9. **Actualizar diagramas de clases** (reflejar entities JPA)

### Fase 4 — Despliegue
10. **Investigar y desplegar logística en la nube**
11. **Configurar broker para switchear entre local y nube**
12. **Colección Postman / Swagger** (se genera casi automáticamente)
