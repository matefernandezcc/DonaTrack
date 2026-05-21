// Mock data for DonaTrack
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
    { fecha: '2025-05-21', donante: 'María González',  entidad: 'Fundación Esperanza',  tipo: 'Alimentos',     estado: 'Entregado'  },
    { fecha: '2025-05-21', donante: 'Carlos Pérez',    entidad: 'Hogar San José',       tipo: 'Medicamentos',  estado: 'En tránsito'},
    { fecha: '2025-05-20', donante: 'Lucía Fernández', entidad: 'Cruz Roja Local',      tipo: 'Ropa',          estado: 'Pendiente'  },
    { fecha: '2025-05-20', donante: 'Pedro Ramírez',   entidad: 'Comedor Comunitario',  tipo: 'Alimentos',     estado: 'Vencido'    },
    { fecha: '2025-05-19', donante: 'Ana Torres',      entidad: 'Fundación Niños Felices', tipo: 'Juguetes',  estado: 'Entregado'  },
    { fecha: '2025-05-19', donante: 'Diego Castro',    entidad: 'Refugio Animal',       tipo: 'Insumos',       estado: 'En tránsito'},
    { fecha: '2025-05-18', donante: 'Sofía Méndez',    entidad: 'Centro de Adultos',    tipo: 'Medicamentos',  estado: 'Entregado'  },
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
    { id: 'ENT-2041', truck: 'Camión #07', status: 'En tránsito', eta: 'ETA 14:30' },
    { id: 'ENT-2042', truck: 'Camión #03', status: 'En carga',    eta: 'Salida 15:00' },
    { id: 'ENT-2043', truck: 'Camión #11', status: 'Entregado',   eta: 'Hace 25 min' },
    { id: 'ENT-2044', truck: 'Camión #02', status: 'En tránsito', eta: 'ETA 16:10' },
  ],

  ranking: [
    { pos: 1, name: 'María González',  amount: '$ 184,200', medal: 'gold'   },
    { pos: 2, name: 'Carlos Pérez',    amount: '$ 156,800', medal: 'silver' },
    { pos: 3, name: 'Lucía Fernández', amount: '$ 132,450', medal: 'bronze' },
    { pos: 4, name: 'Ana Torres',      amount: '$ 98,300',  medal: null     },
    { pos: 5, name: 'Diego Castro',    amount: '$ 87,120',  medal: null     },
    { pos: 6, name: 'Sofía Méndez',    amount: '$ 75,640',  medal: null     },
  ],
};
