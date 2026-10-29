# 📋 Tablero de Estado y Cierre — Entrega 5 (Persistencia Políglota)

**Sistema:** DonaTrack — Plataforma de Gestión y Trazabilidad de Donaciones  
**Cátedra:** Diseño de Sistemas (K3002) — UTN FRBA (2026)  
**Fecha de Actualización:** 29 de Octubre de 2026  
**Rama:** `feature/entrega-5-persistencia-poliglota`  
**Estado General:** ✅ **Núcleo Técnico y Entregables Completados al 95%**

---

## 🚦 1. Requerimientos Oficiales de la Entrega 5

| # | Requerimiento de la Cátedra | Estado | Evidencia / Ubicación |
|---|---|:---:|---|
| **R1** | **Persistencia de entidades por servicio:** Cada servicio debe persistir su propio modelo. | ✅ **COMPLETO** | Esquemas segregados en PostgreSQL (`donaciones`, `incentivos`, `notificaciones`) y MongoDB (`donatrack_logistica`). |
| **R2** | **Persistencia Políglota:** Logística en base documental (NoSQL) con ODM; resto en base relacional con ORM. | ✅ **COMPLETO** | Logística usa **MongoDB** (`mongo:7-jammy` / Atlas) con **Spring Data MongoDB**; Donaciones/Incentivos/Notificaciones usan **PostgreSQL** con **Hibernate/JPA**. |
| **R3** | **Estrategia de Desnormalización:** Diseñada para optimizar consultas de lectura frecuentes. | ✅ **COMPLETO** | Documento raíz agregado `rutas_reparto` con paradas/entregas embebidas y desnormalización de camión/chofer (snapshot histórico inmutable $O(1)$). |

---

## 📦 2. Entregables Formales de la Entrega 5

| # | Entregable Oficial | Estado | Artefacto / Archivo Generado |
|---|---|:---:|---|
| **E1** | **Actualización de Modelo de Dominio y Modelo de Datos** | ✅ **COMPLETO** | - [`DC-Donaciones-Entrega5.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/diagramas/donaciones/DC-Donaciones-Entrega5.puml) (nuevo diagrama con VOs aplanados y JPA)<br>- [`DER-Logistica.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/diagramas/logistica/DER-Logistica.puml) (modelo NoSQL de colecciones e índices) |
| **E2** | **Actualización de Justificaciones de Diseño** | ✅ **COMPLETO** | Archivo Excel institucional `Justificación Decisiones - DonaTrack - Grupo 7.xlsx` con las 10 decisiones de E5 y 6 decisiones de E3/E4 integradas por el usuario. |
| **E3** | **Actualización de Diagrama de Componentes y Despliegue** | ✅ **COMPLETO** | [`DonaTrack-Diagrama-Componentes-y-Despliegue.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/diagramas/DonaTrack-Diagrama-Componentes-y-Despliegue.puml) reflejando doble perfil (Docker local vs. Prod Cloud con MongoDB Atlas, Supabase y CloudAMQP). |
| **E4** | **Implementación en Código y Bases de Datos** | ✅ **COMPLETO** | 10 Documentos BSON, 5 Repositorios Spring Data Mongo, 5 Mappers, 6 Adaptadores Hexagonales y 26/26 tests pasando en Logística. |

---

## 🛠️ 3. Deudas Técnicas Resueltas en Esta Sesión

| Deuda Técnica | Problema Previo | Solución Aplicada | Estado |
|---|---|---|:---:|
| **DEUDA 1** | Discrepancia entre `DC-Donaciones.puml` y el modelo físico JPA (aplanamiento de `Contacto`, `Coordenada`, `DocumentoIdentidad`, `Provincia`, `Pais` y Enums como Strings). | Se preservó el archivo original intacto y se generó [`DC-Donaciones-Entrega5.puml`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/diagramas/donaciones/DC-Donaciones-Entrega5.puml) con notas y estereotipos explícitos (`<<Value Object (Aplanado en DB)>>`, `<<Entity JPA: JOINED>>`, `<<Entity JPA: SINGLE_TABLE>>`), además de incorporar los casos de uso transaccionales y el Broker de Logística. | ✅ **RESUELTO** |
| **DEUDA 2** | Justificaciones no exportadas al Excel institucional. | Completado y subido por el usuario en `docs/Justificación Decisiones - DonaTrack - Grupo 7.xlsx`. | ✅ **RESUELTO** |
| **DEUDA 3** | Workflow N8N Ranking apuntaba a `httpbin.org/post` en vez de Discord. | Se actualizó el nodo `Publish to Discord Webhook` a `={{ $env.DISCORD_WEBHOOK_URL }}` en `n8n/workflows/workflows.json` y se verificó en vivo con ejecuciones manuales (ID 156) y vía webhook HTTP (ID 157) con respuesta 200 OK. | ✅ **RESUELTO** |
| **DEUDA 5** | Guía `testing_base_de_datos.md` usaba consultas SQL obsoletas para Logística. | Se actualizaron los tests 4 y 6 en [`testing_base_de_datos.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/_notas_y_borradores/teorico/testing/testing_base_de_datos.md) con comandos nativos de MongoDB (`db.camiones.find()`, `db.items_planificacion.find()`), endpoints REST y Docker con `mongo-donatrack`. | ✅ **RESUELTO** |

---

## ⏸️ 4. Lo Pospuesto (Fuera del Alcance de la Entrega 5)

Los siguientes ítems se dejan deliberadamente pospuestos ya que no bloquean la Entrega 5:

1. **Módulo `donatrack-auth`:**
   * **Estado:** Se mantiene el esqueleto Maven en el proyecto pero sin desarrollo de lógica interna (JWT, Spring Security).
   * **Motivo:** No está requerido en la consigna de la Entrega 5 de persistencia; se evaluará cuando se disponga de la consigna oficial de la Entrega 6.

2. **Reemplazo de Mock Data por API REST en N8N Ranking:**
   * **Estado:** El workflow en N8N publica con éxito al Webhook real de Discord usando datos de prueba predeterminados.
   * **Motivo:** Preservado por indicación del usuario para no complejizar los jobs programados en esta etapa.

---

## 📌 5. Lo Faltante / Acciones de Cierre para la Entrega 5

Todos los requerimientos de modelado, diseño y persistencia de la Entrega 5 han sido completados:

```
[x] 1. Diagramas actualizados en Lucidchart (Documento: "DonaTrack Actualizado")
       → "Donaciones Entrega 5": Pestaña creada con el nuevo diagrama de clases JPA, VOs aplanados y arquitectura hexagonal.
       → "Componentes y Despliegue": Paleta armonizada (subgrafos neutrales/blancos con bordes púrpuras #9370db, eliminando el fondo amarillo/mostaza para igualar el resto de pestañas).
       → "Logística": Verificado DER-Logistica documental NoSQL (MongoDB) con colecciones e índices en el tablero.
       → URL: https://lucid.app/lucidchart/9c714d54-d357-4b30-bec0-bf22dbca2003/edit

[x] 2. Actualización de justificaciones de arquitectura
       → "_notas_y_borradores/arquitectura.md": Actualizado con la sección completa de persistencia políglota, ODM vs ORM, desnormalización con snapshot histórico O(1), índices multikey, cota BSON 16 MB y referencias técnicas validadas.

[ ] 3. Commit y Push final en el repositorio Git
       → Rama activa: feature/entrega-5-persistencia-poliglota
       → Archivos a comitear:
           * diagramas/donaciones/DC-Donaciones-Entrega5.puml
           * n8n/workflows/workflows.json
           * _notas_y_borradores/arquitectura.md
           * _notas_y_borradores/teorico/testing/testing_base_de_datos.md
           * docs/Justificación Decisiones - DonaTrack - Grupo 7.xlsx

[ ] 4. Ensayo rápido de la defensa con Bruno
       → Verificar que la colección de Bruno corra en el entorno "dev" o "prod" según lo requiera el docente.
```

---

## 📝 6. Próximos Pendientes Prioritarios de Refinamiento y Puesta a Punto

A continuación se detalla la lista de tareas operativas y de refinamiento técnico acordadas para abordar en las próximas sesiones de trabajo:

1. **Agrandar la base de datos documental precargada (Local y Nube):**
   * **Objetivo:** Enriquecer el dataset inicial de MongoDB tanto en el script de Docker ([`database/mongo-init.js`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/database/mongo-init.js)) como en el inicializador automático para MongoDB Atlas en la nube ([`MongoDataInitializer.java`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/infrastructure/config/MongoDataInitializer.java)).
   * **Alcance:** Generar un lote más amplio de rutas de reparto con combinaciones reales de paradas, múltiples entregas con comprobantes fotográficos, choferes, camiones de variada capacidad y donaciones pendientes de asignación geográfica para una demo más robusta.

2. **Revisión de Arquitectura y Justificaciones (Tono Humano) y Maquetación para PDF:**
   * **Objetivo:** Revisar exhaustivamente la redacción del documento de arquitectura ([`_notas_y_borradores/arquitectura.md`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/_notas_y_borradores/arquitectura.md)) y de la matriz de justificaciones en Excel ([`Justificación Decisiones - DonaTrack - Grupo 7.xlsx`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/Justificaci%C3%B3n%20Decisiones%20-%20DonaTrack%20-%20Grupo%207.xlsx)) para que su lectura sea más natural, fluida y convincente (tono humano de equipo de ingeniería, evitando frases excesivamente automáticas).
   * **Maquetación PDF:** Preparar el documento de arquitectura para que pueda ser exportado y presentado formalmente a la cátedra en formato PDF con portada, secciones ejecutivas y diagramas embebidos.

3. **Revisión Integral de Consistencia en Lucidchart:**
   * **Objetivo:** Auditar una por una las 10 pestañas del documento oficial en [Lucidchart](https://lucid.app/lucidchart/9c714d54-d357-4b30-bec0-bf22dbca2003/edit) para certificar que todo el ecosistema gráfico esté perfectamente alineado con el código:
     * Correspondencia exacta de clases, métodos y atributos entre dominio y persistencia.
     * Cardinalidades de relaciones, asociaciones y herencias (`JOINED` / `SINGLE_TABLE`).
     * Tipos de datos, contratos de eventos AMQP y endpoints REST en secuencias y componentes.

4. **Auditoría y Validación Exhaustiva de Requests en Bruno:**
   * **Objetivo:** Ejecutar y verificar de punta a punta cada una de las requests de la colección oficial de Bruno ([`docs/donatrack-api/Donatrack - API/`](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/donatrack-api/Donatrack%20-%20API/)).
   * **Alcance:** Asegurar que todas las peticiones (Donaciones, Logística, Incentivos, Notificaciones, Callbacks de Planificación y Webhooks n8n) respondan exitosamente en ambos entornos (`dev` y `prod`), verificando variables de entorno, headers y aserciones para la defensa.

5. **Corrección del Workflow de Notificación Real en n8n:**
   * **Objetivo:** Reparar el flujo de notificación real en vivo (`Notificaciones Reales` / `donatrack-notificaciones-reales`).
   * **SMS y WhatsApp:** La cuenta trial de Twilio expiró; investigar e integrar un canal alternativo gratuito o funcional para el despacho de alertas móviles (ej. Meta Cloud API directa, Evolution API, webhook alternativo).
   * **Diagnóstico de Gmail:** Identificar la causa por la cual los correos electrónicos no están saliendo o llegando a la bandeja de entrada (verificar credenciales OAuth2 / contraseña de aplicación en el nodo Gmail, permisos de la cuenta y payload del trigger).

