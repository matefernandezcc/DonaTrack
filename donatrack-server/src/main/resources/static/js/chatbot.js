/* DonaTrack — Floating chatbot (UI shell) */
(function () {
  "use strict";

  const fab = document.getElementById("chat-fab");
  const panel = document.getElementById("chat-panel");
  const closeBtn = document.getElementById("chat-close");
  const log = document.getElementById("chat-log");
  const form = document.getElementById("chat-form");
  const input = document.getElementById("chat-input");
  if (!fab || !panel) return;

  const open = () => {
    panel.hidden = false;
    fab.setAttribute("aria-expanded", "true");
    setTimeout(() => input && input.focus(), 50);
  };
  const close = () => {
    panel.hidden = true;
    fab.setAttribute("aria-expanded", "false");
    fab.focus();
  };
  const toggle = () => (panel.hidden ? open() : close());

  fab.addEventListener("click", toggle);
  closeBtn.addEventListener("click", close);
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !panel.hidden) close();
  });

  const addMsg = (text, who) => {
    const div = document.createElement("div");
    div.className = "msg msg--" + who;
    div.textContent = text;
    log.appendChild(div);
    log.scrollTop = log.scrollHeight;
  };

  const respond = (q) => {
    const lower = q.toLowerCase();
    let r =
      "Gracias por tu mensaje. Un miembro del equipo te responderá pronto. Mientras tanto, podés explorar las campañas o registrarte.";
    if (/dona[rn]|donaci/.test(lower))
      r =
        "Para donar: 1) Registrate (toma menos de un minuto), 2) Elegí una campaña o creá una donación libre, 3) Coordiná retiro o entrega. Vas a recibir actualizaciones en cada etapa.";
    else if (/campaña|campan/.test(lower))
      r =
        "Podés ver las campañas activas en la sección 'Campañas destacadas' más arriba. Cada campaña muestra el progreso, los donantes y la ONG verificada.";
    else if (/registr/.test(lower))
      r =
        "El registro es gratuito y rápido. Encontrá los accesos para donantes y organizaciones en la sección 'Sumate'.";
    else if (/seguim|tracking|trazab|estado/.test(lower))
      r =
        "Cada donación recibe un ID. Desde tu panel podés ver el estado en vivo: recibida, en acopio, en transporte, recibida por la ONG y entregada.";
    else if (/privac|datos|ley/.test(lower))
      r =
        "Cuidamos tus datos según la Ley 25.326. Solo recolectamos lo necesario para operar la plataforma y nunca compartimos información con terceros sin tu consentimiento.";
    setTimeout(() => addMsg(r, "bot"), 500);
  };

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const v = input.value.trim();
    if (!v) return;
    addMsg(v, "user");
    input.value = "";
    respond(v);
  });

  document.querySelectorAll(".chip").forEach((chip) =>
    chip.addEventListener("click", () => {
      const q = chip.dataset.q || chip.textContent;
      addMsg(q, "user");
      respond(q);
    })
  );
})();
