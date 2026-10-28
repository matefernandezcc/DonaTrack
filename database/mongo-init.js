// ============================================================================
// DonaTrack - Script de Inicialización y Precarga de Datos para MongoDB
// Base de datos: donatrack_logistica
// ============================================================================

db = db.getSiblingDB('donatrack_logistica');

print('>>> [MongoDB Init] Iniciando precarga de datos en donatrack_logistica...');

// 1. Colección: camiones
if (db.camiones.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando camiones...');
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
        }
    ]);
}

// 2. Colección: choferes
if (db.choferes.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando choferes...');
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
        }
    ]);
    db.choferes.createIndex({ legajo: 1 }, { unique: true });
}

// 3. Colección: items_planificacion
if (db.items_planificacion.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando items de planificación pendientes...');
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
        }
    ]);
}

// 4. Colección: rutas_reparto (Agregado Raíz)
if (db.rutas_reparto.countDocuments() === 0) {
    print('>>> [MongoDB Init] Precargando ruta de reparto de prueba...');
    db.rutas_reparto.insertOne({
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
                        idEntrega: UUID('31008064-071a-4d7a-ac37-33318f7d9841'),
                        estado: 'PENDIENTE',
                        pesoEstimado: 120.0,
                        volumenEstimado: 0.8,
                        comprobanteRecepcion: null
                    }
                ]
            }
        ],
        _class: 'com.donatrack.logistica.infrastructure.adapters.out.persistence.documents.RutaDeRepartoDocument'
    });
    db.rutas_reparto.createIndex({ fechaOperativa: 1 });
    db.rutas_reparto.createIndex({ choferId: 1 });
    db.rutas_reparto.createIndex({ 'paradas.entregas.idEntrega': 1 });
}

print('>>> [MongoDB Init] Precarga de datos completada exitosamente.');
