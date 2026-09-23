-- Datos para el módulo de Logística
INSERT INTO logistica.camiones (camion_id, patente, capacidad_volumen, altura, capacidad_carga) VALUES ('44444444-4444-4444-4444-444444444444', 'AB123CD', 20.0, 3.5, 5000.0), ('55555555-5555-5555-5555-555555555555', 'EF456GH', 40.0, 4.0, 10000.0);

INSERT INTO logistica.choferes (chofer_id, legajo, nombre) VALUES ('66666666-6666-6666-6666-666666666666', 'CH-001', 'Juan Perez'), ('77777777-7777-7777-7777-777777777777', 'CH-002', 'Maria Gomez');

-- Datos para el módulo de Donaciones
INSERT INTO donaciones.categorias (categoria_id, nombre, descripcion) VALUES ('c1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c', 'Alimentos', 'Alimentos perecederos y no perecederos'), ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Ropa', 'Indumentaria para todas las edades'), ('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e', 'Muebles', 'Muebles en buen estado');

INSERT INTO donaciones.subcategorias (subcategoria_id, nombre, descripcion, categoria_id) VALUES ('11111111-1111-1111-1111-111111111111', 'Alimentos No Perecederos', 'Fideos, arroz, latas', 'c1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c'), ('22222222-2222-2222-2222-222222222222', 'Ropa de Invierno', 'Camperas, buzos, bufandas', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'), ('33333333-3333-3333-3333-333333333333', 'Mesas y Sillas', 'Juegos de comedor', 'b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e');
