/**
 * DonaTrack — Panel y Popover de Notificaciones SLDS (UX-08 / UX-13)
 * Consulta GET /api/notificaciones/mis-alertas de forma asíncrona sin bloquear el SSR.
 * Maneja los 3 estados obligatorios: Carga, Error con recuperación y Estado Vacío.
 */
(function () {
  'use strict';

  function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatearFecha(rawFecha) {
    if (!rawFecha) return 'Reciente';
    var str = String(rawFecha);
    if (str.length >= 16 && str.indexOf('T') !== -1) {
      return str.substring(0, 10) + ' ' + str.substring(11, 16);
    }
    return str;
  }

  function renderListaEnContenedor(container, alertas) {
    if (!container) return;
    container.innerHTML = '';

    alertas.forEach(function (a) {
      var li = document.createElement('li');
      li.className = 'slds-item slds-p-vertical_x-small slds-p-horizontal_small';
      li.style.borderBottom = '1px solid #e2e8f0';

      var medio = escapeHtml((a.medio || 'EMAIL').toString().toUpperCase());
      var mensaje = escapeHtml(a.mensaje || 'Notificación del sistema');
      var fecha = escapeHtml(formatearFecha(a.fechaEnvio));
      var completada = a.completada === true;

      li.innerHTML =
        '<div class="slds-grid slds-grid_align-spread slds-grid_vertical-align-start" style="gap: 8px;">' +
          '<div style="min-width: 0; flex: 1;">' +
            '<p class="slds-text-body_regular" style="color: #0f172a; font-weight: 600; font-size: 0.85rem; line-height: 1.35; margin: 0;">' +
              mensaje +
            '</p>' +
            '<span class="slds-text-body_small slds-text-color_weak" style="font-size: 0.74rem;">' +
              fecha + (completada ? ' · Enviada' : ' · En proceso') +
            '</span>' +
          '</div>' +
          '<span class="slds-badge slds-badge_lightest" style="font-size: 0.68rem; font-weight: 700; flex-shrink: 0;">' +
            medio +
          '</span>' +
        '</div>';

      container.appendChild(li);
    });
  }

  function actualizarContenedor(ids, state, data, errorMsg) {
    var loadingEl = document.getElementById(ids.loading);
    var errorEl = document.getElementById(ids.error);
    var errorTextEl = ids.errorText ? document.getElementById(ids.errorText) : null;
    var emptyEl = document.getElementById(ids.empty);
    var listEl = document.getElementById(ids.list);

    if (loadingEl) loadingEl.style.display = (state === 'loading') ? 'block' : 'none';
    if (errorEl) errorEl.style.display = (state === 'error') ? 'block' : 'none';
    if (errorTextEl && errorMsg) errorTextEl.textContent = errorMsg;
    if (emptyEl) emptyEl.style.display = (state === 'empty') ? 'block' : 'none';
    if (listEl) {
      if (state === 'ok') {
        listEl.style.display = 'block';
        renderListaEnContenedor(listEl, data);
      } else {
        listEl.style.display = 'none';
      }
    }
  }

  function actualizarUI(state, data, errorMsg) {
    var badgeEl = document.getElementById('notif-badge-count');
    var panelBadgeEl = document.getElementById('panel-notif-badge');

    if (state === 'ok' || state === 'empty') {
      var count = Array.isArray(data) ? data.length : 0;
      if (badgeEl) {
        badgeEl.textContent = String(count);
        badgeEl.style.display = count > 0 ? 'inline-block' : 'none';
      }
      if (panelBadgeEl) {
        panelBadgeEl.textContent = count + (count === 1 ? ' alerta' : ' alertas');
      }
    }

    actualizarContenedor(
      {
        loading: 'notif-state-loading',
        error: 'notif-state-error',
        errorText: 'notif-error-msg',
        empty: 'notif-state-empty',
        list: 'notif-lista-items'
      },
      state,
      data,
      errorMsg
    );

    actualizarContenedor(
      {
        loading: 'panel-notif-loading',
        error: 'panel-notif-error',
        errorText: 'panel-notif-error-msg',
        empty: 'panel-notif-empty',
        list: 'panel-notif-lista'
      },
      state,
      data,
      errorMsg
    );
  }

  function cargarAlertas() {
    var hasHeaderWidget = document.getElementById('btn-notificaciones-toggle');
    var hasPanelCard = document.getElementById('panel-notificaciones-card');
    if (!hasHeaderWidget && !hasPanelCard) return;

    actualizarUI('loading', []);

    var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    var timeoutId = controller ? setTimeout(function () { controller.abort(); }, 5000) : null;

    fetch('/api/notificaciones/mis-alertas', {
      headers: { 'Accept': 'application/json' },
      signal: controller ? controller.signal : undefined
    })
      .then(function (res) {
        if (timeoutId) clearTimeout(timeoutId);
        if (!res.ok) {
          return res.json().catch(function () { return {}; }).then(function (errBody) {
            throw new Error(errBody.message || ('Error HTTP ' + res.status));
          });
        }
        return res.json();
      })
      .then(function (data) {
        if (!Array.isArray(data) || data.length === 0) {
          actualizarUI('empty', []);
        } else {
          actualizarUI('ok', data);
        }
      })
      .catch(function (err) {
        if (timeoutId) clearTimeout(timeoutId);
        actualizarUI('error', [], err && err.message ? err.message : 'No se pudieron cargar las notificaciones.');
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    var toggleBtn = document.getElementById('btn-notificaciones-toggle');
    var popover = document.getElementById('popover-notificaciones');
    var closeBtn = document.getElementById('btn-notificaciones-close');

    function setPopoverOpen(open) {
      if (!popover || !toggleBtn) return;
      popover.style.display = open ? 'block' : 'none';
      toggleBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
    }

    if (toggleBtn && popover) {
      toggleBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        var isOpen = popover.style.display === 'block';
        setPopoverOpen(!isOpen);
        if (!isOpen) {
          cargarAlertas();
        }
      });

      document.addEventListener('click', function (e) {
        if (popover.style.display === 'block' && !popover.contains(e.target) && !toggleBtn.contains(e.target)) {
          setPopoverOpen(false);
        }
      });

      document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && popover.style.display === 'block') {
          setPopoverOpen(false);
          toggleBtn.focus();
        }
      });
    }

    if (closeBtn) {
      closeBtn.addEventListener('click', function () {
        setPopoverOpen(false);
      });
    }

    ['btn-notificaciones-refresh', 'notif-retry-btn', 'panel-notif-refresh', 'panel-notif-retry'].forEach(function (id) {
      var btn = document.getElementById(id);
      if (btn) {
        btn.addEventListener('click', cargarAlertas);
      }
    });

    cargarAlertas();

    setInterval(function () {
      if (document.visibilityState === 'visible') {
        cargarAlertas();
      }
    }, 45000);
  });

  window.recargarNotificacionesDonaTrack = cargarAlertas;
})();

