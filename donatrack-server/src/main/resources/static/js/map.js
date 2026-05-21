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
    "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png",
    {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>',
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

  points.forEach((p) => {
    const icon = L.divIcon({
      className: "",
      html: `<div class="dt-marker dt-marker--${p.type}"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });
    L.marker(p.coords, { icon, title: p.title })
      .addTo(map)
      .on("click", () => openModal(p));
  });
})();
