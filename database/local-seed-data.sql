-- ============================================================================
-- DonaTrack - Datos de Prueba para Base de Datos LOCAL y CLOUD
-- ============================================================================

-- 1. SCHEMA: donaciones
-- Categorias y Subcategorias
INSERT INTO donaciones.categorias (categoria_id, nombre, descripcion) VALUES
('11111111-1111-1111-1111-111111111111', 'Alimentos', 'Alimentos secos, perecederos y no perecederos'),
('22222222-2222-2222-2222-222222222222', 'Vestimenta', 'Ropa de abrigo, calzado e indumentaria general'),
('33333333-3333-3333-3333-333333333333', 'Higiene', 'Artículos de higiene personal y limpieza')
ON CONFLICT (categoria_id) DO NOTHING;

INSERT INTO donaciones.subcategorias (subcategoria_id, categoria_id, nombre, descripcion) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'No Perecederos', 'Arroz, fideos, legumbres y enlatados'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Abrigo', 'Camperas, frazadas y buzos'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Limpieza', 'Lavandina, jabón y desinfectantes')
ON CONFLICT (subcategoria_id) DO NOTHING;

-- Direcciones
INSERT INTO donaciones.direcciones (direccion_id, calle, altura, localidad, provincia, pais, cp, latitud, longitud) VALUES
('d1111111-1111-4111-8111-111111111111', 'Av. Corrientes', 1234, 'CABA', 'Buenos Aires', 'Argentina', 'C1043', -34.6037, -58.3816),
('d2222222-2222-4222-8222-222222222222', 'Calle 50', 742, 'La Plata', 'Buenos Aires', 'Argentina', 'B1900', -34.9214, -57.9545)
ON CONFLICT (direccion_id) DO NOTHING;

-- Personas (Humana y Jurídica)
INSERT INTO donaciones.personas (persona_id, direccion_id, email, doc_tipo, doc_numero, contacto_correo, contacto_telefono, contacto_whatsapp, contacto_medio_predeterminado) VALUES
('a1111111-1111-4111-8111-111111111111', 'd1111111-1111-4111-8111-111111111111', 'juan.perez@donatrack.org', 'DNI', '32123456', 'juan.perez@donatrack.org', '1123456789', '1123456789', 'CORREO'),
('a2222222-2222-4222-8222-222222222222', 'd2222222-2222-4222-8222-222222222222', 'contacto@fundacionsolidaria.org', 'CUIT', '30-12345678-9', 'contacto@fundacionsolidaria.org', '1198765432', '1198765432', 'CORREO'),
('d8404b9a-0504-40c7-b8c5-0635faae4f2e', 'd1111111-1111-4111-8111-111111111111', 'donante.seed@donatrack.org', 'DNI', '35111222', 'donante.seed@donatrack.org', '1133445566', '1133445566', 'CORREO'),
('d3333333-3333-4333-8333-333333333333', 'd1111111-1111-4111-8111-111111111111', 'maria.gomez@donatrack.org', 'DNI', '38999888', 'maria.gomez@donatrack.org', '1144556677', '1144556677', 'CORREO')
ON CONFLICT (persona_id) DO NOTHING;

INSERT INTO donaciones.personas_humanas (persona_humana_id, nombre, apellido, edad) VALUES
('a1111111-1111-4111-8111-111111111111', 'Juan', 'Perez', 32),
('d8404b9a-0504-40c7-b8c5-0635faae4f2e', 'Carlos', 'Rodriguez', 29),
('d3333333-3333-4333-8333-333333333333', 'Maria', 'Gomez', 34)
ON CONFLICT (persona_humana_id) DO NOTHING;

INSERT INTO donaciones.personas_juridicas (persona_juridica_id, razon_social, tipo, rubro) VALUES
('a2222222-2222-4222-8222-222222222222', 'Fundación Solidaria', 'ONG', 'Ayuda Comunitaria')
ON CONFLICT (persona_juridica_id) DO NOTHING;

-- Roles (Donante y Beneficiario)
INSERT INTO donaciones.roles (rol_id, fecha_alta, persona_id, dtype) VALUES
('b1111111-1111-4111-8111-111111111111', '2026-01-15', 'a1111111-1111-4111-8111-111111111111', 'Donante'),
('b2222222-2222-4222-8222-222222222222', '2026-02-01', 'a2222222-2222-4222-8222-222222222222', 'Beneficiario'),
('e4444444-4444-4444-8444-444444444444', '2026-02-10', 'a2222222-2222-4222-8222-222222222222', 'Beneficiario'),
('b4444444-4444-4444-8444-444444444444', '2026-02-15', 'd3333333-3333-4333-8333-333333333333', 'Donante')
ON CONFLICT (rol_id) DO NOTHING;

INSERT INTO donaciones.roles_donante (rol_donante_id) VALUES
('b1111111-1111-4111-8111-111111111111'),
('b4444444-4444-4444-8444-444444444444')
ON CONFLICT (rol_donante_id) DO NOTHING;

INSERT INTO donaciones.roles_beneficiario (rol_beneficiario_id, correo_representante) VALUES
('b2222222-2222-4222-8222-222222222222', 'contacto@fundacionsolidaria.org'),
('e4444444-4444-4444-8444-444444444444', 'logistica@comedorlosbajitos.org')
ON CONFLICT (rol_beneficiario_id) DO NOTHING;

-- Necesidad declarada para el Beneficiario
INSERT INTO donaciones.necesidades (necesidad_id, beneficiario_id, subcategoria_id, descripcion, fecha_solicitud, dtype, estado, activa, cantidad_objetivo, tipo_periodo) VALUES
('c1111111-1111-4111-8111-111111111111', 'b2222222-2222-4222-8222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Necesidad mensual de alimentos secos para 100 familias', NOW(), 'RECURRENTE', 'PENDIENTE', true, 500, 'MENSUAL'),
('c2222222-2222-4222-8222-222222222222', 'e4444444-4444-4444-8444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Necesidad mensual de alimentos no perecederos comedor', NOW(), 'RECURRENTE', 'PENDIENTE', true, 300, 'MENSUAL')
ON CONFLICT (necesidad_id) DO NOTHING;

-- Donaciones Originales y Donaciones Segmentadas de Prueba
INSERT INTO donaciones.donaciones_originales (donacion_original_id, donante_id, descripcion_general, fecha_recepcion, usuario_id) VALUES
('01111111-1111-4111-8111-111111111111', 'b1111111-1111-4111-8111-111111111111', 'Recepción inicial lote de alimentos y abrigo', NOW(), 'a2222222-2222-4222-8222-222222222222')
ON CONFLICT (donacion_original_id) DO NOTHING;

INSERT INTO donaciones.donaciones (donacion_id, donacion_original_id, subcategoria_id, necesidad_id, estado, fecha_creacion) VALUES
('5cd80011-2a54-4ca7-a39c-28b2b53d8c55', '01111111-1111-4111-8111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1111111-1111-4111-8111-111111111111', 'EN_DEPOSITO', NOW()),
('139e0f9f-9431-4302-9f06-cad97978f7be', '01111111-1111-4111-8111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'c1111111-1111-4111-8111-111111111111', 'ASIGNADA', NOW()),
('35008064-071a-4d7a-ac37-33318f7d9845', '01111111-1111-4111-8111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'c1111111-1111-4111-8111-111111111111', 'EN_TRASLADO', NOW())
ON CONFLICT (donacion_id) DO NOTHING;

INSERT INTO donaciones.bienes (bien_id, donacion_id, descripcion, cantidad, unidad_medicion, es_usado, fecha_vencimiento) VALUES
('61111111-1111-4111-8111-111111111111', '5cd80011-2a54-4ca7-a39c-28b2b53d8c55', 'Cajas de arroz y fideos secos', 25.0, 'KILOS', false, '2027-12-31'),
('62222222-2222-4222-8222-222222222222', '139e0f9f-9431-4302-9f06-cad97978f7be', 'Packs de leche larga vida y legumbres', 50.0, 'KILOS', false, '2027-10-15'),
('63333333-3333-4333-8333-333333333333', '35008064-071a-4d7a-ac37-33318f7d9845', 'Frazadas térmicas y camperas de invierno', 15.0, 'UNIDADES', false, NULL)
ON CONFLICT (bien_id) DO NOTHING;

INSERT INTO donaciones.historial_estado (historial_estado_id, donacion_id, estado, fecha, observacion, usuario_id) VALUES
('71111111-1111-4111-8111-111111111111', '5cd80011-2a54-4ca7-a39c-28b2b53d8c55', 'EN_DEPOSITO', NOW(), 'Recepción inicial de donación en depósito', 'a2222222-2222-4222-8222-222222222222'),
('72222222-2222-4222-8222-222222222222', '139e0f9f-9431-4302-9f06-cad97978f7be', 'ASIGNADA', NOW(), 'Donación asignada a Fundación Solidaria', 'a2222222-2222-4222-8222-222222222222'),
('73333333-3333-4333-8333-333333333333', '35008064-071a-4d7a-ac37-33318f7d9845', 'EN_TRASLADO', NOW(), 'En traslado hacia beneficiario en Ruta 2', 'CH-102')
ON CONFLICT (historial_estado_id) DO NOTHING;


-- 2. SCHEMA: incentivos
-- Insignias
INSERT INTO incentivos.insignias (insignia_id, nombre, descripcion) VALUES
('e1111111-1111-4111-8111-111111111111', 'Primer Aporte', 'Otorgada al realizar la primera donación en el sistema'),
('e2222222-2222-4222-8222-222222222222', 'Donante Frecuente', 'Otorgada por realizar 5 donaciones en el mismo mes'),
('e3333333-3333-4333-8333-333333333333', 'Héroe Solidario', 'Alcanzar más de 1000 puntos acumulados')
ON CONFLICT (insignia_id) DO NOTHING;

-- Perfil de Donante de prueba
INSERT INTO incentivos.perfiles_donante (perfil_donante_id, categoria) VALUES
('a1111111-1111-4111-8111-111111111111', 'ORO'),
('d3333333-3333-4333-8333-333333333333', 'COLABORADOR')
ON CONFLICT (perfil_donante_id) DO NOTHING;

INSERT INTO incentivos.metricas_donante (metricas_donante_id, perfil_donante_id, total_donaciones_historicas, organizaciones_ayudadas) VALUES
('91111111-1111-4111-8111-111111111111', 'a1111111-1111-4111-8111-111111111111', 12, 4),
('92222222-2222-4222-8222-222222222222', 'd3333333-3333-4333-8333-333333333333', 2, 1)
ON CONFLICT (perfil_donante_id) DO NOTHING;


-- 3. SCHEMA: notificaciones
-- Evento y Notificación de ejemplo
INSERT INTO notificaciones.eventos_notificacion (evento_notificacion_id, tipo_evento, descripcion, fecha_recepcion) VALUES
('f1111111-1111-4111-8111-111111111111', 'INICIO_RUTA', 'El chofer CH-101 ha iniciado su recorrido de reparto', NOW())
ON CONFLICT (evento_notificacion_id) DO NOTHING;

INSERT INTO notificaciones.notificaciones (notificacion_id, evento_notificacion_id, destinatario, medio, mensaje, completada, fecha_envio) VALUES
('f2222222-2222-4222-8222-222222222222', 'f1111111-1111-4111-8111-111111111111', 'contacto@fundacionsolidaria.org', 'EMAIL', 'Tu donación está en camino. Puedes seguir el camión en tiempo real.', true, NOW())
ON CONFLICT (notificacion_id) DO NOTHING;
