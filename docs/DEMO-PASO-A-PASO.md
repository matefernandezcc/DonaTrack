# 🚀 DonaTrack — Guía de Demo Paso a Paso (End-to-End & Persistencia Políglota)

Esta guía interactiva está diseñada para ejecutar una **demostración en vivo de punta a punta** de **DonaTrack (Entrega 6)**, validando tanto los endpoints de la colección de Bruno como la **arquitectura distribuida multi-cuenta en Render**, el **cliente liviano SSR con Thymeleaf (`donatrack-web`)**, el **microservicio de autenticación centralizada (`donatrack-auth`) con JWT/SSO**, la separación de layouts (Público moderno vs. Administrativo con blueprints oficiales de Salesforce Lightning Design System - SLDS 2.30.3), y la **persistencia políglota real** en **PostgreSQL** (`donaciones`, `incentivos`, `notificaciones`, `auth`) y **MongoDB** (`donatrack_logistica`), junto con la mensajería asincrónica en **RabbitMQ / CloudAMQP** y las automatizaciones en **n8n / Discord**.

---

## 🗺️ Arquitectura Distribuida (Multi-Cuenta Render)

```mermaid
flowchart LR
    subgraph Browser["Navegador / Cliente Demo"]
        U["Usuario Web (SSR)"]
        BRU["Colección Bruno CLI"]
    end

    subgraph R2["Render - Cuenta 2 (Frontend & Auth & Logística)"]
        WEB["donatrack-web :8080<br/>(Thymeleaf SSR + Dual Layout)"]
        AUTH["donatrack-auth :8004<br/>(JWT HS256 + SSO OAuth2)"]
        LOG["donatrack-logistica :8002<br/>(Rutas, Camiones, Choferes)"]
    end

    subgraph R1["Render - Cuenta 1 (Core de Datos)"]
        DON["donatrack-donaciones :8000<br/>(Recepción, Matchmaking, CSV)"]
        INC["donatrack-incentivos :8001<br/>(Ranking, Métricas, Insignias)"]
        NOT["donatrack-notificaciones :8003<br/>(Multicanal & Auditoría)"]
    end

    subgraph SaaS["Servicios Gestionados Nube"]
        PG[("PostgreSQL / Supabase<br/>Schemas: donaciones, incentivos, notificaciones, auth")]
        MONGO[("MongoDB Atlas<br/>DB: donatrack_logistica")]
        RMQ(("CloudAMQP / RabbitMQ"))
        N8N["n8n :5678<br/>(Discord, WhatsApp, Email)"]
    end

    U -->|HTML público / SLDS backoffice| WEB
    BRU -->|REST APIs| DON & INC & LOG & NOT & AUTH
    WEB -->|"POST /auth/login / exchange"| AUTH
    WEB -->|"Bearer JWT (Server-side)"| DON & INC & LOG
    DON --> PG
    INC --> PG
    NOT --> PG
    AUTH --> PG
    LOG --> MONGO
    LOG <-- "Eventos AMQP" --> RMQ
    RMQ --> DON & INC & NOT
    NOT & INC -- "Webhooks HTTP" --> N8N
```

---

## ⚡ 0. Puesta en Marcha y Validación Automática (Bruno CLI)

### Opción A — Entorno Local (`dev` con Docker Compose)

```bash
# 1. Levantar toda la arquitectura distribuida local (8 contenedores)
make setup
# o directamente: docker compose up -d

# 2. Verificar que los servicios estén saludables
docker compose ps

# 3. Ejecutar los endpoints de la colección Bruno contra el entorno local (dev)
cd docs/donatrack-api/bruno-collection
npx @usebruno/cli run --env dev
cd ../../..
```

### Opción B — Entorno Cloud Híbrido (`prod` multi-cuenta Render + SaaS)

> [!TIP]
> En la arquitectura distribuida de producción:
> - **Cuenta 1 (Compañero):** Core de Datos (`donaciones`, `incentivos`, `notificaciones`).
> - **Cuenta 2 (Mía):** Capa de Interacción y Logística (`donatrack-web`, `donatrack-auth`, `donatrack-logistica`).
> - **Bases de Datos SaaS:** Supabase (PostgreSQL), MongoDB Atlas y CloudAMQP (RabbitMQ).

```bash
cd docs/donatrack-api/bruno-collection
npx @usebruno/cli run --env prod
cd ../../..
```

---

## 🧑‍🤝‍🧑 Flujo 1: Gestión de Personas, Importación CSV y Necesidades de Beneficiarios
**Módulo:** `donatrack-donaciones` (`:8000`) | **Base de Datos:** PostgreSQL (`schema: donaciones`)  
**Requerimiento del Enunciado:** Alta de donantes (personas humanas y jurídicas), carga masiva por CSV, registro de organizaciones beneficiarias y declaración de necesidades (recurrentes y extraordinarias).

### Paso 1.1 — Crear un nuevo Donante (`POST /api/personas`)
*(Equivalente en Bruno: `Donaciones/Personas/Crear Persona`)*

```bash
curl -s -X POST http://localhost:8000/api/personas \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "HUMANA",
    "email": "mateo.demo@donatrack.com",
    "nombre": "Mateo",
    "apellido": "Fernandez",
    "edad": 23,
    "contacto": {
      "correoElectronico": "mateo.demo@donatrack.com",
      "telefono": "+54 11 1234-5678",
      "whatsapp": "+54 11 1234-5678",
      "medioPredeterminado": "CORREO"
    },
    "documento": {
      "tipo": "DNI",
      "numero": "99887766"
    },
    "direccion": {
      "calle": "Av. Corrientes",
      "altura": 4303,
      "localidad": "CABA",
      "provincia": {
        "nombreProvincia": "Buenos Aires",
        "pais": { "nombre": "Argentina" }
      },
      "codigoPostal": "C1181",
      "coordenadas": null
    }
  }' | jq .
```

### Paso 1.2 — Importar Donantes Masivamente desde CSV (`POST /api/personas/importar-csv`)
*(Equivalente en Bruno: `Donaciones/Personas/Importar CSV`)*

```bash
curl -s -X POST http://localhost:8000/api/personas/importar-csv \
  -F "file=@docs/donatrack-api/bruno-collection/Donaciones/Personas/donantes_sample.csv"
echo ""
```

### Paso 1.3 — Declarar una Necesidad Extraordinaria en una Entidad Beneficiaria (`POST /api/beneficiarios/{id}/necesidades`)
*(Equivalente en Bruno: `Donaciones/Beneficiarios/Agregar Necesidad`)*

```bash
curl -i -s -X POST http://localhost:8000/api/beneficiarios/b2222222-2222-4222-8222-222222222222/necesidades \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "extraordinaria",
    "descripcion": "Leche larga vida para merendero barrial",
    "cantidadRequerida": 120.0,
    "subcategoriaRequerida": {
      "nombre": "No Perecederos",
      "descripcion": "Alimentos no perecederos"
    }
  }' | head -n 5
```

### 🔍 Verificación de Persistencia en PostgreSQL (`schema: donaciones`)

```bash
docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT p.persona_id, p.email, ph.nombre, ph.apellido, pj.razon_social, r.dtype AS rol
FROM donaciones.personas p
LEFT JOIN donaciones.personas_humanas ph ON ph.persona_humana_id = p.persona_id
LEFT JOIN donaciones.personas_juridicas pj ON pj.persona_juridica_id = p.persona_id
LEFT JOIN donaciones.roles r ON r.persona_id = p.persona_id
ORDER BY p.email LIMIT 10;
"

docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT n.necesidad_id, n.dtype, s.nombre AS subcategoria, n.descripcion, n.estado, n.cantidad_requerida, n.cantidad_objetivo
FROM donaciones.necesidades n
LEFT JOIN donaciones.subcategorias s ON s.subcategoria_id = n.subcategoria_id;
"
```

---

## 📦 Flujo 2: Recepción de Bienes Brutos, Segmentación Automática, Matchmaking y Asignación
**Módulos:** `donatrack-donaciones` (`:8000`) ➔ `donatrack-incentivos` (`:8001`) + `donatrack-logistica` (`:8002`) + `donatrack-notificaciones` (`:8003`)  
**Requerimiento del Enunciado:** Al recibir bienes heterogéneos en depósito, el sistema genera un `DonacionOriginal` y lo **segmenta automáticamente** en objetos `Donacion` por subcategoría, registrando su `HistorialEstado`, sugiriendo beneficiarios mediante `MatchmakerService` y enviando el pedido de retiro a Logística al asignarse.

### Paso 2.1 — Recibir Bienes Brutos y Segmentar (`POST /api/recepciones`)
*(Equivalente en Bruno: `Donaciones/Crear Donacion`)*

```bash
curl -s -X POST http://localhost:8000/api/recepciones \
  -H "Content-Type: application/json" \
  -d '{
    "idDonante": "a1111111-1111-4111-8111-111111111111",
    "idAdministrador": "a2222222-2222-4222-8222-222222222222",
    "bienesBrutos": [
      {
        "descripcion": "Paquete de arroz 1kg",
        "cantidad": 10.0,
        "unidadMedicion": "KILOS",
        "esUsado": false,
        "fechaVencimiento": "2027-06-01",
        "nombreSubcategoria": "No Perecederos"
      },
      {
        "descripcion": "Campera de abrigo térmica",
        "cantidad": 4.0,
        "unidadMedicion": "UNIDADES",
        "esUsado": true,
        "fechaVencimiento": null,
        "nombreSubcategoria": "Abrigo"
      }
    ]
  }' | jq .
```

### Paso 2.2 — Sugerir Beneficiarios con Matchmaking (`GET /api/donaciones/{id}/matchmaking`)
*(Equivalente en Bruno: `Donaciones/Sugerir Beneficiarios`)*

```bash
curl -s http://localhost:8000/api/donaciones/5cd80011-2a54-4ca7-a39c-28b2b53d8c55/matchmaking | jq .
```

### Paso 2.3 — Asignar Donación a Beneficiario (`PUT /api/donaciones/{id}/estado/asignar`)
*(Equivalente en Bruno: `Donaciones/Asignar Donacion`)*  
Esto cambia la donación a `ASIGNADA`, persiste la transición en `donaciones.historial_estado`, envía el ítem a `donatrack-logistica` (`items_planificacion` en MongoDB) y dispara las notificaciones por RabbitMQ/n8n.

```bash
curl -i -s -X PUT http://localhost:8000/api/donaciones/5cd80011-2a54-4ca7-a39c-28b2b53d8c55/estado/asignar \
  -H "Content-Type: application/json" \
  -d '{
    "id": "e4444444-4444-4444-8444-444444444444"
  }' | head -n 5

# Verificar el nuevo estado y beneficiario asignado por API
curl -s http://localhost:8000/api/donaciones/5cd80011-2a54-4ca7-a39c-28b2b53d8c55 | jq .
```

### 🔍 Verificación de Trazabilidad e Historial en PostgreSQL (`donaciones.donaciones` & `historial_estado`)

```bash
docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT d.donacion_id, s.nombre AS subcategoria, d.estado, d.necesidad_id, d.fecha_creacion
FROM donaciones.donaciones d
LEFT JOIN donaciones.subcategorias s ON s.subcategoria_id = d.subcategoria_id
ORDER BY d.fecha_creacion DESC LIMIT 6;
"

docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT donacion_id, estado, fecha, observacion, usuario_id
FROM donaciones.historial_estado
ORDER BY fecha DESC LIMIT 8;
"
```

---

## 🚚 Flujo 3: Logística NoSQL (MongoDB) y Saga Completa por RabbitMQ
**Módulo:** `donatrack-logistica` (`:8002` en Local / `https://dona-logistica.onrender.com` en Cloud) | **Base de Datos:** MongoDB (`donatrack_logistica`)  
**Requerimiento del Enunciado:** Persistencia NoSQL orientada a documentos de las **6 colecciones** (`camiones`, `choferes`, `items_planificacion`, `solicitudes_planificacion`, `rutas_reparto`, `entregas`), inicio de hoja de ruta, confirmación de entrega con remito fotográfico y reporte de entrega fallida con replanificación.

```mermaid
sequenceDiagram
    actor Chofer
    participant LOG as donatrack-logistica (MongoDB)
    participant RMQ as RabbitMQ / CloudAMQP
    participant DON as donatrack-donaciones (PostgreSQL)
    participant INC as donatrack-incentivos (PostgreSQL)
    participant NOT as donatrack-notificaciones (PostgreSQL + n8n)

    Chofer->>LOG: POST /api/rutas/{id}/iniciar
    LOG->>LOG: Ruta.iniciada=true + Entregas -> EN_TRASLADO
    LOG->>RMQ: Publica RutaIniciadaEvent
    RMQ->>DON: Actualiza Donacion -> EN_TRASLADO + Historial
    DON->>RMQ: Publica NotificacionInicioRutaEvent (con contactos)
    RMQ->>NOT: Envía alerta vía n8n + Persiste en notificaciones

    Chofer->>LOG: POST /api/entregas/{idDonacion}/confirmar (Fotos + Patente)
    LOG->>LOG: Guarda ComprobanteRecepcion en rutas_reparto y entregas (ENTREGADA)
    LOG->>RMQ: Publica EntregaRealizadaEvent
    RMQ->>DON: Actualiza Donacion -> ENTREGADA + Historial
    DON->>INC: Feign POST /api/donantes/{id}/actividad (Suma métricas/misiones/insignias)
    DON->>RMQ: Publica NotificacionEntregaExitosaEvent
    RMQ->>NOT: Envía comprobante vía n8n + Persiste en notificaciones
```

### Paso 3.1 — Ejecutar Planificación y Recibir Callback del Optimizador de Rutas
*(Equivalentes en Bruno: `Logistica/Recepcionar Donacion`, `Logistica/Ejecutar Planificacion`, `Logistica/Callback Planificacion`)*

```bash
# 1. Ingresar un ítem de planificación en la bandeja de salida de Logística
curl -i -s -X POST http://localhost:8002/api/planificacion/items \
  -H "Content-Type: application/json" \
  -d '{
    "idDonacion": "139e0f9f-9431-4302-9f06-cad97978f7be",
    "peso": 25.5,
    "volumen": 0.5,
    "calleDestino": "Av. Santa Fe",
    "alturaDestino": "1234",
    "localidadDestino": "CABA"
  }' | head -n 5

# 2. Recibir el callback con la hoja de ruta optimizada (Camión AE304KL, Chofer CH-105)
curl -i -s -X POST http://localhost:8002/api/planificacion/callback \
  -H "Content-Type: application/json" \
  -d '{
    "idSolicitud": "b4444444-4444-4444-8444-444444444444",
    "rutas": [
      {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "fechaOperativa": "2026-10-29",
        "camion": { "patente": "AE304KL" },
        "chofer": { "legajo": "CH-105" },
        "paradas": [
          {
            "orden": 1,
            "direccion": { "calle": "Av. Corrientes", "altura": "1234", "localidad": "CABA" },
            "coordenada": { "latitud": -34.6037, "longitud": -58.3816 },
            "entregas": [
              {
                "idEntrega": "139e0f9f-9431-4302-9f06-cad97978f7be",
                "pesoEstimado": 25.5,
                "volumenEstimado": 0.5
              }
            ]
          }
        ]
      }
    ]
  }' | head -n 5
```

### Paso 3.2 — Iniciar Hoja de Ruta (`POST /api/rutas/{id}/iniciar`)
*(Equivalente en Bruno: `Logistica/Iniciar Ruta`)*

```bash
curl -s -X POST http://localhost:8002/api/rutas/a1b2c3d4-e5f6-7890-abcd-ef1234567890/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "legajoChofer": "CH-105"
  }'
echo ""
```

### Paso 3.3 — Confirmar Entrega con Comprobante Fotográfico (`POST /api/entregas/{idDonacion}/confirmar`)
*(Equivalente en Bruno: `Logistica/Confirmar Entrega`)*

```bash
curl -s -X POST http://localhost:8002/api/entregas/139e0f9f-9431-4302-9f06-cad97978f7be/confirmar \
  -H "Content-Type: application/json" \
  -d '{
    "fotos": [
      "https://fotos.donatrack.org/remito-1.jpg"
    ],
    "patenteCamion": "AE304KL"
  }'
echo ""
```

### Paso 3.4 — Reportar Falla de Entrega y Disparar Replanificación (`POST /api/entregas/{idDonacion}/falla`)
*(Equivalente en Bruno: `Logistica/Reportar Falla`)*

```bash
curl -s -X POST http://localhost:8002/api/entregas/35008064-071a-4d7a-ac37-33318f7d9845/falla \
  -H "Content-Type: application/json" \
  -d '{
    "motivo": "El beneficiario no se encontraba en el domicilio",
    "puedeReplanificarse": true
  }'
echo ""
```

### 🔍 Verificación de Persistencia Políglota (MongoDB + Efecto Dominó en PostgreSQL)

```bash
# 1. Verificar en MongoDB las 6 colecciones y el comprobante de recepción persistido
docker exec mongo_donatrack mongosh -u donatrack -p donatrack --authenticationDatabase admin donatrack_logistica --quiet --eval '
  print("=== Conteo por Colección en donatrack_logistica ===");
  ["camiones", "choferes", "items_planificacion", "entregas", "solicitudes_planificacion", "rutas_reparto"].forEach(c => {
    print(c + ": " + db.getCollection(c).countDocuments());
  });
  print("\n=== Entrega Confirmada (139e0f9f-...) con Remito ===");
  printjson(db.entregas.findOne({ _id: UUID("139e0f9f-9431-4302-9f06-cad97978f7be") }));
  print("\n=== Entrega Fallida (35008064-...) Marcada NO_RECIBIDA ===");
  printjson(db.entregas.findOne({ _id: UUID("35008064-071a-4d7a-ac37-33318f7d9845") }));
'

# 2. Verificar en PostgreSQL cómo RabbitMQ actualizó automáticamente las donaciones y su historial
docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT donacion_id, estado, fecha, observacion
FROM donaciones.historial_estado
WHERE donacion_id IN ('139e0f9f-9431-4302-9f06-cad97978f7be', '35008064-071a-4d7a-ac37-33318f7d9845')
ORDER BY fecha DESC;
"
```

---

## 🏆 Flujo 4: Gamificación, Misiones, Insignias y Podio Mensual Top 3
**Módulo:** `donatrack-incentivos` (`:8001`) | **Base de Datos:** PostgreSQL (`schema: incentivos`) + **n8n Discord**  
**Requerimiento del Enunciado:** Seguimiento de métricas históricas y rachas del donante, evaluación de misiones según su categoría (`COLABORADOR`, `BRONCE`, `PLATA`, `ORO`), otorgamiento de insignias con difusión automática en redes/Discord vía n8n y cálculo del Ranking Mensual Top 3.

### Paso 4.1 — Registrar Actividad de Donación Exitosa (`POST /api/donantes/{id}/actividad`)
*(Equivalente en Bruno: `Incentivos/Registrar Actividad`)*

```bash
curl -i -s -X POST http://localhost:8001/api/donantes/a1111111-1111-4111-8111-111111111111/actividad \
  -H "Content-Type: application/json" \
  -d '{
    "idDonacion": "31008064-071a-4d7a-ac37-33318f7d9841",
    "cantidadBienes": 5,
    "categorias": ["Alimentos", "Vestimenta"],
    "idEntidadBeneficiaria": "e4b07384-d113-4ec6-a51d-402f06c1af0c",
    "fecha": "2026-10-29"
  }' | head -n 5
```

### Paso 4.2 — Consultar Métricas, Misiones Activas e Insignias del Donante
*(Equivalentes en Bruno: `Incentivos/Obtener Metricas`, `Incentivos/Obtener Misiones`, `Incentivos/Obtener Insignias`)*

```bash
# Métricas acumuladas, racha de meses y organizaciones ayudadas
curl -s http://localhost:8001/api/donantes/a1111111-1111-4111-8111-111111111111/metricas | jq .

# Misión actual en curso
curl -s http://localhost:8001/api/donantes/a1111111-1111-4111-8111-111111111111/misiones | jq .

# Insignias obtenidas (y disparo de difusión a Discord vía n8n)
curl -s http://localhost:8001/api/donantes/a1111111-1111-4111-8111-111111111111/insignias | jq .
```

### Paso 4.3 — Calcular y Persistir el Ranking Mensual Top 3 (`GET /api/ranking/top3`)
*(Equivalente en Bruno: `Incentivos/Obtener Ranking`)*

```bash
curl -s http://localhost:8001/api/ranking/top3 | jq .
```

### 🔍 Verificación de Persistencia en PostgreSQL (`schema: incentivos`)

```bash
docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT p.perfil_donante_id, p.categoria, m.nombre AS mision_actual,
       md.total_donaciones_historicas, md.organizaciones_ayudadas
FROM incentivos.perfiles_donante p
LEFT JOIN incentivos.misiones m ON m.mision_id = p.mision_actual_id
LEFT JOIN incentivos.metricas_donante md ON md.perfil_donante_id = p.perfil_donante_id;
"

docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT io.perfil_donante_id, i.nombre AS insignia, io.fecha_obtencion, io.visible_publicamente
FROM incentivos.insignias_obtenidas io
JOIN incentivos.insignias i ON i.insignia_id = io.insignia_id;
"

docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT r.mes, r.anio, pr.posicion, pr.perfil_donante_id, pr.misiones_completadas_mes
FROM incentivos.rankings_mensuales r
JOIN incentivos.posiciones_ranking pr ON pr.ranking_mensual_id = r.ranking_mensual_id
ORDER BY pr.posicion;
"
```

---

## 🔔 Flujo 5: Notificaciones Multicanal y Auditoría de Eventos (n8n + Discord)
**Módulo:** `donatrack-notificaciones` (`:8003`) + **n8n** (`:5678`) | **Base de Datos:** PostgreSQL (`schema: notificaciones`)  
**Requerimiento del Enunciado:** Despacho multicanal (`CORREO`, `DISCORD`, `WHATSAPP`, `SMS`) desacoplado mediante adaptadores hacia **n8n** y registro de auditoría en `notificaciones.eventos_notificacion` y `notificaciones.notificaciones`.

### Paso 5.1 — Enviar Notificaciones por Correo, Discord, WhatsApp y SMS (`POST /api/mensajes`)
*(Equivalentes en Bruno: `Notificaciones/01..04`)*

```bash
# 1. Correo Electrónico vía n8n
curl -i -s -X POST http://localhost:8003/api/mensajes \
  -H "Content-Type: application/json" \
  -d '{
    "destinatario": "donante@test.com",
    "mensaje": "¡Tu donación está en camino al comedor Los Bajitos!",
    "medio": "CORREO"
  }' | head -n 5

# 2. Publicación en Canal de Discord vía n8n
curl -i -s -X POST http://localhost:8003/api/mensajes \
  -H "Content-Type: application/json" \
  -d '{
    "destinatario": "Canal General Discord",
    "mensaje": "🎉 ¡Nueva donación registrada exitosamente desde la Demo de DonaTrack!",
    "medio": "DISCORD"
  }' | head -n 5
```

### 🔍 Verificación de Persistencia en PostgreSQL (`schema: notificaciones`)

```bash
docker exec postgres_donatrack psql -U postgres -d donatrack -c "
SELECT e.tipo_evento, n.destinatario, n.medio, n.completada, n.fecha_envio, LEFT(n.mensaje, 65) AS mensaje_resumen
FROM notificaciones.notificaciones n
JOIN notificaciones.eventos_notificacion e ON e.evento_notificacion_id = n.evento_notificacion_id
ORDER BY n.fecha_envio DESC LIMIT 10;
"
```



---

## 🖥️ Flujo 6: Cliente Liviano SSR, Autenticación Centralizada y Backoffice Administrativo (SLDS)
**Módulos:** `donatrack-web` (`:8080`) + `donatrack-auth` (`:8004`) | **Bases de Datos:** PostgreSQL (`schema: auth`)  
**Requerimientos de Entrega 6:**
1. **Cliente Liviano SSR con Thymeleaf:** Aplicación desacoplada sin dependencias a entidades pesadas ni JPA; orquesta llamadas REST seguras inyectando tokens Bearer.
2. **Servicio Central de Auth:** Emisión y validación de tokens JWT simétricos (HS256) y soporte de intercambio de códigos de un solo uso para SSO (Google / GitHub).
3. **Estrategia Dual de UI (Aislamiento Estricto):**
   - **Layout Público (`layout-public.html`):** Estilo moderno/e-commerce, amigable, accesible (WCAG AA). Totalmente libre de clases o resets de SLDS.
   - **Layout Administrativo (`layout-admin.html`):** Backoffice puro basado en blueprints oficiales de **Salesforce Lightning Design System (SLDS 2.30.3)** (Global Header, Vertical Navigation, Data Tables, Cards, Modals, Toasts) sin inventar CSS personalizado (`dt-*`).

### Paso 6.1 — Autenticación Centralizada y Emisión de JWT (`POST /auth/login`)
*(Equivalente en Bruno: `Auth/Login`)*

```bash
# 1. Login con credenciales sembradas (admin / admin)
curl -s -X POST http://localhost:8004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}' | jq .

# 2. Login con rol Donante (donante / donante)
curl -s -X POST http://localhost:8004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "donante", "password": "donante"}' | jq .
```

### Paso 6.2 — Acceso al Portal Público y Login Web vía SSR (`POST /login`)

```bash
# El usuario inicia sesión en el frontend liviano SSR:
# Guarda el SecurityContext y JWT en HttpSession del servidor y emite cookie HttpOnly JSESSIONID.
curl -s -i -c /tmp/demo_cookies.txt -X POST http://localhost:8080/login \
  -d "username=admin&password=admin"

# Acceso al portal público moderno (consume ranking real de incentivos)
curl -s -b /tmp/demo_cookies.txt http://localhost:8080/ | head -n 30
```

### Paso 6.3 — Gestión de Flota en Backoffice SLDS (`/app/admin/camiones`)

```bash
# 1. Ver tabla de camiones renderizada con SLDS Data Table
curl -s -b /tmp/demo_cookies.txt http://localhost:8080/app/admin/camiones | grep -A 5 "slds-table"

# 2. Registrar un camión desde el formulario modal de SLDS
curl -s -i -b /tmp/demo_cookies.txt -X POST http://localhost:8080/app/admin/camiones \
  -d "patente=ZZ999XX&capacidadVolumen=28.0&altura=3.6&capacidadCarga=6000.0"

# 3. Verificar persistencia directa en MongoDB (donatrack_logistica.camiones)
docker exec mongo_donatrack mongosh donatrack_logistica --quiet \
  --eval "db.camiones.find({patente: 'ZZ999XX'}).pretty()"
```

### Paso 6.4 — Matchmaking Inteligente de Donaciones (`/app/admin/matchmaking`)

```bash
# Ejecutar sugerencia de compatibilidad para una donación con las organizaciones registradas
curl -s -b /tmp/demo_cookies.txt "http://localhost:8080/app/admin/matchmaking?donacionId=5cd80011-2a54-4ca7-a39c-28b2b53d8c55" | grep -A 4 "Resultados de compatibilidad"
```

### Paso 6.5 — Importación Masiva de Donantes por CSV (`/app/admin/csv`)

```bash
# Carga masiva de personas y sincronización con el schema 'donaciones'
curl -s -b /tmp/demo_cookies.txt http://localhost:8080/app/admin/csv | grep -A 4 "Donantes Registrados en Base de Datos"
```

### Paso 6.6 — Portal Donante con Gamificación y Registro de Bienes (`/app/donante`)

```bash
# 1. Visualizar panel de gamificación con métricas e insignias reales
curl -s -b /tmp/demo_cookies.txt http://localhost:8080/app/donante | grep -A 3 "Insignias y Reconocimientos"

# 2. Registrar una nueva donación de bienes en depósito
curl -s -i -b /tmp/demo_cookies.txt -X POST http://localhost:8080/app/donante/donaciones \
  -d "descripcion=Leche en polvo 800g&cantidad=25&unidadMedicion=KILOS&nombreSubcategoria=No Perecederos&esUsado=false"
```

### Paso 6.7 — Portal Beneficiaria y Certificación de Entrega (`/app/beneficiaria`)

```bash
# 1. Declarar una nueva necesidad material para matchmaking
curl -s -i -b /tmp/demo_cookies.txt -X POST http://localhost:8080/app/beneficiaria/necesidades \
  -d "descripcion=Frazadas polares de una plaza&cantidadRequerida=40&tipo=extraordinaria&nombreSubcategoria=Abrigo"

# 2. Listar entregas logísticas y rutas activas
curl -s -b /tmp/demo_cookies.txt http://localhost:8080/app/beneficiaria/entregas | grep -A 4 "Rutas y Despachos en Operación"
```

---

## 🗄️ 7. Consultas de Verificación en Base de Datos (DBeaver & MongoDB)

Para corroborar la consistencia y la persistencia políglota real tras ejecutar los flujos, podés ejecutar las siguientes consultas en **DBeaver** o consola de base de datos:

### 7.1. Verificación en PostgreSQL (`localhost:5432`)

#### A. Personas, Roles y Direcciones (Schema `donaciones`)
```sql
-- Contar personas registradas
SELECT COUNT(*) AS total_personas FROM donaciones.personas;

-- Verificar persona humana creada y su herencia JOINED
SELECT p.persona_id, p.email, p.doc_tipo, p.doc_numero, ph.nombre, ph.apellido, ph.edad
FROM donaciones.personas p
JOIN donaciones.personas_humanas ph ON p.persona_id = ph.persona_humana_id
ORDER BY p.persona_id DESC LIMIT 5;

-- Verificar persona jurídica
SELECT p.persona_id, p.email, pj.razon_social, pj.tipo
FROM donaciones.personas p
JOIN donaciones.personas_juridicas pj ON p.persona_id = pj.persona_juridica_id;

-- Verificar direcciones asociadas
SELECT p.email, d.calle, d.altura, d.localidad, d.codigo_postal
FROM donaciones.personas p
JOIN donaciones.direcciones d ON p.direccion_id = d.direccion_id
LIMIT 5;

-- Identificar IDs de roles para pruebas manuales
SELECT persona_id, dtype, rol_id FROM donaciones.roles WHERE dtype IN ('Donante', 'Administrador', 'Beneficiario');
```

#### B. Donaciones Originales, Segmentación y Bienes (Schema `donaciones`)
```sql
-- Donaciones originales ingresadas
SELECT * FROM donaciones.donaciones_originales ORDER BY fecha_recepcion DESC LIMIT 5;

-- Donaciones segmentadas por categoría
SELECT d.donacion_id, d.estado, d.fecha_creacion, dorig.descripcion_general
FROM donaciones.donaciones d
JOIN donaciones.donaciones_originales dorig ON d.donacion_original_id = dorig.donacion_original_id
ORDER BY d.fecha_creacion DESC LIMIT 10;

-- Bienes individuales persistidos
SELECT b.bien_id, b.descripcion, b.cantidad, b.unidad_medicion, b.es_usado, b.donacion_id
FROM donaciones.bienes b
ORDER BY b.bien_id DESC LIMIT 10;
```

#### C. Gamificación y Puntajes (Schema `incentivos`)
```sql
-- Perfiles y métricas acumuladas de donantes
SELECT * FROM incentivos.perfiles_donante;
SELECT * FROM incentivos.metricas_donante;

-- Registro histórico de actividades por mes
SELECT * FROM incentivos.registros_donacion ORDER BY mes_donacion DESC LIMIT 10;
```

#### D. Usuarios y Credenciales (Schema `auth`)
```sql
-- Cuentas de usuario y proveedores de identidad
SELECT usuario_id, username, email, rol, persona_id, proveedor_oauth 
FROM auth.usuarios;
```

### 7.2. Verificación en MongoDB NoSQL (`localhost:27017` / Atlas)

Conectándote a la base de datos `donatrack_logistica` mediante DBeaver, MongoDB Compass o la terminal `mongosh`:

```javascript
use donatrack_logistica;

// 1. Flota de camiones
db.camiones.find().pretty();

// 2. Choferes registrados
db.choferes.find().pretty();

// 3. Items recepcionados para planificación de rutas
db.items_planificacion.find().sort({ _id: -1 }).limit(5).pretty();

// 4. Rutas de reparto generadas con sus paradas y entregas embebidas
db.rutas_reparto.find().sort({ _id: -1 }).limit(3).pretty();
```

---

## 🔄 8. Procedimiento de Reset Total y Desarrollo en Caliente

### 8.1. Reset Total ("Volver a Empezar desde Cero")
Si requerís limpiar el estado de la persistencia para repetir una prueba limpia:

```bash
# 1. Bajar todos los microservicios Java (Ctrl+C en las terminales)

# 2. Detener contenedores y eliminar volúmenes de datos
docker compose down -v

# 3. Levantar infraestructura Docker limpia (recrea schemas y ejecuta local-seed-data.sql y mongo-init.js)
docker compose up -d postgres-donatrack mongo-donatrack rabbitmq n8n

# 4. Limpiar artefactos compilados
./mvnw clean   # o: make clean

# 5. Volver a compilar y levantar
make build
make run       # o levantar individualmente con make donaciones, etc.
```

### 8.2. Recarga en Caliente de Código
Si modificás clases `.java` y observás inconsistencias de caché o `NoClassDefFoundError`:

```bash
# 1. Detener el microservicio afectado (Ctrl+C)
# 2. Recompilar limpiamente sin bajar Docker
./mvnw clean compile -pl <nombre-del-modulo> -am
# 3. Volver a ejecutar el servicio
make <nombre-del-modulo>
```

---

## 🛠️ 9. Tabla de Troubleshooting Rápido

| Síntoma | Causa Probable | Solución Inmediata |
|---|---|---|
| `Connection refused` (:8000, :8002, etc.) | Microservicio o contenedor no iniciado | Verificar puertos activos con `docker compose ps` o levantar con `make <servicio>`. |
| `500 Internal Server Error` | Fallo en mapper, entity o conexión inter-servicio | Inspeccionar el stacktrace en la consola del microservicio emisor. |
| `415 Unsupported Media Type` | Falta header HTTP en la solicitud | Agregar `-H "Content-Type: application/json"` en la llamada curl. |
| `400 Bad Request` en importación CSV | Archivo vacío o ruta errónea | Asegurar el prefijo `@` antes de la ruta: `-F "file=@ruta/al/archivo.csv"`. |
| Datos no visibles en DBeaver | Transacción no confirmada o schema incorrecto | Presionar F5 (Refresh) y verificar el schema activo (`donaciones`, `incentivos`, `notificaciones`, `auth`). |
| `could not determine recommended JdbcType` | Mapeo incompatible entre DTO y entidad | Asegurar que los UUIDs se envíen con formato RFC 4122 (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`). |
| Mensajes encolados sin consumir en RabbitMQ | Listener no suscripto o cola sin binding | Abrir consola Web [http://localhost:15672](http://localhost:15672) y verificar bindings de exchanges. |

---

## ✅ 10. Checklist Integral de Verificación E2E

```markdown
- [x] Infraestructura: Contenedores Postgres, Mongo, RabbitMQ y n8n saludables (`docker compose ps`).
- [x] Autenticación: Login admin (:8004) emite token JWT HS256 válido.
- [x] Frontend SSR: Acceso a http://localhost:8080 con layouts Público y Backoffice SLDS.
- [x] Donantes: Creación de persona física (`/api/personas`) persistida en tabla `personas` y `personas_humanas`.
- [x] Importación CSV: Ingesta batch sin duplicados de `donantes_sample.csv`.
- [x] Beneficiarios: Alta de entidad y declaración de necesidades extraordinarias.
- [x] Donaciones: Recepción de bienes físicos con segmentación automática en categorías.
- [x] Logística: Alta de camión y chofer persistidos en colecciones NoSQL de MongoDB.
- [x] Rutas y Despacho: Planificación de ruta e inicio de recorrido con publicación de eventos AMQP.
- [x] Gamificación: Cómputo de puntos, actualización de ranking y asignación de insignias en Incentivos.
- [x] Automatización: Disparo de webhooks a n8n y notificación en Discord.
```
