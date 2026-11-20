package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.out.persistence.UsuarioRepository;
import com.donatrack.auth.infrastructure.config.AuthDataSeeder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthDataSeederTest — Pruebas Unitarias de Seeding y Restauración Canónica")
class AuthDataSeederTest {

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private AuthDataSeeder seeder;

  @BeforeEach
  void setUp() {
    seeder = new AuthDataSeeder(usuarioRepository, passwordEncoder);
  }

  @Test
  @DisplayName("Debe crear los 3 usuarios semilla si no existen en la base de datos (Arrange-Act-Assert)")
  void run_cuandoBaseDeDatosVacia_creaLosTresUsuariosCanonicos() {
    // Arrange
    when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString()))
        .thenAnswer(invocation -> "hash_" + invocation.getArgument(0));

    // Act
    seeder.run();

    // Assert
    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository, times(3)).save(captor.capture());

    List<Usuario> usuariosGuardados = captor.getAllValues();
    assertEquals(3, usuariosGuardados.size());

    Usuario admin =
        usuariosGuardados.stream()
            .filter(u -> "admin".equals(u.getUsername()))
            .findFirst()
            .orElse(null);
    assertNotNull(admin);
    assertEquals(Rol.ADMIN, admin.getRol());
    assertEquals("hash_admin", admin.getPassword());
    assertEquals("admin@donatrack.org", admin.getEmail());
    assertEquals(UUID.fromString("a2222222-2222-4222-8222-222222222222"), admin.getPersonaId());

    Usuario donante =
        usuariosGuardados.stream()
            .filter(u -> "donante".equals(u.getUsername()))
            .findFirst()
            .orElse(null);
    assertNotNull(donante);
    assertEquals(Rol.DONANTE, donante.getRol());
    assertEquals("hash_donante", donante.getPassword());
    assertEquals("maria.gonzalez@donatrack.org", donante.getEmail());
    assertEquals(UUID.fromString("a1111111-1111-4111-8111-111111111111"), donante.getPersonaId());

    Usuario beneficiaria =
        usuariosGuardados.stream()
            .filter(u -> "beneficiaria".equals(u.getUsername()))
            .findFirst()
            .orElse(null);
    assertNotNull(beneficiaria);
    assertEquals(Rol.BENEFICIARIA, beneficiaria.getRol());
    assertEquals("hash_beneficiaria", beneficiaria.getPassword());
  }

  @Test
  @DisplayName("Debe restaurar rol canónico si el usuario existente tiene un rol adulterado")
  void run_cuandoUsuarioExisteConRolCambiado_restauraRolCanonico() {
    // Arrange: donante existente con rol adulterado a ADMIN pero contraseña válida
    Usuario admin =
        Usuario.builder().username("admin").password("hash_admin").rol(Rol.ADMIN).build();
    Usuario beneficiaria =
        Usuario.builder().username("beneficiaria").password("hash_beneficiaria").rol(Rol.BENEFICIARIA).build();
    Usuario donanteModificado =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante")
            .password("hash_donante")
            .rol(Rol.ADMIN) // rol adulterado
            .personaId(UUID.fromString("a1111111-1111-4111-8111-111111111111"))
            .build();

    when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(usuarioRepository.findByUsername("beneficiaria")).thenReturn(Optional.of(beneficiaria));
    when(usuarioRepository.findByUsername("donante")).thenReturn(Optional.of(donanteModificado));

    when(passwordEncoder.matches("admin", "hash_admin")).thenReturn(true);
    when(passwordEncoder.matches("donante", "hash_donante")).thenReturn(true);
    when(passwordEncoder.matches("beneficiaria", "hash_beneficiaria")).thenReturn(true);

    // Act
    seeder.run();

    // Assert: Debe haber guardado la restauración de rol a DONANTE
    assertEquals(Rol.DONANTE, donanteModificado.getRol());
    verify(usuarioRepository).save(donanteModificado);
    verify(usuarioRepository, never()).save(admin);
    verify(usuarioRepository, never()).save(beneficiaria);
  }

  @Test
  @DisplayName("Debe restaurar contraseña canónica si el usuario existente tiene contraseña desactualizada")
  void run_cuandoUsuarioExisteConPasswordModificada_restauraPasswordCanonica() {
    // Arrange: donante existente con rol correcto pero contraseña desactualizada/desconocida
    Usuario admin =
        Usuario.builder().username("admin").password("hash_admin").rol(Rol.ADMIN).build();
    Usuario beneficiaria =
        Usuario.builder().username("beneficiaria").password("hash_beneficiaria").rol(Rol.BENEFICIARIA).build();
    Usuario donanteConPasswordErronea =
        Usuario.builder()
            .id(UUID.randomUUID())
            .username("donante")
            .password("antiguo_hash_invalido")
            .rol(Rol.DONANTE)
            .personaId(UUID.fromString("a1111111-1111-4111-8111-111111111111"))
            .build();

    when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(usuarioRepository.findByUsername("beneficiaria")).thenReturn(Optional.of(beneficiaria));
    when(usuarioRepository.findByUsername("donante"))
        .thenReturn(Optional.of(donanteConPasswordErronea));

    when(passwordEncoder.matches("admin", "hash_admin")).thenReturn(true);
    when(passwordEncoder.matches("donante", "antiguo_hash_invalido")).thenReturn(false);
    when(passwordEncoder.matches("beneficiaria", "hash_beneficiaria")).thenReturn(true);

    when(passwordEncoder.encode("donante")).thenReturn("nuevo_hash_canonico");

    // Act
    seeder.run();

    // Assert
    assertEquals("nuevo_hash_canonico", donanteConPasswordErronea.getPassword());
    verify(usuarioRepository).save(donanteConPasswordErronea);
    verify(usuarioRepository, never()).save(admin);
    verify(usuarioRepository, never()).save(beneficiaria);
  }

  @Test
  @DisplayName("No debe guardar en BD si el usuario existente ya posee rol y contraseña canónicas")
  void run_cuandoUsuarioYaEsCanonico_noRealizaGuardadoInnecesario() {
    // Arrange: Todos los usuarios existen con estado canónico exacto
    Usuario admin =
        Usuario.builder()
            .username("admin")
            .password("hash_admin")
            .rol(Rol.ADMIN)
            .build();
    Usuario donante =
        Usuario.builder()
            .username("donante")
            .password("hash_donante")
            .rol(Rol.DONANTE)
            .build();
    Usuario beneficiaria =
        Usuario.builder()
            .username("beneficiaria")
            .password("hash_beneficiaria")
            .rol(Rol.BENEFICIARIA)
            .build();

    when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(usuarioRepository.findByUsername("donante")).thenReturn(Optional.of(donante));
    when(usuarioRepository.findByUsername("beneficiaria")).thenReturn(Optional.of(beneficiaria));

    when(passwordEncoder.matches("admin", "hash_admin")).thenReturn(true);
    when(passwordEncoder.matches("donante", "hash_donante")).thenReturn(true);
    when(passwordEncoder.matches("beneficiaria", "hash_beneficiaria")).thenReturn(true);

    // Act
    seeder.run();

    // Assert: Ningún save debe ser ejecutado porque no hubo mutaciones
    verify(usuarioRepository, never()).save(any(Usuario.class));
  }
}
