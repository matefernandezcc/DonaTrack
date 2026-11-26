/* DonaTrack — Asistente Nativo para Paneles Logueados (Copiloto In-Page + Autocompletado + n8n) */
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

  const userRole = (panel.dataset.role || "DONANTE").toUpperCase();
  const SESSION_KEY = "donatrack_chat_session_id";
  const HISTORY_KEY = "donatrack_chat_history_v1_" + userRole;
  const MAX_HISTORY = 12;

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

  const saveHistoryEntry = (who, text, actionMeta) => {
    try {
      const raw = sessionStorage.getItem(HISTORY_KEY);
      const list = raw ? JSON.parse(raw) : [];
      if (!Array.isArray(list)) return;
      list.push({ who: who, text: String(text || ""), action: actionMeta || null });
      while (list.length > MAX_HISTORY) list.shift();
      sessionStorage.setItem(HISTORY_KEY, JSON.stringify(list));
    } catch (e) {}
  };

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

  const addMessageBubble = (text, who, skipPersist) => {
    const div = document.createElement("div");
    div.className = "msg msg--" + who;
    if (who === "user") {
      div.textContent = text;
    } else {
      renderSafeMarkdown(div, text);
    }
    log.appendChild(div);
    scrollToBottom();
    if (!skipPersist) {
      saveHistoryEntry(who, text, null);
    }
    return div;
  };

  const isSafeAppUrl = (url) => {
    if (!url || typeof url !== "string") return false;
    const trimmed = url.trim();
    if (!trimmed.startsWith("/app/") || trimmed.startsWith("//")) return false;
    return true;
  };

  const addActionButton = (label, onClickOrUrl, skipPersist) => {
    const wrap = document.createElement("div");
    wrap.className = "chatbot__action-wrap";

    if (typeof onClickOrUrl === "string" && isSafeAppUrl(onClickOrUrl)) {
      const a = document.createElement("a");
      a.className = "chatbot__action-btn";
      a.href = onClickOrUrl;
      a.textContent = label || "Abrir sección →";
      wrap.appendChild(a);
      if (!skipPersist) {
        saveHistoryEntry("action", label || "Abrir sección →", { url: onClickOrUrl });
      }
    } else if (typeof onClickOrUrl === "function") {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "chatbot__action-btn";
      btn.textContent = label || "Ejecutar acción →";
      btn.addEventListener("click", onClickOrUrl);
      wrap.appendChild(btn);
    } else {
      return;
    }

    log.appendChild(wrap);
    scrollToBottom();
  };

  const addSystemNotice = (text) => {
    const div = document.createElement("div");
    div.className = "msg--system";
    div.textContent = text;
    log.appendChild(div);
    scrollToBottom();
  };

  // Restaurar historial reciente de la sesión al navegar entre secciones
  const restoreHistory = () => {
    try {
      const raw = sessionStorage.getItem(HISTORY_KEY);
      if (!raw) return;
      const list = JSON.parse(raw);
      if (!Array.isArray(list) || list.length === 0) return;
      list.forEach((item) => {
        if (!item || !item.who) return;
        if (item.who === "action" && item.action && item.action.url) {
          addActionButton(item.text, item.action.url, true);
        } else if (item.who === "user" || item.who === "bot" || item.who === "agent") {
          addMessageBubble(item.text, item.who, true);
        }
      });
    } catch (e) {}
  };
  restoreHistory();

  // =========================================================================
  // MOTOR DE COMPRENSIÓN EN ESPAÑOL: NAVEGACIÓN IN-PAGE & AUTOCOMPLETADO
  // =========================================================================
  const normalizeText = (s) =>
    String(s || "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .trim();

  const inferCategory = (norm) => {
    if (/(fideo|arroz|leche|lenteja|polenta|aceite|azucar|yerba|harina|galletita|alimento|comida|enlatad|atun|pure|salsa|conserva|agua|merienda|desayuno|pan|vianda|seco)/.test(norm)) {
      return "Alimentos No Perecederos";
    }
    if (/(ropa|campera|frazada|abrigo|buzo|pantalon|zapatilla|calzado|remera|manta|media|bufanda|pullover|sweater|vestimenta|indumentaria|bota|zapato)/.test(norm)) {
      return "Ropa y Abrigo";
    }
    if (/(jabon|lavandina|detergente|shampoo|panal|pañal|higiene|limpieza|alcohol|papel higienico|toalla|pasta dental|cepillo|desinfectante)/.test(norm)) {
      return "Higiene y Limpieza";
    }
    if (/(medicamento|remedio|venda|gasa|botiquin|ibuprofeno|paracetamol|farmacia|primeros auxilios|curita)/.test(norm)) {
      return "Medicamentos";
    }
    if (/(util|cuaderno|mochila|lapiz|lapicera|libro|escolar|carpeta|mesa|silla|cama|colchon|mueble|mobiliario|estante|equipamiento)/.test(norm)) {
      return "Mobiliario y Equipamiento";
    }
    if (/(juguete|pelota|juego|muneca|muñeca|recreacion|bicicleta|bici|rompecabezas|peluche|infantil)/.test(norm)) {
      return "Juguetes y Recreación";
    }
    return "";
  };

  const capitalizeFirst = (s) => {
    const clean = String(s || "").trim();
    if (!clean) return "";
    return clean.charAt(0).toUpperCase() + clean.slice(1);
  };

  const parseItemDetails = (rawText) => {
    const norm = normalizeText(rawText);
    const details = {
      descripcion: "",
      cantidad: null,
      unidad: "",
      categoria: inferCategory(norm),
      esUsado: null,
      modalidad: "",
      direccionRetiro: "",
      tipo: ""
    };

    // Cantidad + unidad (ej: "10 kg", "5 kilos", "20 paquetes", "15 litros", "8 frazadas")
    const qtyUnitMatch = rawText.match(
      /(\d+(?:[.,]\d+)?)\s*(kg|kilos?|kilogramos?|l|lts?|litros?|u|unid(?:ades?)?|paquetes?|cajas?|bolsas?|packs?|bultos?|pares?|botellas?|latas?|unidades?)\b/i
    );
    if (qtyUnitMatch) {
      const num = parseFloat(qtyUnitMatch[1].replace(",", "."));
      if (!isNaN(num) && num > 0) details.cantidad = num;
      const u = normalizeText(qtyUnitMatch[2]);
      if (/^(kg|kilo|kilogramo)/.test(u)) details.unidad = "KILOS";
      else if (/^(l|lt|litro)/.test(u)) details.unidad = "LITROS";
      else details.unidad = "UNIDADES";
    } else {
      const bareQtyMatch = rawText.match(/(?:donar|dono|necesit\w+|ingresar|cargar|registrar|tengo|son|unos?)\s+(?:urgente\s+|mensual\s+|recurrente\s+)?(\d+(?:[.,]\d+)?)\b/i);
      if (bareQtyMatch) {
        const num = parseFloat(bareQtyMatch[1].replace(",", "."));
        if (!isNaN(num) && num > 0) {
          details.cantidad = num;
          details.unidad = "UNIDADES";
        }
      }
    }

    // Condición (usado vs nuevo)
    if (/\b(usad[oa]s?|segunda mano|buen estado)\b/.test(norm)) {
      details.esUsado = true;
    } else if (/\b(nuev[oa]s?|cerrad[oa]s?|sellad[oa]s?|sin abrir)\b/.test(norm)) {
      details.esUsado = false;
    }

    // Modalidad de entrega (retiro vs depósito)
    if (/\b(retiro|retirar|domicilio|pasen a buscar|pasar a buscar|buscarlo|camion a mi casa|en mi casa)\b/.test(norm)) {
      details.modalidad = "RETIRO";
      const dirMatch = rawText.match(/(?:retiro|buscar|domicilio|direccion|en)\s+(?:en\s+|por\s+)?((?:av\.?|avenida|calle|bv\.?|pasaje)\s+[^,.\n]{4,60}(?:,\s*[^.\n]{2,30})?)/i);
      if (dirMatch && dirMatch[1]) {
        details.direccionRetiro = dirMatch[1].trim();
      }
    } else if (/\b(deposito|medrano|lo llevo|los llevo|llevo yo|acercar)\b/.test(norm)) {
      details.modalidad = "DEPOSITO";
    }

    // Urgencia / frecuencia (para necesidades de ONG)
    if (/\b(urgente|urgencia|puntual|emergencia|cuanto antes|ya|extraordinaria)\b/.test(norm)) {
      details.tipo = "extraordinaria";
    } else if (/\b(mensual|recurrente|todos los meses|ordinaria|permanente)\b/.test(norm)) {
      details.tipo = "ordinaria";
    }

    // Extraer descripción limpia del bien/insumo
    let desc = rawText;
    // Recortar frases previas al objeto donado/requerido
    const triggerMatch = desc.match(
      /(?:quiero\s+donar|quisiera\s+donar|voy\s+a\s+donar|para\s+donar|donar|dono|tengo\s+para\s+donar|necesitamos|necesito|publicar\s+(?:una\s+)?necesidad(?:\s+de)?|cargar\s+(?:una\s+)?necesidad(?:\s+de)?|ingresar\s+(?:una\s+)?(?:donaci[oó]n|recepci[oó]n)(?:\s+de)?|ingresar|cargar\s+recepci[oó]n(?:\s+de)?|registrar\s+(?:una\s+)?donaci[oó]n(?:\s+de)?)\s+(.+)/i
    );
    if (triggerMatch && triggerMatch[1]) {
      desc = triggerMatch[1];
    } else {
      desc = "";
    }

    if (desc) {
      // Quitar metadatos de modalidad/urgencia/dirección al final
      desc = desc
        .replace(/\b(en\s+el\s+dep[oó]sito|en\s+dep[oó]sito|con\s+retiro\s+a\s+domicilio|con\s+retiro|para\s+retirar|a\s+domicilio|por\s+cami[oó]n(?:\s+solidario)?)\b.*$/i, "")
        .replace(/\b(urgente|de\s+urgencia|mensual|recurrente|por\s+favor|gracias|hola)\b/gi, "")
        .replace(/^\s*(?:de|unos?|unas?|el|la|los|las)\s+/i, "")
        .replace(/\s{2,}/g, " ")
        .replace(/^[.,;:-\s]+|[.,;:-\s]+$/g, "")
        .trim();

      // Si quedó solo "algo", "cosas", "esto", "hoy" -> no es una descripción concreta
      const descNorm = normalizeText(desc);
      if (!descNorm || /^(algo|esto|cosas|hoy|ahora|aca|aqui|donde|como|por aca|un bien|bienes)$/.test(descNorm)) {
        desc = "";
      }
    }

    details.descripcion = capitalizeFirst(desc);
    return details;
  };

  const buildQueryString = (obj) => {
    const params = new URLSearchParams();
    Object.keys(obj).forEach((k) => {
      const v = obj[k];
      if (v !== null && v !== undefined && String(v).trim() !== "") {
        params.set(k, String(v).trim());
      }
    });
    params.set("source", "chatbot");
    return params.toString();
  };

  const navigateWithBeat = (url) => {
    if (!isSafeAppUrl(url)) return;
    setTimeout(() => {
      window.location.assign(url);
    }, 600);
  };

  const resolveLocalCopilotAction = (rawText) => {
    const norm = normalizeText(rawText);
    const path = window.location.pathname;

    // ---------------------------------------------------------
    // 1. PORTAL BENEFICIARIA (o ADMIN en vistas de beneficiaria)
    // ---------------------------------------------------------
    const isNeedIntent =
      /\b(necesito|necesitamos|publicar necesidad|cargar necesidad|declarar necesidad|nueva necesidad|pedir insumos|faltan)\b/.test(norm);

    if ((userRole === "BENEFICIARIA" || (userRole === "ADMIN" && path.startsWith("/app/beneficiaria"))) && isNeedIntent) {
      const d = parseItemDetails(rawText);
      const hasDetails = Boolean(d.descripcion || d.cantidad || d.categoria);
      const qs = buildQueryString({
        descripcion: d.descripcion,
        cantidad: d.cantidad,
        unidad: d.unidad,
        tipo: d.tipo,
        categoria: d.categoria
      });
      const targetUrl = "/app/beneficiaria/necesidades" + (hasDetails ? "?" + qs : "");

      if (path === "/app/beneficiaria/necesidades" && typeof window.dtAutofillNecesidad === "function" && hasDetails) {
        window.dtAutofillNecesidad(d);
        const resumen = [
          d.cantidad ? d.cantidad + (d.unidad === "LITROS" ? " L" : d.unidad === "KILOS" ? " kg" : " u.") : null,
          d.descripcion || null,
          d.categoria ? "(" + d.categoria + ")" : null
        ].filter(Boolean).join(" ");
        return {
          reply: "¡Listo! Completé el formulario de necesidad con **" + (resumen || "tus datos") + "**. Revisá los campos y presioná **Publicar Necesidad**."
        };
      }

      return {
        reply: hasDetails
          ? "Te llevo a **Necesidades Materiales** con el formulario ya precargado (**" + (d.descripcion || d.categoria || "tu pedido") + "**)."
          : "Podés publicar tus necesidades desde **Necesidades Materiales**. Te llevo ahora mismo.",
        actionLabel: "Ir a Necesidades Materiales →",
        navigateUrl: targetUrl
      };
    }

    if (userRole === "BENEFICIARIA") {
      if (/\b(entrega|entregas|confirmar|camion|camiones|remito|foto|mapa|en camino|arribo)\b/.test(norm)) {
        return {
          reply: "Desde **Confirmar Entregas** podés ver los camiones en camino en el mapa y certificar cada llegada con foto. Te llevo ahora.",
          actionLabel: "Abrir Confirmar Entregas →",
          navigateUrl: "/app/beneficiaria/entregas"
        };
      }
      if (/\b(mi organizacion|mi panel|inicio|perfil|resumen|pagina publica)\b/.test(norm)) {
        return {
          reply: "Te llevo al **Panel de tu Organización**.",
          actionLabel: "Ir a Mi Organización →",
          navigateUrl: "/app/beneficiaria"
        };
      }
      if (/\b(donde|como)\b.*\b(necesidad|pedir|solicitar)\b/.test(norm)) {
        return {
          reply: "Para declarar lo que precisa tu comedor, entrá en **Necesidades Materiales**. También podés decirme directamente qué necesitan (ej: *\"necesitamos urgente 30 kg de leche\"*) y te completo el formulario.",
          actionLabel: "Ir a Necesidades Materiales →",
          navigateUrl: "/app/beneficiaria/necesidades"
        };
      }
    }

    // ---------------------------------------------------------
    // 2. PORTAL ADMINISTRADOR (Navegación y Recepción en Depósito)
    // ---------------------------------------------------------
    if (userRole === "ADMIN") {
      const isAdminReception =
        /\b(ingresar|recepcion|recibir|cargar donacion|registrar donacion|ingreso fisico)\b/.test(norm);
      if (isAdminReception) {
        const d = parseItemDetails(rawText);
        const hasDetails = Boolean(d.descripcion || d.cantidad || d.categoria);
        const qs = buildQueryString({
          descripcion: d.descripcion,
          cantidad: d.cantidad,
          unidad: d.unidad,
          categoria: d.categoria,
          esUsado: d.esUsado
        });
        const targetUrl = "/app/admin/donaciones/recepcion" + (hasDetails ? "?" + qs : "");

        if (path === "/app/admin/donaciones/recepcion" && typeof window.dtAutofillRecepcion === "function" && hasDetails) {
          window.dtAutofillRecepcion(d);
          return {
            reply: "¡Listo! Precargué la recepción en depósito con **" + (d.descripcion || d.categoria || "los bienes indicados") + "**. Seleccioná la persona donante (o Anónimo) y confirmá."
          };
        }
        return {
          reply: hasDetails
            ? "Te llevo a **Ingresar Donación en Depósito** con los datos precargados (**" + (d.descripcion || d.categoria) + "**)."
            : "Te llevo a **Ingresar Donación en Depósito**.",
          actionLabel: "Ir a Recepción en Depósito →",
          navigateUrl: targetUrl
        };
      }

      if (/\b(matchmaking|asignar|asignacion|pendientes de asignar|algoritmo)\b/.test(norm)) {
        return {
          reply: "Te llevo a **Matchmaking y Asignación a ONGs**, donde ves los lotes pendientes en depósito y los 3 criterios de asignación.",
          actionLabel: "Abrir Matchmaking →",
          navigateUrl: "/app/admin/matchmaking"
        };
      }
      if (/\b(ruta|rutas|reparto|planificar|despachar|logistica)\b/.test(norm)) {
        return {
          reply: "Te llevo a **Rutas de Reparto** para planificar e iniciar viajes.",
          actionLabel: "Abrir Rutas de Reparto →",
          navigateUrl: "/app/admin/rutas"
        };
      }
      if (/\b(camion|camiones|flota|patente)\b/.test(norm)) {
        const wantsNew = /\b(nuevo|registrar|agregar|cargar|alta)\b/.test(norm);
        const url = "/app/admin/camiones" + (wantsNew ? "?nuevo=true" : "");
        if (path === "/app/admin/camiones" && wantsNew && typeof window.abrirModalCamion === "function") {
          window.abrirModalCamion();
          return { reply: "¡Listo! Abrí el modal para **Registrar Nuevo Camión** (patentes `AAA123` o `AA123BB`)." };
        }
        return {
          reply: wantsNew
            ? "Te llevo a **Flota de Camiones** y abro el formulario de alta."
            : "Te llevo a **Flota de Camiones**.",
          actionLabel: "Ir a Flota de Camiones →",
          navigateUrl: url
        };
      }
      if (/\b(chofer|choferes|conductor|conductores|legajo)\b/.test(norm)) {
        const wantsNew = /\b(nuevo|registrar|agregar|cargar|alta)\b/.test(norm);
        const url = "/app/admin/choferes" + (wantsNew ? "?nuevo=true" : "");
        if (path === "/app/admin/choferes" && wantsNew && typeof window.abrirModalChofer === "function") {
          window.abrirModalChofer();
          return { reply: "¡Listo! Abrí el modal para **Registrar Nuevo Chofer**." };
        }
        return {
          reply: wantsNew
            ? "Te llevo a **Choferes** y abro el formulario de alta."
            : "Te llevo a **Choferes**.",
          actionLabel: "Ir a Choferes →",
          navigateUrl: url
        };
      }
      if (/\b(usuario|usuarios|aprobar ong|solicitud|solicitudes|resetear clave|roles|permisos)\b/.test(norm)) {
        return {
          reply: "Te llevo a **Usuarios y Solicitudes de ONGs** para gestionar roles, claves y aprobaciones.",
          actionLabel: "Abrir Usuarios y ONGs →",
          navigateUrl: "/app/admin/usuarios"
        };
      }
      if (/\b(csv|importar|carga masiva|padron|plantilla)\b/.test(norm)) {
        return {
          reply: "Te llevo a **Importar CSV**, donde podés arrastrar tu archivo o descargar la plantilla de ejemplo.",
          actionLabel: "Abrir Importar CSV →",
          navigateUrl: "/app/admin/csv"
        };
      }
      if (/\b(ranking|podio|mensual|historicos)\b/.test(norm)) {
        return {
          reply: "Te llevo al **Ranking Mensual**.",
          actionLabel: "Abrir Ranking →",
          navigateUrl: "/app/admin/ranking"
        };
      }
      if (/\b(resumen operativo|panel admin|pendientes de hoy|dashboard admin)\b/.test(norm)) {
        return {
          reply: "Te llevo al **Resumen Operativo** del administrador.",
          actionLabel: "Ir al Resumen Operativo →",
          navigateUrl: "/app/admin"
        };
      }
    }

    // ---------------------------------------------------------
    // 3. PORTAL DONANTE (y preview donante para ADMIN)
    // ---------------------------------------------------------
    const isDonateWithDetails =
      /\b(quiero donar|quisiera donar|voy a donar|tengo para donar|dono|donar)\b/.test(norm) &&
      !/\b(mis donaciones|estado de mi donacion|seguimiento|ver donaciones|donde veo)\b/.test(norm);

    if ((userRole === "DONANTE" || userRole === "ADMIN") && isDonateWithDetails) {
      const d = parseItemDetails(rawText);
      const hasDetails = Boolean(d.descripcion || d.cantidad || d.categoria || d.modalidad);
      const qs = buildQueryString({
        descripcion: d.descripcion,
        cantidad: d.cantidad,
        unidad: d.unidad,
        categoria: d.categoria,
        esUsado: d.esUsado,
        modalidad: d.modalidad,
        direccionRetiro: d.direccionRetiro
      });
      const targetUrl = "/app/donante/donaciones" + (hasDetails ? "?" + qs : "");

      if (path === "/app/donante/donaciones" && typeof window.dtAutofillDonacion === "function" && hasDetails) {
        window.dtAutofillDonacion(d);
        const resumen = [
          d.cantidad ? d.cantidad + (d.unidad === "LITROS" ? " L" : d.unidad === "KILOS" ? " kg" : " u.") : null,
          d.descripcion || null,
          d.categoria ? "(" + d.categoria + ")" : null,
          d.modalidad === "RETIRO" ? "· Retiro a domicilio" : d.modalidad === "DEPOSITO" ? "· Depósito Medrano 951" : null
        ].filter(Boolean).join(" ");
        return {
          reply: "¡Listo! Completé el formulario con **" + (resumen || "tus datos") + "**. Revisá los campos y presioná **Confirmar Donación**."
        };
      }

      if (hasDetails) {
        const resumen = [
          d.cantidad ? d.cantidad + (d.unidad === "LITROS" ? " L" : d.unidad === "KILOS" ? " kg" : " u.") : null,
          d.descripcion || d.categoria
        ].filter(Boolean).join(" ");
        return {
          reply: "¡Genial! Te llevo al formulario de **Nueva Donación** con **" + resumen + "** ya precargado para que solo confirmes.",
          actionLabel: "Ir al formulario precargado →",
          navigateUrl: targetUrl
        };
      }

      return {
        reply: "Podés registrar tu donación en menos de 1 minuto eligiendo si la acercás al depósito (**Medrano 951, CABA**) o si coordinás **retiro por camión solidario**. También podés escribirme qué querés donar (ej: *\"quiero donar 10 kg de fideos\"*) y te completo el formulario.",
        actionLabel: "Abrir formulario de donación →",
        navigateUrl: "/app/donante/donaciones"
      };
    }

    if (userRole === "DONANTE" || (userRole === "ADMIN" && path.startsWith("/app/donante"))) {
      // ¿Dónde dono / cómo dono?
      if (/\b(donde|como)\b.*\b(dono|donar|entrego|entregar|deposito|llevo)\b/.test(norm)) {
        return {
          reply: "Podés cargar tu aporte desde **Registrar Donación** y elegir llevarlo al depósito central (**Medrano 951, CABA**, Lun a Vie de 9 a 18 hs) o pedir **retiro por camión solidario**. Si me decís qué querés donar (ej: *\"quiero donar 5 frazadas usadas\"*) te precargo el formulario automáticamente.",
          actionLabel: "Ir a Registrar Donación →",
          navigateUrl: "/app/donante/donaciones"
        };
      }

      // Seguimiento de mis donaciones (in-page tab switch si ya está en /app/donante!)
      if (/\b(mis donaciones|seguimiento|estado de mi|donde va|donde veo mis|remito|camion asignado)\b/.test(norm)) {
        if (path === "/app/donante" && typeof window.cambiarTabDonante === "function") {
          window.cambiarTabDonante("seguimiento", true);
          if (typeof window.sincronizarSidebarDonante === "function") {
            window.sincronizarSidebarDonante("seguimiento");
          }
          const tabPanel = document.getElementById("tab-panel-seguimiento");
          if (tabPanel) tabPanel.scrollIntoView({ behavior: "smooth", block: "start" });
          return {
            reply: "¡Listo! Abrí la pestaña **Mis Donaciones** para que veas el estado paso a paso y el mapa en vivo de tus envíos."
          };
        }
        return {
          reply: "Te llevo a **Mis Donaciones** para seguir el estado y el camión asignado.",
          actionLabel: "Ver Mis Donaciones →",
          navigateUrl: "/app/donante?tab=seguimiento"
        };
      }

      // Necesidades de ONGs / causas activas
      if (/\b(necesitan|necesidades|causas|urgentes|que donar|a quien donar)\b/.test(norm)) {
        if (path === "/app/donante" && typeof window.cambiarTabDonante === "function") {
          window.cambiarTabDonante("causas", true);
          if (typeof window.sincronizarSidebarDonante === "function") {
            window.sincronizarSidebarDonante("causas");
          }
          const tabPanel = document.getElementById("tab-panel-causas");
          if (tabPanel) tabPanel.scrollIntoView({ behavior: "smooth", block: "start" });
          return {
            reply: "¡Listo! Cambié a **Necesidades de ONGs** para que elijas una causa urgente y dones directo."
          };
        }
        return {
          reply: "Te llevo al buscador de **Necesidades de ONGs** para ver qué insumos se necesitan hoy.",
          actionLabel: "Ver Necesidades de ONGs →",
          navigateUrl: "/app/donante?tab=causas"
        };
      }

      // Directorio de organizaciones / comedores
      if (/\b(comedor|comedores|organizaciones|directorio|ongs|fundaciones)\b/.test(norm)) {
        if (path === "/app/donante" && typeof window.cambiarTabDonante === "function") {
          window.cambiarTabDonante("directorio", true);
          if (typeof window.sincronizarSidebarDonante === "function") {
            window.sincronizarSidebarDonante("directorio");
          }
          const tabPanel = document.getElementById("tab-panel-directorio");
          if (tabPanel) tabPanel.scrollIntoView({ behavior: "smooth", block: "start" });
          return {
            reply: "¡Listo! Abrí el directorio de **Organizaciones** verificadas."
          };
        }
        return {
          reply: "Te llevo al directorio de **Organizaciones y Comedores** verificados.",
          actionLabel: "Ver Organizaciones →",
          navigateUrl: "/app/donante?tab=directorio"
        };
      }

      // Resumen / insignias / puntos / impacto
      if (/\b(insignia|insignias|puntos|rango|mi impacto|resumen|misiones)\b/.test(norm)) {
        if (path === "/app/donante" && typeof window.cambiarTabDonante === "function") {
          window.cambiarTabDonante("resumen", true);
          if (typeof window.sincronizarSidebarDonante === "function") {
            window.sincronizarSidebarDonante("resumen");
          }
          window.scrollTo({ top: 0, behavior: "smooth" });
          return {
            reply: "¡Listo! Te mostré **Resumen & Impacto** con tus puntos, misiones e insignias."
          };
        }
        return {
          reply: "Te llevo a **Resumen & Impacto** para ver tus puntos e insignias.",
          actionLabel: "Ver Resumen & Impacto →",
          navigateUrl: "/app/donante"
        };
      }
    }

    return null;
  };

  // =========================================================================
  // COMUNICACIÓN CON BACKEND n8n (/api/public/chatbot) PARA CONSULTAS LIBRES
  // =========================================================================
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
      addSystemNotice((agentName || "Soporte") + " se unió al chat");
    }
    if (titleEl) {
      titleEl.textContent = agentName || "Soporte DonaTrack";
    }
    if (statusEl) {
      statusEl.replaceChildren();
      const dot = document.createElement("i");
      dot.setAttribute("aria-hidden", "true");
      statusEl.appendChild(dot);
      statusEl.appendChild(document.createTextNode(" En línea"));
    }
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

  const handleServerClientAction = (clientAction) => {
    if (!clientAction || typeof clientAction !== "object") return;
    const type = String(clientAction.type || "").toUpperCase();
    if (type === "NAVIGATE" && isSafeAppUrl(clientAction.url)) {
      addActionButton(clientAction.label || "Abrir sección →", clientAction.url);
    } else if (type === "AUTOFILL" && clientAction.fields && typeof clientAction.fields === "object") {
      const path = window.location.pathname;
      if (path === "/app/donante/donaciones" && typeof window.dtAutofillDonacion === "function") {
        window.dtAutofillDonacion(clientAction.fields);
      } else if (path === "/app/beneficiaria/necesidades" && typeof window.dtAutofillNecesidad === "function") {
        window.dtAutofillNecesidad(clientAction.fields);
      } else if (path === "/app/admin/donaciones/recepcion" && typeof window.dtAutofillRecepcion === "function") {
        window.dtAutofillRecepcion(clientAction.fields);
      } else if (isSafeAppUrl(clientAction.url)) {
        addActionButton(clientAction.label || "Ir al formulario precargado →", clientAction.url);
      }
    }
  };

  const processChatRequest = async (messageText, action) => {
    // 1) Intentar resolución instantánea in-page (navegación / autocompletado de formularios)
    if (!action && messageText) {
      const localResult = resolveLocalCopilotAction(messageText);
      if (localResult) {
        addMessageBubble(localResult.reply, "bot");
        if (localResult.navigateUrl) {
          addActionButton(localResult.actionLabel || "Ir ahora →", localResult.navigateUrl);
          navigateWithBeat(localResult.navigateUrl);
        }
        return;
      }
    }

    // 2) Consulta general al orquestador n8n con contexto de rol y ruta actual
    setInputDisabled(true);
    const waitingLabel = agentMode ? "Soporte está escribiendo..." : "Consultando asistente...";
    const typingEl = showTypingIndicator(waitingLabel);

    try {
      const data = await postChatbotWithCsrfRetry({
        message: messageText,
        mensaje: messageText,
        sessionId: sessionId,
        action: action || null,
        currentPath: window.location.pathname
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
        switchToAgentHeader(data.agentName || "Soporte DonaTrack");
        for (let i = 0; i < chunks.length; i++) {
          const sofiaTyping = showTypingIndicator("Escribiendo...");
          await sleep(800);
          sofiaTyping.remove();
          addMessageBubble(chunks[i], "agent");
        }
      } else {
        chunks.forEach((chunk) => addMessageBubble(chunk, "bot"));
      }

      if (data && data.clientAction) {
        handleServerClientAction(data.clientAction);
      }
    } catch (err) {
      typingEl.remove();
      console.warn("Error al conectar con chatbot proxy:", err);
      addMessageBubble(
        "El asistente conversacional no respondió en este momento, pero igualmente podés pedirme navegación directa (ej: *\"quiero donar 10 kg de fideos\"* o *\"ir a mis donaciones\"*).",
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
    chip.addEventListener("click", () => {
      const q = (chip.dataset.q || chip.textContent || "").trim();
      if (!q) return;
      const action = chip.dataset.action || null;
      addMessageBubble(q, "user");
      enqueueRequest(q, action);
    })
  );
})();
