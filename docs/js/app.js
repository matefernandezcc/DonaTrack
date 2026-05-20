/* DonaTrack — global UI behaviours */
(function () {
  "use strict";

  // --- Theme Toggle ---
  const themeBtn = document.getElementById("theme-toggle");
  if (themeBtn) {
    themeBtn.addEventListener("click", () => {
      const docEl = document.documentElement;
      const isDark = docEl.getAttribute("data-theme") === "dark";
      const newTheme = isDark ? "light" : "dark";
      docEl.setAttribute("data-theme", newTheme);
      localStorage.setItem("dt-theme", newTheme);
    });
  }

  // --- Page Loader ---
  const loader = document.getElementById("page-loader");
  if (loader) {
    document.body.style.overflow = "hidden";
    setTimeout(() => {
      loader.classList.add("is-hidden");
      document.body.style.overflow = "";
    }, 2400);
  }

  // --- Navbar elevation on scroll ---
  const nav = document.getElementById("nav");
  const onScroll = () => {
    if (window.scrollY > 8) nav.classList.add("is-elevated");
    else nav.classList.remove("is-elevated");
  };
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();

  // --- Mobile drawer ---
  const burger = document.getElementById("nav-burger");
  const drawer = document.getElementById("nav-drawer");
  if (burger && drawer) {
    burger.addEventListener("click", () => {
      const open = burger.getAttribute("aria-expanded") === "true";
      burger.setAttribute("aria-expanded", String(!open));
      drawer.hidden = open;
    });
    drawer.querySelectorAll("a").forEach((a) =>
      a.addEventListener("click", () => {
        burger.setAttribute("aria-expanded", "false");
        drawer.hidden = true;
      })
    );
  }

  // --- Animated counters ---
  const counters = document.querySelectorAll(".metric__num");
  const reduce =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  const animate = (el) => {
    const target = parseInt(el.dataset.count || "0", 10);
    if (reduce) {
      el.textContent = target.toLocaleString("es-AR");
      return;
    }
    const dur = 1600;
    const start = performance.now();
    const step = (now) => {
      const p = Math.min(1, (now - start) / dur);
      const eased = 1 - Math.pow(1 - p, 3);
      el.textContent = Math.floor(target * eased).toLocaleString("es-AR");
      if (p < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  };

  const io = new IntersectionObserver(
    (entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          animate(e.target);
          io.unobserve(e.target);
        }
      });
    },
    { threshold: 0.4 }
  );
  counters.forEach((c) => io.observe(c));

  // --- Year ---
  const y = document.getElementById("year");
  if (y) y.textContent = new Date().getFullYear();
})();
