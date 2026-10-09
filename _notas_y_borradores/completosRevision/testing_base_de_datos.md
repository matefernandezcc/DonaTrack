# 🧪 Tests Manuales E2E — DonaTrack

Guía paso a paso para validar que todo ande. Ejecutás con curl/Bruno, verificás en DBeaver.

---

## Prerrequisitos

Antes de todo, asegurate de tener levantados los servicios y la DB:

```bash
# Levantar Postgres + RabbitMQ (si usás Docker)
docker compose up -d postgres-donatrack rabbitmq

# Levantar los servicios Java (cada uno en una terminal)
cd donatrack-donaciones && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-logistica && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-incentivos && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-notificaciones && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Puertos:**
| Servicio | Puerto |
|----------|--------|
| Donaciones | 8000 |
| Incentivos | 8001 |
| Logística | 8002 |
| Notificaciones | 8003 |

**Swagger UI (para probar desde el browser):**
- http://localhost:8000/swagger-ui.html
- http://localhost:8001/swagger-ui.html
- http://localhost:8002/swagger-ui.html
- http://localhost:8003/swagger-ui.html

---

## TEST 1: Crear una Persona Humana

**Qué probamos:** Que el endpoint `POST /api/personas` cree una persona y la persista en la DB.

### Ejecutar (curl):
```bash
curl -X POST http://localhost:8000/api/personas \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "HUMANA",
    "email": "test.manual@donatrack.com",
    "nombre": "Carlos",
    "apellido": "Test",
    "edad": 35,
    "contacto": {
      "correoElectronico": "test.manual@donatrack.com",
      "telefono": "+54 11 1234-5678",
      "whatsapp": null,
      "medioPredeterminado": "CORREO"
    },
    "documento": {
      "tipo": "DNI",
      "numero": "99887766"
    },
    "direccion": {
      "calle": "Av. Corrientes",
      "altura": 1234,
      "localidad": "CABA",
      "provincia": {
        "nombreProvincia": "Buenos Aires",
        "pais": { "nombre": "Argentina" }
      },
      "codigoPostal": "C1043",
      "coordenadas": null
    }
  }'
```

### Respuesta esperada:
```json
{
  "id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "tipo": "HUMANA",
  "email": "test.manual@donatrack.com",
  "nombre": "Carlos",
  "apellido": "Test",
  ...
}
```

### Verificar en DBeaver:
```sql
-- Buscar la persona creada
SELECT * FROM donaciones.personas WHERE email = 'test.manual@donatrack.com';

-- Verificar que se creó la fila en personas_humanas (JOINED)
SELECT p.persona_id, p.email, ph.nombre, ph.apellido, ph.edad
FROM donaciones.personas p
JOIN donaciones.personas_humanas ph ON p.persona_id = ph.persona_humana_id
WHERE p.email = 'test.manual@donatrack.com';

-- Verificar que se creó la dirección
SELECT d.* FROM donaciones.direcciones d
JOIN donaciones.personas p ON p.direccion_id = d.direccion_id
WHERE p.email = 'test.manual@donatrack.com';
```

> [!TIP]
> **¿Qué tiene que pasar?** Deberías ver 1 fila en `personas`, 1 fila en `personas_humanas`, y 1 fila en `direcciones`. Si no ves nada → el endpoint devolvió 200 pero no persistió (revisar logs del servicio).

---

## TEST 2: Importar CSV de Donantes

**Qué probamos:** Que el endpoint `POST /api/personas/importar-csv` procese un archivo CSV y cree las personas en batch.

### Paso 1: Crear el CSV de prueba

Ya tenés uno en el repo: `enunciado/CSV/donantes_sample.csv`. Tiene 10 donantes (9 humanos + 1 jurídica).

### Paso 2: Ejecutar (curl con multipart):
```bash
curl -X POST http://localhost:8000/api/personas/importar-csv \
  -F "file=@enunciado/CSV/donantes_sample.csv"
```

> [!WARNING]
> **Ojo con la ruta del archivo.** El comando asume que estás en la raíz del proyecto `/home/mateo/Desktop/DonaTrack`. Si estás en otro directorio, ajustá la ruta.

### Respuesta esperada:
```
CSV procesado e importado con éxito a la base de datos.
```

### Verificar en DBeaver:
```sql
-- Contar personas creadas
SELECT COUNT(*) AS total_personas FROM donaciones.personas;

-- Ver las personas humanas importadas
SELECT p.persona_id, p.email, p.doc_tipo, p.doc_numero, ph.nombre
FROM donaciones.personas p
JOIN donaciones.personas_humanas ph ON p.persona_id = ph.persona_humana_id
ORDER BY ph.nombre;

-- Ver la persona jurídica importada
SELECT p.persona_id, p.email, pj.razon_social, pj.tipo
FROM donaciones.personas p
JOIN donaciones.personas_juridicas pj ON p.persona_id = pj.persona_juridica_id;
```

**¿Qué tiene que pasar?** Deberías ver ~10 personas nuevas. La de "Santa Fe Industrial Fundación" debería estar en `personas_juridicas`, las demás en `personas_humanas`.

### Paso 3: Re-importar el mismo CSV (test idempotencia):
```bash
curl -X POST http://localhost:8000/api/personas/importar-csv \
  -F "file=@enunciado/CSV/donantes_sample.csv"
```

**¿Qué tiene que pasar?** No debería duplicar personas. El importador busca por email y actualiza los existentes. Verificá que el `COUNT(*)` siga siendo el mismo.

---

## TEST 3: Listar personas (verificar que se guardaron)

```bash
curl http://localhost:8000/api/personas | python3 -m json.tool | head -50
```

Deberías ver la lista de todas las personas (las creadas en Test 1 + las del CSV).

### Buscar una persona por ID:
```bash
# Reemplazá el UUID por uno real que veas en DBeaver
curl http://localhost:8000/api/personas/PONER-UUID-ACA | python3 -m json.tool
```

---

## TEST 4: Crear Camión y Chofer (Logística)

**Qué probamos:** CRUD de la flota en el servicio de logística.

### Crear camión:
```bash
curl -X POST http://localhost:8002/api/camiones \
  -H "Content-Type: application/json" \
  -d '{
    "patente": "TEST01",
    "capacidadVolumen": 15.0,
    "altura": 3.5,
    "capacidadCarga": 5000.0
  }'
```

### Crear chofer:
```bash
curl -X POST http://localhost:8002/api/choferes \
  -H "Content-Type: application/json" \
  -d '{
    "legajo": "CH-TEST-01",
    "nombre": "Roberto Test"
  }'
```

### Listar para verificar:
```bash
curl http://localhost:8002/api/camiones | python3 -m json.tool
curl http://localhost:8002/api/choferes | python3 -m json.tool
```

### Verificar en DBeaver:
```sql
SELECT * FROM logistica.camiones WHERE patente = 'TEST01';
SELECT * FROM logistica.choferes WHERE legajo = 'CH-TEST-01';
```

---

## TEST 5: Recibir Bienes (Crear Donación Completa)

**Qué probamos:** El flujo de recepción de bienes que crea una DonacionOriginal con sus Donaciones segmentadas.

### Paso 1: Necesitás un ID de donante existente

## TEST 5: Donaciones — Recibir Donación Física (Bienes)

**Qué probamos:** Que el endpoint procese una donación física, la segmente automáticamente en las categorías correspondientes (Ropa y Alimentos en este caso) y guarde los bienes.

> [!IMPORTANT]
> **IDs requeridos**: Los campos `idDonante` e `idAdministrador` **DEBEN ser los `persona_id`** (UUIDs de la tabla `personas`), NO los IDs del rol.
> Podés obtener IDs válidos con esta query en DBeaver:
> ```sql
> SELECT persona_id, dtype FROM donaciones.roles WHERE dtype IN ('Donante', 'Administrador');
> ```

```bash
curl -X POST http://localhost:8000/api/recepciones \
  -H "Content-Type: application/json" \
  -d '{
    "idDonante": "a1111111-1111-4111-8111-111111111111",
    "idAdministrador": "a2222222-2222-4222-8222-222222222222",
    "bienesBrutos": [
      {
        "descripcion": "Paquete de arroz 1kg",
        "cantidad": 10.0,
        "unidadMedicion": "unidades",
        "esUsado": false,
        "fechaVencimiento": "2027-06-01",
        "nombreSubcategoria": "Alimentos no perecederos"
      },
      {
        "descripcion": "Campera de abrigo usada",
        "cantidad": 3.0,
        "unidadMedicion": "unidades",
        "esUsado": true,
        "fechaVencimiento": null,
        "nombreSubcategoria": "Indumentaria"
      }
    ]
  }'
```

### Verificar en DBeaver:
```sql
-- La donación original
SELECT * FROM donaciones.donaciones_originales ORDER BY fecha_recepcion DESC LIMIT 5;

-- Las donaciones segmentadas
SELECT d.donacion_id, d.estado, d.fecha_creacion, dorig.descripcion_general
FROM donaciones.donaciones d
JOIN donaciones.donaciones_originales dorig ON d.donacion_original_id = dorig.donacion_original_id
ORDER BY d.fecha_creacion DESC LIMIT 10;

-- Los bienes asociados
SELECT b.*, d.donacion_id
FROM donaciones.bienes b
JOIN donaciones.donaciones d ON b.donacion_id = d.donacion_id
ORDER BY d.fecha_creacion DESC LIMIT 10;
```

---

## TEST 6: Logística — Recepcionar Item para Planificación

**Qué probamos:** Que logística reciba un ítem de donación para planificar rutas.

> [!TIP]
> **ID de Donación Real**: Para hacer un test end-to-end realista, reemplazá el `idDonacion` en el JSON por un UUID real generado en el Test 5. Podés obtener la última donación generada con esta query:
> ```sql
> SELECT d.donacion_id, b.descripcion FROM donaciones.donaciones d JOIN donaciones.bienes b ON d.donacion_id = b.donacion_id ORDER BY d.fecha_creacion DESC LIMIT 1;
> ```

```bash
curl -X POST http://localhost:8002/api/planificacion/items \
  -H "Content-Type: application/json" \
  -d '{
    "idDonacion": "76f9706f-8369-43cd-aaca-cc1603bf67f7",
    "peso": 25.5,
    "volumen": 0.5,
    "calleDestino": "Av. Santa Fe",
    "alturaDestino": "1234",
    "localidadDestino": "CABA"
  }'
```

### Verificar en DBeaver:
```sql
SELECT * FROM logistica.items_planificacion
ORDER BY solicitud_planificacion_id DESC;
```

---

## TEST 7: Incentivos — Registrar Actividad

**Qué probamos:** Que cuando un donante realiza una actividad, se actualicen sus métricas, puntos y pueda desbloquear misiones/insignias.

> [!IMPORTANT]
> **Requisitos Previos y UUIDs**:
> 1. Asegurate de tener corriendo el servidor de **Incentivos** (`donatrack-incentivos` en el puerto 8001).
> 2. El UUID en la **URL del endpoint** (`/api/donantes/.../actividad`) **DEBE ser exactamente el mismo** que el campo `"idDonante"` del JSON.
> 3. Para conseguir los 3 UUIDs necesarios para este test, ejecutá la siguiente query en DBeaver:
> ```sql
> SELECT d.donacion_id AS id_donacion, r_don.persona_id AS id_donante, r_ben.persona_id AS id_entidad_beneficiaria
> FROM donaciones.donaciones d
> JOIN donaciones.donaciones_originales dorig ON d.donacion_original_id = dorig.donacion_original_id
> JOIN donaciones.roles r_don ON dorig.donante_id = r_don.rol_id 
> CROSS JOIN (SELECT persona_id FROM donaciones.roles WHERE dtype = 'Beneficiario' LIMIT 1) r_ben
> ORDER BY d.fecha_creacion DESC LIMIT 1;
> ```

```bash
curl -X POST http://localhost:8001/api/donantes/a1111111-1111-4111-8111-111111111111/actividad \
  -H "Content-Type: application/json" \
  -d '{
    "idDonacion": "76f9706f-8369-43cd-aaca-cc1603bf67f7",
    "idDonante": "a1111111-1111-4111-8111-111111111111",
    "cantidadBienes": 5,
    "categorias": ["Alimentos", "Ropa"],
    "idEntidadBeneficiaria": "a2222222-2222-4222-8222-222222222222",
    "fecha": "2026-09-18"
  }'
```

### Verificar en DBeaver:
```sql
SELECT * FROM incentivos.perfiles_donante;
SELECT * FROM incentivos.metricas_donante;
SELECT * FROM incentivos.registros_donacion ORDER BY mes_donacion DESC;
```

---

## 🔄 Reset Total y Cambios en Caliente

### 1. Reset Total ("Volver a Empezar")
Si rompiste la base de datos haciendo pruebas o querés arrancar desde cero como si recién hubieras clonado el repo, seguí este paso a paso:

```bash
# 1. Bajar todos los microservicios Java
# Apretá Ctrl+C en todas las terminales donde tengas un Spring Boot corriendo.

# 2. Bajar Docker y BORRAR los datos persistidos (elimina la DB y RabbitMQ)
docker compose down -v

# 3. Levantar Docker desde cero con Postgres + RabbitMQ  (esto vuelve a crear las tablas y corre el local-seed-data.sql)
docker compose up -d postgres-donatrack rabbitmq

# 4. Limpiar compilaciones viejas por las dudas (opcional pero recomendado)
../mvnw clean

# 5. Volver a levantar los microservicios
cd donatrack-donaciones && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-logistica && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-incentivos && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd donatrack-notificaciones && ../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 2. Cambiar Código "En Caliente"
Si tocás un archivo `.java` mientras el servidor está levantado, a veces Spring Boot recompila automáticamente y funciona perfecto. **Pero**, si tenés un error raro al reiniciar o ves un `java.lang.NoClassDefFoundError`, significa que Maven se hizo un lío con la caché en la carpeta `target`. 

**Para solucionarlo sin bajar la base de datos:**
```bash
# 1. Frená el microservicio fallado con Ctrl+C
# 2. Forzá una recompilación limpia
../mvnw clean compile
# 3. Volvelo a levantar
../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Troubleshooting Rápido

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| `Connection refused` | Servicio no levantado | Verificar que el puerto esté escuchando: `lsof -i :8000` |
| `500 Internal Server Error` | Error en el mapper/entity | Revisar logs en la terminal del servicio |
| `415 Unsupported Media Type` | Falta `Content-Type` header | Agregar `-H "Content-Type: application/json"` |
| `400 Bad Request` en CSV | Archivo vacío o ruta incorrecta | Verificar que `@` está antes de la ruta: `-F "file=@ruta"` |
| Datos no aparecen en DBeaver | Transacción no commiteó | Hacer refresh (F5) en DBeaver, verificar schema correcto |
| `could not determine recommended JdbcType` | Tipo de dato incorrecto | Verificar que el JSON matchea el tipo del campo (ej: UUID, no string) |

---

## Checklist Rápido de Verificación ✅

```
[x] TEST 1: Crear persona → aparece en donaciones.personas + personas_humanas
[x] TEST 2: Importar CSV → aparecen ~10 personas nuevas
[x] TEST 2b: Re-importar CSV → no se duplican (mismo count)
[x] TEST 3: GET /api/personas → devuelve la lista completa
[x] TEST 4: Crear camión/chofer → aparecen en logistica.camiones/choferes
[x] TEST 5: Recibir bienes → donaciones_originales + donaciones + bienes creados
[x] TEST 6: Item planificación → aparece en logistica.items_planificacion
[x] TEST 7: Actividad incentivos → registros en incentivos.perfiles_donante
```
