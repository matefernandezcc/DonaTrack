# 🛡️ Seguridad y Gestión de Secretos: Webhook de Discord y n8n en DonaTrack

Este documento detalla los principios de seguridad, arquitectura y buenas prácticas implementadas en **DonaTrack** para proteger la URL y el token secreto del webhook de **Discord**, así como los endpoints de **n8n**.

---

## 1. Contexto y Naturaleza del Problema

### ¿Qué es un Webhook de Discord?
Una URL de webhook de Discord sigue el formato:
```
https://discord.com/api/webhooks/{webhook.id}/{webhook.token}
```
El `{webhook.token}` actúa como una **clave de acceso completa y sin restricciones**: cualquier persona, servicio o script que conozca dicha URL puede enviar mensajes, menciones e imágenes directamente al canal de Discord sin necesidad de usuario ni contraseña.

### El Riesgo de Exposición en Git
* **Detección Automática por Bots y GitHub Secret Scanning:** Plataformas como GitHub analizan constantemente los commits en busca de patrones de claves (tokens de AWS, Discord, Slack, etc.).
* **Revocación Inmediata de Discord:** En cuanto Discord detecta que una URL de webhook aparece en un commit o repositorio, **lo revoca e invalida automáticamente en cuestión de segundos** (arrojando el error `{"code": 10015, "message": "Unknown Webhook"}`). Esto inutiliza de inmediato cualquier notificación del sistema.

---

## 2. Vulnerabilidad Detectada y Solución Aplicada

### ❌ Estado Previo (Inseguro)
En la clase [`AdaptadorDiscord.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorDiscord.java), la URL con su token secreto estaba escrita como valor de fallback por defecto en código Java:
```java
// INSEGURO: El token quedaba rastreado en el repositorio de Git
@Value("${discord.webhook.url:https://discord.com/api/webhooks/1565177067...}")
private String discordWebhookUrl;
```

### ✅ Solución Implementada (Segura)
Se eliminó cualquier referencia al webhook real del código fuente de Java:
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
1. El webhook real reside **únicamente** en el archivo local [`.env`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.env):
   ```env
   DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/1565177067119841496/TU_TOKEN_SECRETO_AQUI
   ```
2. El archivo `.env` está explícitamente listado en el [`.gitignore`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.gitignore). **Git jamás lo rastrea ni lo sube a GitHub**.
3. El archivo [`.env.template`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.env.template) sirve como plantilla pública para el equipo sin incluir ningún secreto:
   ```env
   DISCORD_WEBHOOK_URL=
   ```

### Capa 2: Inyección Segura en Docker Compose
En [`compose.yaml`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/compose.yaml), la variable se inyecta directamente a la memoria de los contenedores en tiempo de arranque:
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
En el archivo de exportación de workflows [`n8n/workflows/workflows.json`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/n8n/workflows/workflows.json), los nodos de Discord utilizan expresiones dinámicas:
```json
"url": "={{ $env.DISCORD_WEBHOOK_URL || 'https://httpbin.org/post' }}"
```
* **Ventaja 1:** Al correr `make n8n-export` y subir los workflows a Git, **solo viaja el nombre de la variable**, nunca la URL real.
* **Ventaja 2 (Tolerancia a fallos):** Si un desarrollador levanta el proyecto sin configurar la variable, n8n utiliza el sink simulado sin arrojar errores fatales ni detener los flujos.

### Capa 4: Patrón Proxy (Ocultamiento al Cliente)
* **Colección de Bruno ([`docs/donatrack-api/Donatrack - API/Notificaciones/`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/docs/donatrack-api/Donatrack%20-%20API/Notificaciones/)):** Las 8 peticiones de Bruno solo interactúan con:
  - `POST {{notificacionesUrl}}/api/mensajes` (Microservicio Spring Boot)
  - `POST {{n8nUrl}}/webhook/donatrack/...` (Webhooks de n8n)
* Bruno y el navegador **desconocen por completo** la URL de Discord; el backend actúa como firewall y mediador seguro.

---

## 4. Opciones de Seguridad Adicionales Disponibles

Si en futuras etapas o en producción se desea elevar aún más el estándar de seguridad:

| Mecanismo | Descripción | Nivel de Protección |
|---|---|---|
| **Bóveda Cifrada de n8n** | Almacenar el webhook como credencial nativa de n8n. Los datos se encriptan con **AES-256** en la base de datos de n8n (`n8n_data`). | 🔐 Muy Alto |
| **Header Authentication en Webhooks** | Exigir un encabezado secreto (ej. `X-DonaTrack-Auth: <token>`) en los nodos `Webhook Trigger` de n8n para impedir llamadas no autorizadas desde la red. | 🛡️ Alto |
| **Rotación de Webhooks** | En caso de sospecha de filtración, generar un nuevo webhook en Discord (`Ajustes de Canal > Integraciones > Webhooks`), actualizar el valor en `.env` y reiniciar con `docker compose up -d n8n`. | ⚡ Inmediato |

---

## 5. Resumen de Archivos Involucrados

| Archivo | Rol de Seguridad |
|---|---|
| [`.env`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.env) | **Único lugar** donde reside el valor real del webhook (ignorado por Git). |
| [`.gitignore`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.gitignore) | Garantiza que `.env` no se versione en el repositorio. |
| [`.env.template`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/.env.template) | Plantilla pública sin valores sensibles. |
| [`AdaptadorDiscord.java`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/donatrack-notificaciones/src/main/java/com/donatrack/notificaciones/infrastructure/adapters/out/messaging/AdaptadorDiscord.java) | Código Java limpio, lee de variable de entorno sin default hardcodeado. |
| [`compose.yaml`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/compose.yaml) | Inyecta la variable en memoria a los contenedores Docker. |
| [`workflows.json`](file:///home/maximo/Documentos/Diseño%20de%20Sistemas/DonaTrack/n8n/workflows/workflows.json) | Usa `$env.DISCORD_WEBHOOK_URL` para que el JSON exportado esté limpio de credenciales. |
