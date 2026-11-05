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
    div.innerHTML = text; // Permite renderizar links HTML
    div.style.whiteSpace = "pre-wrap"; // Permite los saltos de línea \n
    log.appendChild(div);
    log.scrollTop = log.scrollHeight;
  };

  const respond = (q) => {
    // Le pegamos al Webhook de producción de n8n
    fetch("http://localhost:5678/webhook/chatbot", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ mensaje: q })
    })
      .then((res) => res.json())
      .then((data) => {
        addMsg(data.respuesta || "Mensaje procesado por n8n (sin respuesta)", "bot");
      })
      .catch((e) => {
        console.warn("Error al conectar con n8n:", e);
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
