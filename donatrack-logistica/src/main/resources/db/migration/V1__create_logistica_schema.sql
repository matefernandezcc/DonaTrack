-- ============================================================
-- Schema de Logística - DonaTrack (DER Oficial)
-- Compatible con PostgreSQL 15+ (local Docker) y Supabase
-- ============================================================

CREATE SCHEMA IF NOT EXISTS logistica;

-- ----------------------------------------------------------
-- Camiones y Choferes
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.camiones (
    camion_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patente           VARCHAR(255) UNIQUE,
    capacidad_volumen DOUBLE PRECISION,
    altura            DOUBLE PRECISION,
    capacidad_carga   DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS logistica.choferes (
    chofer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legajo    VARCHAR(255) UNIQUE,
    nombre    VARCHAR(255)
);

-- ----------------------------------------------------------
-- Solicitudes de Planificación
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.solicitudes_planificacion (
    solicitud_planificacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_solicitud            TIMESTAMP,
    estado                     VARCHAR(50)
);

-- ----------------------------------------------------------
-- Rutas de Reparto
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.rutas_reparto (
    ruta_reparto_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_planificacion_id UUID REFERENCES logistica.solicitudes_planificacion(solicitud_planificacion_id) ON DELETE SET NULL,
    camion_id                  UUID REFERENCES logistica.camiones(camion_id) ON DELETE SET NULL,
    chofer_id                  UUID REFERENCES logistica.choferes(chofer_id) ON DELETE SET NULL,
    fecha_operativa            DATE,
    iniciada                   BOOLEAN DEFAULT FALSE
);

-- ----------------------------------------------------------
-- Paradas
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.paradas (
    parada_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta_reparto_id  UUID REFERENCES logistica.rutas_reparto(ruta_reparto_id) ON DELETE CASCADE,
    orden            INTEGER,
    calle            VARCHAR(255),
    altura           VARCHAR(255),
    localidad        VARCHAR(255),
    latitud          DOUBLE PRECISION,
    longitud         DOUBLE PRECISION
);

-- ----------------------------------------------------------
-- Entregas
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.entregas (
    entrega_id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parada_id                  UUID REFERENCES logistica.paradas(parada_id) ON DELETE CASCADE,
    id_donacion                UUID,
    estado                     VARCHAR(50),
    peso_estimado              DOUBLE PRECISION,
    volumen_estimado           DOUBLE PRECISION,
    comprobante_fecha_hora     TIMESTAMP,
    comprobante_fotos          TEXT,
    comprobante_camion_patente VARCHAR(255),
    justificacion_fallo        TEXT
);

-- ----------------------------------------------------------
-- Items de Planificación
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS logistica.items_planificacion (
    item_planificacion_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_planificacion_id UUID REFERENCES logistica.solicitudes_planificacion(solicitud_planificacion_id) ON DELETE CASCADE,
    id_donacion                UUID,
    peso_estimado              DOUBLE PRECISION,
    volumen_estimado           DOUBLE PRECISION,
    calle_destino              VARCHAR(255),
    altura_destino             VARCHAR(255),
    localidad_destino          VARCHAR(255)
);
