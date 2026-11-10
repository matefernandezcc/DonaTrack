# 📚 Índice de Documentación Técnica — Prototipo Diátaxis

Este directorio contiene la base de conocimiento técnico y notas de diseño de **DonaTrack**, organizado según el marco de documentación **Diátaxis** ([diataxis.fr](https://diataxis.fr/)) en cuatro cuadrantes de navegación:

---

## 🧭 Cuadrantes Diátaxis

```
┌───────────────────────────────────────┬───────────────────────────────────────┐
│              TUTORIALS                │             HOW-TO GUIDES             │
│        (Orientado al aprendizaje)     │       (Orientado a resolver tareas)   │
│  - Onboarding de desarrollo local     │  - Conectar DBeaver                   │
│                                       │  - Cambiar perfiles de Spring         │
│                                       │  - Guías de pruebas y testing         │
│                                       │  - Persistencia JPA y claves          │
├───────────────────────────────────────┼───────────────────────────────────────┤
│              EXPLANATION              │               REFERENCE               │
│        (Orientado a la comprensión)   │        (Orientado a la información)   │
│  - Arquitectura Hexagonal y Use Cases │  - Catálogo de servicios y puertos    │
│  - API Gateway y decisiones           │  - Referencia RabbitMQ y PostgreSQL   │
│  - ADRs y evolución SSR / Seguridad   │  - JUnit y especificaciones Mappers   │
└───────────────────────────────────────┴───────────────────────────────────────┘
```

---

## 1. 🎓 Tutorials (Aprendizaje Guiado)
Documentos orientados a acompañar al desarrollador paso a paso en su primera interacción:
- [`tutorials/onboarding-desarrollo-local.md`](tutorials/onboarding-desarrollo-local.md): Guía completa para clonar, configurar variables de entorno, compilar y ejecutar todo el stack local.

---

## 2. 🛠️ How-To Guides (Guías Prácticas Orientadas a Tareas)
Recetas directas para resolver problemas específicos o interactuar con herramientas:
- [`how-to/dbeaver.md`](how-to/dbeaver.md): Conexión de DBeaver a PostgreSQL local y Supabase Cloud.
- [`how-to/manual-cambiar-perfil.md`](how-to/manual-cambiar-perfil.md): Cómo alternar entre perfiles `local` y `prod`.
- [`how-to/guia-capa-persistencia.md`](how-to/guia-capa-persistencia.md): Guía de desarrollo y mantenimiento de repositorios JPA.
- [`how-to/guia-pruebas-flujo-logistica.md`](how-to/guia-pruebas-flujo-logistica.md): Procedimiento de pruebas para rutas, asignación y despacho logístico.
- [`how-to/testing-base-de-datos.md`](how-to/testing-base-de-datos.md): Estrategia de testing sobre esquemas de base de datos.
- [`how-to/seguridad-clave-discord.md`](how-to/seguridad-clave-discord.md): Manejo seguro de webhooks y tokens de notificación.

---

## 3. 📖 Reference (Información Técnica y Diccionarios)
Tablas y especificaciones técnicas para consulta rápida:
- [`reference/servicios-y-puertos.md`](reference/servicios-y-puertos.md): Matriz completa de microservicios, puertos, URLs de Swagger, OpenAPI JSON y comandos Make.
- [`reference/rabbitmq.md`](reference/rabbitmq.md) y [`reference/rabbitmq-academico.md`](reference/rabbitmq-academico.md): Configuración de exchanges, colas y tópicos de mensajería asíncrona.
- [`reference/postgresql.md`](reference/postgresql.md) y [`reference/orm.md`](reference/orm.md): Schemas relacionales, DDL y mapeo objeto-relacional.
- [`reference/junit.md`](reference/junit.md): Cheat-sheet de aserciones, anotaciones y mocks en JUnit 5.
- [`reference/mappers.md`](reference/mappers.md): Convenciones y contratos para DTOs y Mappers de capa intermedia.

---

## 4. 🏛️ Architecture & Explanation (Conceptos y Decisiones)
Documentos que explican el *por qué* de la arquitectura y el registro histórico de diseño:
- [`architecture/hexagonal.md`](architecture/hexagonal.md): Puertos y adaptadores, aislamiento del dominio de negocio.
- [`architecture/ejemplo-use-case.md`](architecture/ejemplo-use-case.md): Ejemplo práctico de implementación de un caso de uso puro.
- [`architecture/api-gateway.md`](architecture/api-gateway.md): Análisis arquitectónico del rol de Gateway y BFF.
- [`architecture/decisiones-arquitectura-incentivos.md`](architecture/decisiones-arquitectura-incentivos.md): Justificación técnica del motor de gamificación y misiones.
- [`architecture/decisiones-arquitectura.md`](architecture/decisiones-arquitectura.md): Registro de Decisiones de Arquitectura (ADRs).
- [`architecture/entrega-6-ssr-seguridad.md`](architecture/entrega-6-ssr-seguridad.md): Documento técnico de Entrega 6 (cliente SSR Thymeleaf, Spring Security y SSO).

---

## 📌 Documentos Raíz Intactos
Los siguientes entregables formales se preservan en la raíz de `notas_y_borradores/`:
- [`arquitectura.md`](arquitectura.md): Memoria técnica formal de entrega.
- [`Justificación Decisiones - DonaTrack - Grupo 7.xlsx`](Justificación%20Decisiones%20-%20DonaTrack%20-%20Grupo%207.xlsx): Matriz de justificación de decisiones.
- [`arquitectura.pdf`](arquitectura.pdf) y [`arquitectura.docx`](arquitectura.docx): Versiones exportadas para entrega académica.

---

## 🧭 Diagramas de Arquitectura y Clases (UML)
Para consultar los modelos visuales formales en formato PlantUML (`.puml`), dirigirse al directorio de diagramas:
- [`../diagramas/`](../diagramas/)
