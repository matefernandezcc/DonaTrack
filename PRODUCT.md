# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

DonaTrack opera sobre un modelo tripartito de usuarios colaborativos:

1. **Donantes (Personas y Empresas):** Aportan bienes tangibles (alimentos no perecederos, indumentaria, artículos de limpieza/higiene), registran recepciones físicas y siguen el impacto de su aporte a través de un perfil gamificado con puntos e insignias auditadas.
2. **Organizaciones Beneficiarias (Comedores, Hogares, ONGs):** Publican sus necesidades urgentes en cantidades medibles y confirman en destino la llegada efectiva de los cargamentos.
3. **Administradores y Operadores Logísticos:** Gestionan en el backoffice la flota de transporte (camiones, choferes), coordinan rutas, importan remitos masivos por CSV y realizan el matchmaking entre donaciones disponibles y necesidades barriales.

## Product Purpose

Transformar la distribución y trazabilidad de donaciones materiales en una experiencia gamificada, transparente y rigurosa, eliminando la opacidad en el circuito de ayuda humanitaria mediante métricas de impacto real y entregas verificadas.

## Positioning

Plataforma de gamificación comunitaria de impacto solidario respaldada por trazabilidad logística real: a diferencia de sistemas benéficos tradicionales o colectas estáticas, cada insignia y punto acumulado en DonaTrack se origina en un remito de entrega física efectivamente auditado y verificado en destino.

## Operating Context

* **Arquitectura Distribuida:** Ecosistema de microservicios independientes en Java Spring Boot (`donatrack-auth`, `donatrack-donaciones`, `donatrack-incentivos`, `donatrack-logistica`, `donatrack-notificaciones`) coordinados con RabbitMQ, PostgreSQL y MongoDB.
* **Consumo Frontend:** Servidor cliente liviano (`donatrack-web`) con Server-Side Rendering (SSR) puro sobre Thymeleaf, que actúa como BFF (Backend-For-Frontend) consumiendo APIs REST sin acceso directo a bases de datos.
* **Entornos Operativos:**
  * Web pública: Navegación libre para ciudadanos y donantes con mapa interactivo de comedores y puntos de acopio.
  * Backoffice seguro: Gestión operativa y tableros específicos segmentados por rol (`ADMIN`, `DONANTE`, `BENEFICIARIA`).

## Capabilities and Constraints

* **SSR Estricto:** Renderizado 100% en servidor mediante Spring Boot y Thymeleaf; el navegador recibe HTML listo sin dependencias de frameworks SPA ni hidratación cliente pesada.
* **Estrategia Dual de UI (Aislamiento Obligatorio):**
  * *Layout Público:* Interfaz moderna, empática y accesible estilo e-commerce, con selector dinámico de tema claro/oscuro y componentes interactivos fluidos.
  * *Layout Administrativo:* Regla estricta de Salesforce Lightning Design System (SLDS). Solo blueprints HTML oficiales de SLDS (Global Header, Vertical Nav, Data Tables, Cards, Modals); cero CSS personalizado inventado.
* **Autenticación y Seguridad:**
  * JWT HS256 con secreto compartido entre microservicios.
  * Autenticación local + Social Login SSO (Google / GitHub) con provisión Just-In-Time (JIT) en base de datos `auth.usuarios`.
* **Persistencia Políglota:** PostgreSQL para usuarios, donaciones y necesidades; MongoDB para cálculo de rutas de logística y choferes.

## Brand Commitments

* **Isologo Institucional:** Cajita isométrica oficial (`/img/package.svg`), sin símbolos de nubes genéricas ni reemplazos externos.
* **Identidad de Usuarios:** Avatares generativos deterministas basados en hash mediante `blobatar.dev`, enriquecidos con seguimiento de mirada interactivo (eye tracking con cursor).
* **Voz y Tono:** Profesional, sobrio y solidario; celebra el compromiso comunitario sin recurrir a infantilizaciones ni métricas ficticias.

## Evidence on Hand

* **Semillas de Datos:** Esquemas y datos precargados en PostgreSQL (`init.sql` y `local-seed-data.sql`) con usuarios semilla (`admin`, `donante`, `beneficiaria`), categorías de bienes y registros históricos de prueba.
* **Microservicio de Incentivos:** Endpoints REST activos (`/api/donantes/{id}/metricas`, `/api/donantes/{id}/insignias`) que proveen estadísticas reales de donaciones históricas y bienes entregados.
* **Activos Gráficos:** Paquete completo de íconos oficiales SLDS (`utility-sprite`, `standard-sprite`, `action-sprite`) integrados localmente en `/static/assets/slds-icons/`.

## Product Principles

1. **Transparencia Basada en Evidencia:** Toda insignia, punto o progreso comunitario se calcula sobre donaciones físicas registradas y remitos efectivamente auditados en la base de datos.
2. **Dualidad Funcional Clara:** Calidez y engagement público en la web externa; sobriedad, densidad informativa y rigor SLDS en el panel de control operativo.
3. **Desacoplamiento Estricto:** El cliente frontend SSR no contiene lógica de almacenamiento ni acceso a bases de datos; consume microservicios REST especializados como servicios autónomos.
4. **Gamificación con Sentido Social:** Los puntos y las insignias existen para incentivar la recurrencia y premiar la ayuda sostenida a comedores y escuelas rurales.

## Accessibility & Inclusion

* Cumplimiento estricto del estándar WCAG 2.1 AA (contraste mínimo de 4.5:1 para texto normal y 3:1 para componentes de interfaz).
* Cobertura completa de accesibilidad en ambos modos (Dark / Light) en mapas, popups Leaflet y listas de acopio.
* Etiquetas semánticas y atributos accesibles nativos de SLDS (`aria-labelledby`, `role="status"`, saltos de navegación `#main-content`).

