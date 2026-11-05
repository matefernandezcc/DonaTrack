package com.donatrack.auth.infrastructure.adapters.in.api;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.ExchangeRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthController {

  private final AuthService authService;

  @Value("${donatrack.web-url:http://localhost:8080}")
  private String webUrl;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/oauth2/authorization/{provider}")
  public void mockOAuth2Redirect(
      @PathVariable("provider") String provider,
      HttpServletResponse response) throws IOException {
    String prov = provider.toLowerCase();
    String email = prov.equals("google") ? "donante.google@gmail.com" : "donante.github@gmail.com";
    String nombre = prov.equals("google") ? "Donante Google (SSO)" : "Donante GitHub (SSO)";

    Usuario usuario = authService.obtenerOCrearUsuarioSocial(email, nombre, provider.toUpperCase());
    String code = authService.generarCodigoIntercambio(usuario);
    response.sendRedirect(webUrl + "/auth/callback?code=" + code);
  }

  @PostMapping("/auth/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/auth/token/exchange")
  public ResponseEntity<TokenResponse> exchangeCode(@Valid @RequestBody ExchangeRequest request) {
    TokenResponse response = authService.exchangeCode(request.code());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/healthcheck")
  public ResponseEntity<Map<String, String>> healthcheck() {
    return ResponseEntity.ok(Map.of("status", "Servicio de Autenticación DonaTrack online 🚀"));
  }
}

