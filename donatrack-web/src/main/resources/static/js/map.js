/* DonaTrack — Leaflet interactive map */
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

  const points = [
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

  const modal = document.getElementById("map-modal");
  const mTitle = document.getElementById("modal-title");
  const mDesc = document.getElementById("modal-desc");
  const mOrg = document.getElementById("modal-org");
  const mType = document.getElementById("modal-type");
  const mDate = document.getElementById("modal-date");
  const mStatus = document.getElementById("modal-status");
  const mMedia = document.getElementById("modal-media");

  const openModal = (p) => {
    mTitle.textContent = p.title;
    mDesc.textContent = p.desc;
    mOrg.textContent = p.org;
    mType.textContent = p.kind;
    mDate.textContent = p.date;
    mStatus.textContent = p.status;
    mStatus.className =
      "pill " +
      (p.type === "orange"
        ? "pill--orange"
        : p.type === "blue"
          ? "pill--blue"
          : "pill--green");
    if (p.image) {
      mMedia.style.backgroundImage = p.image;
      mMedia.style.backgroundSize = "cover";
      mMedia.textContent = "";
    } else {
      mMedia.style.background = ""; // Resetea al gradiente de CSS
      mMedia.textContent = p.emoji;
    }
    modal.removeAttribute("hidden");
    modal.showModal();
    document.body.style.overflow = "hidden";
  };

  const closeModal = () => {
    modal.close();
  };

  modal.querySelectorAll("[data-close]").forEach((el) =>
    el.addEventListener("click", closeModal)
  );
  modal.addEventListener("close", () => {
    modal.setAttribute("hidden", "");
    document.body.style.overflow = "";
  });

  const markers = [];
  points.forEach((p, idx) => {
    const icon = L.divIcon({
      className: "",
      html: `<div class="dt-marker dt-marker--${p.type}"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });
    const popupContent = `
      <div style="font-family: system-ui, -apple-system, sans-serif; font-size: 13px; line-height: 1.4; min-width: 200px;">
        <strong style="color: #0b2147; font-size: 14px; display: block; margin-bottom: 2px;">${p.title}</strong>
        <span style="color: #64748b; font-size: 12px;">${p.org}</span>
        <p style="margin: 8px 0; color: #334155;">${p.desc}</p>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-top: 10px; gap: 8px;">
          <span style="display:inline-block; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; background: ${p.type === 'blue' ? '#e0f2fe; color: #0369a1' : p.type === 'green' ? '#dcfce7; color: #15803d' : '#ffedd5; color: #c2410c'};">${p.status}</span>
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

  // Vincular apertura del modal fotográfico desde popups
  map.on("popupopen", (e) => {
    const btn = e.popup.getElement()?.querySelector(".dt-popup-modal-btn");
    if (btn) {
      btn.onclick = () => {
        const idx = parseInt(btn.getAttribute("data-point-idx"), 10);
        if (points[idx]) openModal(points[idx]);
      };
    }
  });

  // Coordinar interacción con la lista lateral de ubicaciones
  document.querySelectorAll("[data-coord-index]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const idx = parseInt(btn.getAttribute("data-coord-index"), 10);
      const p = points[idx];
      if (p && markers[idx]) {
        map.flyTo(p.coords, 14, { duration: 0.8 });
        markers[idx].openPopup();
        document.querySelectorAll("[data-coord-index]").forEach(b => b.classList.remove("is-active"));
        btn.classList.add("is-active");
      }
    });
  });
})();
