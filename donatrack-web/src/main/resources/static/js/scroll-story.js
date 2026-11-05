/* DonaTrack — scroll-driven truck on SVG path */
(function () {
  "use strict";

  const stage = document.getElementById("story-stage");
  const truck = document.getElementById("story-truck");
  const truckImg = document.getElementById("truck-image");
  const path = document.getElementById("road");
  if (!stage || !truck || !path) return;

  const reduce =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  const stepEls = stage.querySelectorAll(".step");
  const sio = new IntersectionObserver(
    (entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) e.target.classList.add("is-visible");
      });
    },
    { threshold: 0.35 }
  );
  stepEls.forEach((el) => sio.observe(el));

  if (reduce) {
    // Park truck at the start, skip scroll animation.
    truck.style.transform = "translate(-50%, -50%) scale(1.5)";
    return;
  }

  const total = path.getTotalLength();
  let svgRect, stageRect, ticking = false;

  const measure = () => {
    svgRect = path.ownerSVGElement.getBoundingClientRect();
    stageRect = stage.getBoundingClientRect();
  };

  const update = () => {
    ticking = false;
    const rect = stage.getBoundingClientRect();
    const vh = window.innerHeight;
    // Progress: 0 when the stage top is at the middle of the viewport (vh / 2),
    // 1 when the stage bottom is at the middle of the viewport. This keeps
    // the truck perfectly centered vertically on screen during scroll.
    const raw = ((vh / 2) - rect.top) / Math.max(1, rect.height);
    const p = Math.max(0, Math.min(1, raw));

    // Limit the point length to slightly before the end to always safely look ahead.
    // This prevents the angle from snapping to 0 when p = 1.
    const ptLength = Math.min(p * total, total - 2);
    const pt = path.getPointAtLength(ptLength);
    const ahead = path.getPointAtLength(ptLength + 2);
    let angle = Math.atan2(ahead.y - pt.y, ahead.x - pt.x) * (180 / Math.PI);

    // Smoothly turn the truck horizontally at the very end of the path
    if (p > 0.95) {
      const ease = (p - 0.95) / 0.05; // 0 to 1 progress of the final stretch
      angle = angle * (1 - ease);

      // Switch to the end truck image
      if (truckImg && !truckImg.src.includes("delivery-truck-end")) {
        truckImg.src = "img/delivery-truck-end.svg";
      }
    } else {
      // Ensure it stays as the start truck image
      if (truckImg && !truckImg.src.includes("delivery-truck-start")) {
        truckImg.src = "img/delivery-truck-start.svg";
      }
    }

    // SVG uses viewBox 1000x1800 with preserveAspectRatio=none, so we map
    // path coords proportionally to current rendered SVG size.
    const sx = (pt.x / 1000) * svgRect.width;
    const sy = (pt.y / 1800) * svgRect.height;
    // Position relative to stage (truck is positioned inside stage).
    const left = svgRect.left - stageRect.left + sx;
    const top = svgRect.top - stageRect.top + sy;

    // Flip truck vertically if moving left so it doesn't appear upside down
    const isMovingLeft = Math.abs(angle) > 90;
    const flipY = isMovingLeft ? -1 : 1;

    // Scale the truck up (1.5x) and apply the flip
    truck.style.transform = `translate(${left}px, ${top}px) translate(-50%, -50%) rotate(${angle}deg) scale(1.5, ${1.5 * flipY})`;

    // Highlight closest step
    let closestStep = null;
    let minDist = Infinity;
    stepEls.forEach((el) => {
      el.classList.remove("is-active");
      const stepCenter = el.offsetTop + (el.offsetHeight / 2);
      const dist = Math.abs(top - stepCenter);
      if (dist < minDist) {
        minDist = dist;
        closestStep = el;
      }
    });

    if (closestStep && minDist < 250) {
      closestStep.classList.add("is-active");
    }
  };

  const onScroll = () => {
    if (!ticking) {
      ticking = true;
      requestAnimationFrame(update);
    }
  };

  const onResize = () => {
    measure();
    update();
  };

  measure();
  update();
  window.addEventListener("scroll", onScroll, { passive: true });
  window.addEventListener("resize", onResize);
})();
