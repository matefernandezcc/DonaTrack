/* DonaTrack — Asistente Conversacional (Bot rápido -> Handoff -> Sofía Agentforce) */
(function () {
  "use strict";

  const fab = document.getElementById("chat-fab");
  const panel = document.getElementById("chat-panel");
  const closeBtn = document.getElementById("chat-close");
  const log = document.getElementById("chat-log");
  const form = document.getElementById("chat-form");
  const input = document.getElementById("chat-input");
  const submitBtn = document.getElementById("chat-submit");
  const titleEl = document.getElementById("chat-title");
  const statusEl = document.getElementById("chat-status");
  const chipsWrap = document.getElementById("chat-chips");
  if (!fab || !panel || !log || !form || !input) return;

  const SESSION_KEY = "donatrack_chat_session_id";
  let agentMode = false;
  let sendChain = Promise.resolve();

  const getSessionId = () => {
    try {
      let existing = sessionStorage.getItem(SESSION_KEY);
      if (!existing) {
        existing =
          window.crypto && typeof window.crypto.randomUUID === "function"
            ? window.crypto.randomUUID()
            : "dt-" + Date.now() + "-" + Math.random().toString(36).slice(2, 9);
        sessionStorage.setItem(SESSION_KEY, existing);
      }
      return existing;
    } catch (e) {
      return "dt-anon-" + Date.now();
    }
  };

  const sessionId = getSessionId();

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

  /** Eliminación defensiva de cualquier etiqueta HTML cruda sin usar innerHTML */
  const stripHtml = (raw) => {
    if (raw == null) return "";
    let s = String(raw);
    s = s.replace(/<br\s*\/?>/gi, "\n");
    s = s.replace(/<\/(p|div|h[1-6]|li|tr)>/gi, "\n");
    s = s.replace(/<li[^>]*>/gi, "- ");
    s = s.replace(/<[^>]*>/g, "");
    s = s
      .replace(/&nbsp;/gi, " ")
      .replace(/&amp;/gi, "&")
      .replace(/&lt;/gi, "<")
      .replace(/&gt;/gi, ">")
      .replace(/&quot;/gi, '"')
      .replace(/&#39;/gi, "'");
    return s.replace(/\n{3,}/g, "\n\n").trim();
  };

  /** Renderizador Markdown seguro vía DOM nodes (sin innerHTML / dangerouslySetInnerHTML) */
  const appendInlineFormatted = (target, text) => {
    const regex = /(\*\*[^*]+\*\*|\*[^*]+\*|`[^`]+`)/g;
    let lastIndex = 0;
    let match;
    while ((match = regex.exec(text)) !== null) {
      if (match.index > lastIndex) {
        target.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
      }
      const token = match[0];
      if (token.startsWith("**") && token.endsWith("**")) {
        const el = document.createElement("strong");
        el.textContent = token.slice(2, -2);
        target.appendChild(el);
      } else if (token.startsWith("*") && token.endsWith("*")) {
        const el = document.createElement("em");
        el.textContent = token.slice(1, -1);
        target.appendChild(el);
      } else if (token.startsWith("`") && token.endsWith("`")) {
        const el = document.createElement("code");
        el.textContent = token.slice(1, -1);
        target.appendChild(el);
      }
      lastIndex = regex.lastIndex;
    }
    if (lastIndex < text.length) {
      target.appendChild(document.createTextNode(text.slice(lastIndex)));
    }
  };

  const renderSafeMarkdown = (container, rawText) => {
    const clean = stripHtml(rawText);
    const lines = clean.split(/\r?\n/);
    let currentList = null;

    lines.forEach((line) => {
      const trimmed = line.trim();
      if (!trimmed) {
        currentList = null;
        return;
      }
      if (/^[-•]\s+/.test(trimmed)) {
        if (!currentList) {
          currentList = document.createElement("ul");
          container.appendChild(currentList);
        }
        const li = document.createElement("li");
        appendInlineFormatted(li, trimmed.replace(/^[-•]\s+/, ""));
        currentList.appendChild(li);
      } else {
        currentList = null;
        const p = document.createElement("p");
        appendInlineFormatted(p, trimmed);
        container.appendChild(p);
      }
    });

    if (!container.hasChildNodes()) {
      container.textContent = clean;
    }
  };

  const scrollToBottom = () => {
    log.scrollTop = log.scrollHeight;
  };

  const addMessageBubble = (text, who) => {
    const div = document.createElement("div");
    div.className = "msg msg--" + who;
    if (who === "user") {
      div.textContent = text;
    } else {
      renderSafeMarkdown(div, text);
    }
    log.appendChild(div);
    scrollToBottom();
    return div;
  };

  const addSystemNotice = (text) => {
    const div = document.createElement("div");
    div.className = "msg--system";
    div.textContent = text;
    log.appendChild(div);
    scrollToBottom();
  };

  const showTypingIndicator = (label) => {
    const wrap = document.createElement("div");
    wrap.className = "chatbot__typing";

    const dots = document.createElement("span");
    dots.className = "chatbot__typing-dots";
    dots.setAttribute("aria-hidden", "true");
    for (let i = 0; i < 3; i++) {
      dots.appendChild(document.createElement("span"));
    }

    const txt = document.createElement("span");
    txt.textContent = label;

    wrap.appendChild(dots);
    wrap.appendChild(txt);
    log.appendChild(wrap);
    scrollToBottom();
    return wrap;
  };

  const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

  const switchToAgentHeader = (agentName) => {
    if (!agentMode) {
      agentMode = true;
      panel.classList.add("chatbot--agent");
      if (chipsWrap) chipsWrap.hidden = true;
      addSystemNotice("Sofía se unió al chat");
    }
    if (titleEl) {
      titleEl.textContent = agentName || "Sofía - Soporte Donatrack";
    }
    if (statusEl) {
      statusEl.replaceChildren();
      const dot = document.createElement("i");
      dot.setAttribute("aria-hidden", "true");
      statusEl.appendChild(dot);
      statusEl.appendChild(document.createTextNode(" Agente conectada"));
    }
  };

  const tryLaunchMiawChat = async () => {
    const canLaunch = () =>
      window.embeddedservice_bootstrap &&
      window.embeddedservice_bootstrap.utilAPI &&
      typeof window.embeddedservice_bootstrap.utilAPI.launchChat === "function";

    if (!canLaunch() && !window.__dtMiawReady) {
      await new Promise((resolve) => {
        let done = false;
        const finish = () => {
          if (!done) {
            done = true;
            resolve();
          }
        };
        window.__dtOnMiawReady = finish;
        setTimeout(finish, 2500);
      });
    }

    if (canLaunch() && window.__dtMiawReady) {
      try {
        await window.embeddedservice_bootstrap.utilAPI.launchChat();
        panel.hidden = true;
        fab.setAttribute("aria-expanded", "false");
        fab.style.display = "none";
        return true;
      } catch (err) {
        console.warn("Error al abrir MIAW launchChat:", err);
      }
    }
    return false;
  };

  const addHandoffButton = (label) => {
    const wrap = document.createElement("div");
    wrap.className = "chatbot__handoff-wrap";

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "chatbot__handoff-btn";
    btn.textContent = label || "Contactar con un agente";

    btn.addEventListener("click", async () => {
      document.querySelectorAll(".chatbot__handoff-btn").forEach((b) => {
        b.disabled = true;
      });
      const launched = await tryLaunchMiawChat();
      if (!launched) {
        enqueueRequest("", "HANDOFF_AGENT");
      }
    });

    wrap.appendChild(btn);
    log.appendChild(wrap);
    scrollToBottom();
  };

  const getCsrfHeaders = () => {
    const headers = { "Content-Type": "application/json" };
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    if (match && match[1]) {
      headers["X-XSRF-TOKEN"] = decodeURIComponent(match[1]);
      return headers;
    }
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (tokenMeta && headerMeta && tokenMeta.content && headerMeta.content) {
      headers[headerMeta.content] = tokenMeta.content;
    }
    return headers;
  };

  const setInputDisabled = (disabled) => {
    input.disabled = disabled;
    if (submitBtn) submitBtn.disabled = disabled;
  };

  const postChatbotWithCsrfRetry = async (bodyPayload) => {
    const bodyStr = JSON.stringify(bodyPayload);
    let res = await fetch("/api/public/chatbot", {
      method: "POST",
      credentials: "same-origin",
      headers: getCsrfHeaders(),
      body: bodyStr
    });
    if (res.status === 403) {
      await fetch("/", { method: "GET", credentials: "same-origin" });
      res = await fetch("/api/public/chatbot", {
        method: "POST",
        credentials: "same-origin",
        headers: getCsrfHeaders(),
        body: bodyStr
      });
    }
    if (!res.ok) {
      throw new Error("HTTP " + res.status);
    }
    return res.json();
  };

  const processChatRequest = async (messageText, action) => {
    setInputDisabled(true);
    const waitingLabel =
      agentMode || action === "HANDOFF_AGENT"
        ? "Sofía está escribiendo..."
        : "Consultando asistente...";
    const typingEl = showTypingIndicator(waitingLabel);

    try {
      const data = await postChatbotWithCsrfRetry({
        message: messageText,
        mensaje: messageText,
        sessionId: sessionId,
        action: action || null
      });
      typingEl.remove();

      const mode = data && data.mode ? String(data.mode).toLowerCase() : "bot";
      let chunks = [];
      if (data && Array.isArray(data.chunks) && data.chunks.length > 0) {
        chunks = data.chunks.map((c) => stripHtml(c)).filter(Boolean);
      } else if (data && data.respuesta) {
        chunks = [stripHtml(data.respuesta)].filter(Boolean);
      }
      if (chunks.length === 0) {
        chunks = ["Recibimos tu consulta. En breve te responderemos."];
      }

      if (mode === "agent") {
        switchToAgentHeader(data.agentName || "Sofía - Soporte Donatrack");
        for (let i = 0; i < chunks.length; i++) {
          const sofiaTyping = showTypingIndicator("Sofía está escribiendo...");
          await sleep(1200);
          sofiaTyping.remove();
          addMessageBubble(chunks[i], "agent");
        }
      } else {
        chunks.forEach((chunk) => addMessageBubble(chunk, "bot"));
        if (
          data &&
          data.suggestedAction &&
          data.suggestedAction.type === "HANDOFF_AGENT"
        ) {
          addHandoffButton(data.suggestedAction.label);
        }
      }
    } catch (err) {
      typingEl.remove();
      console.warn("Error al conectar con chatbot proxy:", err);
      addMessageBubble(
        "El asistente está momentáneamente ocupado. Intentá nuevamente en unos segundos.",
        "bot"
      );
    } finally {
      setInputDisabled(false);
      if (!panel.hidden) input.focus();
    }
  };

  const enqueueRequest = (messageText, action) => {
    sendChain = sendChain.then(() => processChatRequest(messageText, action));
    return sendChain;
  };

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    const val = input.value.trim();
    if (!val) return;
    addMessageBubble(val, "user");
    input.value = "";
    enqueueRequest(val, null);
  });

  document.querySelectorAll("#chat-chips .chip").forEach((chip) =>
    chip.addEventListener("click", async () => {
      const q = (chip.dataset.q || chip.textContent || "").trim();
      if (!q) return;
      const action = chip.dataset.action || null;
      addMessageBubble(q, "user");
      if (action === "HANDOFF_AGENT") {
        document.querySelectorAll(".chatbot__handoff-btn").forEach((b) => {
          b.disabled = true;
        });
        const launched = await tryLaunchMiawChat();
        if (launched) return;
      }
      enqueueRequest(q, action);
    })
  );
})();
