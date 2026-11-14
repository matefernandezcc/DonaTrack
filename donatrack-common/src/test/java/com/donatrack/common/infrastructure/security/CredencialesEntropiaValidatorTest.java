package com.donatrack.common.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class CredencialesEntropiaValidatorTest {

  private static final String SECURE_INTERNAL_KEY =
      "clave-interna-segura-prod-con-al-menos-32-caracteres-ok";
  private static final String SECURE_JWT_SECRET =
      "clave-jwt-segura-prod-con-al-menos-32-caracteres-ok-12345678";

  @Test
  @DisplayName("Prod: Claves seguras no lanzan excepción")
  void testProdClavesSegurasPasan() {
    Environment env = mock(Environment.class);
    when(env.getActiveProfiles()).thenReturn(new String[] {"prod"});

    CredencialesEntropiaValidator validator =
        new CredencialesEntropiaValidator(env, SECURE_INTERNAL_KEY, SECURE_JWT_SECRET);

    assertDoesNotThrow(validator::validarCredenciales);
  }

  @Test
  @DisplayName("Prod: Internal API key por defecto lanza IllegalStateException")
  void testProdInternalKeyDefaultFalla() {
    Environment env = mock(Environment.class);
    when(env.getActiveProfiles()).thenReturn(new String[] {"prod"});

    CredencialesEntropiaValidator validator =
        new CredencialesEntropiaValidator(
            env, CredencialesEntropiaValidator.DEFAULT_INTERNAL_KEY, SECURE_JWT_SECRET);

    assertThrows(IllegalStateException.class, validator::validarCredenciales);
  }

  @Test
  @DisplayName("Prod: JWT Secret por defecto lanza IllegalStateException")
  void testProdJwtSecretDefaultFalla() {
    Environment env = mock(Environment.class);
    when(env.getActiveProfiles()).thenReturn(new String[] {"prod"});

    CredencialesEntropiaValidator validator =
        new CredencialesEntropiaValidator(
            env, SECURE_INTERNAL_KEY, CredencialesEntropiaValidator.DEFAULT_JWT_SECRET);

    assertThrows(IllegalStateException.class, validator::validarCredenciales);
  }

  @Test
  @DisplayName("Prod: Clave con longitud menor a 32 caracteres lanza IllegalStateException")
  void testProdClaveCortaFalla() {
    Environment env = mock(Environment.class);
    when(env.getActiveProfiles()).thenReturn(new String[] {"prod"});

    CredencialesEntropiaValidator validator =
        new CredencialesEntropiaValidator(env, "clave-corta", SECURE_JWT_SECRET);

    assertThrows(IllegalStateException.class, validator::validarCredenciales);
  }

  @Test
  @DisplayName("Local / Dev / Test: Claves por defecto son permitidas")
  void testLocalClavesPorDefectoPermitidas() {
    Environment env = mock(Environment.class);
    when(env.getActiveProfiles()).thenReturn(new String[] {"local", "dev"});

    CredencialesEntropiaValidator validator =
        new CredencialesEntropiaValidator(
            env,
            CredencialesEntropiaValidator.DEFAULT_INTERNAL_KEY,
            CredencialesEntropiaValidator.DEFAULT_JWT_SECRET);

    assertDoesNotThrow(validator::validarCredenciales);
  }
}
