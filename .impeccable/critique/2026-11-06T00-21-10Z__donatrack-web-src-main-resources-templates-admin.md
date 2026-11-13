---
target: the users backoffices pages
total_score: 16
max_score: 40
na_heuristics: 
p0_count: 1
p1_count: 1
target_identity: "file:/Users/usuario/Desktop/testing/repos-github/DonaTrack/donatrack-web/src/main/resources/templates/admin"
timestamp: 2026-11-06T00-21-10Z
slug: donatrack-web-src-main-resources-templates-admin
---
# Critique: Portales de Usuario Backoffice (Donante & Beneficiaria)

Method: dual-agent (A: b8615e0b-85a6-4e93-8ef4-21d5b34cb128 · B: 293451ec-a2e7-4b9a-a0aa-0e9098039873)

### Design Health Score
| # | Heuristic | Score | Key Issue |
|---|-----------|:-----:|-----------|
| 1 | Visibility of System Status | 2 | Sin pipeline visual de estado ("En depósito -> En viaje -> Entregado"); beneficiarias sin ETA de arribo de camiones. |
| 2 | Match System / Real World | 1 | Jerga de depósito para donantes ("Carga de Recepción"); exigencia de UUID de 36 caracteres y URL externa a comedores. |
| 3 | User Control and Freedom | 1 | No se pueden editar, pausar ni cancelar necesidades declaradas; sin rectificación de confirmación errónea. |
| 4 | Consistency and Standards | 2 | Abrupta brecha estética entre gamificación Trailhead del donante y tablas desnudas de la beneficiaria; archivo CSS huérfano. |
| 5 | Error Prevention | 1 | Campo de texto libre para UUID en el móvil propicia errores irrecuperables en el campo; sin confirmación modal. |
| 6 | Recognition Rather Than Recall | 1 | La encargada de comedor debe recordar/copiar el UUID de canales externos; donantes no ven histórico en la página de donación. |
| 7 | Flexibility and Efficiency | 2 | Sin donación masiva (lotes) para empresas; sin captura nativa de cámara para remitos. |
| 8 | Aesthetic and Minimalist Design | 2 | Panel donante recargado con eye-tracking Blobatar y tags enterprise; panel beneficiaria con abandono visual. |
| 9 | Error Recovery | 2 | Errores canalizados por banners flash temporales que no preservan el estado del formulario. |
| 10 | Help and Documentation | 2 | Buen roadmap de bienvenida en donante; nula asistencia en beneficiaria ante discrepancias de mercadería. |
| **Total** | | **16/40** | **Poor (Requiere modernización integral de UX)** |

### Design Specificity Verdict
- **LLM Assessment**: Disonancia de personalidad entre dos mundos. El portal del donante sobre-emula un campus corporativo estilo Salesforce Trailhead con gradientes, eye-tracking y badges IT (#Data360), mientras que el portal de la organización beneficiaria es un andamio CRUD frío y hostil que exige tipear UUIDs de 36 caracteres y pegar URLs públicas de fotos al pie del camión de reparto.
- **Deterministic Scan**: 17 hallazgos brutos (15 warnings, 2 advisories). La auditoría aisló 12 defectos reales (7 fallas de contraste WCAG AA en gradientes inline, 3 textos diminutos por debajo de 11-12px, 1 salto de jerarquía h2->h4 y 1 archivo CSS huérfano) y 5 falsos positivos derivados del parser estático sobre directivas de servidor Thymeleaf y el blueprint de coordenadas de SLDS.

### Key Strengths
1. **Roadmap guiado de 4 pasos para nuevos donantes**: Explica con claridad la trazabilidad desde el registro hasta la entrega en comedor.
2. **Autenticidad de métricas e insignias**: Conectado directamente a las APIs del microservicio de incentivos sin hardcodeos.
3. **Fundación accesible SLDS**: Estructura semántica base con aria-attributes y blueprints oficiales de Salesforce.

### Priority Issues
- **[P0] Protocolo inviable de confirmación de entregas en campo**: Exigir UUID hexadecimal y URL externa en `beneficiaria/entregas.html` bloquea la trazabilidad real.
- **[P1] Falta de histórico y seguimiento en vivo para el donante**: En `donante/donaciones.html` el donante envía a ciegas sin ver el destino ni el impacto anterior.
- **[P2] Brecha responsive y desconexión de layout**: `admin-dashboard.css` es código muerto mientras que el sidebar de SLDS carece de colapso en pantallas móviles de campo.
- **[P3] Lenguaje burocrático de depósito y cierre sin gratificación**: Jerga despersonalizada en donaciones y falta de celebración o feedback emocional al completar la ayuda.
