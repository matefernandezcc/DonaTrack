/**
 * DonaTrack — Panel y Popover de Notificaciones SLDS (UX-08 / UX-13)
 * Consulta GET /api/notificaciones/mis-alertas con credentials: 'same-origin'.
 * Sincroniza notificaciones locales (LocalStorage: 'donatrack_user_notifications') transferidas desde Toasts.
 * Maneja los estados obligatorios: Carga, Error resiliente y Estado Vacío, con soporte WCAG y SLDS.
 */
(function () {
  'use strict';

  var STORAGE_KEY = 'donatrack_user_notifications';
  var ultimaListaBackend = [];

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

  function getNotificacionesLocales() {
    try {
      var raw = localStorage.getItem(STORAGE_KEY);
      var parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      console.warn('[DonaTrack] Error leyendo notificaciones de localStorage', e);
      return [];
    }
  }

  function guardarNotificacionesLocales(lista) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(lista));
    } catch (e) {
      console.warn('[DonaTrack] Error guardando notificaciones en localStorage', e);
    }
  }

  function eliminarNotificacionLocal(id) {
    if (!id) return;
    var lista = getNotificacionesLocales();
    var filtrada = lista.filter(function (item) {
      return item.id !== id;
    });
    guardarNotificacionesLocales(filtrada);
  }

  function combinarAlertas(locales, backend) {
    var backendFormateadas = (backend || []).map(function (b) {
      return {
        id: b.id != null ? 'backend-' + b.id : ('backend-' + Math.random()),
        mensaje: b.mensaje,
        tipo: b.tipo || 'info',
        medio: b.medio || 'EMAIL',
        fechaEnvio: b.fechaEnvio,
        completada: b.completada !== false,
        isLocal: false
      };
    });

    var todas = (locales || []).map(function (loc) {
      return Object.assign({}, loc, { isLocal: true });
    }).concat(backendFormateadas);

    todas.sort(function (a, b) {
      var tA = a.fechaEnvio ? new Date(a.fechaEnvio).getTime() : 0;
      var tB = b.fechaEnvio ? new Date(b.fechaEnvio).getTime() : 0;
      return tB - tA;
    });

    return todas;
  }

  function actualizarConteoNotificaciones() {
    var popoverListEl = document.getElementById('notif-lista-items');
    var badgeEl = document.getElementById('notif-badge-count');
    var popoverEmptyEl = document.getElementById('notif-state-empty');

    var panelListEl = document.getElementById('panel-notif-lista');
    var panelBadgeEl = document.getElementById('panel-notif-badge');
    var panelEmptyEl = document.getElementById('panel-notif-empty');

    var count = 0;
    if (popoverListEl) {
      count = popoverListEl.querySelectorAll('li').length;
    } else if (panelListEl) {
      count = panelListEl.querySelectorAll('li').length;
    }

    if (badgeEl) {
      badgeEl.textContent = String(count);
      badgeEl.style.display = count > 0 ? 'inline-block' : 'none';
    }

    if (panelBadgeEl) {
      panelBadgeEl.textContent = String(count);
      panelBadgeEl.style.display = count > 0 ? 'inline-block' : 'none';
    }

    if (count === 0) {
      if (popoverListEl) popoverListEl.style.display = 'none';
      if (popoverEmptyEl) popoverEmptyEl.style.display = 'block';

      if (panelListEl) panelListEl.style.display = 'none';
      if (panelEmptyEl) panelEmptyEl.style.display = 'block';
    }
  }

  function renderListaEnContenedor(container, alertas, isPanelCard) {
    if (!container) return;
    container.innerHTML = '';

    alertas.forEach(function (a) {
      var li = document.createElement('li');
      li.className = isPanelCard
        ? 'slds-p-around_small slds-card'
        : 'slds-item slds-p-vertical_x-small slds-p-horizontal_small dt-notif-item';

      if (!isPanelCard) {
        li.style.borderBottom = '1px solid #e2e8f0';
      } else {
        li.style.border = '1px solid #e2e8f0';
        li.style.borderRadius = '6px';
        li.style.marginBottom = '6px';
      }
      li.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
      if (a.id) li.setAttribute('data-notif-id', a.id);

      var medio;
      var badgeStyle = 'background: #f1f5f9; color: #475569;';
      if (a.isLocal) {
        if (a.tipo === 'success') {
          medio = 'ÉXITO';
          badgeStyle = 'background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0;';
        } else if (a.tipo === 'error') {
          medio = 'ALERTA';
          badgeStyle = 'background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca;';
        } else {
          medio = 'SISTEMA';
          badgeStyle = 'background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd;';
        }
      } else {
        medio = escapeHtml((a.medio || 'EMAIL').toString().toUpperCase());
      }

      var mensaje = escapeHtml(a.mensaje || 'Notificación del sistema');
      var fecha = escapeHtml(formatearFecha(a.fechaEnvio));
      var completada = a.completada === true;

      li.innerHTML =
        '<div class="slds-grid slds-grid_align-spread slds-grid_vertical-align-start" style="gap: 8px;">' +
          '<div style="min-width: 0; flex: 1;">' +
            '<p class="slds-text-body_regular" style="color: #0f172a; font-weight: 600; font-size: 0.85rem; line-height: 1.35; margin: 0; word-break: break-word;">' +
              mensaje +
            '</p>' +
            '<span class="slds-text-body_small slds-text-color_weak" style="font-size: 0.74rem;">' +
              fecha + (completada ? ' · Recibida' : ' · En proceso') +
            '</span>' +
          '</div>' +
          '<div class="slds-grid slds-grid_vertical-align-center" style="gap: 6px; flex-shrink: 0;">' +
            '<span class="slds-badge" style="font-size: 0.65rem; font-weight: 700; padding: 2px 6px; border-radius: 4px; ' + badgeStyle + '">' +
              medio +
            '</span>' +
            '<button type="button" class="dt-notif-delete-btn" aria-label="Eliminar notificación" title="Eliminar notificación" ' +
                    'style="background: transparent; border: none; cursor: pointer; color: #94a3b8; font-size: 1.25rem; line-height: 1; padding: 2px 4px; border-radius: 4px; display: inline-flex; align-items: center; justify-content: center; transition: color 0.15s ease, background 0.15s ease;" ' +
                    'onmouseover="this.style.color=\'#dc2626\'; this.style.background=\'#fef2f2\';" ' +
                    'onmouseout="this.style.color=\'#94a3b8\'; this.style.background=\'transparent\';">' +
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

          if (a.isLocal || (a.id && String(a.id).startsWith('notif-'))) {
            eliminarNotificacionLocal(a.id);
          } else {
            ultimaListaBackend = ultimaListaBackend.filter(function (b) {
              return (b.id != null ? 'backend-' + b.id : '') !== a.id;
            });
          }

          li.style.opacity = '0';
          li.style.transform = 'translateX(10px)';
          setTimeout(function () {
            if (li.parentNode) {
              li.parentNode.removeChild(li);
            }
            actualizarConteoNotificaciones();
          }, 200);
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
    var badgeEl = document.getElementById('notif-badge-count');
    var panelBadgeEl = document.getElementById('panel-notif-badge');

    if (state === 'ok' || state === 'empty') {
      var count = Array.isArray(data) ? data.length : 0;
      if (badgeEl) {
        badgeEl.textContent = String(count);
        badgeEl.style.display = count > 0 ? 'inline-block' : 'none';
      }
      if (panelBadgeEl) {
        panelBadgeEl.textContent = String(count);
        panelBadgeEl.style.display = count > 0 ? 'inline-block' : 'none';
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

  function recargarVistaCombinada() {
    var locales = getNotificacionesLocales();
    var combinadas = combinarAlertas(locales, ultimaListaBackend);
    if (combinadas.length === 0) {
      actualizarUI('empty', []);
    } else {
      actualizarUI('ok', combinadas);
    }
  }

  function cargarAlertas() {
    var hasHeaderWidget = document.getElementById('btn-notificaciones-toggle');
    var hasPanelCard = document.getElementById('panel-notificaciones-card');
    if (!hasHeaderWidget && !hasPanelCard) return;

    var locales = getNotificacionesLocales();
    if (locales.length > 0) {
      actualizarUI('ok', combinarAlertas(locales, ultimaListaBackend));
    } else if (ultimaListaBackend.length === 0) {
      actualizarUI('loading', []);
    }

    var controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    var timeoutId = controller ? setTimeout(function () { controller.abort(); }, 5000) : null;

    fetch('/api/notificaciones/mis-alertas', {
      method: 'GET',
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' },
      signal: controller ? controller.signal : undefined
    })
      .then(function (res) {
        if (timeoutId) clearTimeout(timeoutId);
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (data) {
        ultimaListaBackend = Array.isArray(data) ? data : [];
        var localesActuales = getNotificacionesLocales();
        var combinadas = combinarAlertas(localesActuales, ultimaListaBackend);
        if (combinadas.length === 0) {
          actualizarUI('empty', []);
        } else {
          actualizarUI('ok', combinadas);
        }
      })
      .catch(function () {
        if (timeoutId) clearTimeout(timeoutId);
        // Si la respuesta del fetch falla (error 403, 401 o caída de red),
        // NO mostrar el banner rojo de error si hay notificaciones locales;
        // renderizar las locales o mostrar estado vacío si no hay ninguna.
        var localesActuales = getNotificacionesLocales();
        if (localesActuales.length > 0) {
          actualizarUI('ok', combinarAlertas(localesActuales, ultimaListaBackend));
        } else {
          actualizarUI('empty', []);
        }
      });
  }

  function transferirToastANotificaciones(toast) {
    if (!toast) return;
    var mensaje = toast.getAttribute('data-mensaje');
    if (!mensaje) {
      var h2 = toast.querySelector('h2');
      if (h2) mensaje = h2.textContent.trim();
    }
    if (!mensaje) return;

    var tipo = toast.getAttribute('data-tipo') || 'success';

    var notif = {
      id: 'notif-' + Date.now(),
      mensaje: mensaje,
      tipo: tipo,
      fechaEnvio: new Date().toISOString(),
      completada: true,
      isLocal: true
    };

    var lista = getNotificacionesLocales();
    // Evitar duplicar exactamente el mismo mensaje si ya se guardó hace menos de 4 segundos
    var yaExiste = lista.some(function (item) {
      return item.mensaje === notif.mensaje && (Date.now() - new Date(item.fechaEnvio).getTime() < 4000);
    });

    if (!yaExiste) {
      lista.unshift(notif);
      if (lista.length > 50) lista = lista.slice(0, 50);
      guardarNotificacionesLocales(lista);
    }

    recargarVistaCombinada();
  }

  function inicializarToast() {
    var toast = document.getElementById('toast-notification');
    if (!toast) return;

    var yaTransferido = false;

    function cerrarToast() {
      if (yaTransferido) return;
      yaTransferido = true;

      // Transferir automáticamente a la pestaña de notificaciones
      transferirToastANotificaciones(toast);

      // Desvanecimiento suave
      toast.classList.add('dt-toast-hidden');
      setTimeout(function () {
        if (toast && toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 400);
    }

    // Interceptar botones de cerrar ('x')
    var closeButtons = toast.querySelectorAll('.dt-toast-close, button.slds-button_icon');
    closeButtons.forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        cerrarToast();
      });
    });

    // Temporizador de 3 segundos (3000 ms)
    setTimeout(function () {
      cerrarToast();
    }, 3000);
  }

  document.addEventListener('DOMContentLoaded', function () {
    // Inicializar temporizador y desvanecimiento de toast flash
    inicializarToast();

    // Toggle popover de la campanita
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
