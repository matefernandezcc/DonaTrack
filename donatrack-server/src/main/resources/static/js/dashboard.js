// DonaTrack Dashboard Controller (Supports Admin, Donante, and Beneficiaria roles)
(function (global) {
  "use strict";

  const SLDS_UTIL = '/assets/slds-icons/utility-sprite/svg/symbols.svg';
  const SLDS_STD = '/assets/slds-icons/utility-sprite/svg/symbols.svg';

  // Helper to generate SVG icon HTML
  function iconSvg(sprite, name, cls = 'slds-icon_small') {
    const ICON_MAPPING = {
      'dashboard': 'home',
      'partners': 'people',
      'ranking': 'trail',
      'coaching': 'user',
      'lead_import': 'upload',
      'delivery_truck': 'truck',
      'payment_gateway': 'moneybag',
      'relationship': 'people',
      'lead': 'lead',
      'orders': 'orders',
      'route': 'location',
      'add_contact': 'adduser'
    };
    const mappedName = ICON_MAPPING[name] || name;
    return `<svg class="slds-icon ${cls}" aria-hidden="true" style="fill: currentColor;"><use xlink:href="${SLDS_UTIL}#${mappedName}"></use></svg>`;
  }

  // Status-to-Badge styling map
  const STATUS_MAP = {
    'Entregado': { cls: 'slds-badge slds-theme_success' },
    'Entregada': { cls: 'slds-badge slds-theme_success' },
    'En tránsito': { cls: 'slds-badge slds-badge_inverse' },
    'Asignada': { cls: 'slds-badge slds-badge_inverse' },
    'Asignado': { cls: 'slds-badge slds-badge_inverse' },
    'Asignación realizada': { cls: 'slds-badge slds-badge_inverse' },
    'Pendiente': { cls: 'slds-badge slds-theme_warning' },
    'En depósito': { cls: 'slds-badge slds-theme_warning' },
    'Vencido': { cls: 'slds-badge slds-theme_error' },
    'En carga': { cls: 'slds-badge slds-badge_inverse' },
  };

  function badge(text) {
    const m = STATUS_MAP[text] || { cls: 'slds-badge' };
    return `<span class="${m.cls}">${text}</span>`;
  }

  const SUBCATEGORIES = {
    'Alimentos': ['Fideos secos', 'Arroz', 'Leche en polvo', 'Aceite vegetal', 'Tomate en lata'],
    'Vestimenta': ['Camperas de abrigo', 'Remeras de algodón', 'Pantalones', 'Ropa infantil', 'Frazadas'],
    'Mobiliario': ['Sillas de madera', 'Mesas rectangulares', 'Bancos escolares', 'Camas de una plaza']
  };

  function populateSubcategories(category, subSelectEl) {
    const list = SUBCATEGORIES[category] || [];
    subSelectEl.innerHTML = list.map(sub => `<option value="${sub}">${sub}</option>`).join('');
  }

  function renderSldsComboboxHtml(id, label, defaultVal, sizeClass = '') {
    return `
      <div class="slds-form-element">
        <label class="slds-form-element__label" id="combo-label-${id}">${label}</label>
        <div class="slds-form-element__control">
          <div class="slds-combobox_container ${sizeClass}">
            <div class="slds-combobox slds-dropdown-trigger slds-dropdown-trigger_click" id="combo-${id}">
              <div class="slds-combobox__form-element slds-input-has-icon slds-input-has-icon_right" role="none">
                <div role="combobox" tabindex="0" class="slds-input_faux slds-combobox__input slds-combobox__input-value" id="combo-faux-${id}" aria-haspopup="listbox" style="border: 1px solid #dddbda; border-radius: 4px; padding: 6px 12px; background: white; cursor: pointer; display: flex; align-items: center; min-height: 32px;">
                  <span class="slds-truncate" id="combo-val-${id}" data-value="${defaultVal}">${defaultVal}</span>
                </div>
                <span class="slds-icon_container slds-icon-utility-down slds-input__icon slds-input__icon_right" style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); pointer-events: none;">
                  <span style="font-size: 10px; color: #706e6b;">▼</span>
                </span>
              </div>
              <div class="slds-dropdown slds-dropdown_fluid" role="listbox" style="max-height: 200px; overflow-y: auto; z-index: 5000;">
                <ul class="slds-listbox slds-listbox_vertical" role="presentation" id="combo-list-${id}">
                  <!-- Options populated dynamically -->
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  function bindSldsCombobox(id, options, initialVal, onChange) {
    const comboEl = document.getElementById(`combo-${id}`);
    const fauxEl = document.getElementById(`combo-faux-${id}`);
    const valEl = document.getElementById(`combo-val-${id}`);
    const listEl = document.getElementById(`combo-list-${id}`);
    
    if (!comboEl || !fauxEl || !valEl || !listEl) return;

    // Set initial value
    valEl.textContent = initialVal;
    valEl.setAttribute('data-value', initialVal);

    // Populate list
    const renderOptions = () => {
      const currentVal = valEl.getAttribute('data-value');
      listEl.innerHTML = options.map(opt => {
        const isSelected = opt === currentVal;
        return `
          <li role="presentation" class="slds-listbox__item">
            <div class="slds-media slds-listbox__option slds-listbox__option_plain slds-media_small ${isSelected ? 'slds-is-selected' : ''}" role="option" data-value="${opt}" style="padding: 6px 12px; display: flex; align-items: center; cursor: pointer;">
              <span class="slds-media__figure slds-listbox__option-icon" style="width: 16px; display: inline-flex; align-items: center;">
                ${isSelected ? `<span style="color: #0176d3; font-weight: bold; font-size: 12px;">✓</span>` : ''}
              </span>
              <span class="slds-media__body">
                <span class="slds-truncate" title="${opt}">${opt}</span>
              </span>
            </div>
          </li>
        `;
      }).join('');
    };

    renderOptions();

    // Toggle open
    fauxEl.onclick = (e) => {
      e.stopPropagation();
      document.querySelectorAll('.slds-combobox').forEach(c => {
        if (c !== comboEl) c.classList.remove('slds-is-open');
      });
      comboEl.classList.toggle('slds-is-open');
    };

    // Option click
    listEl.onclick = (e) => {
      const optionDiv = e.target.closest('.slds-listbox__option');
      if (!optionDiv) return;
      const val = optionDiv.getAttribute('data-value');
      
      valEl.textContent = val;
      valEl.setAttribute('data-value', val);
      comboEl.classList.remove('slds-is-open');
      
      renderOptions();
      if (onChange) onChange(val);
    };

    // Close on click outside
    document.addEventListener('click', (e) => {
      if (!comboEl.contains(e.target)) {
        comboEl.classList.remove('slds-is-open');
      }
    });
  }

  function validateSldsTextarea(textareaEl, errorMsg = 'Enter a value.') {
    const parent = textareaEl.closest('.slds-form-element');
    if (!parent) return true;
    
    const val = textareaEl.value.trim();
    let helpEl = parent.querySelector('.slds-form-element__help');
    
    if (!val) {
      parent.classList.add('slds-has-error');
      textareaEl.setAttribute('aria-invalid', 'true');
      if (!helpEl) {
        helpEl = document.createElement('div');
        helpEl.className = 'slds-form-element__help';
        helpEl.textContent = errorMsg;
        parent.appendChild(helpEl);
      }
      return false;
    } else {
      parent.classList.remove('slds-has-error');
      textareaEl.removeAttribute('aria-invalid');
      if (helpEl) helpEl.remove();
      return true;
    }
  }

  function addDonorPoints(pointsAdded) {
    const profile = window.DT_Data.donanteProfile;
    const oldLevel = profile.level;
    profile.points += pointsAdded;
    
    const L1_LIMIT = 10000;
    const L2_LIMIT = 25000;
    const L3_LIMIT = 50000;

    if (profile.points >= L2_LIMIT) {
      profile.level = 'Transformador';
      profile.levelNumber = 3;
      profile.nextLevelPoints = L3_LIMIT;
      profile.pointsProgress = Math.min(Math.floor(((profile.points - L2_LIMIT) / (L3_LIMIT - L2_LIMIT)) * 100), 100);
    } else if (profile.points >= L1_LIMIT) {
      profile.level = 'Sostenedor';
      profile.levelNumber = 2;
      profile.nextLevelPoints = L2_LIMIT;
      profile.pointsProgress = Math.min(Math.floor(((profile.points - L1_LIMIT) / (L2_LIMIT - L1_LIMIT)) * 100), 100);
    } else {
      profile.level = 'Colaborador';
      profile.levelNumber = 1;
      profile.nextLevelPoints = L1_LIMIT;
      profile.pointsProgress = Math.min(Math.floor((profile.points / L1_LIMIT) * 100), 100);
    }

    if (profile.level !== oldLevel) {
      addSystemNotification('donante', `¡Subiste de categoría! Tu nuevo rango es: ${profile.level}.`);
      setTimeout(() => {
        showToast(`¡Felicidades! Subiste al rango ${profile.level}`, 'success');
      }, 500);
    }
  }

  let currentRole = 'admin';
  let currentUser = null;
  let activeHash = '';
  let activeMaps = {}; // Store Leaflet instances

  // Sidebar item configuration by Role
  const ROLE_NAV = {
    admin: [
      { hash: '#admin-dashboard', label: 'Dashboard', icon: 'dashboard', sprite: SLDS_STD, section: 'sec-admin-dashboard' },
      { hash: '#admin-matchmaking', label: 'Matchmaking', icon: 'partners', sprite: SLDS_STD, section: 'sec-admin-matchmaking' },
      { hash: '#admin-ranking', label: 'Ranking', icon: 'coaching', sprite: SLDS_STD, section: 'sec-admin-ranking' },
      { hash: '#admin-csv', label: 'Importar CSV', icon: 'lead_import', sprite: SLDS_STD, section: 'sec-admin-csv' },
      { hash: '#admin-camiones', label: 'Flota Camiones', icon: 'delivery_truck', sprite: SLDS_STD, section: 'sec-admin-camiones' },
    ],
    donante: [
      { hash: '#donante-dashboard', label: 'Mi Perfil', icon: 'coaching', sprite: SLDS_STD, section: 'sec-donante-dashboard' },
      { hash: '#donante-mis-donaciones', label: 'Mis Donaciones', icon: 'payment_gateway', sprite: SLDS_STD, section: 'sec-donante-mis-donaciones' },
      { hash: '#donante-entidades', label: 'Entidades Beneficiarias', icon: 'relationship', sprite: SLDS_STD, section: 'sec-donante-entidades' },
      { hash: '#donante-seguimiento', label: 'Seguimiento Entregas', icon: 'delivery_truck', sprite: SLDS_STD, section: 'sec-donante-seguimiento' },
    ],
    beneficiaria: [
      { hash: '#beneficiaria-dashboard', label: 'Dashboard', icon: 'dashboard', sprite: SLDS_STD, section: 'sec-beneficiaria-dashboard' },
      { hash: '#beneficiaria-necesidades', label: 'Declarar Necesidades', icon: 'lead', sprite: SLDS_STD, section: 'sec-beneficiaria-necesidades' },
      { hash: '#beneficiaria-entregas', label: 'Confirmar Entregas', icon: 'orders', sprite: SLDS_STD, section: 'sec-beneficiaria-entregas' },
      { hash: '#beneficiaria-seguimiento', label: 'Seguimiento Logístico', icon: 'delivery_truck', sprite: SLDS_STD, section: 'sec-beneficiaria-seguimiento' },
    ]
  };

  // Page Header configurations by Hash
  const HEADER_CONFIGS = {
    '#admin-dashboard': { title: 'Panel de Administración', sub: 'Vista general y métricas operativas', icon: 'dashboard', sprite: SLDS_STD, iconClass: 'slds-icon-standard-dashboard', actions: `<button class="slds-button slds-button_brand" id="adminNewDonationBtn">Registrar Recepción</button>` },
    '#admin-matchmaking': { title: 'Algoritmos de Asignación', sub: 'Matchmaking de donaciones a necesidades', icon: 'partners', sprite: SLDS_STD, iconClass: 'slds-icon-standard-partners', actions: '' },
    '#admin-ranking': { title: 'Ranking Mensual', sub: 'Colaboradores destacados este mes', icon: 'coaching', sprite: SLDS_STD, iconClass: 'slds-icon-standard-coaching', actions: '' },
    '#admin-csv': { title: 'Carga Masiva de Donantes', sub: 'Importar históricos desde archivos CSV', icon: 'lead_import', sprite: SLDS_STD, iconClass: 'slds-icon-standard-lead-import', actions: '' },
    '#admin-camiones': { title: 'Camiones y Flota', sub: 'Administración de transportes logísticos', icon: 'delivery_truck', sprite: SLDS_STD, iconClass: 'slds-icon-standard-delivery-truck', actions: '' },

    '#donante-dashboard': { title: 'Mi Perfil', sub: 'Tu impacto social y progreso de aprendizaje', icon: 'coaching', sprite: SLDS_STD, iconClass: 'slds-icon-standard-coaching', actions: '' },
    '#donante-mis-donaciones': { title: 'Mis Colaboraciones', sub: 'Historial de bienes materiales donados', icon: 'payment_gateway', sprite: SLDS_STD, iconClass: 'slds-icon-standard-payment-gateway', actions: '' },
    '#donante-entidades': { title: 'Organizaciones Asociadas', sub: 'Entidades benéficas autorizadas en DonaTrack', icon: 'relationship', sprite: SLDS_STD, iconClass: 'slds-icon-standard-relationship', actions: '' },
    '#donante-seguimiento': { title: 'Seguimiento de Entregas', sub: 'Monitoreo en mapa de tus donaciones en camino', icon: 'delivery_truck', sprite: SLDS_STD, iconClass: 'slds-icon-standard-delivery-truck', actions: '' },

    '#beneficiaria-dashboard': { title: 'Panel de Organización', sub: 'Resumen de necesidades y asignaciones recibidas', icon: 'dashboard', sprite: SLDS_STD, iconClass: 'slds-icon-standard-dashboard', actions: '' },
    '#beneficiaria-necesidades': { title: 'Registro de Necesidades', sub: 'Administra tus demandas recurrentes y extraordinarias', icon: 'lead', sprite: SLDS_STD, iconClass: 'slds-icon-standard-lead', actions: '' },
    '#beneficiaria-entregas': { title: 'Certificación de Entregas', sub: 'Confirma la recepción física cargando pruebas', icon: 'orders', sprite: SLDS_STD, iconClass: 'slds-icon-standard-orders', actions: '' },
    '#beneficiaria-seguimiento': { title: 'Ruta de Transportes', sub: 'Mapa de camiones con dirección a tu sede', icon: 'delivery_truck', sprite: SLDS_STD, iconClass: 'slds-icon-standard-delivery-truck', actions: '' }
  };

  // ----- Shell & Navigation Setup -----
  function setupShell() {
    currentUser = window.DT_Auth.getUser();
    currentRole = currentUser ? currentUser.role : 'admin';

    // Populate top right user info
    document.getElementById('avatarName').textContent = currentUser ? currentUser.name : 'Usuario';
    const avatarImg = document.getElementById('avatarImg');
    if (avatarImg && currentUser) {
      avatarImg.src = `https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=${currentUser.username}`;
    }
    document.getElementById('breadcrumbRole').textContent = currentRole.toUpperCase();

    // Load dynamic sidebar links
    const sidebarList = document.getElementById('sidebarNavList');
    const links = ROLE_NAV[currentRole];
    
    const ICON_MAP = {
      'dashboard': 'home',
      'partners': 'people',
      'ranking': 'trail',
      'coaching': 'user',
      'lead_import': 'upload',
      'delivery_truck': 'truck',
      'payment_gateway': 'moneybag',
      'relationship': 'people',
      'lead': 'lead',
      'orders': 'orders'
    };

    const roleTitles = {
      admin: 'Administración',
      donante: 'Persona Donante',
      beneficiaria: 'Organización'
    };
    const titleEl = document.getElementById('sidebarNavTitle');
    if (titleEl) {
      titleEl.textContent = roleTitles[currentRole] || 'Menú';
    }

    sidebarList.innerHTML = links.map(link => {
      const iconName = ICON_MAP[link.icon] || 'home';
      return `
        <li class="slds-nav-vertical__item" id="nav-${link.hash.slice(1)}">
          <a href="${link.hash}" class="slds-nav-vertical__action" style="display: flex; align-items: center; gap: 8px;">
            <span class="dt-nav__icon slds-icon_container slds-icon-utility-${iconName} slds-current-color" style="width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0;">
              <svg class="slds-icon slds-icon_x-small" aria-hidden="true" style="fill: currentColor; width: 16px; height: 16px;">
                <use xlink:href="/assets/slds-icons/utility-sprite/svg/symbols.svg#${iconName}"></use>
              </svg>
            </span>
            <span class="dt-nav__label slds-truncate">${link.label}</span>
          </a>
        </li>
      `;
    }).join('') + `
      <li class="slds-nav-vertical__item slds-has-divider_top-space">
        <a href="#" id="logoutBtn" class="slds-nav-vertical__action" style="display: flex; align-items: center; gap: 8px;">
          <span class="dt-nav__icon slds-icon_container slds-icon-utility-logout slds-current-color" style="width: 24px; height: 24px; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0;">
            <svg class="slds-icon slds-icon_x-small" aria-hidden="true" style="fill: currentColor; width: 16px; height: 16px;">
              <use xlink:href="/assets/slds-icons/utility-sprite/svg/symbols.svg#logout"></use>
            </svg>
          </span>
          <span class="dt-nav__label slds-truncate">Cerrar sesión</span>
        </a>
      </li>
    `;

    // Collapsible sidebar handling
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('sidebarToggle');
    const isMobile = () => window.innerWidth <= 640;

    if (localStorage.getItem('dt_sidebar_collapsed') === '1' && !isMobile()) {
      sidebar.classList.add('is-collapsed');
    }

    toggleBtn.onclick = () => {
      if (isMobile()) {
        sidebar.classList.toggle('is-open');
      } else {
        sidebar.classList.toggle('is-collapsed');
        localStorage.setItem('dt_sidebar_collapsed', sidebar.classList.contains('is-collapsed') ? '1' : '0');
      }
    };
  }

  // ----- Router & View Switcher -----
  function handleRouting() {
    let hash = window.location.hash || '';
    const links = ROLE_NAV[currentRole];
    
    // Ensure hash is valid for current role, default to first item
    const matchedLink = links.find(l => l.hash === hash);
    if (!matchedLink) {
      hash = links[0].hash;
      window.location.hash = hash;
      return;
    }

    activeHash = hash;

    // Toggle active state in sidebar
    document.querySelectorAll('.slds-nav-vertical__item').forEach(el => el.classList.remove('slds-is-active'));
    const activeNav = document.getElementById(`nav-${hash.slice(1)}`);
    if (activeNav) activeNav.classList.add('slds-is-active');

    // Toggle visible panels
    document.querySelectorAll('.dt-role-section').forEach(el => el.classList.remove('is-active'));
    const activeSection = document.getElementById(matchedLink.section);
    if (activeSection) activeSection.classList.add('is-active');

    // Update Breadcrumbs & Header
    document.getElementById('breadcrumbSection').textContent = matchedLink.label;
    
    const config = HEADER_CONFIGS[hash];
    if (config) {
      document.getElementById('headerTitle').textContent = config.title;
      document.getElementById('headerSubtitle').textContent = config.sub;
      
      const iconContainer = document.getElementById('headerIconContainer');
      iconContainer.className = `slds-icon_container ${config.iconClass}`;
      iconContainer.innerHTML = iconSvg(config.sprite, config.icon, 'slds-page-header__icon');
      
      document.getElementById('headerActions').innerHTML = config.actions;
    }

    // Render contents specific to active view
    triggerViewRender(hash);
  }

  function triggerViewRender(hash) {
    // Admin renders
    if (hash === '#admin-dashboard') {
      renderAdminDashboard();
    } else if (hash === '#admin-matchmaking') {
      renderAdminMatchmaking();
    } else if (hash === '#admin-ranking') {
      renderAdminRanking();
    } else if (hash === '#admin-csv') {
      initDropzone();
    } else if (hash === '#admin-camiones') {
      renderAdminTrucks();
    }
    
    // Donante renders
    else if (hash === '#donante-dashboard') {
      renderDonanteDashboard();
    } else if (hash === '#donante-mis-donaciones') {
      renderDonanteDonations();
    } else if (hash === '#donante-entidades') {
      renderDonanteBeneficiaries();
    } else if (hash === '#donante-seguimiento') {
      renderDonanteMap();
    }
    
    // Beneficiaria renders
    else if (hash === '#beneficiaria-dashboard') {
      renderBeneficiariaDashboard();
    } else if (hash === '#beneficiaria-necesidades') {
      renderBeneficiariaNeeds();
    } else if (hash === '#beneficiaria-entregas') {
      renderBeneficiariaDeliveries();
    } else if (hash === '#beneficiaria-seguimiento') {
      renderBeneficiariaMap();
    }
  }

  // ----- Notifications Panel Controller -----
  function renderNotifications() {
    const list = document.getElementById('notifList');
    const badgeCount = document.getElementById('notifCount');
    const notifs = window.DT_Data.notifications[currentRole] || [];
    
    const unreadCount = notifs.filter(n => n.unread).length;
    badgeCount.textContent = unreadCount;
    badgeCount.style.display = unreadCount > 0 ? 'inline-block' : 'none';

    if (notifs.length === 0) {
      list.innerHTML = `<li class="slds-p-around_small slds-text-color_weak slds-text-align_center">No tienes notificaciones</li>`;
      return;
    }

    list.innerHTML = notifs.map((n, index) => `
      <li class="dt-notification-card ${n.unread ? 'is-unread' : ''}" role="presentation" data-notif-index="${index}">
        <a href="javascript:void(0);" role="menuitem" style="color: #444; text-decoration: none;">
          <div class="slds-text-body_small">${n.text}</div>
          <div class="dt-notification-time">${n.time}</div>
        </a>
      </li>
    `).join('');

    // Toggle dropdown
    const bellBtn = document.getElementById('notifBellBtn');
    const dropdown = document.getElementById('notifDropdown');
    
    bellBtn.onclick = (e) => {
      e.stopPropagation();
      dropdown.classList.toggle('slds-is-open');
    };

    document.addEventListener('click', () => dropdown.classList.remove('slds-is-open'));

    // Notification click listener
    list.querySelectorAll('[data-notif-index]').forEach(el => {
      el.addEventListener('click', (e) => {
        const idx = el.dataset.notifIndex;
        notifs[idx].unread = false;
        renderNotifications();
      });
    });
  }

  function addSystemNotification(role, message) {
    const arr = window.DT_Data.notifications[role];
    if (arr) {
      arr.unshift({ text: message, time: 'Hace unos instantes', unread: true });
      renderNotifications();
    }
  }

  // ==================== RENDERING LOGIC: ADMIN ====================
  function renderAdminDashboard() {
    // KPIs
    const grid = document.getElementById('adminKpis');
    grid.innerHTML = window.DT_Data.kpis.map(k => `
      <div class="dt-kpi">
        <div class="dt-kpi__card">
          <div class="dt-kpi__icon">${iconSvg(SLDS_UTIL, k.icon, '')}</div>
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

    // Activity table
    const tbody = document.getElementById('adminActivityBody');
    tbody.innerHTML = window.DT_Data.activity.map(row => `
      <tr class="slds-hint-parent">
        <th data-label="Fecha" scope="row"><div class="slds-truncate" title="${row.fecha}">${row.fecha}</div></th>
        <td data-label="Donante"><div class="slds-truncate" title="${row.donante}">${row.donante}</div></td>
        <td data-label="Entidad Beneficiaria"><div class="slds-truncate" title="${row.entidad}">${row.entidad}</div></td>
        <td data-label="Tipo de Bien"><div class="slds-truncate" title="${row.tipo}">${row.tipo}</div></td>
        <td data-label="Estado"><div class="slds-truncate" title="${row.estado}">${badge(row.estado)}</div></td>
      </tr>
    `).join('');

    // Quick Actions
    const actGrid = document.getElementById('adminQuickActions');
    actGrid.innerHTML = window.DT_Data.actions.map(a => `
      <div class="dt-action slds-size_1-of-1 slds-m-bottom_small">
        <button class="dt-action__card" style="width: 100%;" onclick="DT_App.openModal('${a.id}')">
          <div class="dt-action__icon">${iconSvg(SLDS_UTIL, a.icon, '')}</div>
          <div>
            <p class="dt-action__title">${a.title}</p>
            <p class="dt-action__desc">${a.desc}</p>
          </div>
        </button>
      </div>
    `).join('');

    // Add receipt button listener
    const recBtn = document.getElementById('adminNewDonationBtn');
    if (recBtn) {
      recBtn.onclick = () => openModal('modal-recepcion');
    }
  }

  function renderAdminMatchmaking() {
    const container = document.getElementById('algoDonationSelectContainer');
    if (!container) return;
    
    const pends = window.DT_Data.matchmaker.pendingDonations;
    const options = pends.map(p => `${p.id} · ${p.category} (${p.subcategory}) - ${p.quantity}`);
    
    // Always render the combobox HTML to reflect the current options state
    container.innerHTML = renderSldsComboboxHtml('algoDonationSelect', 'Donación en Depósito', options[0] || 'No hay donaciones pendientes');
    
    bindSldsCombobox('algoDonationSelect', options, options[0] || 'No hay donaciones pendientes', null);

    const runBtn = document.getElementById('runAlgoBtn');
    const resultsContainer = document.getElementById('algoResultsContainer');
    const resultsBody = document.getElementById('algoResultsBody');

    if (options.length === 0) {
      runBtn.setAttribute('disabled', 'true');
    } else {
      runBtn.removeAttribute('disabled');
    }

    runBtn.onclick = () => {
      const valEl = document.getElementById('combo-val-algoDonationSelect');
      const selectedVal = valEl ? valEl.getAttribute('data-value') : '';
      if (!selectedVal || selectedVal === 'No hay donaciones pendientes') return;
      const selectedId = selectedVal.split(' ')[0];
      const results = window.DT_Data.matchmaker.results[selectedId] || [];
      
      resultsBody.innerHTML = results.map((r, i) => `
        <tr class="slds-hint-parent">
          <th data-label="Entidad Recomendada" scope="row"><div class="slds-truncate" title="${r.org}"><strong>${r.org}</strong></div></th>
          <td data-label="Puntuación"><div class="slds-truncate" title="${r.score}"><span class="slds-text-color_success" style="font-weight:bold">${r.score}</span></div></td>
          <td data-label="Justificación"><div class="slds-truncate" title="${r.reason}">${r.reason}</div></td>
          <td data-label="Recomendación"><div class="slds-truncate" title="${r.recommendation}"><span class="slds-badge">${r.recommendation}</span></div></td>
          <td data-label="Acción">
            <div class="slds-truncate">
              <button class="slds-button slds-button_success" onclick="DT_App.confirmMatch('${selectedId}', '${r.org}')">
                Confirmar Asignación
              </button>
            </div>
          </td>
        </tr>
      `).join('');
      
      resultsContainer.removeAttribute('hidden');
    };
  }

  function renderAdminRanking() {
    const tbody = document.getElementById('adminRankingBody');
    tbody.innerHTML = window.DT_Data.ranking.map(r => {
      const medal = r.medal
        ? `<span class="dt-medal dt-medal_${r.medal}">${r.pos}</span>`
        : `<span class="slds-text-color_weak">#${r.pos}</span>`;
      return `
        <tr class="slds-hint-parent">
          <th data-label="Puesto" scope="row"><div class="slds-truncate">${medal}</div></th>
          <td data-label="Nombre"><div class="slds-truncate" title="${r.name}"><strong>${r.name}</strong></div></td>
          <td data-label="Donaciones"><div class="slds-truncate" title="${r.amount}">${r.amount}</div></td>
          <td data-label="Medalla"><div class="slds-truncate" title="${r.medal ? r.medal.toUpperCase() : 'Ninguna'}">${r.medal ? r.medal.toUpperCase() : 'Ninguna'}</div></td>
        </tr>
      `;
    }).join('');
  }

  function renderAdminTrucks() {
    const tbody = document.getElementById('adminTrucksBody');
    const trucks = window.DT_Data.deliveries;
    tbody.innerHTML = trucks.map(t => `
      <tr class="slds-hint-parent">
        <th data-label="Identificador" scope="row"><div class="slds-truncate" title="${t.truck}"><strong>${t.truck}</strong></div></th>
        <td data-label="Matrícula"><div class="slds-truncate" title="ABC-123-${t.id.slice(-2)}">ABC-123-${t.id.slice(-2)}</div></td>
        <td data-label="Capacidad"><div class="slds-truncate" title="3.5 toneladas">3.5 toneladas</div></td>
        <td data-label="Estado"><div class="slds-truncate" title="${t.status}">${badge(t.status)}</div></td>
      </tr>
    `).join('');
  }

  // ==================== RENDERING LOGIC: DONANTE ====================
  function renderDonanteDashboard() {
    const profile = window.DT_Data.donanteProfile;

    // Set stats
    const thAvatarImg = document.getElementById('thAvatarImg');
    if (thAvatarImg) {
      thAvatarImg.src = `https://api.dicebear.com/10.x/adventurer-neutral/svg?seed=${currentUser.username}`;
    }
    document.getElementById('thProfileName').textContent = currentUser.name;
    document.getElementById('thRankLabel').textContent = profile.level;
    document.getElementById('thRankLevel').textContent = profile.levelNumber;
    document.getElementById('thStatPoints').textContent = profile.points.toLocaleString();
    document.getElementById('thStatBadges').textContent = profile.badgesCount;
    document.getElementById('thStatDonations').textContent = profile.donationsCount;

    // Progress bar
    document.getElementById('thProgressText').textContent = `${profile.points} / ${profile.nextLevelPoints} Puntos`;
    document.getElementById('thProgressBar').style.width = profile.pointsProgress + '%';
    const nextRankLabel = document.getElementById('thNextRankLabel');
    if (nextRankLabel) {
      nextRankLabel.textContent = profile.level === 'Colaborador' ? 'Sostenedor' : (profile.level === 'Sostenedor' ? 'Transformador' : 'Nivel Máximo');
    }

    // Unlocked Badges Grid (Trailhead style)
    const grid = document.getElementById('thBadgesGrid');
    grid.innerHTML = profile.unlockedBadges.map(b => `
      <div class="dt-th-badge-card">
        <div class="dt-th-badge-icon-wrap dt-th-badge-${b.color}">
          ${iconSvg(SLDS_UTIL, b.icon, 'slds-icon_large')}
        </div>
        <p class="dt-th-badge-title">${b.title}</p>
        <p class="dt-th-badge-date">Obtenida: ${b.date}</p>
      </div>
    `).join('');

    // Active missions list
    const mList = document.getElementById('thMisionesList');
    mList.innerHTML = profile.misiones.map(m => `
      <div class="dt-th-mission-item">
        <div class="dt-th-mission-details">
          <p class="dt-th-mission-title">${m.name}</p>
          <p class="dt-th-mission-desc">${m.desc}</p>
          ${m.lossCondition ? `<div class="dt-th-mission-loss-condition">
            <svg viewBox="0 0 24 24" fill="currentColor" style="width: 12px; height: 12px; vertical-align:middle; margin-right: 4px;">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
            Pérdida de progreso: ${m.lossCondition}
          </div>` : ''}
        </div>
        <div>
          <span class="dt-th-mission-badge slds-badge ${m.isComplete ? 'dt-badge_success' : 'dt-badge_info'}">
            ${m.progress}
          </span>
        </div>
      </div>
    `).join('');
  }

  function renderDonanteDonations() {
    const myDons = window.DT_Data.donanteProfile.myDonations;
    
    // Render dynamic filter picklists if they are not in DOM
    const filterStateContainer = document.getElementById('filterStateContainer');
    const filterCategoryContainer = document.getElementById('filterCategoryContainer');
    
    if (filterStateContainer && !document.getElementById('combo-filterState')) {
      filterStateContainer.innerHTML = renderSldsComboboxHtml('filterState', 'Filtrar por Estado', 'Todos los estados');
      const stateOptions = ['Todos los estados', 'En depósito', 'En tránsito', 'Entregado'];
      bindSldsCombobox('filterState', stateOptions, 'Todos los estados', () => {
        renderDonanteDonations();
      });
    }
    
    if (filterCategoryContainer && !document.getElementById('combo-filterCategory')) {
      filterCategoryContainer.innerHTML = renderSldsComboboxHtml('filterCategory', 'Filtrar por Categoría', 'Todas las categorías');
      const catOptions = ['Todas las categorías', 'Alimentos', 'Vestimenta', 'Mobiliario'];
      bindSldsCombobox('filterCategory', catOptions, 'Todas las categorías', () => {
        renderDonanteDonations();
      });
    }

    const valStateEl = document.getElementById('combo-val-filterState');
    const valCatEl = document.getElementById('combo-val-filterCategory');
    
    const rawState = valStateEl ? valStateEl.getAttribute('data-value') : 'Todos los estados';
    const rawCat = valCatEl ? valCatEl.getAttribute('data-value') : 'Todas las categorías';
    
    const fState = rawState === 'Todos los estados' ? 'all' : rawState;
    const fCat = rawCat === 'Todas las categorías' ? 'all' : rawCat;

    const filtered = myDons.filter(d => {
      const matchState = fState === 'all' || d.status === fState;
      const matchCat = fCat === 'all' || d.category === fCat;
      return matchState && matchCat;
    });

    const tbody = document.getElementById('donanteActivityBody');
    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="dt-empty">No se encontraron donaciones con los filtros seleccionados.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtered.map(d => `
      <tr class="slds-hint-parent">
        <th data-label="Identificador" scope="row"><div class="slds-truncate" title="${d.id}"><strong>${d.id}</strong></div></th>
        <td data-label="Fecha"><div class="slds-truncate" title="${d.date}">${d.date}</div></td>
        <td data-label="Categoría"><div class="slds-truncate" title="${d.category}">${d.category}</div></td>
        <td data-label="Subcategoría"><div class="slds-truncate" title="${d.subcategory}">${d.subcategory}</div></td>
        <td data-label="Cantidad"><div class="slds-truncate" title="${d.quantity}">${d.quantity}</div></td>
        <td data-label="Destino"><div class="slds-truncate" title="${d.destination || 'Pendiente de Asignación'}">${d.destination || 'Pendiente de Asignación'}</div></td>
        <td data-label="Estado"><div class="slds-truncate" title="${d.status}">${badge(d.status)}</div></td>
      </tr>
    `).join('');

    // Register button modal trigger
    document.getElementById('donanteNewDonationBtn').onclick = () => {
      openModal('modal-donante-registro');
    };
  }

  function renderDonanteBeneficiaries() {
    const tbody = document.getElementById('donanteBeneficiariesBody');
    const orgs = [
      { name: 'Fundación Esperanza', location: 'Av. Corrientes 4500, CABA', contact: 'Sofía R. · +54 11 5555-0199', needs: 'Fideos secos, Camperas de abrigo infantil, Bancos escolares' },
      { name: 'Comedor El Faro', location: 'Av. Eva Perón 3400, Lanús', contact: 'Juan P. · +54 11 4444-9021', needs: 'Alimentos no perecederos, Vestimenta infantil' },
      { name: 'Hogar Caminos', location: 'Juramento 1200, Belgrano', contact: 'Marta S. · +54 11 3333-8812', needs: 'Mobiliario usado, Frazadas y abrigo' },
      { name: 'Cruz Roja Local', location: 'Paseo Colón 700, CABA', contact: 'Logística · +54 11 2222-7711', needs: 'Kits de primeros auxilios, Camas' }
    ];

    tbody.innerHTML = orgs.map(o => `
      <tr class="slds-hint-parent">
        <th data-label="Nombre" scope="row">
          <div class="slds-truncate" title="${o.name}">
            <strong>${o.name}</strong> 
            <span class="slds-badge slds-theme_success" style="margin-left:8px;">Verificada</span>
          </div>
        </th>
        <td data-label="Ubicación"><div class="slds-truncate" title="${o.location}">${o.location}</div></td>
        <td data-label="Contacto"><div class="slds-truncate" title="${o.contact}">${o.contact}</div></td>
        <td data-label="Necesidades Activas"><div class="slds-truncate" title="${o.needs}">${o.needs}</div></td>
      </tr>
    `).join('');
  }

  // ==================== RENDERING LOGIC: BENEFICIARIA ====================
  function renderBeneficiariaDashboard() {
    // Summary of needs
    const needsList = document.getElementById('benefDashboardNeeds');
    const needs = window.DT_Data.beneficiaryProfile.needs;
    needsList.innerHTML = needs.map(n => `
      <li class="slds-p-around_x-small slds-border_bottom">
        <div class="slds-grid slds-grid_align-spread">
          <span><strong>${n.subcategory}</strong> (${n.quantity})</span>
          <span class="slds-badge ${n.type === 'Recurrente' ? 'dt-badge_info' : 'dt-badge_warning'}">${n.type}</span>
        </div>
        <div class="slds-text-body_small slds-text-color_weak">${n.progress}</div>
      </li>
    `).join('');

    // Summary of incoming deliveries
    const delivList = document.getElementById('benefDashboardDeliveries');
    const assigns = window.DT_Data.beneficiaryProfile.assignedDonations.filter(d => d.status !== 'Entregado');
    if (assigns.length === 0) {
      delivList.innerHTML = `<li class="dt-empty">No hay despachos de donaciones activos en este momento.</li>`;
    } else {
      delivList.innerHTML = assigns.map(a => `
        <li class="slds-p-around_x-small slds-border_bottom slds-grid slds-grid_align-spread">
          <div>
            <strong>${a.id}</strong> · ${a.category} (${a.quantity})
            <div class="slds-text-body_small slds-text-color_weak">Transporte: ${a.trackingId} · ETA: ${a.eta}</div>
          </div>
          <div>${badge(a.status)}</div>
        </li>
      `).join('');
    }
  }

  function renderBeneficiariaNeeds() {
    const tbody = document.getElementById('benefNeedsTableBody');
    const needs = window.DT_Data.beneficiaryProfile.needs;

    tbody.innerHTML = needs.map(n => `
      <tr class="slds-hint-parent">
        <th data-label="Categoría" scope="row"><div class="slds-truncate" title="${n.category}">${n.category}</div></th>
        <td data-label="Subcategoría"><div class="slds-truncate" title="${n.subcategory}"><strong>${n.subcategory}</strong></div></td>
        <td data-label="Cantidad"><div class="slds-truncate" title="${n.quantity}">${n.quantity}</div></td>
        <td data-label="Tipo"><div class="slds-truncate" title="${n.type}"><span class="slds-badge ${n.type === 'Recurrente' ? 'slds-badge_inverse' : 'slds-theme_warning'}">${n.type}</span></div></td>
        <td data-label="Progreso"><div class="slds-truncate" title="${n.progress}">${n.progress}</div></td>
      </tr>
    `).join('');

    // Render Comboboxes if placeholders exist and aren't rendered yet
    const catContainer = document.getElementById('needCategoryContainer');
    const subContainer = document.getElementById('needSubcategoryContainer');
    const unitContainer = document.getElementById('needQtyUnitContainer');
    const typeContainer = document.getElementById('needTypeContainer');

    if (catContainer && !catContainer.innerHTML.trim()) {
      catContainer.innerHTML = renderSldsComboboxHtml('needCategory', 'Categoría del Bien', 'Alimentos');
      subContainer.innerHTML = renderSldsComboboxHtml('needSubcategory', 'Subcategoría del Bien', 'Fideos secos');
      unitContainer.innerHTML = renderSldsComboboxHtml('needQtyUnit', 'Unidad', 'unidades', 'slds-size_full');
      typeContainer.innerHTML = renderSldsComboboxHtml('needType', 'Tipo de Necesidad', 'Recurrente');

      // Bind Category and Subcategory
      const onCategoryChange = (newCat) => {
        const subs = SUBCATEGORIES[newCat] || [];
        bindSldsCombobox('needSubcategory', subs, subs[0] || '', null);
      };

      bindSldsCombobox('needCategory', ['Alimentos', 'Vestimenta', 'Mobiliario'], 'Alimentos', onCategoryChange);
      bindSldsCombobox('needSubcategory', SUBCATEGORIES['Alimentos'], SUBCATEGORIES['Alimentos'][0], null);
      bindSldsCombobox('needQtyUnit', ['unidades', 'kg', 'litros', 'gramos'], 'unidades', null);
      bindSldsCombobox('needType', ['Recurrente', 'Extraordinaria'], 'Recurrente', null);
    }

    // Attach real-time validation for textarea
    const descInput = document.getElementById('needDescription');
    if (descInput) {
      descInput.oninput = () => {
        validateSldsTextarea(descInput, 'Ingresa una descripción para tu necesidad.');
      };
    }

    // Form submission listener
    const form = document.getElementById('needRegisterForm');
    form.onsubmit = (e) => {
      e.preventDefault();
      
      // Validate textarea first
      if (!validateSldsTextarea(descInput, 'Ingresa una descripción para tu necesidad.')) {
        return;
      }

      const getVal = (comboId) => document.getElementById(`combo-val-${comboId}`).getAttribute('data-value');

      const cat = getVal('needCategory');
      const sub = getVal('needSubcategory');
      const qtyVal = document.getElementById('needQtyValue').value;
      const qtyUnit = getVal('needQtyUnit');
      const qty = `${qtyVal} ${qtyUnit}`;
      const type = getVal('needType');
      const desc = descInput.value.trim();

      const newNeed = {
        id: 'NEED-' + Math.floor(Math.random() * 900 + 100),
        category: cat,
        subcategory: sub,
        quantity: qty,
        type: type,
        desc: desc,
        progress: '0 recibidos'
      };

      window.DT_Data.beneficiaryProfile.needs.unshift(newNeed);
      
      // Update UI
      renderBeneficiariaNeeds();
      renderBeneficiariaDashboard();
      form.reset();

      // Reset combobox selections
      bindSldsCombobox('needCategory', ['Alimentos', 'Vestimenta', 'Mobiliario'], 'Alimentos', (newCat) => {
        const subs = SUBCATEGORIES[newCat] || [];
        bindSldsCombobox('needSubcategory', subs, subs[0] || '', null);
      });
      bindSldsCombobox('needSubcategory', SUBCATEGORIES['Alimentos'], SUBCATEGORIES['Alimentos'][0], null);
      bindSldsCombobox('needQtyUnit', ['unidades', 'kg', 'litros', 'gramos'], 'unidades', null);
      bindSldsCombobox('needType', ['Recurrente', 'Extraordinaria'], 'Recurrente', null);
      validateSldsTextarea(descInput); // Reset validation state

      // Trigger user notifications
      addSystemNotification('beneficiaria', `Registraste con éxito la necesidad de: ${sub} (${qty}).`);
      showToast('Necesidad declarada con éxito', 'success');
    };
  }

  function renderBeneficiariaDeliveries() {
    const tbody = document.getElementById('benefDeliveriesBody');
    const assigns = window.DT_Data.beneficiaryProfile.assignedDonations;

    tbody.innerHTML = assigns.map(a => {
      let actionHtml = '';
      if (a.status === 'En tránsito' || a.status === 'Asignado') {
        actionHtml = `
          <button class="slds-button slds-button_success" onclick="DT_App.openConfirmDeliveryModal('${a.id}')">
            Confirmar Recepción
          </button>
        `;
      } else {
        actionHtml = `<span class="slds-text-color_weak">Certificado ✓</span>`;
      }

      return `
        <tr class="slds-hint-parent">
          <th data-label="Identificador" scope="row"><div class="slds-truncate" title="${a.id}"><strong>${a.id}</strong></div></th>
          <td data-label="Donante"><div class="slds-truncate" title="${a.donor}">${a.donor}</div></td>
          <td data-label="Detalle"><div class="slds-truncate" title="${a.subcategory} (${a.quantity})">${a.subcategory} (${a.quantity})</div></td>
          <td data-label="Estado / ETA"><div class="slds-truncate" title="${a.status}">${badge(a.status)} ${a.eta ? `· <span class="slds-text-color_weak">${a.eta}</span>` : ''}</div></td>
          <td data-label="Acción"><div class="slds-truncate">${actionHtml}</div></td>
        </tr>
      `;
    }).join('');
  }

  // ==================== MAP CONTROLLER (Leaflet) ====================
  // Central Coordinates in CABA
  const MAP_CENTER = [-34.61, -58.42];

  function destroyMap(mapId) {
    if (activeMaps[mapId]) {
      activeMaps[mapId].remove();
      delete activeMaps[mapId];
    }
  }

  function renderDonanteMap() {
    const mapContainerId = 'donanteMap';
    const listId = 'donanteTrackingList';
    destroyMap(mapContainerId);

    const container = document.getElementById(mapContainerId);
    if (!container) return;

    // Filter donor active deliveries in transit
    const transits = window.DT_Data.donanteProfile.myDonations.filter(d => d.status === 'En tránsito');
    const listEl = document.getElementById(listId);
    
    if (transits.length === 0) {
      listEl.innerHTML = `<li class="dt-empty">No tienes entregas activas en tránsito.</li>`;
      container.innerHTML = `<div class="slds-align_absolute-center dt-empty" style="height:100%">No hay tránsitos activos que mostrar en el mapa logístico.</div>`;
      return;
    }

    listEl.innerHTML = transits.map(t => `
      <li class="slds-p-around_x-small slds-border_bottom">
        <div><strong>${t.id}</strong> (En camino)</div>
        <div class="slds-text-body_small slds-text-color_weak">Camión: ${t.truck} · Destino: ${t.destination}</div>
      </li>
    `).join('');

    // Show SLDS Spinner
    container.style.position = 'relative';
    container.innerHTML = `
      <div class="slds-spinner_container" id="spinner-${mapContainerId}">
        <div role="status" class="slds-spinner slds-spinner_medium slds-spinner_brand">
          <span class="slds-assistive-text">Cargando Mapa...</span>
          <div class="slds-spinner__dot-a"></div>
          <div class="slds-spinner__dot-b"></div>
        </div>
      </div>
    `;

    setTimeout(() => {
      if (activeMaps[mapContainerId]) return;
      try {
        const spinner = document.getElementById(`spinner-${mapContainerId}`);
        if (spinner) spinner.remove();

        const map = L.map(container, { scrollWheelZoom: false }).setView(MAP_CENTER, 12);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png').addTo(map);
        activeMaps[mapContainerId] = map;

        const streetPath = [
          [-34.6037, -58.3816], // Inicio (Obelisco)
          [-34.6017, -58.3892], // Corrientes y Callao
          [-34.5947, -58.3926], // Callao y Santa Fe
          [-34.5925, -58.4005], // Santa Fe y Pueyrredón (Ubicación actual del Camión)
          [-34.5865, -58.4110], // Santa Fe y Coronel Díaz
          [-34.5807, -58.4204]  // Fin (Plaza Italia / Hogar Caminos)
        ];

        // Draw route pins
        const routePoints = [
          { name: 'Depósito Central (Inicio)', coords: streetPath[0], role: 'depot', emoji: '🏠' },
          { name: 'Hogar Caminos (Fin)', coords: streetPath[streetPath.length - 1], role: 'destination', emoji: '📍' },
          { name: 'Camión #07 (En camino)', coords: streetPath[3], role: 'truck', emoji: '🚚' }
        ];

        routePoints.forEach(p => {
          const icon = L.divIcon({
            className: "",
            html: `<div class="dt-marker-custom dt-marker-custom--${p.role}"><span>${p.emoji}</span></div>`,
            iconSize: [34, 34],
            iconAnchor: [17, 17]
          });
          L.marker(p.coords, { icon }).addTo(map).bindPopup(`<b>${p.name}</b><br/>Ubicación en vivo.`);
        });

        // 1. Recorrido realizado (Sólido azul)
        L.polyline(streetPath.slice(0, 4), {
          color: '#0176d3',
          weight: 4
        }).addTo(map);

        // 2. Recorrido faltante (Línea punteada gris)
        L.polyline(streetPath.slice(3), {
          color: '#706e6b',
          weight: 3,
          dashArray: '6, 8'
        }).addTo(map);

      } catch (e) {
        console.warn("Leaflet error. Rendering backup map SVG:", e);
        renderBackupMap(container);
      }
    }, 150);
  }

  function renderBeneficiariaMap() {
    const mapContainerId = 'benefMap';
    const listId = 'benefTrackingList';
    destroyMap(mapContainerId);

    const container = document.getElementById(mapContainerId);
    if (!container) return;

    // Filter incoming deliveries in transit
    const transits = window.DT_Data.beneficiaryProfile.assignedDonations.filter(d => d.status === 'En tránsito');
    const listEl = document.getElementById(listId);

    if (transits.length === 0) {
      listEl.innerHTML = `<li class="dt-empty">No hay despachos de camiones hacia tu sede.</li>`;
      container.innerHTML = `<div class="slds-align_absolute-center dt-empty" style="height:100%">No hay envíos activos hacia tu sede.</div>`;
      return;
    }

    listEl.innerHTML = transits.map(t => `
      <li class="slds-p-around_x-small slds-border_bottom">
        <div><strong>Despacho ${t.trackingId}</strong> (En ruta)</div>
        <div class="slds-text-body_small slds-text-color_weak">Contiene: ${t.subcategory} · ETA: ${t.eta}</div>
      </li>
    `).join('');

    // Show SLDS Spinner
    container.style.position = 'relative';
    container.innerHTML = `
      <div class="slds-spinner_container" id="spinner-${mapContainerId}">
        <div role="status" class="slds-spinner slds-spinner_medium slds-spinner_brand">
          <span class="slds-assistive-text">Cargando Mapa...</span>
          <div class="slds-spinner__dot-a"></div>
          <div class="slds-spinner__dot-b"></div>
        </div>
      </div>
    `;

    setTimeout(() => {
      if (activeMaps[mapContainerId]) return;
      try {
        const spinner = document.getElementById(`spinner-${mapContainerId}`);
        if (spinner) spinner.remove();

        const map = L.map(container, { scrollWheelZoom: false }).setView(MAP_CENTER, 12);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png').addTo(map);
        activeMaps[mapContainerId] = map;

        const streetPath = [
          [-34.6037, -58.3816], // Inicio (Obelisco)
          [-34.6090, -58.4000], // Plaza Miserere
          [-34.6130, -58.4180], // Rivadavia y Medrano (Camión)
          [-34.6200, -58.4220], // Castro Barros
          [-34.6180, -58.4350]  // Fin (Sede Fundación Esperanza)
        ];

        // Draw route pins
        const routePoints = [
          { name: 'Depósito Central (Inicio)', coords: streetPath[0], role: 'depot', emoji: '🏠' },
          { name: 'Sede Fundación Esperanza (Fin)', coords: streetPath[streetPath.length - 1], role: 'destination', emoji: '📍' },
          { name: 'Camión #07 (En ruta)', coords: streetPath[2], role: 'truck', emoji: '🚚' }
        ];

        routePoints.forEach(p => {
          const icon = L.divIcon({
            className: "",
            html: `<div class="dt-marker-custom dt-marker-custom--${p.role}"><span>${p.emoji}</span></div>`,
            iconSize: [34, 34],
            iconAnchor: [17, 17]
          });
          L.marker(p.coords, { icon }).addTo(map).bindPopup(`<b>${p.name}</b><br/>Seguimiento de envío.`);
        });

        // Recorrido realizado (Sólido azul)
        L.polyline(streetPath.slice(0, 3), {
          color: '#0176d3',
          weight: 4
        }).addTo(map);

        // Recorrido faltante (Punteado gris)
        L.polyline(streetPath.slice(2), {
          color: '#706e6b',
          weight: 3,
          dashArray: '6, 8'
        }).addTo(map);

      } catch (e) {
        console.warn("Leaflet error. Rendering backup map SVG:", e);
        renderBackupMap(container);
      }
    }, 150);
  }

  function renderBackupMap(container) {
    container.innerHTML = `
      <div class="dt-map" style="height:100%; min-height:380px;">
        <svg viewBox="0 0 600 320" preserveAspectRatio="none" class="dt-map__svg">
          <defs>
            <pattern id="b-grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#e5e8ec" stroke-width="1" />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#b-grid)" />
          <path d="M40,260 C160,140 280,300 440,120 L540,60" fill="none" stroke="#0176d3" stroke-width="3" stroke-dasharray="6 5" />
          <circle cx="40" cy="260" r="8" fill="#0176d3" />
          <circle cx="540" cy="60" r="8" fill="#2e844a" />
          <circle cx="300" cy="200" r="6" fill="#fe9339" />
        </svg>
        <div class="dt-map__legend">
          <span><i class="dt-dot" style="background:#0176d3"></i> Origen (Depósito)</span>
          <span><i class="dt-dot" style="background:#fe9339"></i> Camión #07 (En ruta)</span>
          <span><i class="dt-dot" style="background:#2e844a"></i> Destino Final</span>
        </div>
      </div>
    `;
  }

  // ==================== CSV DROPZONE & FILE INPUT ====================
  function initDropzone() {
    const zone = document.getElementById('dropzone');
    const input = document.getElementById('csvFile');
    const status = document.getElementById('csvStatus');
    if (!zone) return;

    function handleFile(file) {
      status.hidden = false;
      if (!file.name.toLowerCase().endsWith('.csv')) {
        status.innerHTML = `<div class="slds-notify slds-notify_alert slds-theme_error" role="alert">
          <h2>Archivo no válido. Debe ser un archivo .csv</h2></div>`;
        return;
      }
      const sizeKb = (file.size / 1024).toFixed(1);
      status.innerHTML = `
        <div class="slds-text-body_small slds-m-bottom_x-small"><strong>${file.name}</strong> · ${sizeKb} KB</div>
        <div class="slds-grid slds-grid_vertical-align-center" style="position: relative; margin-bottom: 8px;">
          <div class="slds-col slds-grow slds-m-right_medium">
            <div class="slds-progress-bar slds-progress-bar_circular slds-progress-bar_large" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0" role="progressbar">
              <span id="csvProgress" class="slds-progress-bar__value" style="width:0%; background:#0176d3"><span class="slds-assistive-text">0%</span></span>
            </div>
          </div>
          <div class="slds-col slds-no-flex" style="width: 20px; height: 20px; position: relative;" id="csvSpinnerContainer">
            <div class="slds-spinner_container" style="background: none; width: 20px; height: 20px;">
              <div role="status" class="slds-spinner slds-spinner_xx-small slds-spinner_brand" style="width: 20px; height: 20px;">
                <span class="slds-assistive-text">Loading</span>
                <div class="slds-spinner__dot-a"></div>
                <div class="slds-spinner__dot-b"></div>
              </div>
            </div>
          </div>
        </div>
        <div id="csvMsg" class="slds-text-body_small slds-m-top_x-small slds-text-color_weak">Leyendo archivo de donantes históricos...</div>
      `;
      let pct = 0;
      const bar = document.getElementById('csvProgress');
      const msg = document.getElementById('csvMsg');
      const t = setInterval(() => {
        pct += 10;
        bar.style.width = pct + '%';
        if (pct === 40) msg.textContent = 'Validando correos duplicados y campos requeridos...';
        if (pct === 70) msg.textContent = 'Procesando registros e importando bases de datos (12,410 filas)...';
        if (pct >= 100) {
          clearInterval(t);
          const spinCont = document.getElementById('csvSpinnerContainer');
          if (spinCont) spinCont.remove();
          msg.outerHTML = `<div class="slds-notify slds-notify_alert slds-theme_success slds-m-top_small" role="status" style="background:#2e844a; color:#fff">
            <h2>✓ Migración Completa: 12,410 donantes agregados/actualizados. Credenciales enviadas por correo.</h2></div>`;
          addSystemNotification('admin', 'Importación masiva completada: 12,410 registros migrados.');
        }
      }, 200);
    }

    zone.ondragover = (e) => { e.preventDefault(); zone.classList.add('is-dragover'); };
    zone.ondragleave = () => zone.classList.remove('is-dragover');
    zone.ondrop = (e) => {
      e.preventDefault();
      zone.classList.remove('is-dragover');
      if (e.dataTransfer.files[0]) handleFile(e.dataTransfer.files[0]);
    };
    input.onchange = (e) => { if (e.target.files[0]) handleFile(e.target.files[0]); };
  }

  // ==================== MATCHMAKING ACTION CONFIRMATION ====================
  function confirmMatch(donationId, orgName) {
    // 1. Remove donation from matchmaking select list
    window.DT_Data.matchmaker.pendingDonations = window.DT_Data.matchmaker.pendingDonations.filter(p => p.id !== donationId);
    
    // 2. Add as active assigned donation in database
    const donationObj = {
      id: donationId,
      fecha: new Date().toISOString().split('T')[0],
      donante: 'Donante Presencial',
      entidad: orgName,
      tipo: donationId === 'DON-1033' ? 'Ropa' : 'Alimentos',
      estado: 'Asignación realizada'
    };
    window.DT_Data.activity.unshift(donationObj);

    // Hide matchmaking results container
    document.getElementById('algoResultsContainer').setAttribute('hidden', '');
    
    // Refresh lists
    renderAdminMatchmaking();
    showToast(`Donación ${donationId} asignada con éxito a ${orgName}`, 'success');
    addSystemNotification('admin', `Donación ${donationId} asignada a ${orgName}.`);
  }

  // ==================== MODALS & FORM CONTROLLERS ====================
  function openConfirmDeliveryModal(donationId) {
    const host = document.getElementById('modalHost');
    host.innerHTML = `
      <section role="dialog" tabindex="-1" aria-modal="true" class="slds-modal slds-fade-in-open slds-modal_small">
        <div class="slds-modal__container">
          <header class="slds-modal__header">
            <button class="slds-button slds-button_icon slds-modal__close slds-button_icon-inverse" onclick="document.getElementById('modalHost').innerHTML = ''" title="Cerrar y cancelar">
              <svg class="slds-button__icon slds-button__icon_large" aria-hidden="true" style="fill: currentColor; width: 24px; height: 24px;">
                <use xlink:href="/assets/slds-icons/utility-sprite/svg/symbols.svg#close"></use>
              </svg>
              <span class="slds-assistive-text">Cerrar y cancelar</span>
            </button>
            <h2 class="slds-modal__title slds-hyphenate">Confirmar Recepción de Donación</h2>
          </header>
          <div class="slds-modal__content slds-p-around_medium">
            <p class="slds-m-bottom_medium">Confirma la entrega física de la donación <strong>${donationId}</strong> en las instalaciones de tu sede:</p>
            
            <div class="slds-form-element slds-m-bottom_small">
              <label class="slds-form-element__label" for="deliveryComment">
                <abbr class="slds-required" title="required" aria-hidden="true">* </abbr>Comentarios / Observaciones de Recepción
              </label>
              <div class="slds-form-element__control">
                <textarea class="slds-textarea" id="deliveryComment" placeholder="Ej: Recibido en perfecto estado, cajas completas..." required></textarea>
              </div>
            </div>
            
            <div class="slds-form-element">
              <label class="slds-form-element__label">Carga de Prueba Fotográfica (Simulado)</label>
              <div class="slds-form-element__control">
                <div class="slds-file-selector slds-file-selector_files">
                  <div class="slds-file-selector__dropzone">
                    <input class="slds-file-selector__input slds-assistive-text" type="file" id="deliveryPhotoFile" accept="image/*" />
                    <label class="slds-file-selector__body" for="deliveryPhotoFile">
                      <span class="slds-file-selector__button slds-button slds-button_neutral">
                        Subir Foto Prueba
                      </span>
                    </label>
                  </div>
                </div>
                <div id="simulatedPhotoMsg" class="slds-text-body_small slds-text-color_success slds-m-top_x-small" style="display:none">
                  ✓ Foto cargada correctamente.
                </div>
              </div>
            </div>
          </div>
          <footer class="slds-modal__footer">
            <button class="slds-button slds-button_neutral" onclick="document.getElementById('modalHost').innerHTML = ''">Cancelar</button>
            <button class="slds-button slds-button_brand" id="modalConfirmBtn">Confirmar Recepción</button>
          </footer>
        </div>
      </section>
      <div class="slds-backdrop slds-backdrop_open"></div>
    `;

    // Photo input change
    const pInput = document.getElementById('deliveryPhotoFile');
    pInput.onchange = () => {
      document.getElementById('simulatedPhotoMsg').style.display = 'block';
    };

    const commentInput = document.getElementById('deliveryComment');
    if (commentInput) {
      commentInput.oninput = () => {
        validateSldsTextarea(commentInput, 'Ingresa observaciones de recepción.');
      };
    }

    // Confirm click
    document.getElementById('modalConfirmBtn').onclick = () => {
      if (!validateSldsTextarea(commentInput, 'Ingresa observaciones de recepción.')) {
        return;
      }

      const comment = commentInput.value.trim();
      
      // Update state in beneficiary model
      const item = window.DT_Data.beneficiaryProfile.assignedDonations.find(d => d.id === donationId);
      if (item) {
        item.status = 'Entregado';
        item.comment = comment;
        item.photoUrl = 'https://images.unsplash.com/photo-1594708767771-a7502209ff51?q=80&w=300';
      }

      // Also update general database if exists
      const act = window.DT_Data.activity.find(d => d.id === donationId);
      if (act) act.estado = 'Entregado';

      // Also update donante model if exists
      const donItem = window.DT_Data.donanteProfile.myDonations.find(d => d.id === donationId);
      if (donItem) {
        donItem.status = 'Entregado';
        
        // Dynamic points update for donor (gamification check!)
        addDonorPoints(1000); // Deliver reward bonus
        window.DT_Data.donanteProfile.donationsCount += 1;

        addSystemNotification('donante', `Tu donación ${donationId} fue recibida y confirmada por Fundación Esperanza.`);
      }

      // Refresh UI
      renderBeneficiariaDeliveries();
      renderBeneficiariaDashboard();
      
      host.innerHTML = '';
      showToast(`Recepción de ${donationId} confirmada con éxito.`, 'success');
      addSystemNotification('beneficiaria', `Entrega certificada para la donación ${donationId}.`);
    };
  }

  function openModal(id) {
    const host = document.getElementById('modalHost');
    let title = '';
    let body = '';

    if (id === 'modal-donante-registro') {
      title = 'Registrar Nueva Colaboración';
      body = `
        <form id="donationRegisterForm">
          <div class="slds-m-bottom_small">
            ${renderSldsComboboxHtml('regCategory', 'Categoría de Bien', 'Alimentos')}
          </div>
          <div class="slds-m-bottom_small">
            ${renderSldsComboboxHtml('regSubcategory', 'Subcategoría del Bien', 'Fideos secos')}
          </div>
          <div class="slds-m-bottom_small">
            <label class="slds-form-element__label">Cantidad Requerida</label>
            <div class="slds-grid slds-gutters_direct-xx-small">
              <div class="slds-col slds-size_2-of-3">
                <input class="slds-input" type="number" id="regQtyValue" min="1" value="10" required />
              </div>
              <div class="slds-col slds-size_1-of-3">
                ${renderSldsComboboxHtml('regQtyUnit', 'Unidad', 'unidades', 'slds-size_full')}
              </div>
            </div>
          </div>
          <div class="slds-m-bottom_small">
            ${renderSldsComboboxHtml('regCondition', 'Estado del Bien', 'Nuevo')}
          </div>
          <div class="slds-form-element slds-m-bottom_small">
            <label class="slds-form-element__label" for="regVencimiento">Fecha de Vencimiento (Si corresponde)</label>
            <div class="slds-form-element__control slds-input-has-icon slds-input-has-icon_right">
              <input id="regVencimiento" type="date" class="slds-input" />
              <button class="slds-button slds-button_icon slds-input__icon slds-input__icon_right" title="Seleccionar fecha" style="pointer-events: none; border: none; background: transparent;">
                <svg class="slds-button__icon" aria-hidden="true" style="fill: #706e6b; width: 16px; height: 16px;">
                  <use xlink:href="/assets/slds-icons/utility-sprite/svg/symbols.svg#event"></use>
                </svg>
                <span class="slds-assistive-text">Seleccionar fecha</span>
              </button>
            </div>
          </div>
        </form>
      `;
    } else if (id === 'modal-presencial') {
      title = 'Registrar persona donante presencial';
      body = `
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Nombre completo</label>
          <div class="slds-form-element__control"><input class="slds-input" type="text" placeholder="Ej: Juan Pérez"></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Documento</label>
          <div class="slds-form-element__control"><input class="slds-input" type="text"></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Email</label>
          <div class="slds-form-element__control"><input class="slds-input" type="email"></div></div>
      `;
    } else if (id === 'modal-recepcion') {
      title = 'Registrar recepción de donación (Depósito)';
      body = `
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Donante</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>María González</option><option>Carlos Pérez</option></select></div></div></div>
        <div class="slds-form-element slds-m-bottom_small"><label class="slds-form-element__label">Tipo de donación</label>
          <div class="slds-form-element__control"><div class="slds-select_container"><select class="slds-select"><option>Alimentos</option><option>Medicamentos</option><option>Ropa</option></select></div></div></div>
        <div class="slds-form-element"><label class="slds-form-element__label">Cantidad</label>
          <div class="slds-form-element__control"><input class="slds-input" type="number" min="1" value="10"></div></div>
      `;
    } else if (id === 'modal-vencidas') {
      title = 'Actualizar donaciones vencidas';
      body = `
        <p class="slds-text-body_regular slds-m-bottom_small">Selecciona las donaciones a marcar como vencidas:</p>
        <table class="slds-table slds-table_bordered slds-table_cell-buffer">
          <thead><tr><th></th><th>ID</th><th>Tipo</th><th>Vencimiento</th></tr></thead>
          <tbody>
            <tr><td><input type="checkbox" class="slds-checkbox" checked></td><td>DON-1032</td><td>Alimentos</td><td>2026-06-15</td></tr>
          </tbody>
        </table>
      `;
    } else if (id === 'modal-asignar') {
      title = 'Asignar donación a entidad final';
      body = `<p class="slds-text-body_regular">Ve a la pestaña <strong>Matchmaking</strong> en el menú lateral para ejecutar los algoritmos de asignación.</p>`;
    } else {
      title = 'Información';
      body = `<p class="slds-text-body_regular">Acción rápida ejecutada correctamente.</p>`;
    }

    host.innerHTML = `
      <section role="dialog" tabindex="-1" aria-modal="true" class="slds-modal slds-fade-in-open slds-modal_medium">
        <div class="slds-modal__container">
          <header class="slds-modal__header">
            <button class="slds-button slds-button_icon slds-modal__close slds-button_icon-inverse" onclick="document.getElementById('modalHost').innerHTML = ''" title="Cerrar y cancelar">
              <svg class="slds-button__icon slds-button__icon_large" aria-hidden="true" style="fill: currentColor; width: 24px; height: 24px;">
                <use xlink:href="/assets/slds-icons/utility-sprite/svg/symbols.svg#close"></use>
              </svg>
              <span class="slds-assistive-text">Cerrar y cancelar</span>
            </button>
            <h2 class="slds-modal__title slds-hyphenate">${title}</h2>
          </header>
          <div class="slds-modal__content slds-p-around_medium">${body}</div>
          <footer class="slds-modal__footer">
            <button class="slds-button slds-button_neutral" onclick="document.getElementById('modalHost').innerHTML = ''">Cancelar</button>
            <button class="slds-button slds-button_brand" id="modalSaveBtn">Guardar</button>
          </footer>
        </div>
      </section>
      <div class="slds-backdrop slds-backdrop_open"></div>
    `;

    // Setup dynamic subcategories for Donante registration
    if (id === 'modal-donante-registro') {
      const onCategoryChange = (newCat) => {
        const subs = SUBCATEGORIES[newCat] || [];
        bindSldsCombobox('regSubcategory', subs, subs[0] || '', null);
      };

      bindSldsCombobox('regCategory', ['Alimentos', 'Vestimenta', 'Mobiliario'], 'Alimentos', onCategoryChange);
      bindSldsCombobox('regSubcategory', SUBCATEGORIES['Alimentos'], SUBCATEGORIES['Alimentos'][0], null);
      bindSldsCombobox('regQtyUnit', ['unidades', 'kg', 'litros', 'gramos'], 'unidades', null);
      bindSldsCombobox('regCondition', ['Nuevo', 'Usado'], 'Nuevo', null);
    }

    // Save button click
    document.getElementById('modalSaveBtn').onclick = () => {
      if (id === 'modal-donante-registro') {
        const getVal = (comboId) => document.getElementById(`combo-val-${comboId}`).getAttribute('data-value');

        const cat = getVal('regCategory');
        const sub = getVal('regSubcategory');
        const qtyVal = document.getElementById('regQtyValue').value;
        const qtyUnit = getVal('regQtyUnit');
        const qty = `${qtyVal} ${qtyUnit}`;
        
        if (!sub || !qtyVal) return;

        const newDon = {
          id: 'DON-' + Math.floor(Math.random() * 900 + 1000),
          category: cat,
          subcategory: sub,
          quantity: qty,
          status: 'En depósito',
          date: new Date().toISOString().split('T')[0],
          destination: 'Pendiente de Asignación'
        };

        // Add to database
        window.DT_Data.donanteProfile.myDonations.unshift(newDon);
        window.DT_Data.activity.unshift({
          id: newDon.id,
          fecha: newDon.date,
          donante: currentUser.name,
          entidad: 'Pendiente',
          tipo: cat,
          estado: 'En depósito'
        });

        // Award Points (gamification!)
        addDonorPoints(500);
        window.DT_Data.donanteProfile.donationsCount += 1;
        
        // Add to matchmaking queue
        window.DT_Data.matchmaker.pendingDonations.push({
          id: newDon.id,
          category: cat,
          subcategory: sub,
          quantity: qty,
          status: 'En depósito'
        });

        renderDonanteDonations();
        showToast('Donación registrada con éxito. ¡Sumaste +500 Puntos!', 'success');
        addSystemNotification('donante', `Registraste con éxito tu donación ${newDon.id}.`);
      } else if (id === 'modal-recepcion') {
        showToast('Recepción de donación ingresada en el depósito.', 'success');
        addSystemNotification('admin', 'Nueva recepción física de donación registrada en depósito.');
      } else if (id === 'modal-vencidas') {
        // Update DON-1032
        const item = window.DT_Data.activity.find(a => a.id === 'DON-1032');
        if (item) item.estado = 'Vencido';
        showToast('Donaciones vencidas actualizadas.', 'warning');
        addSystemNotification('admin', 'Se marcaron 1 donaciones perecederas vencidas.');
        renderAdminDashboard();
      } else {
        showToast('Cambios guardados con éxito', 'success');
      }

      host.innerHTML = '';
    };
  }

  function showToast(message, type = 'info') {
    let themeClass = 'slds-theme_info';
    let iconText = 'ℹ️';
    if (type === 'success') {
      themeClass = 'slds-theme_success';
      iconText = '✅';
    } else if (type === 'warning') {
      themeClass = 'slds-theme_warning';
      iconText = '⚠️';
    } else if (type === 'error') {
      themeClass = 'slds-theme_error';
      iconText = '❌';
    }

    const toastHtml = `
      <div class="slds-notify_container" style="top: 20px; pointer-events: none;">
        <div class="slds-notify slds-notify_toast ${themeClass}" role="status" style="pointer-events: auto;">
          <span class="slds-assistive-text">${type}</span>
          <span class="slds-icon_container slds-m-right_small slds-no-flex slds-align-top" style="font-size: 18px; line-height: 1;">
            ${iconText}
          </span>
          <div class="slds-notify__content" style="flex-grow: 1;">
            <h2 class="slds-text-heading_small">${message}</h2>
          </div>
          <div class="slds-notify__close">
            <button class="slds-button slds-button_icon slds-button_icon-inverse" title="Cerrar" style="border: none; background: transparent; color: white;" onclick="this.closest('.dt-toast-wrapper').remove()">
              <span style="font-size: 20px; font-weight: bold; line-height: 1;">&times;</span>
              <span class="slds-assistive-text">Cerrar</span>
            </button>
          </div>
        </div>
      </div>
    `;
    const toastNode = document.createElement('div');
    toastNode.className = 'dt-toast-wrapper';
    toastNode.style.pointerEvents = 'none';
    toastNode.style.position = 'fixed';
    toastNode.style.top = '0';
    toastNode.style.left = '0';
    toastNode.style.right = '0';
    toastNode.style.zIndex = '10000';
    toastNode.innerHTML = toastHtml;
    document.body.appendChild(toastNode);
    setTimeout(() => {
      if (toastNode.parentNode) {
        toastNode.remove();
      }
    }, 4000);
  }

  // ----- App Entry Point -----
  function init() {
    setupShell();
    renderNotifications();

    // Listen to hash router changes
    window.addEventListener('hashchange', handleRouting);
    handleRouting();
  }

  global.DT_App = {
    init,
    openModal,
    openConfirmDeliveryModal,
    confirmMatch
  };

})(window);
