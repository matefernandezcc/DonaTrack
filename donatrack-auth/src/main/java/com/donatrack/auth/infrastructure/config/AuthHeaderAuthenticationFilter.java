package com.donatrack.auth.infrastructure.config;

import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de autenticación de seguridad por cabecera para donatrack-auth.
 *
 * <p>Puebla el SecurityContextHolder a partir de X-Service-Key (tráfico M2M) o Bearer JWT (HS256).
 * Se instancia directamente en SecurityConfig para evitar doble registro automático como Servlet
 * Filter en Spring Boot.
 */
public class AuthHeaderAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final String internalApiKey;

  public AuthHeaderAuthenticationFilter(JwtService jwtService, String internalApiKey) {
    this.jwtService = jwtService;
    this.internalApiKey = internalApiKey;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      String serviceKey = request.getHeader("X-Service-Key");
      if (serviceKey != null
          && internalApiKey != null
          && MessageDigest.isEqual(
              serviceKey.getBytes(StandardCharsets.UTF_8),
              internalApiKey.getBytes(StandardCharsets.UTF_8))) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
      } else {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
          String token = authHeader.substring(7).trim();
          if (jwtService.validarToken(token)) {
            JWTClaimsSet claims = jwtService.obtenerClaims(token);
            List<GrantedAuthority> authorities = extractAuthorities(claims);
            String principal =
                claims.getSubject() != null ? claims.getSubject() : "authenticated-user";
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private List<GrantedAuthority> extractAuthorities(JWTClaimsSet claims) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    Object rolesClaim = claims.getClaim("roles");
    if (rolesClaim instanceof Collection<?> col) {
      for (Object item : col) {
        if (item != null && !item.toString().isBlank()) {
          String roleName = item.toString().trim().toUpperCase();
          if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
          }
          authorities.add(new SimpleGrantedAuthority(roleName));
        }
      }
    } else if (claims.getClaim("rol") instanceof String rolStr && !rolStr.isBlank()) {
      String roleName = rolStr.trim().toUpperCase();
      if (!roleName.startsWith("ROLE_")) {
        roleName = "ROLE_" + roleName;
      }
      authorities.add(new SimpleGrantedAuthority(roleName));
    }
    return authorities;
  }
}
