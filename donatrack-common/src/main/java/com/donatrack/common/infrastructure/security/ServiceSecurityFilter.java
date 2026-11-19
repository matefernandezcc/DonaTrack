package com.donatrack.common.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

public class ServiceSecurityFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(ServiceSecurityFilter.class);

  @Value("${donatrack.security.enabled:${DONATRACK_SECURITY_ENABLED:true}}")
  private boolean securityEnabled;

  @Value("${INTERNAL_API_KEY:${donatrack.internal-api-key:donatrack-internal-key-change-in-prod}}")
  private String internalApiKey;

  @Value(
      "${JWT_SECRET:${donatrack.jwt-secret:SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!}}")
  private String jwtSecret;

  public ServiceSecurityFilter() {}

  public ServiceSecurityFilter(boolean securityEnabled, String internalApiKey, String jwtSecret) {
    this.securityEnabled = securityEnabled;
    this.internalApiKey = internalApiKey;
    this.jwtSecret = jwtSecret;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!securityEnabled) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
      path = path.substring(contextPath.length());
    }

    // 1. Rutas públicas de infraestructura y documentación
    if (isPublicPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 2. Validación de X-Service-Key (Tráfico Inter-Servicio M2M)
    String serviceKey = request.getHeader("X-Service-Key");
    if (serviceKey != null
        && MessageDigest.isEqual(
            serviceKey.getBytes(StandardCharsets.UTF_8),
            internalApiKey.getBytes(StandardCharsets.UTF_8))) {
      filterChain.doFilter(request, response);
      return;
    }

    // 3. Validación de Bearer Token JWT (HS256)
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7).trim();
      if (isTokenValid(token)) {
        if (isAdminOnlyOperation(request.getMethod(), path)) {
          boolean isAdmin =
              JwtClaimsExtractor.extraerClaims(request, jwtSecret)
                  .map(claims -> claims.hasRole("ADMIN") || claims.hasRole("INTERNAL_SERVICE"))
                  .orElse(false);
          if (!isAdmin) {
            log.warn(
                "Acceso denegado (403): operación crítica {} {} requiere rol ADMIN",
                request.getMethod(),
                path);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response
                .getWriter()
                .write(
                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Acceso denegado: esta operación crítica requiere rol ADMIN\"}");
            return;
          }
        }
        filterChain.doFilter(request, response);
        return;
      }
    }

    // 4. Acceso denegado
    log.warn("Acceso no autorizado bloqueado en {} {}", request.getMethod(), path);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response
        .getWriter()
        .write(
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Acceso no autorizado: se requiere cabecera X-Service-Key o Bearer JWT valido\"}");
  }

  private boolean isAdminOnlyOperation(String method, String path) {
    if ("DELETE".equalsIgnoreCase(method)) {
      return true;
    }
    if (path.equals("/admin") || path.startsWith("/admin/")) {
      return true;
    }

    boolean isRead = "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);

    // Flota y planificación en Logística (escrituras)
    if (!isRead
        && (path.startsWith("/api/camiones")
            || path.startsWith("/api/choferes")
            || path.startsWith("/api/rutas"))) {
      return true;
    }
    if (path.startsWith("/api/planificacion/ejecutar")) {
      return true;
    }

    // Importación masiva, matchmaking, auditoría y cambios de estado críticos en Donaciones
    if (path.startsWith("/api/personas/importar-csv")
        || path.contains("/matchmaking")
        || path.endsWith("/estado/asignar")
        || path.endsWith("/estado/vencer")
        || path.startsWith("/api/donaciones/auditoria")
        || path.matches("^/api/donaciones/[^/]+/estado$")) {
      return true;
    }
    if ("PUT".equalsIgnoreCase(method) && path.matches("^/api/donaciones/[^/]+$")) {
      return true;
    }

    // Gestión administrativa de solicitudes de ONG (POST /api/solicitudes-ong queda habilitado para postulaciones)
    if (path.startsWith("/api/solicitudes-ong") && (isRead || !"POST".equalsIgnoreCase(method))) {
      return true;
    }

    // Alta/edición directa de beneficiarios (POST .../necesidades queda habilitado para ONGs)
    if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))
        && (path.equals("/api/beneficiarios") || path.matches("^/api/beneficiarios/[^/]+$"))) {
      return true;
    }

    // Operaciones administrativas de rankings y envío directo de notificaciones
    return !isRead
        && (path.startsWith("/api/rankings")
            || path.startsWith("/api/mensajes")
            || path.startsWith("/api/notificaciones"));
  }

  private boolean isPublicPath(String path) {
    return path.equals("/")
        || path.equals("/index.html")
        || path.startsWith("/actuator")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/api-docs")
        || path.startsWith("/api/auth/sso/")
        || path.startsWith("/auth/")
        || path.startsWith("/oauth2/")
        || path.startsWith("/login/")
        || path.startsWith("/healthcheck")
        || path.startsWith("/graphiql")
        || path.startsWith("/api/public")
        || path.startsWith("/favicon.ico")
        || path.startsWith("/error");
  }

  private boolean isTokenValid(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        return false;
      }

      // Validar firma HMAC-SHA256
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] signatureBytes =
          mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
      String expectedSignature =
          Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

      if (!MessageDigest.isEqual(
          expectedSignature.getBytes(StandardCharsets.UTF_8),
          parts[2].getBytes(StandardCharsets.UTF_8))) {
        return false;
      }

      // Validar expiración ("exp":<epochSeconds>)
      String payloadJson =
          new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
      int expIdx = payloadJson.indexOf("\"exp\":");
      if (expIdx != -1) {
        int start = expIdx + 6;
        int end = start;
        while (end < payloadJson.length()
            && (Character.isDigit(payloadJson.charAt(end)) || payloadJson.charAt(end) == ' ')) {
          end++;
        }
        long exp = Long.parseLong(payloadJson.substring(start, end).trim());
        long now = Instant.now().getEpochSecond();
        if (now > exp) {
          return false;
        }
      }

      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
