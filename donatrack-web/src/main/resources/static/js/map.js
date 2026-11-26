/* DonaTrack — Mapa interactivo de Depósitos Habilitados CABA (con búsqueda por cercanía GPS) */
(function () {
  "use strict";

  if (typeof L === "undefined") return;
  const mapEl = document.getElementById("map");
  if (!mapEl) return;

  const map = L.map(mapEl, {
    center: [-34.606, -58.438],
    zoom: 12,
    scrollWheelZoom: false,
  });

  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
  }).addTo(map);

  // Catálogo oficial de respaldo (idéntico a /api/public/depositos) para render inmediato sin bloqueo
  const fallbackDepositos = [
    {
      id: "utn-medrano",
      nombre: "Depósito Central — UTN FRBA Medrano",
      barrio: "Almagro",
      direccion: "Av. Medrano 951, CABA",
      horarios: "Lun a Vie 9:00 a 18:00 hs · Sáb 9:00 a 13:00 hs",
      lat: -34.5986,
      lng: -58.4201,
      acepta: "Alimentos No Perecederos · Ropa y Abrigo · Higiene · Útiles",
      referencia: "A 3 cuadras de Estación Medrano (Subte B) · Sede Central DonaTrack",
      descripcion:
        "Centro logístico principal junto a UTN FRBA Sede Medrano. Recepción, clasificación y despacho diario hacia comedores.",
      estado: "Disponible",
    },
    {
      id: "utn-lugano",
      nombre: "Depósito Sur — UTN FRBA Campus Lugano",
      barrio: "Villa Lugano",
      direccion: "Mozart 2300, CABA",
      horarios: "Lun a Vie 9:00 a 17:30 hs",
      lat: -34.6625,
      lng: -58.4611,
      acepta: "Alimentos No Perecederos · Mobiliario · Equipamiento Escolar",
      referencia: "Frente al Parque Roca · Acceso Autopista Dellepiane y Premetro",
      descripcion:
        "Base operativa Zona Sur junto al Campus UTN Lugano con dársena de carga para grandes volúmenes y mobiliario.",
      estado: "Disponible",
    },
    {
      id: "caballito",
      nombre: "Depósito Caballito — Parque Rivadavia",
      barrio: "Caballito",
      direccion: "Av. Rivadavia 5108, CABA",
      horarios: "Lun a Vie 10:00 a 19:00 hs · Sáb 10:00 a 14:00 hs",
      lat: -34.6181,
      lng: -58.4333,
      acepta: "Alimentos No Perecederos · Ropa y Abrigo · Juguetes",
      referencia: "A 1 cuadra de Estación Acoyte (Subte A) y Parque Rivadavia",
      descripcion:
        "Punto barrial céntrico de fácil acceso en Caballito para entregas particulares de alimentos, abrigo y útiles.",
      estado: "Disponible",
    },
    {
      id: "chacarita",
      nombre: "Depósito Chacarita — Estación Lacroze",
      barrio: "Chacarita",
      direccion: "Av. Corrientes 6271, CABA",
      horarios: "Lun a Vie 9:30 a 18:00 hs",
      lat: -34.5907,
      lng: -58.4478,
      acepta: "Alimentos No Perecederos · Ropa y Abrigo · Higiene",
      referencia: "A metros de Estación Federico Lacroze (Subte B / Tren Urquiza)",
      descripcion:
        "Nodo de recepción Zona Norte-Oeste con acopio rápido para donaciones de alimentos secos y artículos de higiene.",
      estado: "Disponible",
    },
    {
      id: "palermo",
      nombre: "Depósito Palermo — Av. Santa Fe",
      barrio: "Palermo",
      direccion: "Av. Santa Fe 3253, CABA",
      horarios: "Lun a Sáb 10:00 a 19:00 hs",
      lat: -34.5888,
      lng: -58.4213,
      acepta: "Ropa y Abrigo · Alimentos · Medicamentos · Juguetes",
      referencia: "A 1 cuadra de Estación Bulnes (Subte D)",
      descripcion:
        "Punto de recepción con horario extendido en Palermo ideal para donaciones particulares y campañas solidarias.",
      estado: "Disponible",
    },
    {
      id: "belgrano",
      nombre: "Depósito Belgrano — Av. Cabildo",
      barrio: "Belgrano",
      direccion: "Av. Cabildo 2040, CABA",
      horarios: "Lun a Vie 9:30 a 18:30 hs · Sáb 10:00 a 13:30 hs",
      lat: -34.5627,
      lng: -58.4565,
      acepta: "Alimentos No Perecederos · Ropa y Abrigo · Útiles Escolares",
      referencia: "A 2 cuadras de Estación Juramento (Subte D)",
      descripcion:
        "Punto de recepción Zona Norte para donantes de Belgrano, Núñez y Colegiales con consolidación diaria hacia la red.",
      estado: "Disponible",
    },
  ];

  let currentDeposits = fallbackDepositos.slice();
  let markers = [];
  let userLocationMarker = null;

  const modal = document.getElementById("map-modal");
  const mTitle = document.getElementById("modal-title");
  const mDesc = document.getElementById("modal-desc");
  const mOrg = document.getElementById("modal-org");
  const mType = document.getElementById("modal-type");
  const mDate = document.getElementById("modal-date");
  const mRef = document.getElementById("modal-ref");
  const mStatus = document.getElementById("modal-status");
  const mMedia = document.getElementById("modal-media");
  const mDonateCta = document.getElementById("modal-donate-cta");

  const escapeHtml = (str) => {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  };

  const haversineKm = (lat1, lng1, lat2, lng2) => {
    const toRad = (deg) => (deg * Math.PI) / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(lat1)) *
        Math.cos(toRad(lat2)) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  };

  const formatDist = (km) => {
    if (typeof km !== "number" || isNaN(km)) return "";
    return km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1)} km`;
  };

  const openModal = (dep) => {
    if (!modal || !dep) return;
    if (mTitle) mTitle.textContent = dep.nombre;
    if (mDesc) mDesc.textContent = dep.descripcion;
    if (mOrg) mOrg.textContent = `${dep.direccion} (${dep.barrio})`;
    if (mType) mType.textContent = dep.acepta;
    if (mDate) mDate.textContent = dep.horarios;
    if (mRef) mRef.textContent = dep.referencia || "Punto de recepción oficial DonaTrack";
    if (mStatus) {
      mStatus.textContent =
        typeof dep.distanceKm === "number"
          ? `${dep.estado || "Disponible"} · a ${formatDist(dep.distanceKm)}`
          : dep.estado || "Disponible";
      mStatus.className = "pill pill--blue";
    }
    if (mMedia) {
      mMedia.style.background = "";
      mMedia.textContent = "📦";
    }
    if (mDonateCta) {
      mDonateCta.href = `/app/donante/donaciones?deposito=${encodeURIComponent(dep.id)}`;
    }
    modal.removeAttribute("hidden");
    if (typeof modal.showModal === "function") {
      modal.showModal();
    }
    document.body.style.overflow = "hidden";
  };

  const closeModal = () => {
    if (modal && typeof modal.close === "function") {
      modal.close();
    }
  };

  if (modal) {
    modal.querySelectorAll("[data-close]").forEach((el) =>
      el.addEventListener("click", closeModal)
    );
    modal.addEventListener("close", () => {
      modal.setAttribute("hidden", "");
      document.body.style.overflow = "";
    });
  }

  function renderizarSidebar(depositos) {
    const countEl = document.getElementById("map-sidebar-count");
    const listEl =
      document.getElementById("map-sidebar-list") ||
      document.querySelector(".map-sec__list");
    if (countEl) {
      countEl.textContent = `Depósitos Disponibles (${depositos.length})`;
    }
    if (!listEl) return;

    listEl.textContent = "";
    depositos.forEach((dep, idx) => {
      const li = document.createElement("li");
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "map-sec__item" + (idx === 0 ? " is-active" : "");
      btn.setAttribute("data-coord-index", String(idx));
      btn.setAttribute("data-deposito-id", dep.id);

      const dot = document.createElement("span");
      dot.className = "dot dot--blue";
      dot.setAttribute("aria-hidden", "true");

      const body = document.createElement("div");
      body.className = "map-sec__item-body";

      const title = document.createElement("strong");
      title.textContent = dep.nombre;

      const sub = document.createElement("span");
      sub.textContent = `${dep.direccion} · ${dep.barrio}`;

      const hours = document.createElement("span");
      hours.textContent = dep.horarios;
      hours.style.fontSize = "0.73rem";

      body.appendChild(title);
      body.appendChild(sub);
      body.appendChild(hours);

      if (typeof dep.distanceKm === "number") {
        const distBadge = document.createElement("span");
        distBadge.className = "map-sec__dist-badge";
        distBadge.textContent =
          idx === 0
            ? `Más cercano · a ${formatDist(dep.distanceKm)}`
            : `a ${formatDist(dep.distanceKm)}`;
        body.appendChild(distBadge);
      }

      btn.appendChild(dot);
      btn.appendChild(body);
      li.appendChild(btn);
      listEl.appendChild(li);
    });
  }

  function renderizarMarcadores(depositos) {
    markers.forEach((m) => map.removeLayer(m));
    markers = [];
    currentDeposits = depositos;
    renderizarSidebar(depositos);

    depositos.forEach((dep, idx) => {
      const icon = L.divIcon({
        className: "",
        html: '<div class="dt-marker dt-marker--blue"></div>',
        iconSize: [24, 24],
        iconAnchor: [12, 12],
      });
      const distLine =
        typeof dep.distanceKm === "number"
          ? `<div style="margin-bottom:6px;"><span style="display:inline-block;padding:2px 8px;border-radius:12px;font-size:11px;font-weight:700;background:#dcfce7;color:#166534;">📍 A ${escapeHtml(formatDist(dep.distanceKm))} de tu ubicación</span></div>`
          : "";
      const popupContent = `
        <div style="font-family: system-ui, -apple-system, sans-serif; font-size: 13px; line-height: 1.4; min-width: 230px;">
          <strong style="color: #0b2147; font-size: 14px; display: block; margin-bottom: 2px;">${escapeHtml(dep.nombre)}</strong>
          <span style="color: #475569; font-size: 12px; display: block; margin-bottom: 6px;">${escapeHtml(dep.direccion)} (${escapeHtml(dep.barrio)})</span>
          ${distLine}
          <p style="margin: 4px 0 8px; color: #334155; font-size: 12px;">🕒 ${escapeHtml(dep.horarios)}</p>
          <div style="display:flex; justify-content:space-between; align-items:center; margin-top: 10px; gap: 6px; flex-wrap: wrap;">
            <button type="button" class="dt-popup-modal-btn" data-point-idx="${idx}" style="background:#f1f5f9; color:#0b2147; border:1px solid #cbd5e1; border-radius:4px; font-size:11px; font-weight:700; padding:6px 10px; min-height:34px; cursor:pointer;">Ver ficha</button>
            <a href="/app/donante/donaciones?deposito=${encodeURIComponent(dep.id)}" style="background:#0b4a8f; color:#ffffff; text-decoration:none; border-radius:4px; font-size:11px; font-weight:700; padding:7px 10px; min-height:34px; display:inline-flex; align-items:center;">Donar acá →</a>
          </div>
        </div>
      `;
      const marker = L.marker([dep.lat, dep.lng], { icon, title: dep.nombre })
        .addTo(map)
        .bindPopup(popupContent);

      marker.on("click", () => {
        document
          .querySelectorAll("[data-coord-index]")
          .forEach((b) => b.classList.remove("is-active"));
        const linkedBtn = document.querySelector(`[data-coord-index="${idx}"]`);
        if (linkedBtn) linkedBtn.classList.add("is-active");
      });

      markers.push(marker);
    });
  }

  function focusDepositById(depositoId) {
    if (!depositoId) return;
    const idx = currentDeposits.findIndex(
      (d) => d.id.toLowerCase() === String(depositoId).toLowerCase()
    );
    if (idx === -1 || !markers[idx]) return;
    const dep = currentDeposits[idx];
    map.flyTo([dep.lat, dep.lng], 14, { duration: 0.7 });
    markers[idx].openPopup();
    document
      .querySelectorAll("[data-coord-index]")
      .forEach((b) => b.classList.remove("is-active"));
    const btn = document.querySelector(`[data-coord-index="${idx}"]`);
    if (btn) btn.classList.add("is-active");
  }

  // Render inicial inmediato con los 6 depósitos oficiales CABA
  renderizarMarcadores(fallbackDepositos);

  // Foco automático si llega ?deposito=<id> desde el chatbot o enlace directo
  try {
    const preselect = new URLSearchParams(window.location.search).get("deposito");
    if (preselect) {
      setTimeout(() => focusDepositById(preselect), 350);
    }
  } catch (e) {}

  map.on("popupopen", (e) => {
    const btn = e.popup.getElement()?.querySelector(".dt-popup-modal-btn");
    if (btn) {
      btn.onclick = () => {
        const idx = parseInt(btn.getAttribute("data-point-idx"), 10);
        if (currentDeposits[idx]) openModal(currentDeposits[idx]);
      };
    }
  });

  const sidebarListEl =
    document.getElementById("map-sidebar-list") ||
    document.querySelector(".map-sec__list");
  if (sidebarListEl) {
    sidebarListEl.addEventListener("click", (event) => {
      const btn = event.target.closest("[data-coord-index]");
      if (!btn) return;
      const idx = parseInt(btn.getAttribute("data-coord-index"), 10);
      const dep = currentDeposits[idx];
      if (dep && markers[idx]) {
        map.flyTo([dep.lat, dep.lng], 14, { duration: 0.8 });
        markers[idx].openPopup();
        sidebarListEl
          .querySelectorAll("[data-coord-index]")
          .forEach((b) => b.classList.remove("is-active"));
        btn.classList.add("is-active");
      }
    });
  }

  // Botón "Encontrar depósito más cercano" (Geolocation + Haversine)
  const geoBtn = document.getElementById("btn-nearest-deposit");
  const geoStatus = document.getElementById("map-geo-status");
  if (geoBtn) {
    geoBtn.addEventListener("click", () => {
      if (!navigator.geolocation) {
        if (geoStatus) {
          geoStatus.textContent =
            "Tu navegador no soporta geolocalización. Elegí una sede de la lista.";
        }
        return;
      }
      geoBtn.disabled = true;
      if (geoStatus) geoStatus.textContent = "Obteniendo tu ubicación...";
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          geoBtn.disabled = false;
          const uLat = pos.coords.latitude;
          const uLng = pos.coords.longitude;

          const ordered = currentDeposits
            .map((d) => Object.assign({}, d, { distanceKm: haversineKm(uLat, uLng, d.lat, d.lng) }))
            .sort((a, b) => a.distanceKm - b.distanceKm);

          renderizarMarcadores(ordered);

          if (userLocationMarker) map.removeLayer(userLocationMarker);
          const userIcon = L.divIcon({
            className: "",
            html: '<div class="dt-marker dt-marker--green"></div>',
            iconSize: [22, 22],
            iconAnchor: [11, 11],
          });
          userLocationMarker = L.marker([uLat, uLng], {
            icon: userIcon,
            title: "Tu ubicación actual",
          })
            .addTo(map)
            .bindPopup("<strong>📍 Tu ubicación actual</strong>");

          const nearest = ordered[0];
          if (nearest && markers[0]) {
            if (geoStatus) {
              geoStatus.textContent = `Más cercano: ${nearest.nombre} (a ${formatDist(nearest.distanceKm)})`;
            }
            const bounds = L.latLngBounds([
              [uLat, uLng],
              [nearest.lat, nearest.lng],
            ]);
            map.fitBounds(bounds.pad(0.35), { maxZoom: 14 });
            markers[0].openPopup();
          }
        },
        () => {
          geoBtn.disabled = false;
          if (geoStatus) {
            geoStatus.textContent =
              "No pudimos acceder a tu ubicación. Seleccioná cualquiera de los 6 depósitos disponibles.";
          }
        },
        { enableHighAccuracy: false, timeout: 6000, maximumAge: 120000 }
      );
    });
  }

  // Hidratación asíncrona desde /api/public/depositos
  const controller =
    typeof AbortController !== "undefined" ? new AbortController() : null;
  const timeoutId = controller ? setTimeout(() => controller.abort(), 2500) : null;

  fetch("/api/public/depositos", {
    headers: { Accept: "application/json" },
    signal: controller ? controller.signal : undefined,
  })
    .then((res) => {
      if (timeoutId) clearTimeout(timeoutId);
      if (!res.ok) throw new Error("API de depósitos respondió " + res.status);
      return res.json();
    })
    .then((data) => {
      if (!Array.isArray(data) || data.length === 0) return;
      renderizarMarcadores(data);
      try {
        const preselect = new URLSearchParams(window.location.search).get("deposito");
        if (preselect) focusDepositById(preselect);
      } catch (e) {}
    })
    .catch((err) => {
      if (timeoutId) clearTimeout(timeoutId);
      console.info("Mapa de depósitos usando catálogo local:", err.message);
    });
})();
