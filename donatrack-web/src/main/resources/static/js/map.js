/* DonaTrack — Leaflet interactive map con hidratación asíncrona resiliente (UX-02) */
(function () {
  "use strict";

  if (typeof L === "undefined") return;
  const mapEl = document.getElementById("map");
  if (!mapEl) return;

  const map = L.map(mapEl, {
    center: [-34.61, -58.42],
    zoom: 11,
    scrollWheelZoom: false,
  });

  L.tileLayer(
    "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
    {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }
  ).addTo(map);

  // Fallback offline predeterminado si el microservicio demora o está en reposo (Render cold-start)
  const staticPoints = [
    {
      type: "blue",
      coords: [-34.603, -58.381],
      title: "Punto de acopio — Centro",
      desc: "Recepción y clasificación de alimentos no perecederos.",
      org: "DonaTrack Hub Centro",
      kind: "Alimentos · Ropa",
      date: "Activo",
      status: "Operativo",
      emoji: "📦",
    },
    {
      type: "green",
      coords: [-34.645, -58.45],
      title: "Comedor El Faro",
      desc: "Comedor barrial que asiste a 220 familias por semana.",
      org: "Asoc. Civil El Faro",
      kind: "Alimentos",
      date: "Verificada 2024",
      status: "ONG verificada",
      emoji: "🏠",
    },
    {
      type: "orange",
      coords: [-34.575, -58.43],
      title: "Entrega #DT-2418",
      desc: "Camperas y frazadas entregadas al hogar Caminos.",
      org: "Hogar Caminos",
      kind: "Abrigo",
      date: "12 May 2026",
      status: "Entregado",
      emoji: "📍",
      image: "url('https://tn.com.ar/resizer/v2/las-bufandas-algunas-de-las-ropas-de-abrigo-que-pueden-donarse-foto-adobe-stock-XXU6MIVU5VCQLMFZ7DF4TGIMSM.jpeg?auth=9fd575f77a87e748a4edceda4f2642d469d8c74460b9261a4449eadfca9b432d&width=1440')",
    },
    {
      type: "green",
      coords: [-34.62, -58.5],
      title: "Fundación Raíces",
      desc: "Apoyo escolar y entrega de útiles a 4 escuelas rurales.",
      org: "Fundación Raíces",
      kind: "Útiles escolares",
      date: "Verificada 2023",
      status: "ONG verificada",
      emoji: "🏫",
    },
    {
      type: "orange",
      coords: [-34.66, -58.39],
      title: "Entrega #DT-2390",
      desc: "Muebles donados por empresa, entregados al comedor.",
      org: "Comedor La Esperanza",
      kind: "Mobiliario",
      date: "08 May 2026",
      status: "Entregado",
      emoji: "🛋️",
      image: "url('https://diariomovil.info/download/multimedia.miniatura.89b66ec132aa87f2.bWluaWF0dXJhLndlYnA%3D.webp')",
    },
    {
      type: "blue",
      coords: [-34.59, -58.46],
      title: "Punto de acopio — Oeste",
      desc: "Centro logístico para ropa y abrigo.",
      org: "DonaTrack Hub Oeste",
      kind: "Ropa",
      date: "Activo",
      status: "Operativo",
      emoji: "📦",
    },
  ];

  let currentPoints = staticPoints;
  let markers = [];

  const modal = document.getElementById("map-modal");
  const mTitle = document.getElementById("modal-title");
  const mDesc = document.getElementById("modal-desc");
  const mOrg = document.getElementById("modal-org");
  const mType = document.getElementById("modal-type");
  const mDate = document.getElementById("modal-date");
  const mStatus = document.getElementById("modal-status");
  const mMedia = document.getElementById("modal-media");

  const openModal = (p) => {
    if (!modal) return;
    if (mTitle) mTitle.textContent = p.title;
    if (mDesc) mDesc.textContent = p.desc;
    if (mOrg) mOrg.textContent = p.org;
    if (mType) mType.textContent = p.kind;
    if (mDate) mDate.textContent = p.date;
    if (mStatus) {
      mStatus.textContent = p.status;
      mStatus.className =
        "pill " +
        (p.type === "orange"
          ? "pill--orange"
          : p.type === "blue"
            ? "pill--blue"
            : "pill--green");
    }
    if (mMedia) {
      if (p.image) {
        mMedia.style.backgroundImage = p.image;
        mMedia.style.backgroundSize = "cover";
        mMedia.textContent = "";
      } else {
        mMedia.style.background = ""; // Resetea al gradiente de CSS
        mMedia.textContent = p.emoji || "📦";
      }
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

  const escapeHtml = (str) => {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  };

  function renderizarMarcadores(puntos) {
    // Limpiar marcadores previos
    markers.forEach((m) => map.removeLayer(m));
    markers = [];
    currentPoints = puntos;

    puntos.forEach((p, idx) => {
      const icon = L.divIcon({
        className: "",
        html: `<div class="dt-marker dt-marker--${p.type}"></div>`,
        iconSize: [24, 24],
        iconAnchor: [12, 12],
      });
      const popupContent = `
        <div style="font-family: system-ui, -apple-system, sans-serif; font-size: 13px; line-height: 1.4; min-width: 210px;">
          <strong style="color: #0b2147; font-size: 14px; display: block; margin-bottom: 2px;">${escapeHtml(p.title)}</strong>
          <span style="color: #64748b; font-size: 12px;">${escapeHtml(p.org)}</span>
          <p style="margin: 8px 0; color: #334155;">${escapeHtml(p.desc)}</p>
          <div style="display:flex; justify-content:space-between; align-items:center; margin-top: 10px; gap: 8px;">
            <span style="display:inline-block; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; background: ${p.type === 'blue' ? '#e0f2fe; color: #0369a1' : p.type === 'green' ? '#dcfce7; color: #15803d' : '#ffedd5; color: #c2410c'};">${escapeHtml(p.status)}</span>
            <button type="button" class="dt-popup-modal-btn" data-point-idx="${idx}" style="background:#0b4a8f; color:#ffffff; border:none; border-radius:4px; font-size:11px; font-weight:600; padding:4px 8px; cursor:pointer;">Ver foto & acta</button>
          </div>
        </div>
      `;
      const marker = L.marker(p.coords, { icon, title: p.title })
        .addTo(map)
        .bindPopup(popupContent);

      marker.on("click", () => {
        document.querySelectorAll("[data-coord-index]").forEach(b => b.classList.remove("is-active"));
        const linkedBtn = document.querySelector(`[data-coord-index="${idx}"]`);
        if (linkedBtn) linkedBtn.classList.add("is-active");
      });

      markers.push(marker);
    });
  }

  // Render inicial inmediato con staticPoints para CERO bloqueo percibido (ADR-03)
  renderizarMarcadores(staticPoints);

  // Vincular apertura del modal fotográfico desde popups
  map.on("popupopen", (e) => {
    const btn = e.popup.getElement()?.querySelector(".dt-popup-modal-btn");
    if (btn) {
      btn.onclick = () => {
        const idx = parseInt(btn.getAttribute("data-point-idx"), 10);
        if (currentPoints[idx]) openModal(currentPoints[idx]);
      };
    }
  });

  // Coordinar interacción con la lista lateral de ubicaciones
  document.querySelectorAll("[data-coord-index]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const idx = parseInt(btn.getAttribute("data-coord-index"), 10);
      const p = currentPoints[idx];
      if (p && markers[idx]) {
        map.flyTo(p.coords, 14, { duration: 0.8 });
        markers[idx].openPopup();
        document.querySelectorAll("[data-coord-index]").forEach(b => b.classList.remove("is-active"));
        btn.classList.add("is-active");
      }
    });
  });

  // Hidratación Asíncrona: consultar /api/public/mapa-entregas con AbortController defensivo (2000 ms)
  const controller = typeof AbortController !== "undefined" ? new AbortController() : null;
  const timeoutId = controller ? setTimeout(() => controller.abort(), 2000) : null;

  fetch("/api/public/mapa-entregas", {
    headers: { "Accept": "application/json" },
    signal: controller ? controller.signal : undefined,
  })
    .then((res) => {
      if (timeoutId) clearTimeout(timeoutId);
      if (!res.ok) throw new Error("API pública respondió con error: " + res.status);
      return res.json();
    })
    .then((data) => {
      if (!data || !Array.isArray(data) || data.length === 0) return;

      const dynamicPoints = data.map((dto) => {
        const tipo = dto.tipo || "orange";
        const emoji = tipo === "blue" ? "📦" : (tipo === "green" ? "🏠" : "📍");
        return {
          type: tipo,
          coords: [dto.lat, dto.lng],
          title: dto.titulo || "Punto de Entrega",
          desc: dto.descripcion || "Lote solidario auditado en destino.",
          org: dto.organizacion || "DonaTrack Red",
          kind: dto.subcategoria || "Bienes",
          date: dto.fecha || "Reciente",
          status: dto.estado || "Auditado",
          emoji: emoji,
          image: dto.fotoUrl ? `url('${dto.fotoUrl}')` : null,
        };
      });

      renderizarMarcadores(dynamicPoints);
    })
    .catch((err) => {
      if (timeoutId) clearTimeout(timeoutId);
      // Falla silenciosa y transparente: el mapa ya renderizó staticPoints resilientes
      console.info("Mapa público usando fallback resiliente estático:", err.message);
    });
})();
