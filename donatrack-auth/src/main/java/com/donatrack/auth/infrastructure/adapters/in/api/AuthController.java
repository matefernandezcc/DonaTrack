package com.donatrack.auth.infrastructure.adapters.in.api;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.ExchangeRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

  @PostMapping("/auth/register")
  public ResponseEntity<TokenResponse> register(
      @Valid @RequestBody com.donatrack.auth.infrastructure.adapters.in.api.dto.RegisterRequest request) {
    TokenResponse response = authService.registrarUsuario(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/admin/usuarios")
  public ResponseEntity<java.util.List<Map<String, Object>>> listarUsuarios() {
    return ResponseEntity.ok(authService.listarUsuarios());
  }

  @PutMapping("/admin/usuarios/{id}/rol")
  public ResponseEntity<Map<String, String>> cambiarRol(
      @PathVariable UUID id, @RequestBody Map<String, String> body) {
    String nuevoRol = body.get("rol");
    if (nuevoRol == null || nuevoRol.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "El campo 'rol' es requerido"));
    }
    authService.cambiarRolUsuario(id, nuevoRol);
    return ResponseEntity.ok(Map.of("mensaje", "Rol actualizado exitosamente a " + nuevoRol));
  }

  @PostMapping("/admin/usuarios/{id}/reset-password")
  public ResponseEntity<Map<String, String>> resetearPassword(@PathVariable UUID id) {
    authService.resetearPasswordTemporal(id);
    return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida exitosamente a 'password123'"));
  }

  @GetMapping("/api/auth/perfil/{id}")
  public ResponseEntity<Map<String, Object>> obtenerPerfil(@PathVariable UUID id) {
    return ResponseEntity.ok(authService.obtenerPerfil(id));
  }

  @PutMapping("/api/auth/perfil/{id}")
  public ResponseEntity<Map<String, String>> actualizarPerfil(
      @PathVariable UUID id, @RequestBody Map<String, String> body) {
    authService.actualizarPerfil(id, body.get("descripcion"), body.get("ubicacion"));
    return ResponseEntity.ok(Map.of("mensaje", "Perfil actualizado con éxito"));
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

