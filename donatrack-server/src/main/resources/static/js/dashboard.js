// DonaTrack dashboard logic
(function (global) {
  const SLDS_UTIL = 'https://cdn.jsdelivr.net/npm/@salesforce-ux/design-system@2.24.4/assets/icons/utility-sprite/svg/symbols.svg';

  const iconSvg = (name, cls = 'dt-nav__icon') =>
    `<svg class="${cls}" aria-hidden="true"><use xlink:href="${SLDS_UTIL}#${name}"></use></svg>`;

  const STATUS_MAP = {
    'Entregado':   { cls: 'dt-badge_success' },
    'En tránsito': { cls: 'dt-badge_info' },
    'Pendiente':   { cls: 'dt-badge_warning' },
    'Vencido':     { cls: 'dt-badge_danger' },
    'En carga':    { cls: 'dt-badge_info' },
  };

  function badge(text) {
    const m = STATUS_MAP[text] || { cls: '' };
    return `<span class="slds-badge ${m.cls}">${text}</span>`;
  }

  // ----- Sidebar -----
  function initSidebar() {
    const sidebar = document.getElementById('sidebar');
    const btn = document.getElementById('sidebarToggle');
    const isMobile = () => window.innerWidth <= 640;

    if (localStorage.getItem('dt_sidebar_collapsed') === '1' && !isMobile()) {
      sidebar.classList.add('is-collapsed');
    }

    btn.addEventListener('click', () => {
      if (isMobile()) {
        sidebar.classList.toggle('is-open');
      } else {
        sidebar.classList.toggle('is-collapsed');
        localStorage.setItem('dt_sidebar_collapsed', sidebar.classList.contains('is-collapsed') ? '1' : '0');
      }
    });

    // Inject icons into nav items
    document.querySelectorAll('.dt-nav__item').forEach((li) => {
      const icon = li.dataset.icon || 'apps';
      const a = li.querySelector('a');
      const label = a.textContent;
      a.innerHTML = `${iconSvg(icon, 'dt-nav__icon')}<span class="dt-nav__label">${label}</span>`;
    });

    // Avatar dropdown toggle
    const avatar = document.getElementById('avatarMenu');
    avatar.querySelector('button').addEventListener('click', (e) => {
      e.stopPropagation();
      avatar.classList.toggle('slds-is-open');
    });
    document.addEventListener('click', () => avatar.classList.remove('slds-is-open'));
  }

  // ----- KPI -----
  function renderKpis() {
    const grid = document.getElementById('kpiGrid');
    grid.innerHTML = window.DT_Data.kpis.map(k => `
      <div class="dt-kpi">
        <div class="dt-kpi__card">
          <div class="dt-kpi__icon">${iconSvg(k.icon, '')}</div>
          <div class="slds-grow">
            <div class="dt-kpi__label">${k.label}</div>
            <div class="dt-kpi__value">${k.value}</div>
            <div class="dt-kpi__meta">
              <span class="slds-badge dt-badge_${k.tone}">${k.delta}</span>
              <span>vs. mes anterior</span>
            </div>
          </div>
        </div>
      </div>
    `).join('');
  }

  // ----- Activity table -----
  function renderActivity() {
    const tbody = document.getElementById('activityBody');
    // skeleton
    tbody.innerHTML = Array.from({ length: 5 }).map(() => `
      <tr class="dt-skeleton-row">
        <td colspan="6"><span class="dt-skeleton" style="width: 100%;"></span></td>
      </tr>`).join('');

    setTimeout(() => {
      tbody.innerHTML = window.DT_Data.activity.map(row => `
        <tr>
          <td><div class="slds-truncate">${row.fecha}</div></td>
          <td><div class="slds-truncate">${row.donante}</div></td>
          <td><div class="slds-truncate">${row.entidad}</div></td>
          <td><div class="slds-truncate">${row.tipo}</div></td>
          <td>${badge(row.estado)}</td>
          <td>
            <button class="slds-button slds-button_icon slds-button_icon-border" title="Ver detalle">
              <svg class="slds-button__icon" aria-hidden="true"><use xlink:href="${SLDS_UTIL}#preview"></use></svg>
            </button>
            <button class="slds-button slds-button_icon slds-button_icon-border slds-m-left_xx-small" title="Editar">
              <svg class="slds-button__icon" aria-hidden="true"><use xlink:href="${SLDS_UTIL}#edit"></use></svg>
            </button>
          </td>
        </tr>
      `).join('');
    }, 500);
  }

  // ----- Quick actions -----
  function renderActions() {
    const wrap = document.getElementById('quickActions');
    wrap.innerHTML = window.DT_Data.actions.map(a => `
      <div class="dt-action">
        <button class="dt-action__card" data-modal="${a.id}">
          <div class="dt-action__icon">${iconSvg(a.icon, '')}</div>
          <div>
            <p class="dt-action__title">${a.title}</p>
            <p class="dt-action__desc">${a.desc}</p>
          </div>
        </button>
      </div>
    `).join('');

    document.body.addEventListener('click', (e) => {
      const t = e.target.closest('[data-modal]');
      if (t) openModal(t.dataset.modal);
    });
  }

  // ----- Deliveries -----
  function renderDeliveries() {
    const ul = document.getElementById('deliveriesList');
    const items = window.DT_Data.deliveries;
    if (!items.length) {
      ul.innerHTML = `<li class="dt-empty">No hay entregas activas en este momento.</li>`;
      return;
    }
    ul.innerHTML = items.map(d => `
      <li>
        <div>
          <div class="dt-delivery__id">${d.id}</div>
          <div class="dt-delivery__meta">${d.truck} · ${d.eta}</div>
        </div>
        ${badge(d.status)}
      </li>
    `).join('');
  }

  // ----- Ranking -----
  function renderRanking() {
    const tbody = document.getElementById('rankingBody');
    tbody.innerHTML = window.DT_Data.ranking.map(r => {
      const medal = r.medal
        ? `<span class="dt-medal dt-medal_${r.medal}">${r.pos}</span>`
        : `<span class="slds-text-color_weak">#${r.pos}</span>`;
      const insignias = r.medal
        ? `<span class="slds-badge dt-badge_${r.medal === 'gold' ? 'warning' : 'info'}">${r.medal.toUpperCase()}</span>`
        : `<span class="slds-text-color_weak slds-text-body_small">—</span>`;
      return `
        <tr>
          <td>${medal}</td>
          <td>${r.name}</td>
          <td>${r.amount}</td>
          <td>${insignias}</td>
        </tr>
      `;
    }).join('');
  }

  // ----- CSV dropzone -----
  function initDropzone() {
    const zone = document.getElementById('dropzone');
    const input = document.getElementById('csvFile');
    const status = document.getElementById('csvStatus');

    function handleFile(file) {
      status.hidden = false;
      if (!file.name.toLowerCase().endsWith('.csv')) {
        status.innerHTML = `<div class="slds-notify slds-notify_alert slds-theme_error" role="alert">
          <h2>Archivo no válido. Solo se aceptan archivos .csv</h2></div>`;
        return;
      }
      const sizeKb = (file.size / 1024).toFixed(1);
      status.innerHTML = `
        <div class="slds-text-body_small slds-m-bottom_x-small"><strong>${file.name}</strong> · ${sizeKb} KB</div>
        <div class="slds-progress-bar slds-progress-bar_circular" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0" role="progressbar">
          <span id="csvProgress" class="slds-progress-bar__value" style="width:0%"><span class="slds-assistive-text">0%</span></span>
        </div>
        <div id="csvMsg" class="slds-text-body_small slds-m-top_x-small slds-text-color_weak">Procesando...</div>
      `;
      let pct = 0;
      const bar = document.getElementById('csvProgress');
      const msg = document.getElementById('csvMsg');
      const t = setInterval(() => {
        pct += 10;
        bar.style.width = pct + '%';
        if (pct >= 100) {
          clearInterval(t);
          msg.outerHTML = `<div class="slds-notify slds-notify_alert slds-theme_success slds-m-top_small" role="status">
            <h2>Importación completada · 248 donantes procesados</h2></div>`;
        }
      }, 120);
    }

    zone.addEventListener('dragover', (e) => { e.preventDefault(); zone.classList.add('is-dragover'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('is-dragover'));
    zone.addEventListener('drop', (e) => {
      e.preventDefault();
      zone.classList.remove('is-dragover');
      if (e.dataTransfer.files[0]) handleFile(e.dataTransfer.files[0]);
    });
    input.addEventListener('change', (e) => { if (e.target.files[0]) handleFile(e.target.files[0]); });
  }

  // ----- Modals -----
  const MODAL_CONTENT = {
    'modal-presencial': {
      title: 'Registrar persona donante presencial',
      body: `
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Nombre completo</label>
          <div class="slds-form-element__control"><input class="slds-input" type="text" placeholder="Ej: Juan Pérez"></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Documento</label>
          <div class="slds-form-element__control"><input class="slds-input" type="text"></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Email</label>
          <div class="slds-form-element__control"><input class="slds-input" type="email"></div></div>
        <div class="slds-form-element"><label class="slds-form-element__label">Teléfono</label>
          <div class="slds-form-element__control"><input class="slds-input" type="tel"></div></div>
      `,
    },
    'modal-recepcion': {
      title: 'Registrar recepción de donación',
      body: `
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Donante</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>María González</option><option>Carlos Pérez</option></select></div></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Tipo de donación</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>Alimentos</option><option>Medicamentos</option><option>Ropa</option></select></div></div></div>
        <div class="slds-form-element"><label class="slds-form-element__label">Cantidad</label>
          <div class="slds-form-element__control"><input class="slds-input" type="number" min="1"></div></div>
      `,
    },
    'modal-vencidas': {
      title: 'Actualizar donaciones vencidas',
      body: `
        <p class="slds-text-body_regular slds-m-bottom_small">Selecciona las donaciones a marcar como vencidas:</p>
        <table class="slds-table slds-table_bordered slds-table_cell-buffer">
          <thead><tr><th></th><th>ID</th><th>Tipo</th><th>Vencimiento</th></tr></thead>
          <tbody>
            <tr><td><input type="checkbox" class="slds-checkbox"></td><td>DON-1021</td><td>Alimentos</td><td>2025-05-15</td></tr>
            <tr><td><input type="checkbox" class="slds-checkbox"></td><td>DON-1022</td><td>Medicamentos</td><td>2025-05-18</td></tr>
            <tr><td><input type="checkbox" class="slds-checkbox"></td><td>DON-1024</td><td>Alimentos</td><td>2025-05-20</td></tr>
          </tbody>
        </table>
      `,
    },
    'modal-asignar': {
      title: 'Asignar donación a entidad final',
      body: `
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Donación</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>DON-1030 · Alimentos</option><option>DON-1031 · Ropa</option></select></div></div></div>
        <div class="slds-form-element"><label class="slds-form-element__label">Entidad beneficiaria</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>Fundación Esperanza</option><option>Hogar San José</option><option>Cruz Roja Local</option></select></div></div></div>
      `,
    },
    'modal-camiones': {
      title: 'Administrar camiones',
      body: `
        <table class="slds-table slds-table_bordered slds-table_cell-buffer">
          <thead><tr><th>Camión</th><th>Patente</th><th>Capacidad</th><th>Estado</th></tr></thead>
          <tbody>
            <tr><td>Camión #02</td><td>AB-123-CD</td><td>2.5 ton</td><td><span class="slds-badge dt-badge_info">En tránsito</span></td></tr>
            <tr><td>Camión #03</td><td>EF-456-GH</td><td>4 ton</td><td><span class="slds-badge dt-badge_success">Disponible</span></td></tr>
            <tr><td>Camión #07</td><td>IJ-789-KL</td><td>3 ton</td><td><span class="slds-badge dt-badge_warning">Mantenimiento</span></td></tr>
          </tbody>
        </table>
      `,
    },
    'modal-ranking': {
      title: 'Ranking mensual',
      body: `<p class="slds-text-body_regular">Consulta la sección "Ranking mensual de donantes" del panel principal para ver el top completo del mes.</p>`,
    },
    'modal-csv': {
      title: 'Importar donantes (CSV)',
      body: `<p class="slds-text-body_regular">Utiliza el área de importación masiva al final del panel para arrastrar tu archivo .csv.</p>`,
    },
  };

  function openModal(id) {
    const data = MODAL_CONTENT[id];
    if (!data) return;
    const host = document.getElementById('modalHost');
    host.innerHTML = `
      <section role="dialog" tabindex="-1" aria-modal="true" class="slds-modal slds-fade-in-open slds-modal_medium">
        <div class="slds-modal__container">
          <header class="slds-modal__header">
            <button class="slds-button slds-button_icon slds-modal__close slds-button_icon-inverse" data-close>
              <svg class="slds-button__icon slds-button__icon_large" aria-hidden="true"><use xlink:href="${SLDS_UTIL}#close"></use></svg>
              <span class="slds-assistive-text">Cerrar</span>
            </button>
            <h2 class="slds-modal__title slds-hyphenate">${data.title}</h2>
          </header>
          <div class="slds-modal__content slds-p-around_medium">${data.body}</div>
          <footer class="slds-modal__footer">
            <button class="slds-button slds-button_neutral" data-close>Cancelar</button>
            <button class="slds-button slds-button_brand" data-close>Guardar</button>
          </footer>
        </div>
      </section>
      <div class="slds-backdrop slds-backdrop_open"></div>
    `;
    host.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => host.innerHTML = ''));
    host.querySelector('.slds-backdrop').addEventListener('click', () => host.innerHTML = '');
  }

  function init() {
    initSidebar();
    renderKpis();
    renderActivity();
    renderActions();
    renderDeliveries();
    renderRanking();
    initDropzone();
  }

  global.DT_App = { init };
})(window);
