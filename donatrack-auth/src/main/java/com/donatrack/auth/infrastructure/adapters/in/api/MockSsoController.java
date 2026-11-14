package com.donatrack.auth.infrastructure.adapters.in.api;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "test", "dev"})
public class MockSsoController {

  private final AuthService authService;
  private final String webUrl;

  public MockSsoController(
      AuthService authService,
      @Value("${donatrack.web-url:http://localhost:8080}") String webUrl) {
    this.authService = authService;
    this.webUrl = webUrl;
  }

  private record MockIdentity(String email, String nombre) {}

  private MockIdentity resolveMockIdentity(String provider, String emailParam, String nombreParam) {
    String prov = provider != null ? provider.toLowerCase() : "";
    String defaultEmail =
        "google".equals(prov) ? "donante.google@gmail.com" : "donante.github@gmail.com";
    String defaultNombre = "google".equals(prov) ? "Donante Google (SSO)" : "Donante GitHub (SSO)";

    String email = (emailParam != null && !emailParam.isBlank()) ? emailParam : defaultEmail;
    String nombre = (nombreParam != null && !nombreParam.isBlank()) ? nombreParam : defaultNombre;
    return new MockIdentity(email, nombre);
  }

  @GetMapping("/oauth2/authorization/{provider}")
  public void mockOAuth2Redirect(
      @PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
    MockIdentity identity = resolveMockIdentity(provider, null, null);
    Usuario usuario =
        authService.obtenerOCrearUsuarioSocial(
            identity.email(), identity.nombre(), provider.toUpperCase());
    String code = authService.generarCodigoIntercambio(usuario);
    response.sendRedirect(webUrl + "/auth/callback?code=" + code);
  }

  @GetMapping("/api/auth/sso/codigo")
  public ResponseEntity<Map<String, String>> obtenerCodigoSocial(
      @RequestParam("provider") String provider,
      @RequestParam(value = "email", required = false) String emailParam,
      @RequestParam(value = "nombre", required = false) String nombreParam) {
    MockIdentity identity = resolveMockIdentity(provider, emailParam, nombreParam);
    Usuario usuario =
        authService.obtenerOCrearUsuarioSocial(
            identity.email(), identity.nombre(), provider.toUpperCase());
    String code = authService.generarCodigoIntercambio(usuario);
    return ResponseEntity.ok(Map.of("code", code));
  }
}
