package com.donatrack.web.gateway;

import com.donatrack.web.dto.TokenResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthGateway {

  private static final Logger log = LoggerFactory.getLogger(AuthGateway.class);
  private final RestClient authClient;

  public AuthGateway(@Qualifier("authClient") RestClient authClient) {
    this.authClient = authClient;
  }

  public TokenResponse login(String username, String password) {
    return authClient
        .post()
        .uri("/auth/login")
        .body(Map.of("username", username, "password", password))
        .retrieve()
        .body(TokenResponse.class);
  }

  public TokenResponse exchangeToken(String code) {
    return authClient
        .post()
        .uri("/auth/token/exchange")
        .body(Map.of("code", code))
        .retrieve()
        .body(TokenResponse.class);
  }

  public TokenResponse registrar(Map<String, Object> payload) {
    return authClient
        .post()
        .uri("/auth/register")
        .body(payload)
        .retrieve()
        .body(TokenResponse.class);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<Map<String, Object>> listarUsuarios() {
    try {
      return authClient
          .get()
          .uri("/admin/usuarios")
          .retrieve()
          .body(java.util.List.class);
    } catch (Exception e) {
      log.error("Error al listar usuarios desde auth service: {}", e.getMessage());
      return java.util.Collections.emptyList();
    }
  }

  public void cambiarRol(java.util.UUID usuarioId, String nuevoRol) {
    authClient
        .put()
        .uri("/admin/usuarios/{id}/rol", usuarioId)
        .body(Map.of("rol", nuevoRol))
        .retrieve()
        .toBodilessEntity();
  }

  public void resetearPassword(java.util.UUID usuarioId) {
    authClient
        .post()
        .uri("/admin/usuarios/{id}/reset-password", usuarioId)
        .retrieve()
        .toBodilessEntity();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> obtenerPerfil(java.util.UUID usuarioId) {
    try {
      return authClient
          .get()
          .uri("/api/auth/perfil/{id}", usuarioId)
          .retrieve()
          .body(Map.class);
    } catch (Exception e) {
      log.error("Error al consultar perfil de usuario: {}", e.getMessage());
      return Map.of();
    }
  }

  public void actualizarPerfil(java.util.UUID usuarioId, String descripcion, String ubicacion) {
    authClient
        .put()
        .uri("/api/auth/perfil/{id}", usuarioId)
        .body(Map.of(
            "descripcion", descripcion != null ? descripcion : "",
            "ubicacion", ubicacion != null ? ubicacion : ""
        ))
        .retrieve()
        .toBodilessEntity();
  }
}

