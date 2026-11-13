---
target: landing page donatrack-web/src/main/resources/templates/public/index.html
total_score: 20
max_score: 32
na_heuristics: 7,10
p0_count: 1
p1_count: 2
target_identity: "file:/Users/usuario/Desktop/testing/repos-github/DonaTrack/donatrack-web/src/main/resources/templates/public/index.html"
target_fingerprint: "sha256:2b63749887d84dc4210c8df00a6035fe86aa3975d00bbfc335f052263204323e"
target_path: /Users/usuario/Desktop/testing/repos-github/DonaTrack/donatrack-web/src/main/resources/templates/public/index.html
timestamp: 2026-11-05T22-02-46Z
slug: ain-resources-templates-public-index-html-c9a4ad7f
---
#### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|:-----:|-----------|
| 1 | Visibility of System Status | 3 | Trazador interactivo y camión visualizan el avance; falta `aria-pressed` en toggle de tema |
| 2 | Match System / Real World | 3 | Vocabulario solidario argentino genuino; campañas usan porcentajes en vez de unidades físicas |
| 3 | User Control and Freedom | 2 | Desplazamiento fluido y reduced-motion; botones principales con `href="#"` que no permiten accionar |
| 4 | Consistency and Standards | 2 | Redundancia cognitiva: 5 pasos de recorrido seguidos de 3 pasos genéricos de "Cómo funciona" |
| 5 | Error Prevention | 3 | Límites de mapa seguros (flyTo) y FAQ preventiva que aclara dudas operativas |
| 6 | Recognition Rather Than Recall | 3 | Código cromático (Azul/Verde/Naranja) unificado entre mapa, leyenda y tarjetas |
| 7 | Flexibility and Efficiency | n/a | Superficie Persuade: aceleradores y atajos no aplican a visitantes de landing page pública |
| 8 | Aesthetic and Minimalist Design | 2 | **Fallo crítico de contraste**: texto blanco sobre blanco en testimonios modo claro (`.quote figcaption span`), más gradientes y blobs SaaS genéricos |
| 9 | Error Recovery | 2 | Enlaces rotos (`href="#"`) fallan silenciosamente sin feedback ni modales de contingencia |
| 10 | Help and Documentation | n/a | Superficie Persuade: no requiere manuales; FAQ contextual resuelve la incorporación |
| **Total** | | **20/32** | **Aceptable (62.5%)** |

#### Design Specificity Verdict

**LLM assessment**: DonaTrack posee un propósito cívico real y potente (logística transparente de alimentos, abrigo y útiles auditada con remitos físicos y pesaje de camiones). En los componentes diseñados para su misión (el remito `#DT-2418`, el camión interactivo de recorrido SVG y el mapa Leaflet de comedores comunitarios) la identidad es sólida y auténtica. Sin embargo, el resto de la página cae en patrones genéricos de plantillas SaaS: fondos con blobs borrosos, títulos con degradados (`grad-text`), tarjetas de campañas con porcentajes de crowdfunding monetario ("72%") en lugar de kilos de alimentos, y avatares geométricos abstractos (`blobatar`) que despersonalizan a los coordinadores de comedores reales.

**Deterministic scan**: El escaneo determinista detectó **15 advertencias sobre 6 reglas** en el DOM renderizado:
- `gradient-text` (6 instancias): Degradados en el título hero ("esperanza") y números métricos.
- `icon-tile-stack` (5 instancias): Cuadrados de 52px y 56px centrados sobre títulos de tarjetas en "Cómo funciona" y "Sumate".
- `hero-eyebrow-chip` (1 instancia): Chip pill con tracking mayúsculo ("UTN Solidaria · Trazabilidad transparente").
- `flat-type-hierarchy` (1 instancia): Salto de escala de solo 1.20:1 entre `body` (16px) y `h3` (19.2px).
- `dark-glow` (1 instancia): Sombra resplandeciente cromática naranja (`rgba(242, 92, 31, 0.55)`).
- `overused-font` (1 instancia): Combinación ubicua Inter + Manrope.
*(Las 2 advertencias de `broken-image` en las plantillas parciales de cabecera y pie fueron analizadas como falsos positivos del analizador ante la sintaxis dinámica `th:src` de Thymeleaf sin fallback estático).*

#### Overall Impression
La landing page transmite un propósito noble y cuenta con componentes visuales de alto valor (el mapa comunitario y el camión interactivo de entregas). No obstante, sufre de **bloqueos críticos de conversión** (enlaces clave que no van a ningún lado), un **error grave de accesibilidad** en modo claro (texto blanco sobre fondo blanco en testimonios) y una **redundancia explicativa** que agota al visitante antes de llegar a la acción.

#### What's Working
1. **Recorrido logístico interactivo (`#recorrido`)**: El camión animado guiado por el scroll a lo largo de la ruta SVG materializa de forma didáctica e inolvidable la promesa de trazabilidad.
2. **Mapa interactivo territorial (Leaflet)**: La integración de centros de acopio, comedores y entregas confirmadas en tiempo real conecta inmediatamente la web con la geografía solidaria argentina.
3. **Arquitectura desacoplada SSR**: Renderizado en servidor ágil, carga instantánea sin hydration SPA, soporte de modo oscuro por variables CSS y cumplimiento de `prefers-reduced-motion`.

#### Priority Issues

- **[P0] Enlaces muertos `href="#"` en llamadas a la acción primarias**
  - **Why it matters**: Los botones "Donar a esta campaña" (3 tarjetas), "Crear cuenta" (donante) y "Solicitar verificación" (organización) no hacen nada. El embudo de conversión está interrumpido y proyecta un producto ficticio o roto.
  - **Fix**: Conectar "Crear cuenta" y donaciones a `/login?rol=DONANTE` (o diálogo accesible de registro) y "Solicitar verificación" a `/login?rol=BENEFICIARIA`.
  - **Suggested command**: `/impeccable harden`

- **[P1] Defecto de accesibilidad: Texto blanco sobre blanco en testimonios en modo claro**
  - **Why it matters**: En `components.css:280`, `.quote figcaption span` tiene `color: #fff;` forzado dentro de una tarjeta con fondo blanco en tema claro. Los roles y ubicaciones ("Donante, Rosario", "Coordinador, Comedor El Faro") son 100% invisibles (contraste 1.0:1), destruyendo la prueba social.
  - **Fix**: Modificar la regla CSS para usar `color: var(--muted);` en modo claro y conservar contraste adecuado en modo oscuro.
  - **Suggested command**: `/impeccable polish`

- **[P1] Redundancia cognitiva: 5 pasos de recorrido seguidos de 3 pasos de "Cómo funciona"**
  - **Why it matters**: Dos secciones consecutivas intentan explicar el circuito operativo con granularidades distintas, provocando fatiga y confusión sobre cuál es el flujo real.
  - **Fix**: Mantener el recorrido interactivo de 5 pasos como el gran explicador visual de la plataforma y transformar las 3 tarjetas de "Cómo funciona" en "¿Qué podés donar?" destacando categorías tangibles (Alimentos no perecederos, Ropa y abrigo, Mobiliario).
  - **Suggested command**: `/impeccable distill`

- **[P2] Modal de prueba fotográfica de entrega desconectado en el mapa**
  - **Why it matters**: En el HTML existe un diálogo `<dialog class="modal" id="map-modal">` con foto y remito digital de entrega, pero `map.js` nunca lo activa, mostrando solo un popup de texto comprimido. Se desaprovecha la prueba visual de transparencia de la entrega.
  - **Fix**: Conectar el evento click del marcador de entrega en `map.js` para abrir el modal nativo con la fotografía y datos del remito digital.
  - **Suggested command**: `/impeccable delight`

- **[P2] Ambigüedad de recaudación: Campañas expresadas en porcentajes monetarios abstractos**
  - **Why it matters**: Mostrar "72%" o "1.284 donantes" imita plataformas de dinero (Kickstarter/GoFundMe) y desdibuja el modelo de DonaTrack, que gestiona bienes tangibles y cubicaje.
  - **Fix**: Expresar el progreso en unidades físicas concretas (ej. `1.450 / 2.000 kg Alimentos` o `180 / 250 Frazadas`) e indicar la subcategoría prioritaria.
  - **Suggested command**: `/impeccable clarify`

#### Persona Red Flags

**Jordan (Donante Primerizo)**:
- Lee los 5 pasos del camión y luego ve otros 3 pasos diferentes; duda de qué tiene que hacer físicamente para donar.
- Quiere donar ropa de abrigo para el invierno pero la campaña dice "72% completado" sin especificar talles o tipos de prenda requeridos.
- Hace click en "Donar a esta campaña" y no pasa nada (`href="#"`); sospecha que la plataforma no funciona y se retira.

**Alex (Donante Recurrente)**:
- Busca certificar el destino de sus donaciones anteriores pero los contadores globales son estáticos y no permiten filtrar por su historial ni ver el libro de actas.
- El botón "Quiero Donar" lo envía a un login general sin recordar la causa o categoría que estaba explorando.
- No encuentra ningún anticipo de las insignias cívicas o puntos de reputación prometidos en el perfil Trailblazer.

**Coordinadora de Comedor (Beneficiaria)**:
- Entra a "Soy organización", hace click en "Solicitar verificación" para inscribir su comedor barrial y el enlace está roto (`href="#"`).
- En el mapa solo ve puntos en CABA y el conurbano; no tiene buscador para consultar si DonaTrack llega a su localidad o provincia.
- No puede ver fotos de entregas similares para comprobar el estado en que llegan los alimentos.

#### Minor Observations
- El botón de alternancia de tema cambia íconos visualmente pero carece del atributo accesible `aria-pressed="true|false"`.
- Los blobs difusos en el hero (`.blob--blue`, `.blob--green`) y el texto degradado le restan sobriedad y seriedad institucional a la plataforma.
- En `scroll-story.js` (`line 64`), las rutas de las imágenes usan rutas relativas sin barra inicial (`img/delivery-truck-end.svg`).

#### Questions to Consider
- *¿Qué pasaría si en lugar de un banner con un código de remito estático, el hero tuviera un buscador interactivo en vivo donde cualquiera pueda escribir `#DT-2418` y ver al instante la foto y el acta de entrega firmada?*
- *¿Cómo cambiaría la tasa de donación si las campañas mostraran exactamente cuántos kilos o frazadas faltan con un botón directo a los puntos de entrega cercanos?*
- *¿Podríamos simplificar la página eliminando la sección "Cómo funciona" y jerarquizando las categorías de bienes que más se necesitan en este momento?*
