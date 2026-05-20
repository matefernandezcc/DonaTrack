/* DonaTrack — scroll-driven truck on SVG path */
(function () {
  "use strict";

  const stage = document.getElementById("story-stage");
  const truck = document.getElementById("story-truck");
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
    truck.style.transform = "translate(-50%, -50%)";
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
    // Progress: 0 when the stage just enters the viewport (top === vh),
    // 1 when the stage is fully revealed (bottom === vh). This way the
    // truck completes its path before the user scrolls past the section.
    const raw = (vh - rect.top) / Math.max(1, rect.height);
    const p = Math.max(0, Math.min(1, raw));

    const pt = path.getPointAtLength(p * total);
    const ahead = path.getPointAtLength(Math.min(total, p * total + 1));
    const angle = Math.atan2(ahead.y - pt.y, ahead.x - pt.x) * (180 / Math.PI);

    // SVG uses viewBox 1000x1800 with preserveAspectRatio=none, so we map
    // path coords proportionally to current rendered SVG size.
    const sx = (pt.x / 1000) * svgRect.width;
    const sy = (pt.y / 1800) * svgRect.height;
    // Position relative to stage (truck is positioned inside stage).
    const left = svgRect.left - stageRect.left + sx;
    const top = svgRect.top - stageRect.top + sy;

    truck.style.transform = `translate(${left}px, ${top}px) translate(-50%, -50%) rotate(${angle}deg)`;
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
