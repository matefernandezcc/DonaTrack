package com.donatrack.auth.infrastructure.adapters.in.api;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.ExchangeRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import com.donatrack.common.infrastructure.security.JwtClaimsExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthController {

  private final AuthService authService;
  private final String webUrl;
  private final String jwtSecret;
  private final String internalApiKey;

  public AuthController(AuthService authService) {
    this(
        authService,
        "http://localhost:8080",
        "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!",
        "donatrack-internal-key-change-in-prod");
  }

  public AuthController(
      AuthService authService,
      @Value("${donatrack.web-url:http://localhost:8080}") String webUrl) {
    this(
        authService,
        webUrl,
        "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!",
        "donatrack-internal-key-change-in-prod");
  }

  public AuthController(
      AuthService authService,
      @Value("${donatrack.web-url:http://localhost:8080}") String webUrl,
      @Value(
          "${donatrack.jwt-secret:${JWT_SECRET:SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!}}")
          String jwtSecret,
      @Value(
          "${donatrack.internal-api-key:${INTERNAL_API_KEY:donatrack-internal-key-change-in-prod}}")
          String internalApiKey) {
    this.authService = authService;
    this.webUrl = webUrl;
    this.jwtSecret = jwtSecret;
    this.internalApiKey = internalApiKey;
  }

  @PostMapping("/auth/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/auth/register")
  public ResponseEntity<TokenResponse> register(
      @Valid @RequestBody
          com.donatrack.auth.infrastructure.adapters.in.api.dto.RegisterRequest request) {
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
    return ResponseEntity.ok(
        Map.of("mensaje", "Contraseña restablecida exitosamente a 'password123'"));
  }

  @GetMapping("/api/auth/perfil/{id}")
  public ResponseEntity<Map<String, Object>> obtenerPerfil(
      @PathVariable UUID id, HttpServletRequest request) {
    boolean esAdminOInternal =
        JwtClaimsExtractor.esAdminOInternalService(request, internalApiKey);
    UUID callerUserId = JwtClaimsExtractor.extraerUserId(request, jwtSecret).orElse(null);
    UUID callerPersonaId = JwtClaimsExtractor.extraerPersonaId(request, jwtSecret).orElse(null);

    boolean esDueno = id != null && (id.equals(callerUserId) || id.equals(callerPersonaId));

    if (!esAdminOInternal && !esDueno) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(authService.obtenerPerfil(id));
  }

  @PutMapping("/api/auth/perfil/{id}")
  public ResponseEntity<Map<String, String>> actualizarPerfil(
      @PathVariable UUID id,
      @RequestBody Map<String, String> body,
      HttpServletRequest request) {
    boolean esAdminOInternal =
        JwtClaimsExtractor.esAdminOInternalService(request, internalApiKey);
    UUID callerUserId = JwtClaimsExtractor.extraerUserId(request, jwtSecret).orElse(null);
    UUID callerPersonaId = JwtClaimsExtractor.extraerPersonaId(request, jwtSecret).orElse(null);

    boolean esDueno = id != null && (id.equals(callerUserId) || id.equals(callerPersonaId));

    if (!esAdminOInternal && !esDueno) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
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
