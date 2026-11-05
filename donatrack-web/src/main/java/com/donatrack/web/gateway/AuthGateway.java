package com.donatrack.web.gateway;

import com.donatrack.web.dto.TokenResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthGateway {

  private static final Logger log = LoggerFactory.getLogger(AuthGateway.class);
  private final RestClient authClient;

  public AuthGateway(@Qualifier("authClient") RestClient authClient) {
    this.authClient = authClient;
  }

  public TokenResponse login(String username, String password) {
    return authClient
        .post()
        .uri("/auth/login")
        .body(Map.of("username", username, "password", password))
        .retrieve()
        .body(TokenResponse.class);
  }

  public TokenResponse exchangeToken(String code) {
    return authClient
        .post()
        .uri("/auth/token/exchange")
        .body(Map.of("code", code))
        .retrieve()
        .body(TokenResponse.class);
  }
}

