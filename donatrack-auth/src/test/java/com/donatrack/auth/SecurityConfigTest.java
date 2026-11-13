package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.auth.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();

  @Test
  @DisplayName(
      "Sin credenciales de Google/GitHub debe retornar ClientRegistrationRepository válido con registro disabled (evita NPE en Spring Boot)")
  void testSinCredencialesRetornaRepositorioDisabled() {
    ClientRegistrationRepository repo =
        securityConfig.clientRegistrationRepository(null, null, null, null);

    assertNotNull(repo, "El bean ClientRegistrationRepository no debe ser null");
    ClientRegistration disabled = repo.findByRegistrationId("disabled");
    assertNotNull(disabled, "Debe contener el registro inerte 'disabled'");
    assertNull(repo.findByRegistrationId("google"), "No debe tener google configurado");
    assertNull(repo.findByRegistrationId("github"), "No debe tener github configurado");
  }

  @Test
  @DisplayName("Valores placeholder en credenciales no deben habilitar registros reales")
  void testPlaceholdersSonIgnorados() {
    ClientRegistrationRepository repo =
        securityConfig.clientRegistrationRepository(
            "placeholder_google_id", "placeholder_secret", "placeholder_github", "");

    assertNotNull(repo);
    assertNotNull(repo.findByRegistrationId("disabled"));
    assertNull(repo.findByRegistrationId("google"));
    assertNull(repo.findByRegistrationId("github"));
  }

  @Test
  @DisplayName(
      "Con credenciales válidas de Google debe registrar proveedor google sin el fallback disabled")
  void testConCredencialesGoogleRegistraProveedor() {
    ClientRegistrationRepository repo =
        securityConfig.clientRegistrationRepository(
            "real-client-id", "real-client-secret", null, null);

    assertNotNull(repo);
    assertNotNull(repo.findByRegistrationId("google"), "Debe tener registrado 'google'");
    assertNull(
        repo.findByRegistrationId("disabled"),
        "No debe incluir 'disabled' si hay proveedores reales");
  }

  @Test
  @DisplayName(
      "Con credenciales válidas de GitHub debe registrar proveedor github sin el fallback disabled")
  void testConCredencialesGithubRegistraProveedor() {
    ClientRegistrationRepository repo =
        securityConfig.clientRegistrationRepository(null, null, "gh-client-id", "gh-client-secret");

    assertNotNull(repo);
    assertNotNull(repo.findByRegistrationId("github"), "Debe tener registrado 'github'");
    assertNull(
        repo.findByRegistrationId("disabled"),
        "No debe incluir 'disabled' si hay proveedores reales");
  }
}
