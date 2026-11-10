package com.donatrack.web.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificacionesGateway {

  private static final Logger log = LoggerFactory.getLogger(NotificacionesGateway.class);
  private final RestClient notificacionesClient;

  public NotificacionesGateway(@Qualifier("notificacionesClient") RestClient notificacionesClient) {
    this.notificacionesClient = notificacionesClient;
  }

  public List<Map<String, Object>> obtenerNotificacionesUsuario(String destinatario) {
    try {
      String uri = "/api/mensajes";
      if (destinatario != null && !destinatario.isBlank()) {
        uri +=
            "?destinatario="
                + java.net.URLEncoder.encode(destinatario, java.nio.charset.StandardCharsets.UTF_8);
      }
      List<Map<String, Object>> result =
          notificacionesClient
              .get()
              .uri(uri)
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.warn("No se pudieron consultar notificaciones para {}: {}", destinatario, e.getMessage());
      return Collections.emptyList();
    }
  }

  public void enviarNotificacion(String destinatario, String mensaje, String medio) {
    try {
      Map<String, String> body =
          Map.of(
              "destinatario", destinatario != null ? destinatario : "",
              "mensaje", mensaje != null ? mensaje : "",
              "medio", medio != null ? medio : "EMAIL");
      notificacionesClient.post().uri("/api/mensajes").body(body).retrieve().toBodilessEntity();
    } catch (Exception e) {
      log.warn("No se pudo emitir alerta a {}: {}", destinatario, e.getMessage());
    }
  }
}
