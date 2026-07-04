-- ============================================================
-- Schema de Logística - DonaTrack
-- Compatible con PostgreSQL 15+ (local Docker) y Supabase
-- ============================================================

CREATE SCHEMA IF NOT EXISTS logistica;

-- ----------------------------------------------------------
-- Tablas independientes (sin FK)
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.camiones (
    patente           VARCHAR(10)       PRIMARY KEY,
    capacidad_volumen DOUBLE PRECISION,
    altura            DOUBLE PRECISION,
    capacidad_carga   DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS logistica.choferes (
    legajo  VARCHAR(20)  PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL
);

-- ----------------------------------------------------------
-- Solicitudes de Planificación
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.solicitudes_planificacion (
    id               UUID        PRIMARY KEY,
    fecha_solicitud  TIMESTAMP   NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    ids_donaciones   UUID[]
);

-- ----------------------------------------------------------
-- Items de Planificación (Value Objects de Solicitud)
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.items_planificacion (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_id          UUID REFERENCES logistica.solicitudes_planificacion(id) ON DELETE CASCADE,
    id_donacion_original  UUID NOT NULL,
    peso_estimado         DOUBLE PRECISION,
    volumen_estimado      DOUBLE PRECISION,
    -- Dirección destino (embebida)
    calle                 VARCHAR(200),
    altura_dir            VARCHAR(20),
    localidad             VARCHAR(100)
);

-- ----------------------------------------------------------
-- Rutas de Reparto
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.rutas_reparto (
    id              UUID    PRIMARY KEY,
    solicitud_id    UUID    REFERENCES logistica.solicitudes_planificacion(id) ON DELETE SET NULL,
    fecha_operativa DATE    NOT NULL,
    iniciada        BOOLEAN NOT NULL DEFAULT FALSE,
    camion_patente  VARCHAR(10) REFERENCES logistica.camiones(patente),
    chofer_legajo   VARCHAR(20) REFERENCES logistica.choferes(legajo)
);

-- ----------------------------------------------------------
-- Paradas
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.paradas (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta_id    UUID    NOT NULL REFERENCES logistica.rutas_reparto(id) ON DELETE CASCADE,
    orden      INTEGER NOT NULL,
    -- Dirección (embebida)
    calle      VARCHAR(200),
    altura_dir VARCHAR(20),
    localidad  VARCHAR(100),
    -- Coordenada (embebida)
    latitud    DOUBLE PRECISION,
    longitud   DOUBLE PRECISION
);

-- ----------------------------------------------------------
-- Entregas
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.entregas (
    id_entrega        UUID        PRIMARY KEY,
    parada_id         UUID        NOT NULL REFERENCES logistica.paradas(id) ON DELETE CASCADE,
    estado            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    peso_estimado     DOUBLE PRECISION,
    volumen_estimado  DOUBLE PRECISION,
    -- Comprobante de Recepción (embebido, nullable)
    comprobante_fecha_hora     TIMESTAMP,
    comprobante_fotos          TEXT[],
    comprobante_camion_patente VARCHAR(10)
);

-- ----------------------------------------------------------
-- Índices útiles
-- ----------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_items_solicitud     ON logistica.items_planificacion(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_items_donacion      ON logistica.items_planificacion(id_donacion_original);
CREATE INDEX IF NOT EXISTS idx_rutas_solicitud     ON logistica.rutas_reparto(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_paradas_ruta        ON logistica.paradas(ruta_id);
CREATE INDEX IF NOT EXISTS idx_entregas_parada     ON logistica.entregas(parada_id);
CREATE INDEX IF NOT EXISTS idx_entregas_estado     ON logistica.entregas(estado);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado  ON logistica.solicitudes_planificacion(estado);
