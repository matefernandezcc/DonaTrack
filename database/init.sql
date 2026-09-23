-- ============================================================================
-- DonaTrack - Script de Inicialización de Base de Datos Local y Producción
-- 4 Schemas: donaciones, incentivos, logistica, notificaciones
-- ============================================================================

-- EXTENSIONES
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- SCHEMA: donaciones
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS donaciones;

CREATE TABLE IF NOT EXISTS donaciones.direcciones (
    direccion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calle VARCHAR(255),
    altura DOUBLE PRECISION,
    localidad VARCHAR(255),
    provincia VARCHAR(255),
    pais VARCHAR(255),
    cp VARCHAR(50),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS donaciones.personas (
    persona_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    direccion_id UUID REFERENCES donaciones.direcciones(direccion_id) ON DELETE SET NULL,
    email VARCHAR(255),
    doc_tipo VARCHAR(50),
    doc_numero VARCHAR(255),
    contacto_correo VARCHAR(255),
    contacto_telefono VARCHAR(255),
    contacto_whatsapp VARCHAR(255),
    contacto_medio_predeterminado VARCHAR(50),
    nacionalidad VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donaciones.personas_humanas (
    persona_humana_id UUID PRIMARY KEY REFERENCES donaciones.personas(persona_id) ON DELETE CASCADE,
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    edad INTEGER,
    genero VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS donaciones.personas_juridicas (
    persona_juridica_id UUID PRIMARY KEY REFERENCES donaciones.personas(persona_id) ON DELETE CASCADE,
    razon_social VARCHAR(255),
    tipo VARCHAR(50),
    rubro VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donaciones.roles (
    rol_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id UUID REFERENCES donaciones.personas(persona_id) ON DELETE CASCADE,
    fecha_alta DATE,
    dtype VARCHAR(31)
);

CREATE TABLE IF NOT EXISTS donaciones.roles_donante (
    rol_donante_id UUID PRIMARY KEY REFERENCES donaciones.roles(rol_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS donaciones.roles_representante (
    rol_representante_id UUID PRIMARY KEY REFERENCES donaciones.roles(rol_id) ON DELETE CASCADE,
    cargo VARCHAR(255),
    organizacion_id UUID REFERENCES donaciones.personas_juridicas(persona_juridica_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS donaciones.roles_beneficiario (
    rol_beneficiario_id UUID PRIMARY KEY REFERENCES donaciones.roles(rol_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS donaciones.categorias (
    categoria_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donaciones.subcategorias (
    subcategoria_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_id UUID NOT NULL REFERENCES donaciones.categorias(categoria_id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donaciones.necesidades (
    necesidad_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiario_id UUID REFERENCES donaciones.roles_beneficiario(rol_beneficiario_id) ON DELETE CASCADE,
    subcategoria_id UUID REFERENCES donaciones.subcategorias(subcategoria_id) ON DELETE SET NULL,
    descripcion TEXT,
    fecha_solicitud TIMESTAMP,
    dtype VARCHAR(31),
    estado VARCHAR(50),
    cantidad_requerida DOUBLE PRECISION,
    activa BOOLEAN,
    cantidad_objetivo DOUBLE PRECISION,
    tipo_periodo VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS donaciones.periodos_necesidad (
    periodo_necesidad_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    necesidad_id UUID REFERENCES donaciones.necesidades(necesidad_id) ON DELETE CASCADE,
    fecha_inicio DATE,
    fecha_fin DATE,
    estado VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS donaciones.donaciones_originales (
    donacion_original_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donante_id UUID REFERENCES donaciones.roles_donante(rol_donante_id) ON DELETE SET NULL,
    descripcion_general TEXT,
    fecha_recepcion TIMESTAMP,
    usuario_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS donaciones.donaciones (
    donacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donacion_original_id UUID REFERENCES donaciones.donaciones_originales(donacion_original_id) ON DELETE CASCADE,
    subcategoria_id UUID REFERENCES donaciones.subcategorias(subcategoria_id) ON DELETE SET NULL,
    necesidad_id UUID REFERENCES donaciones.necesidades(necesidad_id) ON DELETE SET NULL,
    periodo_id UUID REFERENCES donaciones.periodos_necesidad(periodo_necesidad_id) ON DELETE SET NULL,
    estado VARCHAR(50),
    fecha_creacion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donaciones.bienes (
    bien_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donacion_id UUID REFERENCES donaciones.donaciones(donacion_id) ON DELETE CASCADE,
    descripcion VARCHAR(255),
    cantidad DOUBLE PRECISION,
    unidad_medicion VARCHAR(50),
    es_usado BOOLEAN,
    fecha_vencimiento DATE
);

CREATE TABLE IF NOT EXISTS donaciones.fotos (
    foto_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bien_id UUID REFERENCES donaciones.bienes(bien_id) ON DELETE CASCADE,
    descripcion VARCHAR(255),
    url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS donaciones.historial_estado (
    historial_estado_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donacion_id UUID REFERENCES donaciones.donaciones(donacion_id) ON DELETE CASCADE,
    estado VARCHAR(50),
    fecha TIMESTAMP,
    observacion TEXT,
    usuario_id VARCHAR(255)
);

-- ============================================================================
-- SCHEMA: incentivos
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS incentivos;

CREATE TABLE IF NOT EXISTS incentivos.insignias (
    insignia_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS incentivos.misiones (
    mision_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    recompensa_insignia_id UUID REFERENCES incentivos.insignias(insignia_id) ON DELETE SET NULL,
    tipo_metrica VARCHAR(50),
    objetivo INTEGER,
    orden_ejecucion INTEGER
);

CREATE TABLE IF NOT EXISTS incentivos.perfiles_donante (
    perfil_donante_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria VARCHAR(50),
    mision_actual_id UUID REFERENCES incentivos.misiones(mision_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS incentivos.insignias_obtenidas (
    insignia_obtenida_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    perfil_donante_id UUID REFERENCES incentivos.perfiles_donante(perfil_donante_id) ON DELETE CASCADE,
    insignia_id UUID REFERENCES incentivos.insignias(insignia_id) ON DELETE CASCADE,
    fecha_obtencion DATE,
    visible_publicamente BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS incentivos.progreso_misiones (
    progreso_mision_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    perfil_donante_id UUID REFERENCES incentivos.perfiles_donante(perfil_donante_id) ON DELETE CASCADE,
    mision_id UUID REFERENCES incentivos.misiones(mision_id) ON DELETE CASCADE,
    progreso_actual INTEGER DEFAULT 0,
    estado VARCHAR(50),
    mes_completada VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS incentivos.metricas_donante (
    metricas_donante_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    perfil_donante_id UUID UNIQUE REFERENCES incentivos.perfiles_donante(perfil_donante_id) ON DELETE CASCADE,
    total_donaciones_historicas INTEGER DEFAULT 0,
    organizaciones_ayudadas INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS incentivos.registros_donacion (
    registro_donacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metricas_donante_id UUID REFERENCES incentivos.metricas_donante(metricas_donante_id) ON DELETE CASCADE,
    id_donacion_origen UUID,
    cantidad_bienes INTEGER,
    categorias TEXT,
    id_entidad_beneficiaria_origen UUID,
    mes_donacion VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS incentivos.rankings_mensuales (
    ranking_mensual_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mes VARCHAR(20),
    anio INTEGER
);

CREATE TABLE IF NOT EXISTS incentivos.posiciones_ranking (
    posicion_ranking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ranking_mensual_id UUID REFERENCES incentivos.rankings_mensuales(ranking_mensual_id) ON DELETE CASCADE,
    perfil_donante_id UUID REFERENCES incentivos.perfiles_donante(perfil_donante_id) ON DELETE CASCADE,
    posicion INTEGER,
    misiones_completadas_mes INTEGER
);

-- ============================================================================
-- SCHEMA: logistica
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS logistica;

CREATE TABLE IF NOT EXISTS logistica.camiones (
    camion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patente VARCHAR(255) UNIQUE,
    capacidad_volumen DOUBLE PRECISION,
    altura DOUBLE PRECISION,
    capacidad_carga DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS logistica.choferes (
    chofer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legajo VARCHAR(255) UNIQUE,
    nombre VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS logistica.solicitudes_planificacion (
    solicitud_planificacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_solicitud TIMESTAMP,
    estado VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS logistica.rutas_reparto (
    ruta_reparto_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_planificacion_id UUID REFERENCES logistica.solicitudes_planificacion(solicitud_planificacion_id) ON DELETE SET NULL,
    camion_id UUID REFERENCES logistica.camiones(camion_id) ON DELETE SET NULL,
    chofer_id UUID REFERENCES logistica.choferes(chofer_id) ON DELETE SET NULL,
    fecha_operativa DATE,
    iniciada BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS logistica.paradas (
    parada_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta_reparto_id UUID REFERENCES logistica.rutas_reparto(ruta_reparto_id) ON DELETE CASCADE,
    orden INTEGER,
    calle VARCHAR(255),
    altura VARCHAR(255),
    localidad VARCHAR(255),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS logistica.entregas (
    entrega_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parada_id UUID REFERENCES logistica.paradas(parada_id) ON DELETE CASCADE,
    id_donacion UUID,
    estado VARCHAR(50),
    peso_estimado DOUBLE PRECISION,
    volumen_estimado DOUBLE PRECISION,
    comprobante_fecha_hora TIMESTAMP,
    comprobante_fotos TEXT,
    comprobante_camion_patente VARCHAR(255),
    justificacion_fallo TEXT
);

CREATE TABLE IF NOT EXISTS logistica.items_planificacion (
    item_planificacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_planificacion_id UUID REFERENCES logistica.solicitudes_planificacion(solicitud_planificacion_id) ON DELETE CASCADE,
    id_donacion UUID,
    peso_estimado DOUBLE PRECISION,
    volumen_estimado DOUBLE PRECISION,
    calle_destino VARCHAR(255),
    altura_destino VARCHAR(255),
    localidad_destino VARCHAR(255)
);

-- ============================================================================
-- SCHEMA: notificaciones
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS notificaciones;

CREATE TABLE IF NOT EXISTS notificaciones.eventos_notificacion (
    evento_notificacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_evento VARCHAR(50),
    descripcion VARCHAR(255),
    fecha_recepcion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notificaciones.notificaciones (
    notificacion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_notificacion_id UUID REFERENCES notificaciones.eventos_notificacion(evento_notificacion_id) ON DELETE CASCADE,
    destinatario VARCHAR(255),
    medio VARCHAR(50),
    mensaje TEXT,
    fecha_envio TIMESTAMP,
    completada BOOLEAN DEFAULT FALSE
);
