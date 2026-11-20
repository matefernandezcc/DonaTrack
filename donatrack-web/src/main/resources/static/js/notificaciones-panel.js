/**
 * DonaTrack — Centro de Notificaciones SLDS en Header (UX-08 / UX-13)
 * Combina lo mejor de ambas arquitecturas:
 * - Historial aislado por usuario en LocalStorage (máx. 30 alertas) con estado leída/no leída.
 * - Captura automática de toasts flash (flashOk / flashError) con auto-cierre suave a los 3s.
 * - Eliminación individual con botón ×, botón "Vaciar historial" y soporte completo para Modo Oscuro.
 */
(function () {
  'use strict';

  var MAX_NOTIFS = 30;
  var MAX_DISMISSED = 200;

  function getUserKeySuffix() {
    var btn = document.getElementById('btn-notificaciones-toggle');
    var user = (btn && btn.getAttribute('data-user')) ? btn.getAttribute('data-user').trim() : 'anon';
    return (user || 'anon').toLowerCase();
  }

  function getStorageKey() {
    return 'donatrack_notif_feed_' + getUserKeySuffix();
  }

  function getDismissedKey() {
    return 'donatrack_notif_dismissed_' + getUserKeySuffix();
  }

  function getClearFlagKey() {
    return 'donatrack_notif_cleared_' + getUserKeySuffix();
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
    var d = new Date(rawFecha);
    if (!isNaN(d.getTime())) {
      var hoy = new Date();
      var esMismoDia = d.getDate() === hoy.getDate() &&
                       d.getMonth() === hoy.getMonth() &&
                       d.getFullYear() === hoy.getFullYear();
      var hh = ('0' + d.getHours()).slice(-2);
      var mm = ('0' + d.getMinutes()).slice(-2);
      if (esMismoDia) {
        return 'Hoy a las ' + hh + ':' + mm;
      }
      var dd = ('0' + d.getDate()).slice(-2);
      var mes = ('0' + (d.getMonth() + 1)).slice(-2);
      return dd + '/' + mes + ' ' + hh + ':' + mm;
    }
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
      localStorage.setItem(getStorageKey(), JSON.stringify((feed || []).slice(0, MAX_NOTIFS)));
    } catch (e) {}
  }

  function getDismissedIds() {
    try {
      var raw = localStorage.getItem(getDismissedKey());
      var parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  function agregarDismissedIds(idsToAdd) {
    if (!idsToAdd || !idsToAdd.length) return;
    try {
      var ids = getDismissedIds();
      idsToAdd.forEach(function (id) {
        if (id && ids.indexOf(id) === -1) {
          ids.push(id);
        }
      });
      if (ids.length > MAX_DISMISSED) {
        ids = ids.slice(-MAX_DISMISSED);
      }
      localStorage.setItem(getDismissedKey(), JSON.stringify(ids));
    } catch (e) {}
  }

  function normalizeItem(raw) {
    var msg = (raw && raw.mensaje) ? String(raw.mensaje).trim() : 'Notificación del sistema';
    var fecha = (raw && raw.fechaEnvio) ? String(raw.fechaEnvio) : new Date().toISOString();
    var medio = (raw && raw.medio) ? String(raw.medio).toUpperCase() : 'SISTEMA';
    var tipo = (raw && raw.tipo) ? String(raw.tipo).toLowerCase() : (raw && raw.esError ? 'error' : 'info');
    var key = (raw && raw.id != null) ? String(raw.id) : (medio + '|' + msg);
    return {
      id: key,
      mensaje: msg,
      medio: medio,
      tipo: tipo,
      fechaEnvio: fecha,
      completada: raw && raw.completada === false ? false : true,
      esError: Boolean((raw && raw.esError) || tipo === 'error'),
      isLocal: Boolean(raw && raw.isLocal),
      leida: Boolean(raw && raw.leida)
    };
  }

  function mergeIntoFeed(incomingList) {
    var dismissed = getDismissedIds();
    var current = loadStoredFeed().filter(function (item) {
      return item && item.id && dismissed.indexOf(item.id) === -1;
    });
    var byKey = {};
    current.forEach(function (item) {
      byKey[item.id] = item;
    });

    (incomingList || []).forEach(function (raw) {
      var item = normalizeItem(raw);
      if (dismissed.indexOf(item.id) !== -1) return;
      if (!byKey[item.id]) {
        current.unshift(item);
        byKey[item.id] = item;
      }
    });

    current.sort(function (a, b) {
      var tA = a.fechaEnvio ? new Date(a.fechaEnvio).getTime() : 0;
      var tB = b.fechaEnvio ? new Date(b.fechaEnvio).getTime() : 0;
      return tB - tA;
    });

    var trimmed = current.slice(0, MAX_NOTIFS);
    saveStoredFeed(trimmed);
    return trimmed;
  }

  function actualizarBadge(feed) {
    var unread = 0;
    (feed || []).forEach(function (x) {
      if (x && !x.leida) unread++;
    });

    var badgeEl = document.getElementById('notif-badge-count');
    if (badgeEl) {
      badgeEl.textContent = String(unread);
      badgeEl.style.display = unread > 0 ? 'inline-block' : 'none';
    }

    var panelBadgeEl = document.getElementById('panel-notif-badge');
    if (panelBadgeEl) {
      panelBadgeEl.textContent = String(unread);
      panelBadgeEl.style.display = unread > 0 ? 'inline-block' : 'none';
    }
  }

  function eliminarNotificacionIndividual(id, li) {
    if (id) {
      agregarDismissedIds([id]);
    }
    var feed = loadStoredFeed().filter(function (item) {
      return item && item.id !== id;
    });
    saveStoredFeed(feed);

    if (li) {
      li.style.opacity = '0';
      li.style.transform = 'translateX(10px)';
    }

    setTimeout(function () {
      if (feed.length === 0) {
        actualizarUI('empty', []);
      } else {
        actualizarUI('ok', feed);
      }
    }, 180);
  }

  function renderListaEnContenedor(container, alertas, isPanelCard) {
    if (!container) return;
    container.innerHTML = '';

    alertas.forEach(function (a) {
      var li = document.createElement('li');
      var unreadClass = a.leida ? '' : ' dt-notif-item_unread';
      li.className = (isPanelCard
        ? 'slds-p-around_small slds-card dt-notif-panel-item'
        : 'slds-item slds-p-vertical_x-small slds-p-horizontal_small dt-notif-item') + unreadClass;

      if (!isPanelCard) {
        li.style.borderBottom = '1px solid #e2e8f0';
      } else {
        li.style.border = '1px solid #e2e8f0';
        li.style.borderRadius = '6px';
        li.style.marginBottom = '6px';
      }
      li.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
      if (a.id) li.setAttribute('data-notif-id', a.id);

      var medio;
      var badgeClass = 'dt-notif-badge dt-notif-badge_backend';
      if (a.esError || a.tipo === 'error') {
        medio = 'ALERTA';
        badgeClass = 'dt-notif-badge dt-notif-badge_error';
      } else if (a.tipo === 'success') {
        medio = 'ÉXITO';
        badgeClass = 'dt-notif-badge dt-notif-badge_success';
      } else if (a.medio === 'SISTEMA' || a.isLocal) {
        medio = 'SISTEMA';
        badgeClass = 'dt-notif-badge dt-notif-badge_system';
      } else {
        medio = escapeHtml((a.medio || 'EMAIL').toString().toUpperCase());
      }

      var mensaje = escapeHtml(a.mensaje || 'Notificación del sistema');
      var fecha = escapeHtml(formatearFecha(a.fechaEnvio));
      var dotHtml = a.leida
        ? ''
        : '<span title="No leída" style="display:inline-block;width:7px;height:7px;border-radius:50%;background:#0176d3;margin-right:6px;flex-shrink:0;"></span>';

      li.innerHTML =
        '<div class="slds-grid slds-grid_align-spread slds-grid_vertical-align-start" style="gap: 8px;">' +
          '<div style="min-width: 0; flex: 1;">' +
            '<div style="display: flex; align-items: center;">' +
              dotHtml +
              '<p class="slds-text-body_regular dt-notif-msg" style="font-weight: ' + (a.leida ? '500' : '700') + '; font-size: 0.84rem; line-height: 1.35; margin: 0; word-break: break-word;">' +
                mensaje +
              '</p>' +
            '</div>' +
            '<span class="slds-text-body_small slds-text-color_weak dt-notif-time" style="font-size: 0.72rem; display: block; margin-top: 2px;">' +
              fecha +
            '</span>' +
          '</div>' +
          '<div class="slds-grid slds-grid_vertical-align-center" style="gap: 6px; flex-shrink: 0;">' +
            '<span class="slds-badge ' + badgeClass + '" style="font-size: 0.65rem; font-weight: 700; padding: 2px 6px; border-radius: 4px;">' +
              medio +
            '</span>' +
            '<button type="button" class="dt-notif-delete-btn" aria-label="Eliminar notificación" title="Eliminar notificación">' +
              '&times;' +
              '<span class="slds-assistive-text">Eliminar notificación</span>' +
            '</button>' +
          '</div>' +
        '</div>';

      var deleteBtn = li.querySelector('.dt-notif-delete-btn');
      if (deleteBtn) {
        deleteBtn.addEventListener('click', function (e) {
          e.preventDefault();
          e.stopPropagation();
          eliminarNotificacionIndividual(a.id, li);
        });
      }

      container.appendChild(li);
    });
  }

  function actualizarContenedor(ids, state, data, errorMsg, isPanelCard) {
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
        listEl.style.display = isPanelCard ? 'flex' : 'block';
        renderListaEnContenedor(listEl, data, isPanelCard);
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
      errorMsg,
      false
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
      errorMsg,
      true
    );
  }

  function marcarTodasComoLeidas() {
    var feed = loadStoredFeed();
    var changed = false;
    feed = feed.map(function (item) {
      if (item && !item.leida) {
        item.leida = true;
        changed = true;
      }
      return item;
    });
    if (changed) {
      saveStoredFeed(feed);
      actualizarBadge(feed);
      if (feed.length > 0) {
        actualizarUI('ok', feed);
      }
    }
  }

  function limpiarNotificaciones() {
    var current = loadStoredFeed();
    var ids = current.map(function (item) { return item && item.id; }).filter(Boolean);
    agregarDismissedIds(ids);
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
      method: 'GET',
      credentials: 'same-origin',
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
        var backendNormalizadas = incoming.map(function (b) {
          return {
            id: b.id != null ? 'backend-' + b.id : ('backend|' + (b.mensaje || '')),
            mensaje: b.mensaje,
            medio: b.medio || 'EMAIL',
            tipo: b.tipo || 'info',
            fechaEnvio: b.fechaEnvio,
            completada: b.completada !== false,
            esError: Boolean(b.esError),
            isLocal: false,
            leida: false
          };
        });
        if (backendNormalizadas.length > 0) {
          try { localStorage.removeItem(getClearFlagKey()); } catch (e) {}
        }
        var merged = mergeIntoFeed(backendNormalizadas);
        if (merged.length === 0) {
          actualizarUI('empty', []);
        } else {
          actualizarUI('ok', merged);
        }
      })
      .catch(function () {
        if (timeoutId) clearTimeout(timeoutId);
        var fallbackFeed = loadStoredFeed();
        if (fallbackFeed.length > 0) {
          actualizarUI('ok', fallbackFeed);
        } else {
          actualizarUI('empty', []);
        }
      });
  }

  function registrarToastEnFeed(mensaje, tipo) {
    if (!mensaje || !mensaje.trim()) return;
    var cleanMsg = mensaje.trim();
    var current = loadStoredFeed();
    var yaExisteReciente = current.some(function (item) {
      return item && item.mensaje === cleanMsg &&
        (Date.now() - new Date(item.fechaEnvio).getTime() < 4000);
    });
    if (yaExisteReciente) return;

    var esError = tipo === 'error';
    try { localStorage.removeItem(getClearFlagKey()); } catch (e) {}
    var merged = mergeIntoFeed([{
      id: 'notif-' + Date.now() + '-' + Math.floor(Math.random() * 1000),
      mensaje: cleanMsg,
      medio: esError ? 'ALERTA' : 'SISTEMA',
      tipo: esError ? 'error' : 'success',
      fechaEnvio: new Date().toISOString(),
      completada: true,
      esError: esError,
      isLocal: true,
      leida: false
    }]);

    if (merged.length > 0) {
      actualizarUI('ok', merged);
    }
  }

  function configurarAutoCierreToast(toast) {
    if (!toast) return;
    var cerrado = false;
    var timerId = null;

    function cerrarToast() {
      if (cerrado) return;
      cerrado = true;
      if (timerId) {
        clearTimeout(timerId);
        timerId = null;
      }
      toast.classList.add('dt-toast-hidden');
      setTimeout(function () {
        if (toast && toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 400);
    }

    var closeButtons = toast.querySelectorAll('.dt-toast-close, button.slds-button_icon');
    closeButtons.forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        cerrarToast();
      });
    });

    // Auto-cierre suave a los 3 segundos (3000 ms)
    timerId = setTimeout(cerrarToast, 3000);
  }

  function inicializarToasts() {
    var mainToast = document.getElementById('toast-notification');
    if (mainToast) {
      var msg = mainToast.getAttribute('data-mensaje');
      if (!msg) {
        var h2 = mainToast.querySelector('h2');
        if (h2) msg = h2.textContent.trim();
      }
      var tipo = mainToast.getAttribute('data-tipo') || 'success';
      registrarToastEnFeed(msg, tipo);
      configurarAutoCierreToast(mainToast);
    }

    var okToast = document.getElementById('dt-flash-ok-toast');
    if (okToast) {
      registrarToastEnFeed(okToast.getAttribute('data-flash-msg'), 'success');
      configurarAutoCierreToast(okToast);
    }

    var errToast = document.getElementById('dt-flash-error-toast');
    if (errToast) {
      registrarToastEnFeed(errToast.getAttribute('data-flash-msg'), 'error');
      configurarAutoCierreToast(errToast);
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    inicializarToasts();

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
