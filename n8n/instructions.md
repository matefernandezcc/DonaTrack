# Instrucciones de Trabajo: Workflow "DonaTrack - Insignias" en n8n

Este documento define el entorno de ejecución, las herramientas, los comandos y las buenas prácticas para el desarrollo y mantenimiento del flujo de n8n orientado a la gestión de insignias de donantes.

---

## 📍 Información de Conexión y Entorno

> [!IMPORTANT]
> El entorno local de n8n corre en contenedores Docker y se encuentra disponitest/donatrack/badge-earnedble en la siguiente dirección:
> - **URL Local:** [http://localhost:5678/](http://localhost:5678/)
> - **Nombre del Workflow:** `DonaTrack - Insignias`
> - **Contenedor n8n:** `n8n_donatrack`
> - **Contenedor Base de Datos:** `db_donatrack`

---

## ⚙️ Comandos Útiles (`Makefile`)

El proyecto cuenta con un `Makefile` en la raíz para facilitar las tareas de dockerización e importación/exportación de workflows.

| Acción | Comando | Descripción |
| :--- | :--- | :--- |
| **Levantar Entorno** | `sudo docker compose up -d` o `make setup` | Levanta los contenedores e importa los workflows preconfigurados. |
| **Bajar Entorno** | `make docker-down` | Detiene y remueve los contenedores de n8n y la base de datos. |
| **Exportar Flujo** | `make n8n-export` | Extrae los flujos activos del contenedor y los guarda localmente en `./n8n/workflows/`. |
| **Importar Flujo** | `make n8n-import` | Carga el flujo guardado desde `./n8n/workflows/` hacia la instancia activa de n8n. |

> [!WARNING]
> Antes de detener los contenedores con `make docker-down` o hacer cambios grandes, asegúrate de correr `make n8n-export` para respaldar tu trabajo en el repositorio de Git.

---

## 🛠️ Guía de Skills y Patrones Aplicables

Para construir y mantener el flujo `DonaTrack - Insignias`, el agente debe usar las siguientes skills instaladas en el workspace (`.agents/skills/`):

### 1. `n8n-workflow-patterns`
- **Uso:** Estructurar el flujo con patrones estándar de n8n (p. ej., Webhooks para recibir eventos de nuevas donaciones, HTTP Request para consultar el microservicio de insignias, etc.).
- **Patrón recomendado:** Recepción del evento de donación -> Validación del donante -> Evaluación de metas/insignias -> Notificación / Actualización en base de datos.

### 2. `n8n-node-configuration`
- **Uso:** Configurar correctamente cada nodo con los tipos de datos correctos, manejo de reintentos en caso de fallos de red y control de errores.

### 3. `n8n-code-javascript`
- **Uso:** Cuando sea necesario procesar o transformar arrays de datos de donaciones (p. ej., calcular el total donado acumulado para otorgar una insignia de plata/oro).
- **Regla:** Mantener los nodos Code con JavaScript limpio y usar la sintaxis moderna de n8n (`$input.all()`, `$json`, etc.).

### 4. `n8n-mcp-tools-expert`
- **Uso:** Integrar herramientas MCP de n8n si es necesario automatizar llamadas avanzadas a servicios externos.

---

## 📝 Procedimiento de Desarrollo Paso a Paso

1. **Puesta en Marcha:** Levantar los servicios y verificar que la interfaz de n8n sea accesible en `http://localhost:5678/`.
2. **Creación del Workflow:** Si no existe, crear un nuevo workflow llamado exacto: `DonaTrack - Insignias`.
3. **Construcción y Lógica:** 
   - Agregar disparadores (Triggers) o consultas periódicas (Cron) según la lógica de asignación de insignias.
   - Conectar con el microservicio correspondiente (`donatrack-incentivos` o base de datos).
4. **Pruebas Locales:** Ejecutar el flujo manualmente en el canvas de n8n y verificar los payloads de entrada y salida.
5. **Persistencia:** Correr `make n8n-export` en la terminal para guardar los cambios en el archivo del proyecto y hacer commit.
