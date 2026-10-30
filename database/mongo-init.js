// ============================================================================
// DonaTrack - Script de Inicialización y Precarga de Datos para MongoDB
// Base de datos: donatrack_logistica
// Modelado para Spring Data MongoDB (ODM) con soporte para las 6 colecciones
// ============================================================================

db = db.getSiblingDB('donatrack_logistica');

print('>>> [MongoDB Init] Iniciando sincronización y precarga en donatrack_logistica...');

function upsertCollection(collName, docs) {
    print(`>>> [MongoDB Init] Procesando ${docs.length} documentos en colección: ${collName}...`);
    let count = 0;
    docs.forEach(doc => {
        db.getCollection(collName).replaceOne({ _id: doc._id }, doc, { upsert: true });
        count++;
    });
    print(`>>> [MongoDB Init] Sincronizados ${count} documentos en ${collName}. Total actual: ${db.getCollection(collName).countDocuments()}`);
}

// ----------------------------------------------------------------------------
// 1. Colección: camiones (Flota de transporte)
// ----------------------------------------------------------------------------
const camiones = [
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
    },
    {
        _id: UUID('c7777777-7777-4777-8777-777777777777'),
        patente: 'AH890QW',
        capacidadVolumen: 45.0,
        altura: 4.0,
        capacidadCarga: 12000.0,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
    },
    {
        _id: UUID('c8888888-8888-4888-8888-888888888888'),
        patente: 'AI432MN',
        capacidadVolumen: 8.0,
        altura: 2.2,
        capacidadCarga: 1200.0,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.CamionDocument'
    }
];
upsertCollection('camiones', camiones);

// ----------------------------------------------------------------------------
// 2. Colección: choferes (Personal de conducción)
// ----------------------------------------------------------------------------
const choferes = [
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
    },
    {
        _id: UUID('f7777777-7777-4777-8777-777777777777'),
        legajo: 'CH-107',
        nombre: 'Lucía Herrera',
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
    },
    {
        _id: UUID('f8888888-8888-4888-8888-888888888888'),
        legajo: 'CH-108',
        nombre: 'Mariano Di Pietro',
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ChoferDocument'
    }
];
upsertCollection('choferes', choferes);

// ----------------------------------------------------------------------------
// 3. Colección: items_planificacion (Donaciones para agrupar y rutear)
// ----------------------------------------------------------------------------
const itemsPlanificacion = [
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
    },
    {
        _id: UUID('d7777777-7777-4777-8777-777777777777'),
        idDonacionOriginal: UUID('37008064-071a-4d7a-ac37-33318f7d9847'),
        pesoEstimado: 420.0,
        volumenEstimado: 3.0,
        destino: {
            calle: 'Av. Rivadavia',
            altura: '8500',
            localidad: 'Flores, CABA'
        },
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
    },
    {
        _id: UUID('d8888888-8888-4888-8888-888888888888'),
        idDonacionOriginal: UUID('38008064-071a-4d7a-ac37-33318f7d9848'),
        pesoEstimado: 60.0,
        volumenEstimado: 0.4,
        destino: {
            calle: 'Calle 12',
            altura: '1430',
            localidad: 'Berazategui'
        },
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.ItemPlanificacionDocument'
    }
];
upsertCollection('items_planificacion', itemsPlanificacion);

// ----------------------------------------------------------------------------
// 4. Colección: entregas (Tracking individual de entregas con remitos)
// ----------------------------------------------------------------------------
const entregas = [
    {
        _id: UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
        estado: 'PENDIENTE',
        pesoEstimado: 120.0,
        volumenEstimado: 0.8,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('32008064-071a-4d7a-ac37-33318f7d9842'),
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
        },
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('33008064-071a-4d7a-ac37-33318f7d9843'),
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
        },
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('34008064-071a-4d7a-ac37-33318f7d9844'),
        estado: 'PENDIENTE',
        pesoEstimado: 310.0,
        volumenEstimado: 2.1,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('35008064-071a-4d7a-ac37-33318f7d9845'),
        estado: 'EN_TRASLADO',
        pesoEstimado: 175.0,
        volumenEstimado: 1.1,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('36008064-071a-4d7a-ac37-33318f7d9846'),
        estado: 'PENDIENTE',
        pesoEstimado: 95.0,
        volumenEstimado: 0.6,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('37008064-071a-4d7a-ac37-33318f7d9847'),
        estado: 'NO_RECIBIDA',
        pesoEstimado: 420.0,
        volumenEstimado: 3.0,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    },
    {
        _id: UUID('139e0f9f-9431-4302-9f06-cad97978f7be'),
        estado: 'EN_TRASLADO',
        pesoEstimado: 50.0,
        volumenEstimado: 0.4,
        comprobanteRecepcion: null,
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.EntregaDocument'
    }
];
upsertCollection('entregas', entregas);

// ----------------------------------------------------------------------------
// 5. Colección: solicitudes_planificacion (Agrupamiento por lote para optimizador)
// ----------------------------------------------------------------------------
const solicitudesPlanificacion = [
    {
        _id: UUID('b1111111-1111-4111-8111-111111111111'),
        fechaSolicitud: new Date('2026-10-26T08:00:00Z'),
        estado: 'PROCESADA',
        idsDonaciones: [
            UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
            UUID('36008064-071a-4d7a-ac37-33318f7d9846')
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument'
    },
    {
        _id: UUID('b2222222-2222-4222-8222-222222222222'),
        fechaSolicitud: new Date('2026-10-27T08:30:00Z'),
        estado: 'PROCESADA',
        idsDonaciones: [
            UUID('32008064-071a-4d7a-ac37-33318f7d9842')
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument'
    },
    {
        _id: UUID('b3333333-3333-4333-8333-333333333333'),
        fechaSolicitud: new Date('2026-10-28T09:00:00Z'),
        estado: 'RECIBIDA',
        idsDonaciones: [
            UUID('33008064-071a-4d7a-ac37-33318f7d9843'),
            UUID('35008064-071a-4d7a-ac37-33318f7d9845')
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument'
    },
    {
        _id: UUID('b4444444-4444-4444-8444-444444444444'),
        fechaSolicitud: new Date('2026-10-29T10:15:00Z'),
        estado: 'PENDIENTE',
        idsDonaciones: [
            UUID('34008064-071a-4d7a-ac37-33318f7d9844'),
            UUID('37008064-071a-4d7a-ac37-33318f7d9847')
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument'
    },
    {
        _id: UUID('b5555555-5555-4555-8555-555555555555'),
        fechaSolicitud: new Date('2026-10-29T11:45:00Z'),
        estado: 'PENDIENTE',
        idsDonaciones: [
            UUID('38008064-071a-4d7a-ac37-33318f7d9848'),
            UUID('139e0f9f-9431-4302-9f06-cad97978f7be')
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.SolicitudPlanificacionDocument'
    }
];
upsertCollection('solicitudes_planificacion', solicitudesPlanificacion);

// ----------------------------------------------------------------------------
// 6. Colección: rutas_reparto (Agregados Raíz con diversos estados de ejecución)
// ----------------------------------------------------------------------------
const rutasReparto = [
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
    },

    // RUTA 4: Ruta Operativa Activa para Pruebas de API y Casos de Uso
    {
        _id: UUID('a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
        fechaOperativa: new Date('2026-10-29T00:00:00Z'),
        iniciada: false,
        camionId: UUID('c5555555-5555-4555-8555-555555555555'),
        camionPatente: 'AE304KL',
        camionCapacidadVolumen: 25.0,
        camionAltura: 3.5,
        camionCapacidadCarga: 5500.0,
        choferId: UUID('f5555555-5555-4555-8555-555555555555'),
        choferLegajo: 'CH-105',
        choferNombre: 'Sofía Morales',
        paradas: [
            {
                orden: 1,
                direccion: {
                    calle: 'Av. del Libertador',
                    altura: '1000',
                    localidad: 'Vicente López'
                },
                coordenada: {
                    latitud: -34.5298,
                    longitud: -58.4732
                },
                entregas: [
                    {
                        _id: UUID('139e0f9f-9431-4302-9f06-cad97978f7be'),
                        idEntrega: UUID('139e0f9f-9431-4302-9f06-cad97978f7be'),
                        estado: 'EN_TRASLADO',
                        pesoEstimado: 50.0,
                        volumenEstimado: 0.4,
                        comprobanteRecepcion: null
                    }
                ]
            }
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument'
    }
];
upsertCollection('rutas_reparto', rutasReparto);

print('>>> [MongoDB Init] Sincronización y precarga completada exitosamente en las 6 colecciones.');
