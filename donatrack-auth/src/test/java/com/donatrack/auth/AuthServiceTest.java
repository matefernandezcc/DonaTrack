package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.auth.application.service.AuthService;
import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.LoginRequest;
import com.donatrack.auth.infrastructure.adapters.in.api.dto.TokenResponse;
import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.donatrack.auth.infrastructure.adapters.out.persistence.UsuarioRepository;
import java.util.*;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

  private FakeUsuarioRepository usuarioRepository;
  private JwtService jwtService;
  private PasswordEncoder passwordEncoder;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    usuarioRepository = new FakeUsuarioRepository();
    jwtService = new JwtService("SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!", 60);
    passwordEncoder = new BCryptPasswordEncoder();
    authService = new AuthService(usuarioRepository, jwtService, passwordEncoder);
  }

  @Test
  @DisplayName("Debe autenticar correctamente con credenciales válidas")
  void testLoginExitoso() {
    Usuario usuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante")
            .password(passwordEncoder.encode("donante123"))
            .nombre("María")
            .email("maria@donatrack.org")
            .rol(Rol.DONANTE)
            .personaId(UUID.randomUUID())
            .build();

    usuarioRepository.save(usuario);

    TokenResponse response = authService.login(new LoginRequest("donante", "donante123"));

    assertNotNull(response);
    assertNotNull(response.accessToken());
    assertEquals("donante", response.user().username());
    assertEquals("DONANTE", response.user().rol());
  }

  @Test
  @DisplayName("Debe fallar al intentar loguearse con contraseña incorrecta")
  void testLoginContrasenaIncorrecta() {
    Usuario usuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante")
            .password(passwordEncoder.encode("correcta"))
            .nombre("María")
            .email("maria@donatrack.org")
            .rol(Rol.DONANTE)
            .build();

    usuarioRepository.save(usuario);

    assertThrows(
        IllegalArgumentException.class,
        () -> authService.login(new LoginRequest("donante", "incorrecta")));
  }

  @Test
  @DisplayName("Debe generar código de intercambio y resolverlo exitosamente una única vez")
  void testExchangeCodeSSO() {
    Usuario usuario =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("sso_user")
            .password("none")
            .nombre("SSO User")
            .email("sso@gmail.com")
            .rol(Rol.DONANTE)
            .build();

    String code = authService.generarCodigoIntercambio(usuario);
    assertNotNull(code);

    TokenResponse response = authService.exchangeCode(code);
    assertNotNull(response);
    assertEquals("sso_user", response.user().username());

    // Una segunda vez debe fallar porque el código se consume (de un solo uso)
    assertThrows(IllegalArgumentException.class, () -> authService.exchangeCode(code));
  }

  // Fake in-memory repository to guarantee Java 21-25 agent-agnostic tests
  static class FakeUsuarioRepository implements UsuarioRepository {
    private final Map<UUID, Usuario> db = new HashMap<>();

    @Override
    public Optional<Usuario> findByUsername(String username) {
      return db.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
      return db.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
      return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
      return findByEmail(email).isPresent();
    }

    @Override
    public <S extends Usuario> S save(S entity) {
      if (entity.getId() == null) {
        entity.setId(UUID.randomUUID());
      }
      db.put(entity.getId(), entity);
      return entity;
    }

    @Override public Optional<Usuario> findById(UUID id) { return Optional.ofNullable(db.get(id)); }
    @Override public boolean existsById(UUID id) { return db.containsKey(id); }
    @Override public List<Usuario> findAll() { return new ArrayList<>(db.values()); }
    @Override public List<Usuario> findAllById(Iterable<UUID> ids) { return Collections.emptyList(); }
    @Override public long count() { return db.size(); }
    @Override public void deleteById(UUID id) { db.remove(id); }
    @Override public void delete(Usuario entity) { db.remove(entity.getId()); }
    @Override public void deleteAllById(Iterable<? extends UUID> ids) {}
    @Override public void deleteAll(Iterable<? extends Usuario> entities) {}
    @Override public void deleteAll() { db.clear(); }
    @Override public void flush() {}
    @Override public <S extends Usuario> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends Usuario> List<S> saveAllAndFlush(Iterable<S> entities) { return Collections.emptyList(); }
    @Override public void deleteAllInBatch(Iterable<Usuario> entities) {}
    @Override public void deleteAllByIdInBatch(Iterable<UUID> ids) {}
    @Override public void deleteAllInBatch() {}
    @Override public Usuario getOne(UUID id) { return db.get(id); }
    @Override public Usuario getById(UUID id) { return db.get(id); }
    @Override public Usuario getReferenceById(UUID id) { return db.get(id); }
    @Override public <S extends Usuario> List<S> findAll(Example<S> example) { return Collections.emptyList(); }
    @Override public <S extends Usuario> List<S> findAll(Example<S> example, Sort sort) { return Collections.emptyList(); }
    @Override public <S extends Usuario> List<S> saveAll(Iterable<S> entities) { return Collections.emptyList(); }
    @Override public List<Usuario> findAll(Sort sort) { return Collections.emptyList(); }
    @Override public Page<Usuario> findAll(Pageable pageable) { return Page.empty(); }
    @Override public <S extends Usuario> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override public <S extends Usuario> Page<S> findAll(Example<S> example, Pageable pageable) { return Page.empty(); }
    @Override public <S extends Usuario> long count(Example<S> example) { return 0; }
    @Override public <S extends Usuario> boolean exists(Example<S> example) { return false; }
    @Override public <S extends Usuario, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
  }
}
