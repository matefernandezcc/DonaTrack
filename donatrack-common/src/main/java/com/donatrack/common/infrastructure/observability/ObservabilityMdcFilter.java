package com.donatrack.common.infrastructure.observability;

import com.donatrack.common.infrastructure.security.JwtClaimsExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro transversal de observabilidad que enriquece el contexto MDC con campos estructurados
 * obligatorios (requestId, user, sessionId) y propaga X-Request-Id / X-Trace-Id en la respuesta
 * HTTP para correlación directa en Loki y Zipkin.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ObservabilityMdcFilter extends OncePerRequestFilter {

  public static final String HEADER_REQUEST_ID = "X-Request-Id";
  public static final String HEADER_TRACE_ID = "X-Trace-Id";

  @Value(
      "${JWT_SECRET:${donatrack.jwt-secret:SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!}}")
  private String jwtSecret;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestId = request.getHeader(HEADER_REQUEST_ID);
    if (requestId == null || requestId.isBlank() || requestId.length() > 64) {
      requestId = UUID.randomUUID().toString();
    } else {
      requestId = requestId.replaceAll("[^a-zA-Z0-9\\-_.]", "");
    }

    String user = resolverUsuario(request);
    String sessionId = resolverSesion(request);

    MDC.put("requestId", requestId);
    MDC.put("user", user);
    MDC.put("sessionId", sessionId);

    response.setHeader(HEADER_REQUEST_ID, requestId);
    String traceId = MDC.get("traceId");
    if (traceId != null && !traceId.isBlank()) {
      response.setHeader(HEADER_TRACE_ID, traceId);
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("requestId");
      MDC.remove("user");
      MDC.remove("sessionId");
    }
  }

  private String resolverUsuario(HttpServletRequest request) {
    try {
      var claimsOpt = JwtClaimsExtractor.extraerClaims(request, jwtSecret);
      if (claimsOpt.isPresent()) {
        var c = claimsOpt.get();
        if (c.username() != null && !c.username().isBlank()) {
          return c.username();
        }
        if (c.userId() != null) {
          return c.userId().toString();
        }
      }
    } catch (Exception ignored) {
    }

    try {
      var auth =
          org.springframework.security.core.context.SecurityContextHolder.getContext()
              .getAuthentication();
      if (auth != null
          && auth.isAuthenticated()
          && auth.getName() != null
          && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
        return auth.getName();
      }
    } catch (Throwable ignored) {
    }

    if (request.getHeader("X-Service-Key") != null) {
      return "internal-service";
    }
    return "anonymous";
  }

  private String resolverSesion(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null && session.getId() != null) {
      String id = session.getId();
      return id.length() > 8 ? id.substring(0, 8) : id;
    }
    return "stateless";
  }
}
