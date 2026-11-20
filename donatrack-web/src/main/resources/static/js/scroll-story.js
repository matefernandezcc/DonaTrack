/* DonaTrack — Circular Storytelling: "El Círculo se Cierra" */
(function () {
  "use strict";

  const STEPS = [
    {
      num: "01",
      angle: 0,
      title: "Donación Realizada",
      short: "Registro",
      desc: "Registrás los bienes y se inicia el seguimiento.",
      status: "Trazabilidad Iniciada",
      origin: "Recepción en depósito",
      points: "+10 Puntos de Impacto",
      progress: 0
    },
    {
      num: "02",
      angle: 72,
      title: "Centro de Recepción",
      short: "Recepción",
      desc: "Pesamos y clasificamos el lote en depósito.",
      status: "Lote Auditado",
      origin: "Depósito Central DonaTrack",
      points: "Control de Calidad 100%",
      progress: 0.2
    },
    {
      num: "03",
      angle: 144,
      title: "Transporte en Ruta",
      short: "En Ruta",
      desc: "Asignamos camión y chofer hacia la ONG.",
      status: "Móvil Despachado",
      origin: "Ruta Activa con Chofer",
      points: "Monitoreo Satelital",
      progress: 0.4
    },
    {
      num: "04",
      angle: 216,
      title: "Entrega en ONG",
      short: "En Sede",
      desc: "El móvil arriba al comedor y descarga los bienes.",
      status: "En Destino",
      origin: "Comedor Verificado",
      points: "Recepción Física",
      progress: 0.6
    },
    {
      num: "05",
      angle: 288,
      title: "Comprobante y Remito",
      short: "Remito",
      desc: "Se firma el remito digital con foto verificable.",
      status: "Entrega Certificada",
      origin: "Comprobante + Foto S3",
      points: "+25 Puntos de Entrega",
      progress: 0.8
    },
    {
      num: "06",
      angle: 360,
      title: "Recorrido Completo",
      short: "En Sucursal",
      desc: "El recorrido está completo y el camión volvió a la sucursal para continuar con otra donación.",
      status: "Ciclo 100% Completado",
      origin: "Sucursal Central DonaTrack",
      points: "100% Trazabilidad Auditada",
      progress: 1.0
    }
  ];

  let currentStep = 1;
  let currentAngle = 0;
  let completedFullLoop = false;
  let animFrameId = null;
  let closingTimeoutId = null;
  let isPlaying = true;
  let autoplayTimer = null;
  const AUTOPLAY_INTERVAL = 15000; // ms (15 segundos exactos)
  const ANIM_DURATION = 900; // ms

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
    for (let deg = 0; deg < 360; deg += 9) {
      const isMajor = deg % 72 === 0; // Hitos principales (5 etapas)
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

  // 2. Sincronizar posición del camión y trazado verde en el dial
  function applyDialAngle(deg) {
    currentAngle = deg;
    truck.style.transform = `translate(-50%, -50%) rotate(${deg.toFixed(2)}deg) translateY(calc(-1 * var(--radius, 190px)))`;
    // Luego de completar la primera vuelta, el círculo permanece 100% verde
    if (completedFullLoop) {
      progressCircle.style.strokeDashoffset = "0";
      return;
    }
    // El trazo verde queda unos grados detrás del centro del camión para que éste lo vaya pintando al avanzar
    const trailDeg = deg <= 0 ? 0 : deg >= 359.5 ? 360 : Math.max(0, deg - 5);
    const targetOffset = CIRCUMFERENCE * (1 - trailDeg / 360);
    progressCircle.style.strokeDashoffset = targetOffset.toFixed(1);
  }

  function cancelDialAnimation() {
    if (animFrameId !== null) {
      cancelAnimationFrame(animFrameId);
      animFrameId = null;
    }
    if (closingTimeoutId !== null) {
      clearTimeout(closingTimeoutId);
      closingTimeoutId = null;
    }
  }

  function animateDialTo(targetAngle, onDone) {
    cancelDialAnimation();
    let startAngle = currentAngle;
    if (startAngle >= 360 && targetAngle < 360) {
      startAngle = 0;
      applyDialAngle(0);
    }
    if (prefersReducedMotion || Math.abs(targetAngle - startAngle) < 0.5) {
      applyDialAngle(targetAngle);
      if (onDone) onDone();
      return;
    }

    const startTime = performance.now();
    function frame(now) {
      const elapsed = now - startTime;
      const t = Math.min(1, elapsed / ANIM_DURATION);
      const eased = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
      applyDialAngle(startAngle + (targetAngle - startAngle) * eased);
      if (t < 1) {
        animFrameId = requestAnimationFrame(frame);
      } else {
        animFrameId = null;
        applyDialAngle(targetAngle);
        if (onDone) onDone();
      }
    }
    animFrameId = requestAnimationFrame(frame);
  }

  // 3. Actualizar la vista al paso seleccionado
  window.selectCircleStep = function (stepNum, manualClick = true) {
    if (stepNum < 1 || stepNum > 6) return;
    currentStep = stepNum;
    const data = STEPS[stepNum - 1];

    // Actualizar Texto de Tarjeta Dinámica con suave fade
    if (card) {
      card.style.opacity = "0.4";
      card.style.transform = "translateY(4px)";
      setTimeout(() => {
        if (stagePill) stagePill.textContent = `ETAPA ${data.num} / 06`;
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
      hubStage.style.color = stepNum >= 5 ? "#16a34a" : "var(--fg)";
    }

    // Cambiar imagen del camión según etapa
    if (truckImg) {
      truckImg.src = stepNum >= 5 ? "/img/delivery-truck-end.svg" : "/img/delivery-truck-start.svg";
    }

    // Mover camión y pintar trazo verde sincronizado frame a frame
    if (stepNum === 1 && currentAngle >= 280) {
      if (currentAngle >= 359) {
        completedFullLoop = true;
        cancelDialAnimation();
        applyDialAngle(0);
      } else {
        if (truckImg) truckImg.src = "/img/delivery-truck-start.svg";
        animateDialTo(360, () => {
          completedFullLoop = true;
          applyDialAngle(0);
        });
      }
    } else {
      animateDialTo(data.angle, () => {
        if (stepNum === 6) {
          completedFullLoop = true;
        }
        if (truckImg && stepNum >= 5) {
          truckImg.src = "/img/delivery-truck-end.svg";
        }
      });
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

    // Actualizar Nodos en el Dial (luego de la primera vuelta quedan siempre completados en verde)
    document.querySelectorAll(".dial-node").forEach((node) => {
      const nodeStep = parseInt(node.getAttribute("data-node"), 10);
      node.classList.remove("is-active", "is-passed", "is-closing");
      if (nodeStep === stepNum) {
        node.classList.add("is-active");
        if (stepNum === 5) node.classList.add("is-closing");
      } else if (completedFullLoop || nodeStep < stepNum) {
        node.classList.add("is-passed");
      }
      if (stepNum === 6 && nodeStep === 1) {
        node.classList.add("is-closing");
      }
    });

    // Si el usuario hizo clic manual, resetear el timer de auto-play para no pisar su interacción
    if (manualClick && isPlaying) {
      resetAutoplayTimer();
    }
  };

  // 4. Control de Autoplay (desplazamiento continuo en ciclo cada 15 segundos)
  function stepForward() {
    let next = currentStep + 1;
    if (next > 6) {
      completedFullLoop = true;
      next = 1;
    }
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

  // Pausar en hover solo sobre la tarjeta de lectura si el usuario apoya el cursor en ella
  if (card) {
    card.addEventListener("mouseenter", () => {
      if (isPlaying && autoplayTimer) {
        clearInterval(autoplayTimer);
        autoplayTimer = null;
      }
    });
    card.addEventListener("mouseleave", () => {
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

  // Soporte de teclado (Enter / Espacio) para los nodos accesibles del dial
  document.querySelectorAll(".dial-node").forEach((node) => {
    node.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " " || e.code === "Space") {
        e.preventDefault();
        const step = parseInt(node.getAttribute("data-node"), 10);
        if (step) {
          selectCircleStep(step);
        }
      }
    });
  });

  // Inicialización
  renderTicks();
  selectCircleStep(1, false);
  if (!prefersReducedMotion) {
    startAutoplay();
  } else {
    stopAutoplay();
  }
})();
