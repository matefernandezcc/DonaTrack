package com.donatrack.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.auth.domain.entities.Rol;
import com.donatrack.auth.domain.entities.Usuario;
import com.donatrack.auth.infrastructure.adapters.out.jwt.JwtService;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;
  private final String secret = "SuperSecretoDonaTrack2026ParaFirmaHS256DebeTenerAlMenos32Bytes!";

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(secret, 60);
  }

  @Test
  @DisplayName("Debe generar un JWT HS256 válido con claims de usuario y roles")
  void testGenerarYValidarToken() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID personaId = UUID.randomUUID();

    Usuario usuario =
        Usuario.builder()
            .id(userId)
            .username("admin")
            .nombre("Admin General")
            .email("admin@donatrack.org")
            .rol(Rol.ADMIN)
            .personaId(personaId)
            .build();

    String token = jwtService.generarToken(usuario);
    assertNotNull(token);
    assertFalse(token.isBlank());

    assertTrue(jwtService.validarToken(token));

    JWTClaimsSet claims = jwtService.obtenerClaims(token);
    assertEquals("donatrack-auth", claims.getIssuer());
    assertEquals(userId.toString(), claims.getSubject());
    assertEquals("admin", claims.getStringClaim("username"));
    assertEquals(personaId.toString(), claims.getStringClaim("personaId"));

    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.getClaim("roles");
    assertEquals(1, roles.size());
    assertEquals("ADMIN", roles.get(0));
  }

  @Test
  @DisplayName("Debe rechazar un token manipulado o con firma inválida")
  void testTokenInvalido() {
    assertFalse(jwtService.validarToken("token.invalido.falso"));
  }
}

