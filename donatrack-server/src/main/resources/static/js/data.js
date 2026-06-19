// Unified mock database for DonaTrack (Admin, Donante, and Beneficiaria views)
window.DT_Data = {
  kpis: [
    { label: 'Total donaciones', value: '12,847', icon: 'moneybag', delta: '+8.2%', tone: 'success' },
    { label: 'Donaciones entregadas', value: '9,431', icon: 'package', delta: '+5.1%', tone: 'success' },
    { label: 'Entidades activas', value: '184', icon: 'company', delta: '+3', tone: 'info' },
    { label: 'Camiones disponibles', value: '27', icon: 'truck', delta: '-2', tone: 'warning' },
    { label: 'Donantes registrados', value: '6,205', icon: 'people', delta: '+142', tone: 'success' },
    { label: 'Entregas en tránsito', value: '38', icon: 'route', delta: 'Activo', tone: 'info' },
  ],

  activity: [
    { id: 'DON-1040', fecha: '2026-06-18', donante: 'María González',  entidad: 'Fundación Esperanza',  tipo: 'Alimentos',     estado: 'Entregado'  },
    { id: 'DON-1035', fecha: '2026-06-18', donante: 'María González',  entidad: 'Hogar Caminos',        tipo: 'Vestimenta',    estado: 'En tránsito'},
    { id: 'DON-1034', fecha: '2026-06-17', donante: 'Carlos Pérez',    entidad: 'Hogar San José',       tipo: 'Medicamentos',  estado: 'En tránsito'},
    { id: 'DON-1033', fecha: '2026-06-16', donante: 'Lucía Fernández', entidad: 'Cruz Roja Local',      tipo: 'Ropa',          estado: 'Pendiente'  },
    { id: 'DON-1032', fecha: '2026-06-15', donante: 'Pedro Ramírez',   entidad: 'Comedor Comunitario',  tipo: 'Alimentos',     estado: 'Vencido'    },
    { id: 'DON-1031', fecha: '2026-06-14', donante: 'Ana Torres',      entidad: 'Fundación Niños Felices', tipo: 'Juguetes',  estado: 'Entregado'  },
  ],

  actions: [
    { id: 'modal-presencial',  title: 'Registrar donante presencial',  desc: 'Alta rápida de persona donante en sede.', icon: 'add_contact' },
    { id: 'modal-recepcion',   title: 'Registrar recepción',           desc: 'Ingresa una nueva donación recibida.',     icon: 'package' },
    { id: 'modal-vencidas',    title: 'Actualizar vencidas',           desc: 'Marca donaciones como vencidas.',          icon: 'warning' },
    { id: 'modal-asignar',     title: 'Asignar a entidad',             desc: 'Asigna donaciones a entidades finales.',   icon: 'company' },
    { id: 'modal-camiones',    title: 'Administrar camiones',          desc: 'Gestiona la flota de transporte.',         icon: 'truck' },
    { id: 'modal-ranking',     title: 'Ver ranking mensual',           desc: 'Top donantes del mes en curso.',           icon: 'ranking' },
    { id: 'modal-csv',         title: 'Importar donantes (CSV)',       desc: 'Carga masiva desde archivo CSV.',          icon: 'upload' },
  ],

  deliveries: [
    { id: 'ENT-2041', truck: 'Camión #07', status: 'En tránsito', eta: 'ETA 14:30', coords: [-34.615, -58.425] },
    { id: 'ENT-2042', truck: 'Camión #03', status: 'En carga',    eta: 'Salida 15:00', coords: [-34.603, -58.381] },
    { id: 'ENT-2043', truck: 'Camión #11', status: 'Entregado',   eta: 'Hace 25 min',  coords: [-34.645, -58.450] },
    { id: 'ENT-2044', truck: 'Camión #02', status: 'En tránsito', eta: 'ETA 16:10', coords: [-34.620, -58.460] },
  ],

  // Ranking strictly by number of donations (non-monetary)
  ranking: [
    { pos: 1, name: 'María González',  amount: '38 donaciones', medal: 'gold'   },
    { pos: 2, name: 'Carlos Pérez',    amount: '29 donaciones', medal: 'silver' },
    { pos: 3, name: 'Lucía Fernández', amount: '25 donaciones', medal: 'bronze' },
    { pos: 4, name: 'Ana Torres',      amount: '18 donaciones',  medal: null     },
    { pos: 5, name: 'Diego Castro',    amount: '14 donaciones',  medal: null     },
    { pos: 6, name: 'Sofía Méndez',    amount: '12 donaciones',  medal: null     },
  ],

  // ----------------------------------------------------
  // DONANTE SECTION (María González - User: donante)
  // ----------------------------------------------------
  donanteProfile: {
    level: 'Colaborador',
    levelNumber: 1,
    points: 8400,
    nextLevelPoints: 10000,
    badgesCount: 4,
    donationsCount: 19,
    pointsProgress: 84, // (8400 / 10000) * 100
    unlockedBadges: [
      { title: 'Primer Paso', desc: 'Registraste tu primera donación en el sistema.', icon: 'check', date: '12/03/2026', color: 'orange' },
      { title: 'Racha Activa', desc: 'Realizaste donaciones durante 3 meses consecutivos.', icon: 'custom_apps', date: '15/04/2026', color: 'blue' },
      { title: 'Hábil Donador', desc: 'Entregaste una donación con más de 100 bienes materiales.', icon: 'magicwand', date: '02/05/2026', color: 'green' },
      { title: 'Corazón de Oro', desc: 'Tus donaciones ayudaron a 5 o más entidades beneficiarias.', icon: 'favorite', date: '18/05/2026', color: 'purple' },
    ],
    misiones: [
      { name: 'Racha Consecutiva', desc: 'Realizar una donación durante 3 meses seguidos.', progress: '3/3 meses', status: 'Completada', isComplete: true },
      { name: 'Donante Especializado', desc: 'Realizar donaciones que abarquen al menos 3 categorías distintas.', progress: '3/3 categorías', status: 'Completada', isComplete: true },
      { name: 'Impacto Comprobado', desc: 'Lograr que 5 donaciones sean recibidas exitosamente por las ONGs.', progress: '4/5 entregas', status: 'En progreso', isComplete: false },
      { name: 'Hábil Donador Nivel II', desc: 'Realizar una carga de donaciones que supere los 150 productos.', progress: '80/150 productos', status: 'En progreso', isComplete: false, lossCondition: 'Si no donas en los próximos 30 días, el contador parcial se reducirá un 10%.' }
    ],
    myDonations: [
      { id: 'DON-1040', category: 'Alimentos', subcategory: 'Fideos secos', quantity: '50 unidades', status: 'Entregado', date: '2026-06-18', destination: 'Fundación Esperanza', path: 'Entregado en depósito' },
      { id: 'DON-1035', category: 'Vestimenta', subcategory: 'Camperas de abrigo', quantity: '20 unidades', status: 'En tránsito', date: '2026-06-18', destination: 'Hogar Caminos', truck: 'Camión #07', trackingId: 'ENT-2041' },
      { id: 'DON-1021', category: 'Alimentos', subcategory: 'Leche en polvo', quantity: '30 kg', status: 'Entregado', date: '2026-05-21', destination: 'Fundación Esperanza' },
      { id: 'DON-1010', category: 'Mobiliario', subcategory: 'Sillas de madera', quantity: '6 unidades', status: 'Entregado', date: '2026-05-10', destination: 'Comedor La Esperanza' },
    ]
  },

  // ----------------------------------------------------
  // BENEFICIARIA SECTION (Fundación Esperanza - User: beneficiaria)
  // ----------------------------------------------------
  beneficiaryProfile: {
    orgName: 'Fundación Esperanza',
    representative: 'Sofía R.',
    phone: '+54 11 5555-0199',
    address: 'Av. Corrientes 4500, CABA',
    needs: [
      { id: 'NEED-201', category: 'Alimentos', subcategory: 'Fideos secos', quantity: '100 kg', type: 'Recurrente', desc: 'Consumo habitual para el comedor comunitario diario.', progress: '40 kg recibidos' },
      { id: 'NEED-202', category: 'Vestimenta', subcategory: 'Camperas de abrigo infantil', quantity: '40 unidades', type: 'Extraordinaria', desc: 'Para niños de 3 a 12 años ante la ola de frío extremo.', progress: '12 unidades recibidas' },
      { id: 'NEED-203', category: 'Mobiliario', subcategory: 'Bancos escolares', quantity: '15 unidades', type: 'Extraordinaria', desc: 'Reponer bancos dañados por filtración de agua.', progress: '0 recibidos' }
    ],
    assignedDonations: [
      { id: 'DON-1035', donor: 'María González', category: 'Vestimenta', subcategory: 'Camperas de abrigo', quantity: '20 unidades', status: 'En tránsito', eta: 'Hoy 16:30', trackingId: 'ENT-2041', date: '2026-06-18' },
      { id: 'DON-1042', donor: 'Carlos Pérez', category: 'Alimentos', subcategory: 'Arroz', quantity: '80 kg', status: 'Asignado', eta: 'Planificando ruta', trackingId: 'ENT-2045', date: '2026-06-19' },
      { id: 'DON-1021', donor: 'María González', category: 'Alimentos', subcategory: 'Leche en polvo', quantity: '30 kg', status: 'Entregado', eta: 'Entregado el 2026-05-21', date: '2026-05-21', comment: 'Llegó en perfecto estado, muy amables los del camión.', photoUrl: 'https://images.unsplash.com/photo-1594708767771-a7502209ff51?q=80&w=300' }
    ]
  },

  // ----------------------------------------------------
  // ALGORITHMS & MATCHMAKING
  // ----------------------------------------------------
  matchmaker: {
    pendingDonations: [
      { id: 'DON-1033', category: 'Ropa', subcategory: 'Remeras', quantity: '50 unidades', status: 'En depósito' },
      { id: 'DON-1045', category: 'Alimentos', subcategory: 'Arroz', quantity: '100 kg', status: 'En depósito' }
    ],
    algorithms: [
      { id: 'algo-semantic', name: 'Algoritmo de Compatibilidad Semántica', desc: 'Compara semánticamente la donación con las necesidades declaradas por las ONGs y prioriza coincidencias exactas.' },
      { id: 'algo-priority', name: 'Algoritmo de Prioridad a Sub-atendidos', desc: 'Evalúa la cantidad de donaciones que recibió cada ONG en el último trimestre y da prioridad a las que menos recibieron.' }
    ],
    results: {
      'DON-1033': [
        { org: 'Cruz Roja Local', score: '95%', reason: 'Tiene necesidad extraordinaria urgente de abrigo/ropa.', recommendation: 'Ambos algoritmos' },
        { org: 'Comedor La Esperanza', score: '82%', reason: 'Tiene necesidad recurrente de indumentaria.', recommendation: 'Compatibilidad Semántica' },
        { org: 'Hogar Caminos', score: '78%', reason: 'No recibió donaciones en los últimos 45 días.', recommendation: 'Prioridad a Sub-atendidos' }
      ],
      'DON-1045': [
        { org: 'Fundación Esperanza', score: '98%', reason: 'Tiene una necesidad activa de fideos secos/arroz.', recommendation: 'Ambos algoritmos' },
        { org: 'Comedor Comunitario', score: '88%', reason: 'Tiene alta demanda de insumos de cocina.', recommendation: 'Compatibilidad Semántica' }
      ]
    }
  },

  // ----------------------------------------------------
  // SYSTEM NOTIFICATIONS BY ROLE
  // ----------------------------------------------------
  notifications: {
    admin: [
      { text: 'Importación masiva de donantes (CSV) completada con éxito: 12,410 registros procesados.', time: 'Hace 30 min', unread: true },
      { text: 'El camión #07 reportó una parada por congestión de tráfico.', time: 'Hace 1 hora', unread: true },
      { text: 'Matchmaking ejecutado de forma asíncrona: 4 donaciones pendientes de asignación.', time: 'Ayer', unread: false }
    ],
    donante: [
      { text: '¡Felicidades! Desbloqueaste la insignia "Corazón de Oro" por ayudar a 5 ONGs.', time: 'Hace 10 min', unread: true },
      { text: 'Tu donación DON-1040 de fideos secos fue entregada con éxito a Fundación Esperanza.', time: 'Hace 2 horas', unread: true },
      { text: '¡Increíble! Subiste de rango a Sostenedor (Nivel 2). Revisa tus nuevas misiones.', time: 'Hace 1 día', unread: false }
    ],
    beneficiaria: [
      { text: 'La donación DON-1035 de camperas está en camino en el Camión #07.', time: 'Hace 5 min', unread: true },
      { text: 'Una nueva donación (80 kg de Arroz) te ha sido asignada. Esperando planificación de ruta.', time: 'Hace 1 hora', unread: true },
      { text: 'Entrega de Leche en polvo confirmada de forma exitosa.', time: 'Hace 2 semanas', unread: false }
    ]
  }
};
