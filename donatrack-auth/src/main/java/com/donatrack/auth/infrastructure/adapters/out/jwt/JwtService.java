package com.donatrack.auth.infrastructure.adapters.out.jwt;

import com.donatrack.auth.domain.entities.Usuario;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final byte[] secretKey;
  private final long ttlMinutes;

  public JwtService(
      @Value("${donatrack.jwt-secret}") String secret,
      @Value("${donatrack.jwt-ttl-minutes:120}") long ttlMinutes) {
    this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    this.ttlMinutes = ttlMinutes;
  }

  public String generarToken(Usuario usuario) {
    try {
      Instant now = Instant.now();
      Instant expiration = now.plus(ttlMinutes, ChronoUnit.MINUTES);

      JWTClaimsSet.Builder claimsBuilder =
          new JWTClaimsSet.Builder()
              .issuer("donatrack-auth")
              .subject(usuario.getId().toString())
              .issueTime(Date.from(now))
              .expirationTime(Date.from(expiration))
              .claim("username", usuario.getUsername())
              .claim("name", usuario.getNombre())
              .claim("email", usuario.getEmail())
              .claim("roles", List.of(usuario.getRol().name()));

      if (usuario.getPersonaId() != null) {
        claimsBuilder.claim("personaId", usuario.getPersonaId().toString());
      }
      if (usuario.getDescripcion() != null) {
        claimsBuilder.claim("descripcion", usuario.getDescripcion());
      }
      if (usuario.getUbicacion() != null) {
        claimsBuilder.claim("ubicacion", usuario.getUbicacion());
      }

      SignedJWT signedJWT =
          new SignedJWT(
              new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
              claimsBuilder.build());

      JWSSigner signer = new MACSigner(secretKey);
      signedJWT.sign(signer);

      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Error al generar JWT para usuario: " + usuario.getUsername(), e);
    }
  }

  public boolean validarToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(secretKey);
      if (!signedJWT.verify(verifier)) {
        return false;
      }
      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expirationTime != null && expirationTime.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public JWTClaimsSet obtenerClaims(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet();
    } catch (Exception e) {
      throw new RuntimeException("Token inválido o malformado", e);
    }
  }

  public long getExpiresInSeconds() {
    return ttlMinutes * 60;
  }
}

