package com.donatrack.web.dto;

import java.util.UUID;

public record UserInfoDto(
    UUID id,
    String username,
    String nombre,
    String email,
    String rol,
    UUID personaId
) {}

