package com.donatrack.common.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JwtClaimsExtractor {

  private static final Logger log = LoggerFactory.getLogger(JwtClaimsExtractor.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private JwtClaimsExtractor() {}

  public record ClaimsInfo(
      UUID userId,
      UUID personaId,
      String username,
      List<String> roles) {

    public boolean hasRole(String role) {
      if (roles == null || role == null) {
        return false;
      }
      return roles.stream()
          .anyMatch(r -> r.equalsIgnoreCase(role) || r.equalsIgnoreCase("ROLE_" + role));
    }
  }

  public static Optional<ClaimsInfo> extraerClaims(HttpServletRequest request, String jwtSecret) {
    if (request == null) {
      return Optional.empty();
    }
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return Optional.empty();
    }
    String token = authHeader.substring(7).trim();
    return parseToken(token, jwtSecret);
  }

  public static Optional<UUID> extraerUserId(HttpServletRequest request, String jwtSecret) {
    return extraerClaims(request, jwtSecret).map(ClaimsInfo::userId);
  }

  public static Optional<UUID> extraerPersonaId(HttpServletRequest request, String jwtSecret) {
    return extraerClaims(request, jwtSecret)
        .map(c -> c.personaId() != null ? c.personaId() : c.userId());
  }

  public static boolean esAdminOInternalService(
      HttpServletRequest request, String internalApiKey) {
    if (request == null) {
      return false;
    }

    // 1. Validación de X-Service-Key (M2M)
    String serviceKey = request.getHeader("X-Service-Key");
    if (serviceKey != null && internalApiKey != null) {
      if (MessageDigest.isEqual(
          serviceKey.getBytes(StandardCharsets.UTF_8),
          internalApiKey.getBytes(StandardCharsets.UTF_8))) {
        return true;
      }
    }

    // 2. Validación de JWT Bearer token para roles ADMIN o INTERNAL_SERVICE
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7).trim();
      try {
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
          JsonNode payload =
              objectMapper.readTree(
                  new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
          if (payload.has("roles")) {
            JsonNode rolesNode = payload.get("roles");
            if (rolesNode.isArray()) {
              for (JsonNode r : rolesNode) {
                String role = r.asText("");
                if ("ADMIN".equalsIgnoreCase(role)
                    || "ROLE_ADMIN".equalsIgnoreCase(role)
                    || "INTERNAL_SERVICE".equalsIgnoreCase(role)
                    || "ROLE_INTERNAL_SERVICE".equalsIgnoreCase(role)) {
                  return true;
                }
              }
            }
          }
        }
      } catch (Exception e) {
        log.debug("Error al inspeccionar roles en token: {}", e.getMessage());
      }
    }

    // 3. Validación de SecurityContext de Spring si estuviera autenticado
    try {
      org.springframework.security.core.Authentication auth =
          org.springframework.security.core.context.SecurityContextHolder.getContext()
              .getAuthentication();
      if (auth != null && auth.isAuthenticated()) {
        boolean hasAdminRole =
            auth.getAuthorities().stream()
                .anyMatch(
                    a ->
                        a.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
                            || a.getAuthority().equalsIgnoreCase("ADMIN")
                            || a.getAuthority().equalsIgnoreCase("ROLE_INTERNAL_SERVICE")
                            || a.getAuthority().equalsIgnoreCase("INTERNAL_SERVICE"));
        if (hasAdminRole) {
          return true;
        }
      }
    } catch (Throwable ignored) {
    }

    return false;
  }

  private static Optional<ClaimsInfo> parseToken(String token, String jwtSecret) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        return Optional.empty();
      }

      // Validar firma HMAC-SHA256 si se provee el secreto
      if (jwtSecret != null && !jwtSecret.isBlank()) {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signatureBytes =
            mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
        String expectedSignature =
            Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        if (!MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            parts[2].getBytes(StandardCharsets.UTF_8))) {
          log.warn("Firma de JWT inválida");
          return Optional.empty();
        }
      }

      JsonNode payload =
          objectMapper.readTree(
              new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));

      // Validar expiración si está presente
      if (payload.has("exp")) {
        long exp = payload.get("exp").asLong();
        if (Instant.now().getEpochSecond() > exp) {
          log.warn("JWT expirado");
          return Optional.empty();
        }
      }

      UUID userId = null;
      if (payload.has("sub")) {
        try {
          userId = UUID.fromString(payload.get("sub").asText());
        } catch (IllegalArgumentException ignored) {
        }
      }
      if (userId == null && payload.has("userId")) {
        try {
          userId = UUID.fromString(payload.get("userId").asText());
        } catch (IllegalArgumentException ignored) {
        }
      }

      UUID personaId = null;
      if (payload.has("personaId")) {
        try {
          personaId = UUID.fromString(payload.get("personaId").asText());
        } catch (IllegalArgumentException ignored) {
        }
      }

      String username = payload.has("username") ? payload.get("username").asText() : null;

      List<String> roles = new ArrayList<>();
      if (payload.has("roles") && payload.get("roles").isArray()) {
        for (JsonNode r : payload.get("roles")) {
          roles.add(r.asText());
        }
      }

      return Optional.of(new ClaimsInfo(userId, personaId, username, roles));
    } catch (Exception e) {
      log.warn("Error al decodificar JWT: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
