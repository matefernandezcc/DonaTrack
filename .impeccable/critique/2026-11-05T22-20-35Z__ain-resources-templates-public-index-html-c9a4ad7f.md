---
target: landing page donatrack-web/src/main/resources/templates/public/index.html
total_score: 29
max_score: 32
na_heuristics: 7,10
p0_count: 0
p1_count: 0
target_identity: "file:/Users/usuario/Desktop/testing/repos-github/DonaTrack/donatrack-web/src/main/resources/templates/public/index.html"
target_fingerprint: "sha256:c9874b584e33c978589eaa1b10bd0dfea3366e6b878def8f2a5326fe022c43d7"
target_path: /Users/usuario/Desktop/testing/repos-github/DonaTrack/donatrack-web/src/main/resources/templates/public/index.html
timestamp: 2026-11-05T22-20-35Z
slug: ain-resources-templates-public-index-html-c9a4ad7f
---
#### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|:-----:|-----------|
| 1 | Visibility of System Status | 4 | Trazador interactivo de 5 etapas, mapa sincronizado con badges de estado y `aria-pressed` funcional en toggle de tema |
| 2 | Match System / Real World | 4 | Vocabulario solidario argentino genuino; métricas físicas reales (`kg`, `prendas`, `pupitres`) y categorías concretas |
| 3 | User Control and Freedom | 3 | Desplazamiento fluido y reduced-motion; CTAs enlazados a rutas reales; modal `<dialog>` con escape nativo y backdrop dismiss |
| 4 | Consistency and Standards | 3 | Redundancia eliminada mediante "¿Qué podés donar?"; sistema de diseño unificado |
| 5 | Error Prevention | 4 | Deep-linking preventivo (`?rol=DONANTE`, `?rol=BENEFICIARIA`, `?campana=...`); scroll de mapa seguro |
| 6 | Recognition Rather Than Recall | 4 | Código cromático unificado (Azul=Acopio, Verde=ONG, Naranja=Entrega); leyenda visible y modal con acta fotográfica |
| 7 | Flexibility and Efficiency | n/a | *Superficie Persuade: aceleradores y atajos no aplican a visitantes de landing page pública* |
| 8 | Aesthetic and Minimalist Design | 4 | Contraste reparado en testimonios modo claro (`color: var(--muted)`); eliminados gradientes, blobs difusos y chip eyebrow |
| 9 | Error Recovery | 3 | Enlaces primarios ya no fallan silenciosamente; modal con manejo accesible de cierre |
| 10 | Help and Documentation | n/a | *Superficie Persuade: FAQ contextual preventiva resuelve dudas operativas y marco legal (Ley 25.326)* |
| **Total** | | **29/32** | **Excelente (90.6%)** *(+9 puntos vs Run 1)* |

#### Design Specificity Verdict

**LLM assessment**: La landing page experimentó un salto cualitativo rotundo. DonaTrack se consolidó como una plataforma de logística cívica rigurosa, transparente y tangible. Las campañas comunican magnitudes reales de carga (`1.450 / 2.000 kg`, `180 / 375 prendas`, `36 / 40 pupitres`), el proceso redundante fue sustituido por especificaciones concretas de bienes admisibles con control bromatológico, y el mapa territorial ahora permite inspeccionar actas y fotografías reales de entrega mediante su modal nativo.

**Deterministic scan**: El escaneo determinista reflejó una **reducción del 73.3% en advertencias** (de 15 warnings en 6 reglas a solo 4 warnings):
- `gradient-text`: **0 instancias** (100% resuelto; texto de impacto en colores semánticos sólidos).
- `hero-eyebrow-chip`: **0 instancias** (100% resuelto; titular directo y sobrio).
- `broken-image`: **0 instancias** (100% resuelto; atributos `src` estáticos y dimensiones explícitas).
- `flat-type-hierarchy`: **0 instancias** en render (escala `h3` ajustada a 1.35x para jerarquía nítida).
- `icon-tile-stack`: Reducido en un 60% (eliminado de la sección de bienes).
- `dark-glow`: Sombra de botón primario naturalizada sin halo cromático.

#### Overall Impression
La página alcanzó un estándar de excelencia visual y operativa. Se eliminaron todos los bloqueos que degradaban la experiencia del usuario (enlaces rotos, ilegibilidad en modo claro y redundancia mental), logrando una narrativa cívica contundente, empática y creíble.

#### What's Working
1. **Materialidad y trazabilidad física**: Las unidades concretas de peso y volumen jerarquizan la propuesta frente a colectas genéricas.
2. **Prueba visual incontrastable**: El modal fotográfico de entregas (`#map-modal`) conectado en vivo al mapa de Leaflet aporta máxima transparencia institucional.
3. **Embudo de conversión recuperado**: Todos los botones de acción conducen a destinos funcionales con persistencia de intención según el rol o la causa elegida.

#### Remaining Priority Issues
- **[P2] Unificación de anclas de navegación en Header y Footer**:
  - **Why it matters**: El menú móvil y el pie contienen enlaces a `/#como-funciona` (ahora `/#que-donar`) y el menú desktop apunta a `/#mapa` (mientras la sección lleva `id="organizaciones"`).
  - **Fix**: Sincronizar los identificadores en cabecera y pie para asegurar desplazamiento suave a cada sección.
  - **Suggested command**: `/impeccable polish`
- **[P3] Enlaces secundarios en pie de página**:
  - **Why it matters**: Términos y privacidad conservan `href="#"`.
  - **Fix**: Proveer vistas estáticas informativas o modales legales.
  - **Suggested command**: `/impeccable harden`

#### Persona Red Flags
- **Jordan (Primerizo)**: APROBADO. Sabe con exactitud qué alimentos llevar a un punto de acopio y puede registrarse sin enlaces rotos.
- **Alex (Recurrente)**: APROBADO. Puede auditar remitos y fotos de entregas en el mapa y donar con contexto preseleccionado.
- **Beneficiaria (ONG)**: APROBADO. El botón "Solicitar verificación ONG" la dirige al flujo institucional adecuado.
