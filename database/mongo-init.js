// ============================================================================
// DonaTrack - Script de Inicialización y Precarga de Datos para MongoDB
// Base de datos: donatrack_logistica
// ============================================================================

db = db.getSiblingDB('donatrack_logistica');

print('>>> [MongoDB Init] Iniciando precarga de datos enriquecidos en donatrack_logistica...');

// 1. Colección: camiones (Flota completa con diversidad de capacidades)
if (db.camiones.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando 6 camiones en la flota...');
    db.camiones.insertMany([
        {
            _id: UUID('c1111111-1111-4111-8111-111111111111'),
            patente: 'AA123BB',
            capacidadVolumen: 20.0,
            altura: 3.2,
            capacidadCarga: 4000.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        },
        {
            _id: UUID('c2222222-2222-4222-8222-222222222222'),
            patente: 'AF456CD',
            capacidadVolumen: 12.5,
            altura: 2.8,
            capacidadCarga: 2500.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        },
        {
            _id: UUID('c3333333-3333-4333-8333-333333333333'),
            patente: 'AC789EE',
            capacidadVolumen: 30.0,
            altura: 3.8,
            capacidadCarga: 8000.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        },
        {
            _id: UUID('c4444444-4444-4444-8444-444444444444'),
            patente: 'AD102ZX',
            capacidadVolumen: 15.0,
            altura: 3.0,
            capacidadCarga: 3200.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        },
        {
            _id: UUID('c5555555-5555-4555-8555-555555555555'),
            patente: 'AE304KL',
            capacidadVolumen: 25.0,
            altura: 3.5,
            capacidadCarga: 5500.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        },
        {
            _id: UUID('c6666666-6666-4666-8666-666666666666'),
            patente: 'AG555OP',
            capacidadVolumen: 10.0,
            altura: 2.5,
            capacidadCarga: 1800.0,
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
        }
    ]);
}

// 2. Colección: choferes (Personal de conducción)
if (db.choferes.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando 6 choferes...');
    db.choferes.insertMany([
        {
            _id: UUID('f1111111-1111-4111-8111-111111111111'),
            legajo: 'CH-101',
            nombre: 'Roberto Gómez',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        },
        {
            _id: UUID('f2222222-2222-4222-8222-222222222222'),
            legajo: 'CH-102',
            nombre: 'Laura Fernández',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        },
        {
            _id: UUID('f3333333-3333-4333-8333-333333333333'),
            legajo: 'CH-103',
            nombre: 'Carlos Atlas',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        },
        {
            _id: UUID('f4444444-4444-4444-8444-444444444444'),
            legajo: 'CH-104',
            nombre: 'Martín Benítez',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        },
        {
            _id: UUID('f5555555-5555-4555-8555-555555555555'),
            legajo: 'CH-105',
            nombre: 'Sofía Morales',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        },
        {
            _id: UUID('f6666666-6666-4666-8666-666666666666'),
            legajo: 'CH-106',
            nombre: 'Esteban Rossi',
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
        }
    ]);
}

// 3. Colección: items_planificacion (Donaciones listas para agrupar y rutear)
if (db.items_planificacion.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando 6 items de planificación en distintas zonas...');
    db.items_planificacion.insertMany([
        {
            _id: UUID('d1111111-1111-4111-8111-111111111111'),
            idDonacionOriginal: UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
            pesoEstimado: 120.0,
            volumenEstimado: 0.8,
            destino: {
                calle: 'Av. Corrientes',
                altura: '1234',
                localidad: 'CABA'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        },
        {
            _id: UUID('d2222222-2222-4222-8222-222222222222'),
            idDonacionOriginal: UUID('32008064-071a-4d7a-ac37-33318f7d9842'),
            pesoEstimado: 250.0,
            volumenEstimado: 1.5,
            destino: {
                calle: 'Calle 50',
                altura: '742',
                localidad: 'La Plata'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        },
        {
            _id: UUID('d3333333-3333-4333-8333-333333333333'),
            idDonacionOriginal: UUID('33008064-071a-4d7a-ac37-33318f7d9843'),
            pesoEstimado: 85.0,
            volumenEstimado: 0.5,
            destino: {
                calle: 'Av. Santa Fe',
                altura: '3200',
                localidad: 'Palermo, CABA'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        },
        {
            _id: UUID('d4444444-4444-4444-8444-444444444444'),
            idDonacionOriginal: UUID('34008064-071a-4d7a-ac37-33318f7d9844'),
            pesoEstimado: 310.0,
            volumenEstimado: 2.1,
            destino: {
                calle: 'Belgrano',
                altura: '450',
                localidad: 'Quilmes'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        },
        {
            _id: UUID('d5555555-5555-4555-8555-555555555555'),
            idDonacionOriginal: UUID('35008064-071a-4d7a-ac37-33318f7d9845'),
            pesoEstimado: 175.0,
            volumenEstimado: 1.1,
            destino: {
                calle: 'Av. Centenario',
                altura: '1820',
                localidad: 'San Isidro'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        },
        {
            _id: UUID('d6666666-6666-4666-8666-666666666666'),
            idDonacionOriginal: UUID('36008064-071a-4d7a-ac37-33318f7d9846'),
            pesoEstimado: 95.0,
            volumenEstimado: 0.6,
            destino: {
                calle: 'Defensa',
                altura: '800',
                localidad: 'San Telmo, CABA'
            },
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
        }
    ]);
}

// 4. Colección: rutas_reparto (Agregados Raíz con diversos estados de ejecución)
if (db.rutas_reparto.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando 3 rutas de reparto (Pendiente, En Curso y Entregada)...');
    db.rutas_reparto.insertMany([
        // RUTA 1: Pendiente de inicio (Para iniciar hoy por chofer Roberto Gómez)
        {
            _id: UUID('d3b07384-d113-4a11-9a28-97193a0dc951'),
            fechaOperativa: new Date('2026-10-28T00:00:00Z'),
            iniciada: false,
            camionId: UUID('c1111111-1111-4111-8111-111111111111'),
            camionPatente: 'AA123BB',
            camionCapacidadVolumen: 20.0,
            camionAltura: 3.2,
            camionCapacidadCarga: 4000.0,
            choferId: UUID('f1111111-1111-4111-8111-111111111111'),
            choferLegajo: 'CH-101',
            choferNombre: 'Roberto Gómez',
            paradas: [
                {
                    orden: 1,
                    direccion: {
                        calle: 'Av. Corrientes',
                        altura: '1234',
                        localidad: 'CABA'
                    },
                    coordenada: {
                        latitud: -34.6037,
                        longitud: -58.3816
                    },
                    entregas: [
                        {
                            _id: UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
                            idEntrega: UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
                            estado: 'PENDIENTE',
                            pesoEstimado: 120.0,
                            volumenEstimado: 0.8,
                            comprobanteRecepcion: null
                        }
                    ]
                },
                {
                    orden: 2,
                    direccion: {
                        calle: 'Defensa',
                        altura: '800',
                        localidad: 'San Telmo, CABA'
                    },
                    coordenada: {
                        latitud: -34.6186,
                        longitud: -58.3712
                    },
                    entregas: [
                        {
                            _id: UUID('36008064-071a-4d7a-ac37-33318f7d9846'),
                            idEntrega: UUID('36008064-071a-4d7a-ac37-33318f7d9846'),
                            estado: 'PENDIENTE',
                            pesoEstimado: 95.0,
                            volumenEstimado: 0.6,
                            comprobanteRecepcion: null
                        }
                    ]
                }
            ],
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument'
        },

        // RUTA 2: En curso (Iniciada por Laura Fernández, con 1 parada entregada y 1 en traslado)
        {
            _id: UUID('e4c18495-e224-4b22-ab39-08204b1ed062'),
            fechaOperativa: new Date('2026-10-28T00:00:00Z'),
            iniciada: true,
            camionId: UUID('c2222222-2222-4222-8222-222222222222'),
            camionPatente: 'AF456CD',
            camionCapacidadVolumen: 12.5,
            camionAltura: 2.8,
            camionCapacidadCarga: 2500.0,
            choferId: UUID('f2222222-2222-4222-8222-222222222222'),
            choferLegajo: 'CH-102',
            choferNombre: 'Laura Fernández',
            paradas: [
                {
                    orden: 1,
                    direccion: {
                        calle: 'Av. Santa Fe',
                        altura: '3200',
                        localidad: 'Palermo, CABA'
                    },
                    coordenada: {
                        latitud: -34.5863,
                        longitud: -58.4103
                    },
                    entregas: [
                        {
                            _id: UUID('33008064-071a-4d7a-ac37-33318f7d9843'),
                            idEntrega: UUID('33008064-071a-4d7a-ac37-33318f7d9843'),
                            estado: 'ENTREGADA',
                            pesoEstimado: 85.0,
                            volumenEstimado: 0.5,
                            comprobanteRecepcion: {
                                fechaHora: new Date('2026-10-28T09:30:00Z'),
                                fotos: [
                                    'https://storage.donatrack.org/remitos/remito-3300.jpg',
                                    'https://storage.donatrack.org/fotos/caja-alimentos-1.jpg'
                                ],
                                camionPatente: 'AF456CD'
                            }
                        }
                    ]
                },
                {
                    orden: 2,
                    direccion: {
                        calle: 'Av. Centenario',
                        altura: '1820',
                        localidad: 'San Isidro'
                    },
                    coordenada: {
                        latitud: -34.4716,
                        longitud: -58.5284
                    },
                    entregas: [
                        {
                            _id: UUID('35008064-071a-4d7a-ac37-33318f7d9845'),
                            idEntrega: UUID('35008064-071a-4d7a-ac37-33318f7d9845'),
                            estado: 'EN_TRASLADO',
                            pesoEstimado: 175.0,
                            volumenEstimado: 1.1,
                            comprobanteRecepcion: null
                        }
                    ]
                }
            ],
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument'
        },

        // RUTA 3: Finalizada histórica (Carlos Atlas, entregada con remito en La Plata)
        {
            _id: UUID('f5d29506-f335-4c33-bc40-19315c2fe173'),
            fechaOperativa: new Date('2026-10-27T00:00:00Z'),
            iniciada: true,
            camionId: UUID('c3333333-3333-4333-8333-333333333333'),
            camionPatente: 'AC789EE',
            camionCapacidadVolumen: 30.0,
            camionAltura: 3.8,
            camionCapacidadCarga: 8000.0,
            choferId: UUID('f3333333-3333-4333-8333-333333333333'),
            choferLegajo: 'CH-103',
            choferNombre: 'Carlos Atlas',
            paradas: [
                {
                    orden: 1,
                    direccion: {
                        calle: 'Calle 50',
                        altura: '742',
                        localidad: 'La Plata'
                    },
                    coordenada: {
                        latitud: -34.9214,
                        longitud: -57.9545
                    },
                    entregas: [
                        {
                            _id: UUID('32008064-071a-4d7a-ac37-33318f7d9842'),
                            idEntrega: UUID('32008064-071a-4d7a-ac37-33318f7d9842'),
                            estado: 'ENTREGADA',
                            pesoEstimado: 250.0,
                            volumenEstimado: 1.5,
                            comprobanteRecepcion: {
                                fechaHora: new Date('2026-10-27T16:45:00Z'),
                                fotos: [
                                    'https://storage.donatrack.org/remitos/remito-lp-742.pdf',
                                    'https://storage.donatrack.org/fotos/descarga-fundacion.jpg'
                                ],
                                camionPatente: 'AC789EE'
                            }
                        }
                    ]
                }
            ],
            _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument'
        }
    ]);
}

print('>>> [MongoDB Init] Precarga de datos enriquecidos completada exitosamente.');
