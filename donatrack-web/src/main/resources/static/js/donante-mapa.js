/**
 * DonaTrack — Mapa de Camiones en Ruta para el Donante
 * Consume GET /api/donante/mapa-camiones asincrónicamente.
 * Muestra una donación seleccionada a la vez con marcador pulsante.
 */
(function () {
  'use strict';

  var mapInstance = null;
  var markersGroup = null;
  var mapaDonanteInicializado = false;
  var movilesCargados = [];
  var selectedDonacionId = null;

  function initDonanteMapa() {
    var container = document.getElementById('mapa-donante-camiones');
    if (!container) return;

    var loadingEl = document.getElementById('mapa-donante-loading');
    var errorEl = document.getElementById('mapa-donante-error');
    var emptyEl = document.getElementById('mapa-donante-empty');
    var emptyTitleEl = document.getElementById('mapa-donante-empty-title');
    var emptyDescEl = document.getElementById('mapa-donante-empty-desc');
    var selectorEl = document.getElementById('mapa-donante-selector');
    var retryBtn = document.getElementById('mapa-donante-retry');

    if (retryBtn) {
      retryBtn.addEventListener('click', function () {
        cargarCamiones();
      });
    }

    if (selectorEl) {
      selectorEl.addEventListener('change', function () {
        if (selectorEl.value) {
          window.seleccionarDonacionEnMapa(selectorEl.value, false);
        }
      });
    }

    function mostrarEstado(estado, customTitle, customDesc) {
      if (loadingEl) loadingEl.style.display = (estado === 'loading') ? 'flex' : 'none';
      if (errorEl) errorEl.style.display = (estado === 'error') ? 'block' : 'none';
      if (emptyEl) emptyEl.style.display = (estado === 'empty') ? 'block' : 'none';
      if (emptyTitleEl && customTitle) emptyTitleEl.textContent = customTitle;
      if (emptyDescEl && customDesc) emptyDescEl.textContent = customDesc;
      if (container) {
        container.style.display = (estado === 'empty' || estado === 'error') ? 'none' : 'block';
        container.style.opacity = (estado === 'loading') ? '0.35' : '1';
      }
      if (retryBtn) {
        if (estado === 'loading') {
          retryBtn.setAttribute('disabled', 'disabled');
          retryBtn.style.opacity = '0.6';
          retryBtn.style.cursor = 'not-allowed';
        } else {
          retryBtn.removeAttribute('disabled');
          retryBtn.style.opacity = '1';
          retryBtn.style.cursor = 'pointer';
        }
      }
    }

    function asegurarMapa() {
      if (typeof L === 'undefined') {
        console.warn('Leaflet (L) no disponible aún para el mapa del donante.');
        return null;
      }
      if (!mapInstance) {
        mapInstance = L.map(container, {
          center: [-34.615, -58.425],
          zoom: 12,
          scrollWheelZoom: false
        });

        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
          maxZoom: 19
        }).addTo(mapInstance);

        markersGroup = L.featureGroup().addTo(mapInstance);
      }
      return mapInstance;
    }

    function escapeHtml(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    function crearIconoCamion() {
      return L.divIcon({
        className: 'dt-truck-leaflet-marker',
        html: '<div class="dt-truck-pulse" style="background: #0176d3; color: #ffffff; width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2.5px solid #ffffff; font-size: 16px;">🚚</div>',
        iconSize: [36, 36],
        iconAnchor: [18, 18],
        popupAnchor: [0, -18]
      });
    }

    function resaltarCardDonacion(donId) {
      var cards = document.querySelectorAll('.donacion-item-card');
      cards.forEach(function (card) {
        var cardId = (card.getAttribute('data-donacion-id') || '').toLowerCase();
        if (donId && cardId === donId.toLowerCase()) {
          card.classList.add('dt-donacion-selected');
        } else {
          card.classList.remove('dt-donacion-selected');
        }
      });
    }

    function renderizarMovilSeleccionado(donId) {
      if (!movilesCargados || movilesCargados.length === 0) {
        mostrarEstado(
          'empty',
          'No hay camiones en tránsito para tus donaciones',
          'Cuando tus donaciones sean asignadas y un móvil inicie la ruta logística, vas a ver aquí la patente, chofer y destino real.'
        );
        return;
      }

      var target = null;
      if (donId) {
        for (var i = 0; i < movilesCargados.length; i++) {
          if (String(movilesCargados[i].idDonacion).toLowerCase() === String(donId).toLowerCase()) {
            target = movilesCargados[i];
            break;
          }
        }
      }

      if (!target) {
        if (donId) {
          mostrarEstado(
            'empty',
            'Esta donación aún no tiene un camión asignado en ruta',
            'Cuando el administrador asigne una unidad de flota y chofer a esta donación, su ubicación aparecerá aquí.'
          );
          return;
        }
        target = movilesCargados[0];
      }

      selectedDonacionId = String(target.idDonacion);
      if (selectorEl) {
        selectorEl.value = selectedDonacionId;
      }
      resaltarCardDonacion(selectedDonacionId);

      mostrarEstado('ok');
      var map = asegurarMapa();
      if (!map) return;

      if (markersGroup) {
        markersGroup.clearLayers();
      }

      if (typeof target.lat !== 'number' || typeof target.lng !== 'number') {
        return;
      }

      var latLng = [target.lat, target.lng];
      var icono = crearIconoCamion();
      var badgeBg = target.estado === 'ENTREGADA' ? '#dcfce7' : (target.estado === 'EN_TRASLADO' ? '#ffedd5' : '#e0f2fe');
      var badgeColor = target.estado === 'ENTREGADA' ? '#166534' : (target.estado === 'EN_TRASLADO' ? '#c2410c' : '#0369a1');

      var popupHtml = '' +
        '<div style="font-family: -apple-system, BlinkMacSystemFont, \'Segoe UI\', Roboto, sans-serif; font-size: 12px; line-height: 1.4; min-width: 215px; padding: 2px;">' +
          '<div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px;">' +
            '<strong style="color: #014486; font-size: 13px; font-family: monospace;">' + escapeHtml(target.codigoCorto || '#DT-DON') + '</strong>' +
            '<span style="background: ' + badgeBg + '; color: ' + badgeColor + '; padding: 2px 6px; border-radius: 4px; font-weight: 700; font-size: 10px;">' + escapeHtml(target.estado || 'PLANIFICADA') + '</span>' +
          '</div>' +
          '<p style="margin: 3px 0; color: #0f172a;"><strong>Camión:</strong> ' + escapeHtml(target.patenteCamion || 'Sin patente') + '</p>' +
          '<p style="margin: 3px 0; color: #0f172a;"><strong>Chofer:</strong> ' + escapeHtml(target.choferNombre || 'Chofer por asignar') + '</p>' +
          '<p style="margin: 3px 0; color: #334155;"><strong>Destino:</strong> ' + escapeHtml(target.destinoComedor || 'Organización Beneficiaria') + '</p>' +
          '<div style="margin-top: 8px; font-size: 11px; color: #64748b; display: flex; align-items: center; gap: 4px;">' +
            '<span>⏱</span>' +
            '<span>Fecha operativa: ' + escapeHtml(target.ultimaActualizacion || 'Programada') + '</span>' +
          '</div>' +
        '</div>';

      var marker = L.marker(latLng, { icon: icono }).bindPopup(popupHtml);
      if (markersGroup) {
        markersGroup.addLayer(marker);
      }

      map.setView(latLng, 14);
      setTimeout(function () {
        if (mapInstance) {
          mapInstance.invalidateSize();
          marker.openPopup();
        }
      }, 220);
    }

    function poblarSelector(data) {
      if (!selectorEl) return;
      selectorEl.innerHTML = '';
      if (!data || data.length === 0) {
        selectorEl.style.display = 'none';
        return;
      }
      data.forEach(function (m) {
        var opt = document.createElement('option');
        opt.value = String(m.idDonacion);
        opt.textContent = (m.codigoCorto || 'Donación') + ' · ' + (m.patenteCamion || 'Móvil') + ' (' + (m.estado || '') + ')';
        selectorEl.appendChild(opt);
      });
      selectorEl.style.display = 'inline-block';
    }

    function cargarCamiones() {
      mostrarEstado('loading');

      var controller = (typeof AbortController !== 'undefined') ? new AbortController() : null;
      var timeoutId = controller ? setTimeout(function () { controller.abort(); }, 7000) : null;

      fetch('/api/donante/mapa-camiones', {
        headers: { 'Accept': 'application/json' },
        signal: controller ? controller.signal : undefined
      })
        .then(function (res) {
          if (timeoutId) clearTimeout(timeoutId);
          if (!res.ok) {
            throw new Error('Respuesta no satisfactoria del servidor (' + res.status + ')');
          }
          return res.json();
        })
        .then(function (data) {
          movilesCargados = Array.isArray(data) ? data : [];
          poblarSelector(movilesCargados);

          if (movilesCargados.length === 0) {
            selectedDonacionId = null;
            renderizarMovilSeleccionado(null);
            return;
          }

          var existeSelected = selectedDonacionId && movilesCargados.some(function (m) {
            return String(m.idDonacion).toLowerCase() === String(selectedDonacionId).toLowerCase();
          });
          renderizarMovilSeleccionado(existeSelected ? selectedDonacionId : String(movilesCargados[0].idDonacion));
        })
        .catch(function (err) {
          if (timeoutId) clearTimeout(timeoutId);
          console.error('Error al cargar camiones en vivo del donante:', err);
          if (selectorEl) selectorEl.style.display = 'none';
          mostrarEstado('error');
        });
    }

    window.seleccionarDonacionEnMapa = function (donacionId, scrollAlMapa) {
      if (scrollAlMapa === undefined) scrollAlMapa = true;
      selectedDonacionId = donacionId ? String(donacionId) : null;
      resaltarCardDonacion(selectedDonacionId);
      renderizarMovilSeleccionado(selectedDonacionId);
      if (scrollAlMapa) {
        var cardMapa = document.getElementById('card-mapa-donante');
        if (cardMapa && typeof cardMapa.scrollIntoView === 'function') {
          cardMapa.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }
    };

    // Lazy Mounting idempotente del Mapa de Camiones
    window.inicializarOMostrarMapaDonante = function () {
      var panel = document.getElementById('tab-panel-seguimiento');
      if (!panel || panel.style.display === 'none') {
        return;
      }
      if (!mapaDonanteInicializado) {
        mapaDonanteInicializado = true;
        asegurarMapa();
        cargarCamiones();
      } else {
        setTimeout(function () {
          if (mapInstance) mapInstance.invalidateSize();
        }, 100);
      }
    };

    window.refrescarMapaDonante = window.inicializarOMostrarMapaDonante;
  }

  function autoInitSiVisible() {
    initDonanteMapa();
    var panel = document.getElementById('tab-panel-seguimiento');
    var tabParam = new URLSearchParams(window.location.search).get('tab');
    if ((panel && panel.style.display !== 'none') || tabParam === 'seguimiento') {
      if (typeof window.inicializarOMostrarMapaDonante === 'function') {
        window.inicializarOMostrarMapaDonante();
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', autoInitSiVisible);
  } else {
    autoInitSiVisible();
  }
})();
