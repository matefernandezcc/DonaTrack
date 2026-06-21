<div align="center">

# 📦 DonaTrack

**Sistema de Gestión y Trazabilidad de Donaciones**

*Trabajo Práctico Anual Integrador — Diseño de Sistemas (K3002) — 2026*

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Multi--Module-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)](https://swagger.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

</div>

---

## 📖 Contexto

**DonaTrack** es una solución digital desarrollada para [**UTN Solidaria**](https://www.frba.utn.edu.ar/), orientada a organizar, registrar y monitorear donaciones de bienes materiales desde su recepción en el depósito hasta su entrega a entidades beneficiarias.

La plataforma resuelve los desafíos de gestión y trazabilidad que enfrenta la organización, mejorando la distribución de recursos y fortaleciendo la transparencia frente a donantes y beneficiarios.

---

## 🏗️ Arquitectura

El sistema está diseñado como una **arquitectura distribuida de microservicios**:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DonaTrack Platform                          │
├──────────────┬──────────────┬──────────────┬───────────────────────┤
│  Donaciones  │  Incentivos  │  Logística   │   Notificaciones      │
│   :8000      │   :8001      │   :8002      │      :8003            │
├──────────────┴──────────────┴──────────────┴───────────────────────┤
│                     Server / Gateway :8080                          │
├────────────────────────────────┬────────────────────────────────────┤
│     PostgreSQL :5432           │         n8n :5678                  │
│     (schemas por servicio)     │     (automatización de flujos)     │
└────────────────────────────────┴────────────────────────────────────┘
```

> Para más detalles sobre puertos, URLs de Swagger y comandos de ejecución, consultá [`SERVICIOS.md`](SERVICIOS.md).

---

## 🛠️ Tech Stack

| Capa | Tecnología |
|---|---|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 3.5 |
| **Build** | Maven (multi-módulo) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Base de Datos** | PostgreSQL 15 / H2 (desarrollo) |
| **Contenedores** | Docker + Docker Compose |
| **Automatización** | n8n (flujos low-code) |
| **Testing** | JUnit 5 |

---

## 👥 Integrantes del Equipo

| Integrante | Legajo |
|---|---|
| Fernandez Cruz, Mateo | 209.986-0 |
| Barco Palacios, Ezequiel | 208.891-5 |
| Calabro, Lucio | 222.171-8 |
| Di Giuseppe, Tomas | 176.745-8 |
| Coaquira, Hugo | 222.228-0 |
| Ferrufino, Alan | 222.304-1 |
| Hidalgo, Máximo Juan Manuel | 176.585-1 |
| Vargas, Tomás Ezequiel | 209.874-0 |

---

## 🚀 Entregas del Proyecto

El trabajo práctico se construye de manera evolutiva e incremental a lo largo de 7 entregas. Cada entrega amplía y refina decisiones de diseño previas.

---

### ✅ Entrega 1 — Arquitectura y Modelado en Objetos (Parte I)
> 📅 22 de Mayo

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Servicio de Donaciones** — Gestión de Donantes y Donaciones.
- **Servicio de Donaciones** — Importación masiva de donantes mediante archivo CSV.
- **Servicio de Donaciones** — Gestión de Entidades Beneficiarias y Necesidades.
- **Bocetos de interfaz de usuario.**

#### Dominio abordado
- Modelado de personas donantes (humanas y jurídicas) con medios de contacto.
- Registro de donaciones con segmentación automática por subcategoría.
- Entidades beneficiarias con necesidades recurrentes y extraordinarias.
- Importación masiva de donantes desde CSV (+10.000 filas).
- Bocetos de las principales interfaces de usuario.

#### Entregables
1. Modelo del Dominio (diagrama de clases por servicio).
2. Diagramas de Arquitectura (despliegue y componentes).
3. Justificaciones de Diseño Iniciales.
4. Diagrama General de Casos de Uso.
5. Bocetos de interfaz de usuario.
6. Implementación de los requerimientos.

</details>

---

### ✅ Entrega 2 — Arquitectura y Modelado en Objetos (Parte II)
> 📅 19 de Junio

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Servicio de Donaciones** — Trazabilidad de estados de las donaciones.
- **Servicio de Donaciones** — Asignación de donaciones a entidades beneficiarias.
- **Servicio de Incentivos** — Analítica de donantes, misiones e insignias.
- **Servicio de Incentivos** — Difusión de insignias y ranking mensual.
- **Servicio de Notificaciones** — Integración con medios de notificación (email, SMS, WhatsApp).
- **Exposición REST** de los servicios (Parte I).

#### Dominio abordado
- Máquina de estados de donaciones (En Depósito → Asignada → Lista para entregar → En traslado → Entregada / Vencida / Entrega fallida).
- Algoritmos de asignación: Compatibilidad Semántica y Prioridad a Sub-atendidos (ejecución asincrónica en horarios de baja carga).
- Sistema de incentivos con categorías (Colaborador, Sostenedor, Transformador), misiones secuenciales e insignias.
- Integración con n8n para publicación automática en redes sociales al obtener insignias.
- Ranking mensual de donantes más activos.
- Servicio de Notificaciones con envío simulado por email, SMS y WhatsApp.
- Eventos de notificación: inactividad de donante (+20 días), asignación de donación, cumplimiento de misión, cambio de categoría.

#### Entregables
1. Modelo del Dominio (diagrama de clases).
2. Diagrama de despliegue y componentes.
3. Justificaciones de Diseño y Diagramas Complementarios.
4. Implementación de requerimientos.
5. Implementación del flujo automatizado de publicación y difusión de insignias (n8n).

</details>

---

### 🔄 Entrega 3 — Arquitectura y Modelado en Objetos (Parte III)
> 📅 3 de Julio

<details>
<summary><b>Ver alcance completo</b></summary>

#### Alcance
- **Revisión general** de aspectos destacados de Entrega 1 y 2.
- **Servicio de Logística** — Planificación de rutas y trazabilidad de entregas.
- **Servicio de Donaciones** — Notificaciones de Eventos por Logística.

#### Dominio abordado
- Gestión de flota de camiones (patente, capacidad de volumen, altura, capacidad de carga) y choferes.
- Generación de rutas de reparto integrándose con un componente externo de planificación vía callback URL (máx. 100 donaciones por lote).
- Trazabilidad de entregas: Pendiente → En traslado → Entregada / No recibida.
- Comprobante de recepción con fotos, fecha/hora y camión responsable.
- Notificaciones por inicio de ruta, entrega exitosa y entrega no satisfactoria.
- Documentación Swagger de todos los endpoints por servicio.
- Contenerización con Docker de cada servicio.

#### Revisión E1 y E2
1. Segmentación de donaciones correctamente separada.
2. Necesidades recurrentes bien definidas.
3. Importación masiva de CSV funcional.
4. Proceso de asignación con pattern Strategy.
5. Exposición correcta de endpoints REST (Donaciones, Incentivos, Notificaciones).
6. Manejo de pérdida de progreso en misiones (ej. "Racha").
7. Integración con redes sociales vía n8n.
8. Impacto de donaciones en el cálculo de progreso de misiones.
9. Notificaciones por diversos medios con Strategy.

#### Entregables
1. Modelo del Dominio (diagrama de clases por servicio).
2. Justificaciones de Diseño y Diagramas Complementarios.
3. Endpoints presentados en Postman y documentados en Swagger.
4. Contenedores Docker para cada servicio.
5. Diagrama de componentes y despliegue de la solución completa.

</details>

---

### ⏳ Entrega 4 — Arquitectura e Integración
> 📅 Agosto

*Los detalles de esta etapa se actualizarán próximamente.*

---

### ⏳ Entrega 5 — Persistencia y Maquetado de Interfaz de Usuario
> 📅 Semana del 14 de Septiembre

*Los detalles de esta etapa se actualizarán próximamente.*

---

### ⏳ Entrega 6 — Arquitectura Web MVC
> 📅 Semana del 19 de Octubre

*Los detalles de esta etapa se actualizarán próximamente.*

---

### ⏳ Entrega 7 — Despliegue, Observabilidad y Seguridad
> 📅 Semana del 23 de Noviembre

*Los detalles de esta etapa se actualizarán próximamente.*

---

## ⚡ Quick Start

```bash
# Clonar el repositorio
git clone https://github.com/matefernandezcc/DonaTrack.git
cd DonaTrack

# Compilar todo el proyecto
make build

# Levantar un servicio individual
make donaciones      # Puerto 8000
make incentivos      # Puerto 8001
make logistica       # Puerto 8002
make notificaciones  # Puerto 8003
make server          # Puerto 8080

# Levantar todo con Docker
docker compose up --build -d
```

> 📄 Ver [`SERVICIOS.md`](SERVICIOS.md) para la guía completa de puertos, Swagger y comandos.
