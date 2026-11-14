package com.donatrack.web.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de limitación de tasa de solicitudes (Rate Limiting) basado en IP y Token Bucket. Utiliza
 * Bucket4j respaldado por una caché en memoria Caffeine con expiración LRU y TTL.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

  private static final String ERROR_PAYLOAD =
      "{\"error\": \"RATE_LIMIT_EXCEEDED\", \"mensaje\": \"Demasiadas solicitudes. Por favor, reintente en unos instantes.\"}";

  private final boolean enabled;
  private final int requestsPerMinute;

  private final Cache<String, Bucket> bucketCache =
      Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(10_000).build();

  public RateLimitingFilter(
      @Value("${donatrack.rate-limit.enabled:true}") boolean enabled,
      @Value("${donatrack.rate-limit.requests-per-minute:60}") int requestsPerMinute) {
    this.enabled = enabled;
    this.requestsPerMinute = requestsPerMinute;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!enabled) {
      return true;
    }
    String path = request.getServletPath();
    if (path == null || path.isEmpty()) {
      path = request.getRequestURI();
    }
    return isStaticOrActuator(path);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = resolveClientIp(request);
    Bucket bucket = bucketCache.get(clientIp, key -> createNewBucket());

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
      filterChain.doFilter(request, response);
    } else {
      log.warn(
          "Rate limit excedido para IP [{}] en endpoint [{}]", clientIp, request.getRequestURI());
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.getWriter().write(ERROR_PAYLOAD);
      response.getWriter().flush();
    }
  }

  /** Crea un nuevo bucket con cuota de recarga greedy por minuto. */
  private Bucket createNewBucket() {
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(requestsPerMinute)
            .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
            .build();
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Extrae y sanitiza la IP del cliente evaluando X-Forwarded-For con fallback a getRemoteAddr().
   */
  private String resolveClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      String firstIp = xForwardedFor.split(",")[0].trim();
      if (!firstIp.isEmpty()) {
        return firstIp;
      }
    }
    String remoteAddr = request.getRemoteAddr();
    return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
  }

  /**
   * Identifica si la ruta corresponde a recursos estáticos o endpoints de observabilidad/actuator.
   */
  private boolean isStaticOrActuator(String path) {
    if (path == null) {
      return false;
    }
    return path.startsWith("/css/")
        || path.startsWith("/js/")
        || path.startsWith("/assets/")
        || path.startsWith("/img/")
        || path.startsWith("/remitos/")
        || path.startsWith("/fotos/")
        || path.startsWith("/actuator")
        || path.equals("/favicon.ico")
        || path.equals("/favicon.svg")
        || path.equals("/healthcheck");
  }

  // Package-private para facilitar aserciones y tests
  Cache<String, Bucket> getBucketCache() {
    return bucketCache;
  }
}
