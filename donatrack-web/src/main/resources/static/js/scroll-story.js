/* DonaTrack — Circular Storytelling: "El Círculo se Cierra" */
(function () {
  "use strict";

  const STEPS = [
    {
      num: "01",
      angle: 0,
      title: "Donación Realizada",
      short: "Registro",
      desc: "Registrás tu donación en segundos indicando tipo de bien, peso aproximado y punto de retiro o entrega. Se genera el remito digital con hash criptográfico único.",
      status: "Trazabilidad Iniciada",
      origin: "Recepción en depósito",
      points: "+10 Puntos de Impacto",
      progress: 0.15
    },
    {
      num: "02",
      angle: 72,
      title: "Centro de Acopio",
      short: "Acopio",
      desc: "La mercadería es recibida en el centro logístico más cercano. Personal capacitado realiza el pesaje, control bromatológico y clasificación en pallets.",
      status: "Lote Auditado",
      origin: "Depósito Central DonaTrack",
      points: "Control de Calidad 100%",
      progress: 0.38
    },
    {
      num: "03",
      angle: 144,
      title: "Transporte en Ruta",
      short: "En Ruta",
      desc: "El despacho se asigna a un camión de la red con chofer profesional. Seguimiento satelital y hoja de ruta optimizada en tiempo real.",
      status: "Móvil Despachado",
      origin: "Ruta Activa con Chofer",
      points: "Monitoreo Satelital",
      progress: 0.62
    },
    {
      num: "04",
      angle: 216,
      title: "Arribo a Sede Comunitaria",
      short: "En Sede",
      desc: "El camión arriba a la ONG o comedor beneficiario asignado. El referente de la sede inspecciona la mercadería al pie del vehículo.",
      status: "Arribo Confirmado",
      origin: "Comedor Verificado",
      points: "Matchmaking Comunitario",
      progress: 0.82
    },
    {
      num: "05",
      angle: 288,
      title: "Entrega Completada y Certificada",
      short: "Círculo Cerrado",
      desc: "Recepción confirmada con firma digital y foto del remito subida a Supabase S3. Recibís la notificación y el impacto tangible. ¡El círculo se cierra!",
      status: "Ciclo 100% Completado",
      origin: "Remito Digital en S3",
      points: "+25 Puntos de Entrega (35 pts totales)",
      progress: 1.0
    }
  ];

  let currentStep = 1;
  let isPlaying = true;
  let autoplayTimer = null;
  const AUTOPLAY_INTERVAL = 3800; // ms

  const CIRCUMFERENCE = 2 * Math.PI * 190; // ~1193.8

  // Elementos DOM
  const progressCircle = document.getElementById("dial-progress-circle");
  const truck = document.getElementById("dial-truck");
  const truckImg = document.getElementById("dial-truck-img");
  const ticksGroup = document.getElementById("dial-ticks");
  const hubStage = document.getElementById("dial-hub-stage");

  const stagePill = document.getElementById("story-stage-pill");
  const stageStatus = document.getElementById("story-stage-status");
  const stageTitle = document.getElementById("story-stage-title");
  const stageDesc = document.getElementById("story-stage-desc");
  const metaOrigin = document.getElementById("story-meta-origin");
  const metaPoints = document.getElementById("story-meta-points");
  const card = document.getElementById("story-active-card");

  const autoplayBtn = document.getElementById("story-autoplay-toggle");
  const autoplayIcon = document.getElementById("story-autoplay-icon");
  const autoplayText = document.getElementById("story-autoplay-text");
  const section = document.getElementById("recorrido");

  if (!progressCircle || !truck || !hubStage) return;

  const prefersReducedMotion =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // 1. Generar los Ticks Radiales en el SVG del Dial (estilo reloj / Impeccable)
  function renderTicks() {
    if (!ticksGroup) return;
    const cx = 270;
    const cy = 270;
    const rOuter = 196;
    const rInnerMinor = 186;
    const rInnerMajor = 180;

    let svgHtml = "";
    for (let deg = 0; deg < 360; deg += 10) {
      const isMajor = deg % 72 === 0; // Hitos principales
      const rInner = isMajor ? rInnerMajor : rInnerMinor;
      const rad = ((deg - 90) * Math.PI) / 180;

      const x1 = cx + rInner * Math.cos(rad);
      const y1 = cy + rInner * Math.sin(rad);
      const x2 = cx + rOuter * Math.cos(rad);
      const y2 = cy + rOuter * Math.sin(rad);

      const color = isMajor ? "#0b4a8f" : "currentColor";
      const width = isMajor ? "2.5" : "1";
      const opacity = isMajor ? "0.9" : "0.35";

      svgHtml += `<line x1="${x1.toFixed(1)}" y1="${y1.toFixed(1)}" x2="${x2.toFixed(1)}" y2="${y2.toFixed(1)}" stroke="${color}" stroke-width="${width}" opacity="${opacity}" />`;
    }
    ticksGroup.innerHTML = svgHtml;
  }

  // 2. Actualizar la vista al paso seleccionado
  window.selectCircleStep = function (stepNum, manualClick = true) {
    if (stepNum < 1 || stepNum > 5) return;
    currentStep = stepNum;
    const data = STEPS[stepNum - 1];

    // Actualizar Texto de Tarjeta Dinámica con suave fade
    if (card) {
      card.style.opacity = "0.4";
      card.style.transform = "translateY(4px)";
      setTimeout(() => {
        if (stagePill) stagePill.textContent = `ETAPA ${data.num} / 05`;
        if (stageStatus) stageStatus.textContent = data.status;
        if (stageTitle) stageTitle.textContent = data.title;
        if (stageDesc) stageDesc.textContent = data.desc;
        if (metaOrigin) metaOrigin.textContent = data.origin;
        if (metaPoints) metaPoints.textContent = data.points;
        card.style.opacity = "1";
        card.style.transform = "translateY(0)";
      }, 120);
    }

    // Actualizar Texto del Hub Central
    if (hubStage) {
      hubStage.textContent = data.short;
      hubStage.style.color = stepNum === 5 ? "#16a34a" : "var(--fg)";
    }

    // Actualizar Progreso del Círculo (stroke-dashoffset)
    const targetOffset = CIRCUMFERENCE * (1 - data.progress);
    progressCircle.style.strokeDashoffset = targetOffset.toFixed(1);

    // Mover el Camión sobre la Circunferencia
    // Para el paso 5 ("El círculo se cierra"), movemos el camión a 360° para completar el giro
    const truckAngle = stepNum === 5 ? 360 : data.angle;
    truck.style.transform = `translate(-50%, -50%) rotate(${truckAngle}deg) translateY(calc(-1 * var(--radius, 190px)))`;

    // Cambiar imagen de camión en llegada (paso 5)
    if (truckImg) {
      if (stepNum === 5) {
        truckImg.src = "/img/delivery-truck-end.svg";
      } else {
        truckImg.src = "/img/delivery-truck-start.svg";
      }
    }

    // Actualizar Pills de Control
    document.querySelectorAll(".story-pill").forEach((pill) => {
      const step = parseInt(pill.getAttribute("data-step"), 10);
      if (step === stepNum) {
        pill.classList.add("is-active");
        pill.setAttribute("aria-selected", "true");
      } else {
        pill.classList.remove("is-active");
        pill.setAttribute("aria-selected", "false");
      }
    });

    // Actualizar Nodos en el Dial
    document.querySelectorAll(".dial-node").forEach((node) => {
      const nodeStep = parseInt(node.getAttribute("data-node"), 10);
      node.classList.remove("is-active", "is-passed", "is-closing");
      if (nodeStep === stepNum) {
        node.classList.add("is-active");
        if (stepNum === 5) node.classList.add("is-closing");
      } else if (nodeStep < stepNum) {
        node.classList.add("is-passed");
      }
    });

    // Si el usuario hizo clic manual, resetear el timer de auto-play para no pisar su interacción
    if (manualClick && isPlaying) {
      resetAutoplayTimer();
    }
  };

  // 3. Control de Autoplay
  function stepForward() {
    let next = currentStep + 1;
    if (next > 5) next = 1;
    selectCircleStep(next, false);
  }

  function startAutoplay() {
    if (prefersReducedMotion) return;
    stopAutoplay();
    isPlaying = true;
    autoplayTimer = setInterval(stepForward, AUTOPLAY_INTERVAL);
    if (autoplayIcon) autoplayIcon.textContent = "⏸";
    if (autoplayText) autoplayText.textContent = "Pausar ciclo automático";
    if (autoplayBtn) autoplayBtn.setAttribute("aria-label", "Pausar ciclo automático");
  }

  function stopAutoplay() {
    if (autoplayTimer) {
      clearInterval(autoplayTimer);
      autoplayTimer = null;
    }
    isPlaying = false;
    if (autoplayIcon) autoplayIcon.textContent = "▶";
    if (autoplayText) autoplayText.textContent = "Reanudar ciclo";
    if (autoplayBtn) autoplayBtn.setAttribute("aria-label", "Reanudar ciclo automático");
  }

  function resetAutoplayTimer() {
    if (!isPlaying) return;
    if (autoplayTimer) clearInterval(autoplayTimer);
    autoplayTimer = setInterval(stepForward, AUTOPLAY_INTERVAL);
  }

  window.toggleCircleAutoplay = function () {
    if (isPlaying) {
      stopAutoplay();
    } else {
      startAutoplay();
    }
  };

  // Pausar en hover para permitir lectura cómoda
  if (section) {
    section.addEventListener("mouseenter", () => {
      if (isPlaying) {
        clearInterval(autoplayTimer);
      }
    });
    section.addEventListener("mouseleave", () => {
      if (isPlaying) {
        resetAutoplayTimer();
      }
    });
  }

  // Activar solo cuando la sección entra en el viewport
  if ("IntersectionObserver" in window && section) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            if (isPlaying && !autoplayTimer) {
              startAutoplay();
            }
          } else {
            if (autoplayTimer) {
              clearInterval(autoplayTimer);
              autoplayTimer = null;
            }
          }
        });
      },
      { threshold: 0.2 }
    );
    observer.observe(section);
  }

  // Inicialización
  renderTicks();
  selectCircleStep(1, false);
  if (!prefersReducedMotion) {
    startAutoplay();
  } else {
    stopAutoplay();
  }
})();
