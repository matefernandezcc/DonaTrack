package com.donatrack.web.dto;

public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserInfoDto user
) {}

