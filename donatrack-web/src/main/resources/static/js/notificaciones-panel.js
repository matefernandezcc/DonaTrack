/**
 * DonaTrack — Centro de Notificaciones SLDS en Header (UX-08 / UX-13)
 * Acumula notificaciones del backend (GET /api/notificaciones/mis-alertas) y
 * alertas operativas (flashOk / flashError) en un feed por usuario.
 */
(function () {
  'use strict';

  var MAX_NOTIFS = 30;

  function getStorageKey() {
    var btn = document.getElementById('btn-notificaciones-toggle');
    var user = (btn && btn.getAttribute('data-user')) ? btn.getAttribute('data-user').trim() : 'anon';
    return 'donatrack_notif_feed_' + (user || 'anon').toLowerCase();
  }

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

  function loadStoredFeed() {
    try {
      var raw = localStorage.getItem(getStorageKey());
      var parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  function saveStoredFeed(feed) {
    try {
      localStorage.setItem(getStorageKey(), JSON.stringify(feed.slice(0, MAX_NOTIFS)));
    } catch (e) {}
  }

  function normalizeItem(raw) {
    var msg = (raw && raw.mensaje) ? String(raw.mensaje).trim() : 'Notificación del sistema';
    var fecha = (raw && raw.fechaEnvio) ? String(raw.fechaEnvio) : new Date().toISOString();
    var medio = (raw && raw.medio) ? String(raw.medio).toUpperCase() : 'SISTEMA';
    var key = (raw && raw.id) ? String(raw.id) : (medio + '|' + msg);
    return {
      id: key,
      mensaje: msg,
      medio: medio,
      fechaEnvio: fecha,
      completada: raw && raw.completada === false ? false : true,
      esError: Boolean(raw && raw.esError),
      leida: Boolean(raw && raw.leida)
    };
  }

  function mergeIntoFeed(incomingList) {
    var current = loadStoredFeed();
    var byKey = {};
    current.forEach(function (item) {
      if (item && item.id) byKey[item.id] = item;
    });

    (incomingList || []).forEach(function (raw) {
      var item = normalizeItem(raw);
      if (!byKey[item.id]) {
        current.unshift(item);
        byKey[item.id] = item;
      }
    });

    current.sort(function (a, b) {
      return String(b.fechaEnvio || '').localeCompare(String(a.fechaEnvio || ''));
    });

    var trimmed = current.slice(0, MAX_NOTIFS);
    saveStoredFeed(trimmed);
    return trimmed;
  }

  function captureFlashToasts() {
    var captured = [];
    var okToast = document.getElementById('dt-flash-ok-toast');
    if (okToast) {
      var okMsg = okToast.getAttribute('data-flash-msg');
      if (okMsg && okMsg.trim()) {
        captured.push({
          id: 'flash-ok|' + okMsg.trim(),
          mensaje: okMsg.trim(),
          medio: 'SISTEMA',
          fechaEnvio: new Date().toISOString(),
          completada: true,
          esError: false,
          leida: false
        });
      }
    }
    var errToast = document.getElementById('dt-flash-error-toast');
    if (errToast) {
      var errMsg = errToast.getAttribute('data-flash-msg');
      if (errMsg && errMsg.trim()) {
        captured.push({
          id: 'flash-err|' + errMsg.trim(),
          mensaje: errMsg.trim(),
          medio: 'ALERTA',
          fechaEnvio: new Date().toISOString(),
          completada: true,
          esError: true,
          leida: false
        });
      }
    }
    if (captured.length > 0) {
      mergeIntoFeed(captured);
    }
  }

  function renderListaEnContenedor(container, alertas) {
    if (!container) return;
    container.innerHTML = '';

    alertas.forEach(function (a) {
      var li = document.createElement('li');
      li.className = 'slds-item slds-p-vertical_x-small slds-p-horizontal_small';
      li.style.borderBottom = '1px solid #e5e7eb';
      li.style.backgroundColor = a.leida ? '#ffffff' : '#f0f7ff';

      var medio = escapeHtml((a.medio || 'SISTEMA').toString().toUpperCase());
      var mensaje = escapeHtml(a.mensaje || 'Notificación del sistema');
      var fecha = escapeHtml(formatearFecha(a.fechaEnvio));
      var badgeBg = a.esError ? '#fee2e2' : '#e0f2fe';
      var badgeColor = a.esError ? '#991b1b' : '#014486';
      var dotHtml = a.leida
        ? ''
        : '<span title="No leída" style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#0176d3;margin-right:6px;flex-shrink:0;"></span>';

      li.innerHTML =
        '<div class="slds-media slds-media_small slds-grid_align-spread" style="gap: 8px; align-items: flex-start;">' +
          '<div class="slds-media__body" style="min-width: 0; flex: 1;">' +
            '<div style="display: flex; align-items: center; gap: 4px;">' +
              dotHtml +
              '<p class="slds-text-body_regular" style="color: #0f172a; font-weight: ' + (a.leida ? '500' : '700') + '; font-size: 0.84rem; line-height: 1.35; margin: 0;">' +
                mensaje +
              '</p>' +
            '</div>' +
            '<span class="slds-text-body_small slds-text-color_weak" style="font-size: 0.72rem; display: block; margin-top: 2px;">' +
              fecha +
            '</span>' +
          '</div>' +
          '<span class="slds-badge" style="background:' + badgeBg + '; color:' + badgeColor + '; font-size: 0.66rem; font-weight: 700; flex-shrink: 0;">' +
            medio +
          '</span>' +
        '</div>';

      container.appendChild(li);
    });
  }

  function actualizarBadge(feed) {
    var badgeEl = document.getElementById('notif-badge-count');
    if (!badgeEl) return;
    var unread = 0;
    (feed || []).forEach(function (x) {
      if (!x.leida) unread++;
    });
    var displayCount = unread > 0 ? unread : 0;
    badgeEl.textContent = String(displayCount);
    badgeEl.style.display = displayCount > 0 ? 'inline-block' : 'none';
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
    if (state === 'ok' || state === 'empty') {
      actualizarBadge(data);
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
  }

  function marcarTodasComoLeidas() {
    var feed = loadStoredFeed().map(function (item) {
      item.leida = true;
      return item;
    });
    saveStoredFeed(feed);
    actualizarBadge(feed);
  }

  function getClearFlagKey() {
    var btn = document.getElementById('btn-notificaciones-toggle');
    var user = (btn && btn.getAttribute('data-user')) ? btn.getAttribute('data-user').trim() : 'anon';
    return 'donatrack_notif_cleared_' + (user || 'anon').toLowerCase();
  }

  function limpiarNotificaciones() {
    saveStoredFeed([]);
    try { localStorage.setItem(getClearFlagKey(), '1'); } catch (e) {}
    actualizarUI('empty', []);
  }

  function cargarAlertas() {
    var hasHeaderWidget = document.getElementById('btn-notificaciones-toggle');
    if (!hasHeaderWidget) return;

    var cached = loadStoredFeed();
    if (cached.length > 0) {
      actualizarUI('ok', cached);
    } else {
      actualizarUI('loading', []);
    }

    var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    var timeoutId = controller ? setTimeout(function () { controller.abort(); }, 6000) : null;

    fetch('/api/notificaciones/mis-alertas', {
      headers: { 'Accept': 'application/json' },
      signal: controller ? controller.signal : undefined
    })
      .then(function (res) {
        if (timeoutId) clearTimeout(timeoutId);
        if (!res.ok) {
          throw new Error('No se pudieron cargar las notificaciones en este momento.');
        }
        return res.json();
      })
      .then(function (data) {
        var incoming = Array.isArray(data) ? data : [];
        if (incoming.length > 0) {
          try { localStorage.removeItem(getClearFlagKey()); } catch (e) {}
        }
        var merged = mergeIntoFeed(incoming);
        var cleared = false;
        try { cleared = localStorage.getItem(getClearFlagKey()) === '1'; } catch (e) {}
        if (merged.length === 0 && !cleared) {
          merged = mergeIntoFeed([{
            id: 'sistema-sesion-activa',
            mensaje: 'Sesión activa en DonaTrack. Aquí se acumularán tus alertas operativas, remitos y entregas.',
            medio: 'SISTEMA',
            fechaEnvio: new Date().toISOString(),
            completada: true,
            esError: false,
            leida: false
          }]);
        }
        if (merged.length === 0) {
          actualizarUI('empty', []);
        } else {
          actualizarUI('ok', merged);
        }
      })
      .catch(function () {
        if (timeoutId) clearTimeout(timeoutId);
        var fallbackFeed = loadStoredFeed();
        if (fallbackFeed.length === 0) {
          fallbackFeed = mergeIntoFeed([{
            id: 'sistema-sesion-activa',
            mensaje: 'Sesión activa en DonaTrack. Aquí se acumularán tus alertas operativas, remitos y entregas.',
            medio: 'SISTEMA',
            fechaEnvio: new Date().toISOString(),
            completada: true,
            esError: false,
            leida: false
          }]);
        }
        actualizarUI('ok', fallbackFeed);
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    captureFlashToasts();

    var toggleBtn = document.getElementById('btn-notificaciones-toggle');
    var popover = document.getElementById('popover-notificaciones');
    var closeBtn = document.getElementById('btn-notificaciones-close');
    var clearBtn = document.getElementById('btn-notificaciones-clear');

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
          setTimeout(marcarTodasComoLeidas, 800);
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

    if (clearBtn) {
      clearBtn.addEventListener('click', limpiarNotificaciones);
    }

    ['btn-notificaciones-refresh', 'notif-retry-btn'].forEach(function (id) {
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
