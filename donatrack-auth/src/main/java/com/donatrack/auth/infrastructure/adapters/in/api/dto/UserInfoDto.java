package com.donatrack.auth.infrastructure.adapters.in.api.dto;

import java.util.UUID;

public record UserInfoDto(
    UUID id,
    String username,
    String nombre,
    String email,
    String rol,
    UUID personaId,
    String descripcion,
    String ubicacion) {}
