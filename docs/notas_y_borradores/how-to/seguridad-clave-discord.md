# 🛡️ Seguridad y Gestión de Secretos: Webhook de Discord y n8n en DonaTrack

Este documento detalla los principios de seguridad, arquitectura y buenas prácticas implementadas en **DonaTrack** para proteger la URL y el token secreto del webhook de **Discord**, así como los endpoints de **n8n**.

---

## 1. Contexto y Naturaleza del Problema

### ¿Qué es un Webhook de Discord?
Una URL de webhook de Discord sigue el formato:
```text
https://discord.com/api/webhooks/{webhook.id}/{webhook.token}
```
El `{webhook.token}` actúa como una **clave de acceso completa y sin restricciones**: cualquier persona, servicio o script que conozca dicha URL puede enviar mensajes, menciones e imágenes directamente al canal de Discord sin necesidad de usuario ni contraseña.

### El Riesgo de Exposición en Git
* **Detección Automática por Bots y GitHub Secret Scanning:** Plataformas como GitHub analizan constantemente los commits en busca de patrones de claves (tokens de AWS, Discord, Slack, etc.).
* **Revocación Inmediata de Discord:** En cuanto Discord detecta que una URL de webhook aparece en un commit o repositorio público/sincronizado, **lo revoca e invalida automáticamente en cuestión de segundos** (arrojando el error `{"code": 10015, "message": "Unknown Webhook"}`). Esto inutiliza de inmediato cualquier notificación del sistema.

---

## 2. Vulnerabilidad Detectada, Caso Real y Solución Aplicada

### ❌ Estado Previo (Inseguro)
En la clase [`AdaptadorDiscord.java`](../../../donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorDiscord.java), la URL con su token secreto estaba escrita como valor de fallback por defecto en código Java:
```java
// INSEGURO: El token quedaba rastreado en el repositorio de Git
@Value("${discord.webhook.url:https://discord.com/api/webhooks/123456789012345678/tu_token_secreto_aqui}")
private String discordWebhookUrl;
```

### 🔍 Caso Real de Estudio: Verificación Empírica de Revocación
Al auditar el historial de commits de Git en ramas compartidas, se constató que un webhook previo había sido registrado en commits históricos. Al consultar dicho webhook directamente contra la API de Discord:
```bash
curl -i https://discord.com/api/webhooks/1553029764351262720/OajZ-dc1m...
```
La API de Discord respondió:
```json
HTTP/1.1 404 Not Found
{"message": "Unknown Webhook", "code": 10015}
```
Esto demostró empíricamente la efectividad del sistema de revocación automática ante fugas en Git: **un secreto commiteado debe considerarse comprometido e inutilizado inmediatamente**.

### ✅ Solución Implementada (Segura)
Se eliminó cualquier referencia a webhooks reales del código fuente de Java:
```java
// SEGURO: Si no existe la variable de entorno, queda vacío sin exponer secretos
@Value("${discord.webhook.url:${DISCORD_WEBHOOK_URL:}}")
private String discordWebhookUrl;
```
Ahora el código fuente es agnóstico y seguro para commitear y subir a cualquier repositorio remoto.

---

## 3. Arquitectura de Seguridad en Capas (Defense in Depth)

DonaTrack protege la clave mediante un modelo de cuatro capas:

```
┌────────────────────────────────────────────────────────┐
│  Cliente / Bruno / UI                                  │
│  (Solo conoce endpoints internos, NUNCA Discord)       │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  DonaTrack Backend / n8n Gateway (Proxy Seguro)        │
│  - Recibe la petición REST interna                     │
│  - Inyecta el secreto en memoria en tiempo de ejecución│
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  Discord API (Destino Final)                           │
│  https://discord.com/api/webhooks/{id}/{token}         │
└────────────────────────────────────────────────────────┘
```

### Capa 1: Aislamiento Local (`.env` + `.gitignore`)
1. El webhook real reside **únicamente** en el archivo local [`.env`](../../../.env):
   ```env
   DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/123456789012345678/tu_token_secreto_aqui
   ```
2. El archivo `.env` está explícitamente listado en [`.gitignore`](../../../.gitignore). **Git jamás lo rastrea ni lo sube a GitHub**.
3. El archivo [`.env.template`](../../../.env.template) sirve como plantilla pública para el equipo sin incluir ningún secreto:
   ```env
   DISCORD_WEBHOOK_URL=
   ```

### Capa 2: Inyección Segura en Docker Compose
En [`compose.yaml`](../../../compose.yaml), la variable se inyecta directamente a la memoria de los contenedores en tiempo de arranque:
```yaml
services:
  n8n:
    environment:
      - DISCORD_WEBHOOK_URL=${DISCORD_WEBHOOK_URL}
      
  notificaciones:
    env_file:
      - .env
```
Ningún secreto queda grabado en las imágenes Docker (`Dockerfile`) ni en capas de build.

### Capa 3: Abstracción en Workflows de n8n (`workflows.json`)
En el archivo de exportación de workflows [`n8n/workflows/workflows.json`](../../../n8n/workflows/workflows.json), los nodos de Discord utilizan expresiones dinámicas con fallback tolerante a fallos:
```json
"url": "={{ $env.DISCORD_WEBHOOK_URL || 'https://httpbin.org/post' }}"
```
* **Ventaja 1 (Higiene de repositorio):** Al correr `make n8n-export` y versionar los flujos en Git, **solo viaja el identificador de la variable**, nunca la URL real.
* **Ventaja 2 (Tolerancia a fallos y resiliencia):** Si un desarrollador levanta el proyecto sin configurar la variable, n8n utiliza el sink simulado sin arrojar errores fatales ni detener los flujos.
* **Ventaja 3 (Control de errores con `onError`):** Se configuró `"onError": "continueRegularOutput"` en los nodos de publicación externa para que caídas de red o revocaciones imprevistas no interrumpan la respuesta HTTP hacia el cliente.

### Capa 4: Patrón Proxy (Ocultamiento al Cliente)
* **Colección de Bruno ([`docs/donatrack-api/Donatrack - API/Notificaciones/`](../../../docs/donatrack-api/Donatrack%20-%20API/Notificaciones/)):** Las 8 peticiones de Bruno solo interactúan con:
  - `POST {{notificacionesUrl}}/api/mensajes` (Microservicio Spring Boot)
  - `POST {{n8nUrl}}/webhook/donatrack/...` (Webhooks de n8n)
* Bruno y el navegador **desconocen por completo** la URL de Discord; el backend actúa como firewall y mediador seguro.

---

## 4. Procedimiento Operativo de Rotación de Secretos

Si un webhook queda comprometido o invalidado por Discord:

1. **Generación de Credencial Limpia:**
   * En Discord, ingresar a *Ajustes de Canal > Integraciones > Webhooks > Nuevo Webhook*.
   * Copiar la URL generada.
2. **Actualización de Entorno Local:**
   * Reemplazar el valor en el archivo local `.env`:
     ```env
     DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/<NUEVO_ID>/<NUEVO_TOKEN>
     ```
3. **Propagación en Caliente:**
   * Recrear los contenedores para recargar variables en memoria sin reconstruir código:
     ```bash
     docker compose up -d n8n notificaciones
     ```
4. **Validación:**
   * Ejecutar la prueba de integración vía Bruno o n8n MCP para certificar la entrega exitosa.

---

## 5. Resumen de Archivos Involucrados

| Archivo | Rol de Seguridad |
|---|---|
| [`.env`](../../../.env) | **Único lugar** donde reside el valor real del webhook (ignorado por Git). |
| [`.gitignore`](../../../.gitignore) | Garantiza que `.env` no se versione en el repositorio. |
| [`.env.template`](../../../.env.template) | Plantilla pública sin valores sensibles. |
| [`AdaptadorDiscord.java`](../../../donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorDiscord.java) | Código Java limpio, lee de variable de entorno sin default hardcodeado. |
| [`compose.yaml`](../../../compose.yaml) | Inyecta la variable en memoria a los contenedores Docker. |
| [`workflows.json`](../../../n8n/workflows/workflows.json) | Usa `$env.DISCORD_WEBHOOK_URL` para que el JSON exportado esté limpio de credenciales. |

---

# Referencias

* [Discord Developer Portal — Webhook Resource & Security Model](https://discord.com/developers/docs/resources/webhook)
* [GitHub Documentation — About secret scanning and partner revocations](https://docs.github.com/en/code-security/secret-scanning/introduction/about-secret-scanning)
* [n8n Docs — Environment Variables Configuration Methods](https://docs.n8n.io/hosting/environment-variables/configuration-methods/)
* [AdaptadorDiscord.java](../../../donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorDiscord.java)
* [compose.yaml](../../../compose.yaml)
* [workflows.json](../../../n8n/workflows/workflows.json)
