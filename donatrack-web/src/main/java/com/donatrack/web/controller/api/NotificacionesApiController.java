package com.donatrack.web.controller.api;

import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.NotificacionesGateway;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

  private static final Logger log = LoggerFactory.getLogger(NotificacionesApiController.class);

  private final NotificacionesGateway notificacionesGateway;
  private final DonacionesGateway donacionesGateway;

  public NotificacionesApiController(NotificacionesGateway notificacionesGateway) {
    this(notificacionesGateway, null);
  }

  @Autowired
  public NotificacionesApiController(
      NotificacionesGateway notificacionesGateway,
      @Autowired(required = false) DonacionesGateway donacionesGateway) {
    this.notificacionesGateway = notificacionesGateway;
    this.donacionesGateway = donacionesGateway;
  }

  @GetMapping("/mis-alertas")
  public ResponseEntity<?> misAlertas(Authentication auth) {
    if (auth == null) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    Set<String> destinatarios = new LinkedHashSet<>();
    if (auth instanceof JwtAuthenticationToken jwt) {
      String email = jwt.getToken().getClaimAsString("email");
      if (email != null && !email.isBlank()) {
        destinatarios.add(email.trim());
      }
      String personaIdStr = jwt.getToken().getClaimAsString("personaId");
      if (personaIdStr != null && !personaIdStr.isBlank()) {
        destinatarios.add("donante" + personaIdStr + "@test.com");
        if (donacionesGateway != null) {
          try {
            UUID personaId = UUID.fromString(personaIdStr);
            Map<String, Object> persona = donacionesGateway.obtenerPersona(personaId);
            if (persona != null && persona.get("email") != null) {
              String pEmail = persona.get("email").toString().trim();
              if (!pEmail.isBlank()) {
                destinatarios.add(pEmail);
              }
            }
            Map<String, Object> benef = donacionesGateway.obtenerBeneficiario(personaId);
            if (benef != null && benef.get("correoRepresentante") != null) {
              String bEmail = benef.get("correoRepresentante").toString().trim();
              if (!bEmail.isBlank()) {
                destinatarios.add(bEmail);
              }
            }
          } catch (Exception ignored) {
          }
        }
      }
    }

    if (auth.getName() != null && !auth.getName().isBlank() && destinatarios.isEmpty()) {
      destinatarios.add(auth.getName().trim());
    }

    if (destinatarios.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    if (notificacionesGateway == null) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    try {
      List<String> lista = new ArrayList<>(destinatarios);
      List<Map<String, Object>> alertas =
          lista.size() == 1
              ? notificacionesGateway.obtenerNotificacionesUsuario(lista.get(0))
              : notificacionesGateway.obtenerNotificacionesUsuario(lista);
      return ResponseEntity.ok(alertas != null ? alertas : Collections.emptyList());
    } catch (Exception e) {
      log.warn("Error al consultar notificaciones para destinatarios {}: {}", destinatarios, e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(
              Map.of(
                  "error",
                  "NOTIFICACIONES_UNAVAILABLE",
                  "message",
                  "No se pudieron obtener tus alertas en este momento. Reintentá en unos segundos."));
    }
  }
}
