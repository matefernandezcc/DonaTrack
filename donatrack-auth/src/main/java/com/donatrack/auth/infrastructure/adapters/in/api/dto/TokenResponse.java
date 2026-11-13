package com.donatrack.auth.infrastructure.adapters.in.api.dto;

public record TokenResponse(
    String accessToken, String tokenType, long expiresIn, UserInfoDto user) {
  public TokenResponse(String accessToken, long expiresIn, UserInfoDto user) {
    this(accessToken, "Bearer", expiresIn, user);
  }
}
