package com.donatrack.auth.infrastructure.config;

import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.out.persistence.UsuarioRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "dev", "test"})
public class AuthDataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AuthDataSeeder.class);

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthDataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    seedUsuario(
        "admin",
        "admin",
        "Administrador General",
        "admin@donatrack.org",
        Rol.ADMIN,
        UUID.fromString("a2222222-2222-4222-8222-222222222222"));

    seedUsuario(
        "donante",
        "donante",
        "María González",
        "maria.gonzalez@donatrack.org",
        Rol.DONANTE,
        UUID.fromString("a1111111-1111-4111-8111-111111111111"));

    seedUsuario(
        "beneficiaria",
        "beneficiaria",
        "Fundación Esperanza",
        "contacto@fundacionesperanza.org",
        Rol.BENEFICIARIA,
        UUID.fromString("b2222222-2222-4222-8222-222222222222"));
  }

  private void seedUsuario(
      String username, String rawPassword, String nombre, String email, Rol rol, UUID personaId) {
    java.util.Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
    if (usuarioOpt.isEmpty()) {
      Usuario usuario =
          Usuario.builder()
              .username(username)
              .password(passwordEncoder.encode(rawPassword))
              .nombre(nombre)
              .email(email)
              .rol(rol)
              .personaId(personaId)
              .proveedorOAuth("LOCAL")
              .build();
      usuarioRepository.save(usuario);
      log.info("Usuario seed creado: {} (Rol: {})", username, rol);
    } else {
      Usuario usuario = usuarioOpt.get();
      if (usuario.getRol() != rol) {
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
        log.info("Usuario seed restaurado a rol canónico: {} (Rol: {})", username, rol);
      }
    }
  }
}
