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
    if (who === "bot") {
      // Renderiza respuestas enriquecidas con HTML (encabezados, listas, enlaces y destacados)
      div.innerHTML = text;
    } else {
      // Mensajes del usuario se mantienen como texto plano seguro (anti-XSS)
      div.textContent = text;
      div.style.whiteSpace = "pre-wrap";
    }
    log.appendChild(div);
    log.scrollTop = log.scrollHeight;
  };

  const getCsrfHeaders = () => {
    const headers = { "Content-Type": "application/json" };
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (tokenMeta && headerMeta && tokenMeta.content && headerMeta.content) {
      headers[headerMeta.content] = tokenMeta.content;
    } else {
      const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
      if (match) {
        headers["X-XSRF-TOKEN"] = decodeURIComponent(match[1]);
      }
    }
    return headers;
  };

  const respond = (q) => {
    // Le pegamos al endpoint proxy de DonaTrack Web (evita llamadas directas y fallas CORS/host)
    fetch("/api/public/chatbot", {
      method: "POST",
      headers: getCsrfHeaders(),
      body: JSON.stringify({ mensaje: q })
    })
      .then((res) => res.json())
      .then((data) => {
        addMsg(data.respuesta || "Recibimos tu consulta. En breve te responderemos.", "bot");
      })
      .catch((e) => {
        console.warn("Error al conectar con chatbot proxy:", e);
        addMsg("Ups, el bot está fuera de servicio en este momento.", "bot");
      });
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
