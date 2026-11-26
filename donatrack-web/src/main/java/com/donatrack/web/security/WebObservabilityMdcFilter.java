package com.donatrack.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de observabilidad en el BFF web que enriquece el contexto MDC con requestId, user y
 * sessionId y expone X-Request-Id / X-Trace-Id en las cabeceras de respuesta HTTP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class WebObservabilityMdcFilter extends OncePerRequestFilter {

  public static final String HEADER_REQUEST_ID = "X-Request-Id";
  public static final String HEADER_TRACE_ID = "X-Trace-Id";

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

    String user = "anonymous";
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null
        && auth.isAuthenticated()
        && auth.getName() != null
        && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
      user = auth.getName();
    }

    HttpSession session = request.getSession(false);
    String sessionId = "stateless";
    if (session != null && session.getId() != null) {
      String rawId = session.getId();
      sessionId = rawId.length() > 8 ? rawId.substring(0, 8) : rawId;
    }

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
}
