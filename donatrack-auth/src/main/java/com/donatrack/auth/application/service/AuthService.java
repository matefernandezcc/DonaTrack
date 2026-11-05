package com.donatrack.auth.application.service;

import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.UserInfoDto;
import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.donatrack.auth.infrastructure.adapters.out.persistence.UsuarioRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  // Cache en memoria para codigos de autorizacion de un solo uso (SSO)
  // key: code, value: (Usuario, expiraInstant)
  private final Map<String, CodeEntry> exchangeCodes = new ConcurrentHashMap<>();

  private record CodeEntry(Usuario usuario, Instant expiresAt) {}

  public AuthService(
      UsuarioRepository usuarioRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  public TokenResponse login(LoginRequest request) {
    Usuario usuario =
        usuarioRepository
            .findByUsername(request.username().trim().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

    if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
      throw new IllegalArgumentException("Credenciales inválidas");
    }

    return construirTokenResponse(usuario);
  }

  public TokenResponse registrarUsuario(com.donatrack.auth.infrastructure.adapters.in.api.dto.RegisterRequest request) {
    String username = request.username().trim().toLowerCase();
    String email = request.email().trim().toLowerCase();

    if (usuarioRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("El nombre de usuario '" + username + "' ya está registrado");
    }
    if (usuarioRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("El correo '" + email + "' ya está registrado");
    }

    com.donatrack.auth.domain.entities.Rol rol = com.donatrack.auth.domain.entities.Rol.DONANTE;
    if (request.rol() != null && !request.rol().isBlank()) {
      try {
        rol = com.donatrack.auth.domain.entities.Rol.valueOf(request.rol().trim().toUpperCase());
      } catch (IllegalArgumentException ignored) {}
    }

    Usuario nuevo =
        Usuario.builder()
            .username(username)
            .password(passwordEncoder.encode(request.password()))
            .nombre(request.nombre().trim())
            .email(email)
            .rol(rol)
            .personaId(UUID.randomUUID())
            .proveedorOAuth("LOCAL")
            .build();

    Usuario guardado = usuarioRepository.save(nuevo);
    return construirTokenResponse(guardado);
  }

  public java.util.List<Map<String, Object>> listarUsuarios() {
    return usuarioRepository.findAll().stream()
        .map(u -> {
          Map<String, Object> map = new java.util.LinkedHashMap<>();
          map.put("id", u.getId() != null ? u.getId().toString() : "");
          map.put("username", u.getUsername());
          map.put("nombre", u.getNombre());
          map.put("email", u.getEmail());
          map.put("rol", u.getRol().name());
          map.put("personaId", u.getPersonaId() != null ? u.getPersonaId().toString() : "");
          map.put("proveedorOAuth", u.getProveedorOAuth());
          map.put("fechaCreacion", u.getFechaCreacion() != null ? u.getFechaCreacion().toString() : "");
          return map;
        })
        .collect(java.util.stream.Collectors.toList());
  }

  public void cambiarRolUsuario(UUID usuarioId, String nuevoRolStr) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
    com.donatrack.auth.domain.entities.Rol nuevoRol =
        com.donatrack.auth.domain.entities.Rol.valueOf(nuevoRolStr.trim().toUpperCase());
    usuario.setRol(nuevoRol);
    usuarioRepository.save(usuario);
  }

  public String generarCodigoIntercambio(Usuario usuario) {
    String code = UUID.randomUUID().toString();
    exchangeCodes.put(code, new CodeEntry(usuario, Instant.now().plusSeconds(60)));
    return code;
  }

  public TokenResponse exchangeCode(String code) {
    CodeEntry entry = exchangeCodes.remove(code);
    if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Código de intercambio inválido o expirado");
    }
    return construirTokenResponse(entry.usuario());
  }

  public Usuario obtenerOCrearUsuarioSocial(String email, String nombre, String proveedor) {
    return usuarioRepository
        .findByEmail(email)
        .orElseGet(
            () -> {
              String usernameBase = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
              String username = usernameBase;
              int suffix = 1;
              while (usuarioRepository.existsByUsername(username)) {
                username = usernameBase + suffix++;
              }
              Usuario nuevo =
                  Usuario.builder()
                      .username(username)
                      .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                      .nombre(nombre)
                      .email(email)
                      .rol(com.donatrack.auth.domain.entities.Rol.DONANTE)
                      .proveedorOAuth(proveedor)
                      .personaId(UUID.randomUUID())
                      .build();
              return usuarioRepository.save(nuevo);
            });
  }

  public UserInfoDto construirUserInfoDto(Usuario usuario) {
    return new UserInfoDto(
        usuario.getId(),
        usuario.getUsername(),
        usuario.getNombre(),
        usuario.getEmail(),
        usuario.getRol().name(),
        usuario.getPersonaId());
  }

  public TokenResponse construirTokenResponse(Usuario usuario) {
    if (usuario.getPersonaId() == null) {
      usuario.setPersonaId(UUID.randomUUID());
      usuario = usuarioRepository.save(usuario);
    }
    String token = jwtService.generarToken(usuario);
    return new TokenResponse(
        token,
        jwtService.getExpiresInSeconds(),
        construirUserInfoDto(usuario));
  }
}

