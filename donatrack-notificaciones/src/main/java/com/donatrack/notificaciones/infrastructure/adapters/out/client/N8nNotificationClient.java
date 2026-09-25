package com.donatrack.notificaciones.infrastructure.adapters.out.client;

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

@Component
public class N8nNotificationClient {

  private static final Logger logger = LoggerFactory.getLogger(N8nNotificationClient.class);
  private final RestTemplate restTemplate;

  @Value("${n8n.webhook.url:http://n8n:5678/webhook/donatrack/notificaciones}")
  private String n8nWebhookUrl;

  public N8nNotificationClient() {
    this.restTemplate = new RestTemplate();
  }

  public N8nNotificationClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public boolean enviar(String destinatario, String mensaje, String medio) {
    boolean enviado = enviarHttp(destinatario, mensaje, medio, n8nWebhookUrl);

    if (!enviado && n8nWebhookUrl.contains("n8n:5678")) {
      String localUrl = n8nWebhookUrl.replace("n8n:5678", "localhost:5678");
      enviado = enviarHttp(destinatario, mensaje, medio, localUrl);
    }

    return enviado;
  }

  private boolean enviarHttp(String destinatario, String mensaje, String medio, String url) {
    try {
      Map<String, Object> payload = new HashMap<>();
      payload.put("destinatario", destinatario);
      payload.put("mensaje", mensaje);
      payload.put("medio", medio);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

      restTemplate.postForObject(url, request, String.class);
      logger.info("Notificación ({}) enviada con éxito a n8n webhook: {}", medio, url);
      return true;
    } catch (RestClientException e) {
      logger.warn(
          "No se pudo enviar notificación ({}) a n8n en {}: {}", medio, url, e.getMessage());
      return false;
    }
  }
}
