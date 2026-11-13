package com.donatrack.web.controller.api;

import com.donatrack.web.gateway.NotificacionesGateway;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@PreAuthorize("isAuthenticated()")
public class NotificacionesApiController {

  private final NotificacionesGateway notificacionesGateway;

  public NotificacionesApiController(NotificacionesGateway notificacionesGateway) {
    this.notificacionesGateway = notificacionesGateway;
  }

  @GetMapping("/mis-alertas")
  public ResponseEntity<List<Map<String, Object>>> misAlertas(Authentication auth) {
    String destinatario = auth != null ? auth.getName() : null;
    if (auth instanceof JwtAuthenticationToken jwt) {
      String email = jwt.getToken().getClaimAsString("email");
      if (email != null && !email.isBlank()) {
        destinatario = email;
      }
    }

    if (destinatario == null || destinatario.isBlank()) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    List<Map<String, Object>> alertas =
        notificacionesGateway.obtenerNotificacionesUsuario(destinatario);
    return ResponseEntity.ok(alertas != null ? alertas : Collections.emptyList());
  }
}
