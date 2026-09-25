package com.donatrack.notificaciones.infrastructure.adapters.out.messaging;

import com.donatrack.notificaciones.application.ports.out.NotificacionAdapter;
import com.donatrack.notificaciones.domain.entities.Notificacion;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component("DISCORD")
public class AdaptadorDiscord implements NotificacionAdapter {

  private static final Logger logger = LoggerFactory.getLogger(AdaptadorDiscord.class);
  private final RestTemplate restTemplate;

  @Value("${n8n.webhook.url:http://n8n:5678/webhook/donatrack/notificaciones}")
  private String n8nWebhookUrl;

  @Value(
      "${discord.webhook.url:https://discord.com/api/webhooks/1553029764351262720/OajZ-dc1mGlmUY9736gqTY8L2DeoEdaJfMh8t3F7PdaprYjZPBLZlnWBdRNeD7ZLfotK}")
  private String discordWebhookUrl;

  public AdaptadorDiscord() {
    this.restTemplate = new RestTemplate();
  }

  public AdaptadorDiscord(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void enviar(Notificacion notificacion) {
    logger.info(
        "Procesando notificación Discord para {}: {}",
        notificacion.getDestinatario(),
        notificacion.getMensaje());

    // 1. Intentar enviar a través del workflow Notificador de n8n
    boolean enviado = enviarViaN8n(notificacion, n8nWebhookUrl);

    // Si falló por URL Docker (n8n:5678) cuando se ejecuta en host local, intentar localhost
    if (!enviado && n8nWebhookUrl.contains("n8n:5678")) {
      String localN8nUrl = n8nWebhookUrl.replace("n8n:5678", "localhost:5678");
      enviado = enviarViaN8n(notificacion, localN8nUrl);
    }

    // 2. Si n8n no está disponible, fallback directo a Discord Webhook
    if (!enviado && discordWebhookUrl != null && !discordWebhookUrl.isBlank()) {
      enviado = enviarDirectoDiscord(notificacion);
    }

    notificacion.setCompletada(enviado);
  }

  private boolean enviarViaN8n(Notificacion notificacion, String webhookUrl) {
    try {
      Map<String, Object> payload = new HashMap<>();
      payload.put("destinatario", notificacion.getDestinatario());
      payload.put("mensaje", notificacion.getMensaje());
      payload.put("medio", "DISCORD");
      // Campos adicionales para compatibilidad con templates
      payload.put("user", notificacion.getDestinatario());
      payload.put("badge", "Notificación del Sistema");
      payload.put("description", notificacion.getMensaje());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

      restTemplate.postForObject(webhookUrl, request, String.class);
      logger.info("Notificación enviada con éxito vía n8n webhook: {}", webhookUrl);
      return true;
    } catch (RestClientException e) {
      logger.warn(
          "No se pudo enviar a n8n en {} (se intentará fallback si está configurado): {}",
          webhookUrl,
          e.getMessage());
      return false;
    }
  }

  private boolean enviarDirectoDiscord(Notificacion notificacion) {
    try {
      Map<String, Object> payload = new HashMap<>();
      payload.put("username", "DonaTrack • Notificaciones");
      payload.put(
          "avatar_url", "https://api.dicebear.com/9.x/icons/png?seed=donatrack-bell&size=128");

      Map<String, Object> embed = new HashMap<>();
      embed.put("title", "📢 Nueva Notificación de Evento");
      embed.put("description", "> 💬 **Mensaje:**\n> *" + notificacion.getMensaje() + "*");
      embed.put("color", 5793266);

      Map<String, Object> fieldDest = new HashMap<>();
      fieldDest.put("name", "👤 Destinatario");
      fieldDest.put("value", "`" + notificacion.getDestinatario() + "`");
      fieldDest.put("inline", true);

      Map<String, Object> fieldMedio = new HashMap<>();
      fieldMedio.put("name", "📡 Canal");
      fieldMedio.put("value", "`DISCORD`");
      fieldMedio.put("inline", true);

      Map<String, Object> fieldEstado = new HashMap<>();
      fieldEstado.put("name", "⚡ Estado");
      fieldEstado.put("value", "🟢 `ENTREGADO`");
      fieldEstado.put("inline", true);

      embed.put("fields", java.util.List.of(fieldDest, fieldMedio, fieldEstado));

      Map<String, String> footer = new HashMap<>();
      footer.put("text", "DonaTrack • Plataforma de Gestión y Trazabilidad");
      embed.put("footer", footer);

      payload.put("embeds", java.util.List.of(embed));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

      restTemplate.postForObject(discordWebhookUrl, request, String.class);
      logger.info("Notificación enviada directamente a Discord Webhook.");
      return true;
    } catch (RestClientException e) {
      logger.error(
          "Error al enviar notificación directamente a Discord Webhook: {}", e.getMessage());
      return false;
    }
  }
}
