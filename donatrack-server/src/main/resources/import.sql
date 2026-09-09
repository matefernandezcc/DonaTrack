-- Datos para el módulo de Logística
INSERT INTO logistica.camiones (patente, capacidad_volumen, altura, capacidad_carga) VALUES ('AB123CD', 20.0, 3.5, 5000.0), ('EF456GH', 40.0, 4.0, 10000.0);

INSERT INTO logistica.choferes (legajo, nombre) VALUES ('CH-001', 'Juan Perez'), ('CH-002', 'Maria Gomez');

-- Datos para el módulo de Donaciones
INSERT INTO donaciones.categorias (id, nombre, descripcion) VALUES ('c1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c', 'Alimentos', 'Alimentos perecederos y no perecederos'), ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Ropa', 'Indumentaria para todas las edades'), ('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e', 'Muebles', 'Muebles en buen estado');

INSERT INTO donaciones.subcategorias (id, nombre, descripcion, categoria_id) VALUES ('11111111-1111-1111-1111-111111111111', 'Alimentos No Perecederos', 'Fideos, arroz, latas', 'c1a2b3c4-d5e6-7f8a-9b0c-1d2e3f4a5b6c'), ('22222222-2222-2222-2222-222222222222', 'Ropa de Invierno', 'Camperas, buzos, bufandas', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'), ('33333333-3333-3333-3333-333333333333', 'Mesas y Sillas', 'Juegos de comedor', 'b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e');
