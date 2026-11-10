/**
 * DonaTrack — Mapa de Camiones en Ruta para el Donante
 * Consume GET /api/donante/mapa-camiones asincrónicamente.
 * Cumple con los tres estados obligatorios de UI: Carga activa, Error con recuperación y Estado vacío.
 */
(function () {
  'use strict';

  var mapInstance = null;
  var markersGroup = null;
  var mapaDonanteInicializado = false;

  function initDonanteMapa() {
    var container = document.getElementById('mapa-donante-camiones');
    if (!container) return;

    var loadingEl = document.getElementById('mapa-donante-loading');
    var errorEl = document.getElementById('mapa-donante-error');
    var emptyEl = document.getElementById('mapa-donante-empty');
    var retryBtn = document.getElementById('mapa-donante-retry');

    if (retryBtn) {
      retryBtn.addEventListener('click', function () {
        cargarCamiones();
      });
    }

    function mostrarEstado(estado) {
      if (loadingEl) loadingEl.style.display = (estado === 'loading') ? 'flex' : 'none';
      if (errorEl) errorEl.style.display = (estado === 'error') ? 'block' : 'none';
      if (emptyEl) emptyEl.style.display = (estado === 'empty') ? 'block' : 'none';
      if (container) {
        // El contenedor del mapa permanece visible salvo si hay error antes de iniciar
        container.style.opacity = (estado === 'loading') ? '0.35' : '1';
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
        html: '<div style="background: #0176d3; color: #ffffff; width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(1, 118, 211, 0.45); border: 2px solid #ffffff; font-size: 16px;">🚚</div>',
        iconSize: [34, 34],
        iconAnchor: [17, 17],
        popupAnchor: [0, -18]
      });
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
          if (!data || !Array.isArray(data) || data.length === 0) {
            mostrarEstado('empty');
            return;
          }

          mostrarEstado('ok');
          var map = asegurarMapa();
          if (!map) return;

          if (markersGroup) {
            markersGroup.clearLayers();
          }

          var bounds = [];
          var icono = crearIconoCamion();

          data.forEach(function (movil) {
            if (typeof movil.lat !== 'number' || typeof movil.lng !== 'number') return;

            var latLng = [movil.lat, movil.lng];
            bounds.push(latLng);

            var popupHtml = '' +
              '<div style="font-family: -apple-system, BlinkMacSystemFont, \'Segoe UI\', Roboto, sans-serif; font-size: 12px; line-height: 1.4; min-width: 210px; padding: 2px;">' +
                '<div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px;">' +
                  '<strong style="color: #014486; font-size: 13px; font-family: monospace;">' + escapeHtml(movil.codigoCorto || '#DT-DON') + '</strong>' +
                  '<span style="background: #e0f2fe; color: #0369a1; padding: 2px 6px; border-radius: 4px; font-weight: 700; font-size: 10px;">' + escapeHtml(movil.estado || 'EN TRASLADO') + '</span>' +
                '</div>' +
                '<p style="margin: 3px 0; color: #0f172a;"><strong>Camión:</strong> ' + escapeHtml(movil.patenteCamion || 'En asignación') + '</p>' +
                '<p style="margin: 3px 0; color: #0f172a;"><strong>Chofer:</strong> ' + escapeHtml(movil.choferNombre || 'Personal Logístico') + '</p>' +
                '<p style="margin: 3px 0; color: #334155;"><strong>Destino:</strong> ' + escapeHtml(movil.destinoComedor || 'Comedor Comunitario') + '</p>' +
                '<div style="margin-top: 8px; font-size: 11px; color: #64748b; display: flex; align-items: center; gap: 4px;">' +
                  '<span>⏱</span>' +
                  '<span>' + escapeHtml(movil.ultimaActualizacion || 'Actualizado recientemente') + '</span>' +
                '</div>' +
              '</div>';

            var marker = L.marker(latLng, { icon: icono })
              .bindPopup(popupHtml);

            if (markersGroup) {
              markersGroup.addLayer(marker);
            }
          });

          if (bounds.length > 0) {
            if (bounds.length === 1) {
              map.setView(bounds[0], 13);
            } else {
              map.fitBounds(bounds, { padding: [30, 30] });
            }
          }

          // Invalidar tamaño de Leaflet tras render para evitar renderizado parcial
          setTimeout(function () {
            if (mapInstance) mapInstance.invalidateSize();
          }, 250);
        })
        .catch(function (err) {
          if (timeoutId) clearTimeout(timeoutId);
          console.error('Error al cargar camiones en vivo del donante:', err);
          mostrarEstado('error');
        });
    }

    // FASE 2: Lazy Mounting idempotente del Mapa de Camiones
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

    // Reajustar tamaño de Leaflet / compatibilidad hacia atrás
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
