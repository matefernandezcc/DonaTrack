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
    return obtenerNotificacionesUsuario(
        destinatario != null && !destinatario.isBlank()
            ? List.of(destinatario)
            : Collections.emptyList());
  }

  public List<Map<String, Object>> obtenerNotificacionesUsuario(List<String> destinatarios) {
    if (destinatarios == null || destinatarios.isEmpty()) {
      return Collections.emptyList();
    }
    StringBuilder uri = new StringBuilder("/api/mensajes");
    boolean first = true;
    for (String dest : destinatarios) {
      if (dest == null || dest.isBlank()) continue;
      uri.append(first ? "?" : "&")
          .append("destinatario=")
          .append(java.net.URLEncoder.encode(dest.trim(), java.nio.charset.StandardCharsets.UTF_8));
      first = false;
    }
    if (first) {
      return Collections.emptyList();
    }
    try {
      List<Map<String, Object>> result =
          notificacionesClient
              .get()
              .uri(uri.toString())
              .retrieve()
              .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      log.warn(
          "No se pudieron consultar notificaciones para {}: {}",
          enmascararDestinatario(destinatarios.get(0)),
          e.getMessage());
      throw new RuntimeException("Servicio de notificaciones no disponible", e);
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
      log.warn(
          "No se pudo emitir alerta a {}: {}",
          enmascararDestinatario(destinatario),
          e.getMessage());
    }
  }

  private String enmascararDestinatario(String dest) {
    if (dest == null || dest.isBlank()) {
      return "anónimo";
    }
    int atIndex = dest.indexOf('@');
    if (atIndex > 1) {
      String userPart = dest.substring(0, atIndex);
      String domainPart = dest.substring(atIndex);
      String maskedUser = userPart.charAt(0) + "***" + userPart.charAt(userPart.length() - 1);
      return maskedUser + domainPart;
    }
    if (dest.length() > 4) {
      return dest.substring(0, 2) + "***" + dest.substring(dest.length() - 2);
    }
    return "***";
  }
}
