-- ============================================================================
-- DonaTrack - Datos de Prueba para Base de Datos LOCAL
-- ============================================================================

-- 1. SCHEMA: donaciones
-- Categorias y Subcategorias
INSERT INTO donaciones.categorias (categoria_id, nombre, descripcion) VALUES
('11111111-1111-1111-1111-111111111111', 'Alimentos', 'Alimentos secos, perecederos y no perecederos'),
('22222222-2222-2222-2222-222222222222', 'Vestimenta', 'Ropa de abrigo, calzado e indumentaria general'),
('33333333-3333-3333-3333-333333333333', 'Higiene', 'ArtÃ­culos de higiene personal y limpieza')
ON CONFLICT (categoria_id) DO NOTHING;

INSERT INTO donaciones.subcategorias (subcategoria_id, categoria_id, nombre, descripcion) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'No Perecederos', 'Arroz, fideos, legumbres y enlatados'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Abrigo', 'Camperas, frazadas y buzos'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'Limpieza', 'Lavandina, jabÃ³n y desinfectantes')
ON CONFLICT (subcategoria_id) DO NOTHING;

-- Direcciones
INSERT INTO donaciones.direcciones (direccion_id, calle, altura, localidad, provincia, pais, cp, latitud, longitud) VALUES
('d1111111-1111-4111-8111-111111111111', 'Av. Corrientes', 1234, 'CABA', 'Buenos Aires', 'Argentina', 'C1043', -34.6037, -58.3816),
('d2222222-2222-4222-8222-222222222222', 'Calle 50', 742, 'La Plata', 'Buenos Aires', 'Argentina', 'B1900', -34.9214, -57.9545)
ON CONFLICT (direccion_id) DO NOTHING;

-- Personas (Humana y JurÃ­dica)
INSERT INTO donaciones.personas (persona_id, direccion_id, email, doc_tipo, doc_numero, contacto_correo, contacto_telefono) VALUES
('a1111111-1111-4111-8111-111111111111', 'd1111111-1111-4111-8111-111111111111', 'juan.perez@donatrack.org', 'DNI', '32123456', 'juan.perez@donatrack.org', '1123456789'),
('a2222222-2222-4222-8222-222222222222', 'd2222222-2222-4222-8222-222222222222', 'contacto@fundacionsolidaria.org', 'CUIT', '30-12345678-9', 'contacto@fundacionsolidaria.org', '1198765432')
ON CONFLICT (persona_id) DO NOTHING;

INSERT INTO donaciones.personas_humanas (persona_humana_id, nombre, apellido, edad) VALUES
('a1111111-1111-4111-8111-111111111111', 'Juan', 'Perez', 32)
ON CONFLICT (persona_humana_id) DO NOTHING;

INSERT INTO donaciones.personas_juridicas (persona_juridica_id, razon_social, tipo, rubro) VALUES
('a2222222-2222-4222-8222-222222222222', 'FundaciÃ³n Solidaria', 'ONG', 'Ayuda Comunitaria')
ON CONFLICT (persona_juridica_id) DO NOTHING;

-- Roles (Donante y Beneficiario)
INSERT INTO donaciones.roles (rol_id, fecha_alta, persona_id, dtype) VALUES
('b1111111-1111-4111-8111-111111111111', '2026-01-15', 'a1111111-1111-4111-8111-111111111111', 'Donante'),
('b2222222-2222-4222-8222-222222222222', '2026-02-01', 'a2222222-2222-4222-8222-222222222222', 'Beneficiario')
ON CONFLICT (rol_id) DO NOTHING;

INSERT INTO donaciones.roles_donante (rol_donante_id) VALUES
('b1111111-1111-4111-8111-111111111111')
ON CONFLICT (rol_donante_id) DO NOTHING;

INSERT INTO donaciones.roles_beneficiario (rol_beneficiario_id) VALUES
('b2222222-2222-4222-8222-222222222222')
ON CONFLICT (rol_beneficiario_id) DO NOTHING;

-- Necesidad declarada para el Beneficiario
INSERT INTO donaciones.necesidades (necesidad_id, beneficiario_id, subcategoria_id, descripcion, fecha_solicitud, dtype, estado, activa, cantidad_objetivo, tipo_periodo) VALUES
('c1111111-1111-4111-8111-111111111111', 'b2222222-2222-4222-8222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Necesidad mensual de alimentos secos para 100 familias', NOW(), 'RECURRENTE', 'PENDIENTE', true, 500, 'MENSUAL')
ON CONFLICT (necesidad_id) DO NOTHING;


-- 2. SCHEMA: incentivos
-- Insignias
INSERT INTO incentivos.insignias (insignia_id, nombre, descripcion) VALUES
('e1111111-1111-4111-8111-111111111111', 'Primer Aporte', 'Otorgada al realizar la primera donaciÃ³n en el sistema'),
('e2222222-2222-4222-8222-222222222222', 'Donante Frecuente', 'Otorgada por realizar 5 donaciones en el mismo mes'),
('e3333333-3333-4333-8333-333333333333', 'HÃ©roe Solidario', 'Alcanzar mÃ¡s de 1000 puntos acumulados')
ON CONFLICT (insignia_id) DO NOTHING;

-- Perfil de Donante de prueba
INSERT INTO incentivos.perfiles_donante (perfil_donante_id, categoria) VALUES
('a1111111-1111-4111-8111-111111111111', 'ORO')
ON CONFLICT (perfil_donante_id) DO NOTHING;

INSERT INTO incentivos.metricas_donante (metricas_donante_id, perfil_donante_id, total_donaciones_historicas, organizaciones_ayudadas) VALUES
('91111111-1111-4111-8111-111111111111', 'a1111111-1111-4111-8111-111111111111', 12, 4)
ON CONFLICT (metricas_donante_id) DO NOTHING;


-- 3. SCHEMA: notificaciones
-- Evento y NotificaciÃ³n de ejemplo
INSERT INTO notificaciones.eventos_notificacion (evento_notificacion_id, tipo_evento, descripcion, fecha_recepcion) VALUES
('f1111111-1111-4111-8111-111111111111', 'INICIO_RUTA', 'El chofer CH-101 ha iniciado su recorrido de reparto', NOW())
ON CONFLICT (evento_notificacion_id) DO NOTHING;

INSERT INTO notificaciones.notificaciones (notificacion_id, evento_notificacion_id, destinatario, medio, mensaje, completada, fecha_envio) VALUES
('f2222222-2222-4222-8222-222222222222', 'f1111111-1111-4111-8111-111111111111', 'contacto@fundacionsolidaria.org', 'EMAIL', 'Tu donaciÃ³n estÃ¡ en camino. Puedes seguir el camiÃ³n en tiempo real.', true, NOW())
ON CONFLICT (notificacion_id) DO NOTHING;
