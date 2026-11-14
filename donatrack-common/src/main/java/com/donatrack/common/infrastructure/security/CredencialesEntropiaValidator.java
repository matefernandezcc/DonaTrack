package com.donatrack.common.infrastructure.security;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CredencialesEntropiaValidator {

  private static final Logger log = LoggerFactory.getLogger(CredencialesEntropiaValidator.class);

  public static final String DEFAULT_INTERNAL_KEY = "donatrack-internal-key-change-in-prod";
  public static final String DEFAULT_JWT_SECRET =
      "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!";

  @Autowired private Environment environment;

  @Value(
      "${donatrack.internal-api-key:${INTERNAL_API_KEY:donatrack-internal-key-change-in-prod}}")
  private String internalApiKey;

  @Value(
      "${donatrack.jwt-secret:${JWT_SECRET:SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!}}")
  private String jwtSecret;

  public CredencialesEntropiaValidator() {}

  public CredencialesEntropiaValidator(Environment environment, String internalApiKey, String jwtSecret) {
    this.environment = environment;
    this.internalApiKey = internalApiKey;
    this.jwtSecret = jwtSecret;
  }

  @PostConstruct
  public void validarCredenciales() {
    boolean esProd =
        environment != null && Arrays.asList(environment.getActiveProfiles()).contains("prod");

    if (esProd) {
      if (internalApiKey == null
          || internalApiKey.length() < 32
          || DEFAULT_INTERNAL_KEY.equals(internalApiKey)) {
        throw new IllegalStateException(
            "CRÍTICO: INTERNAL_API_KEY insegura en producción. Debe poseer al menos 32 caracteres y diferir del valor por defecto.");
      }

      if (jwtSecret == null
          || jwtSecret.length() < 32
          || DEFAULT_JWT_SECRET.equals(jwtSecret)) {
        throw new IllegalStateException(
            "CRÍTICO: JWT_SECRET insegura en producción. Debe poseer al menos 32 caracteres y diferir del valor por defecto.");
      }
    } else {
      if (DEFAULT_INTERNAL_KEY.equals(internalApiKey)) {
        log.warn(
            "ADVERTENCIA DE SEGURIDAD: INTERNAL_API_KEY utiliza la clave por defecto. Válido únicamente en entorno local/test.");
      }
      if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
        log.warn(
            "ADVERTENCIA DE SEGURIDAD: JWT_SECRET utiliza la clave por defecto. Válido únicamente en entorno local/test.");
      }
    }
  }
}
