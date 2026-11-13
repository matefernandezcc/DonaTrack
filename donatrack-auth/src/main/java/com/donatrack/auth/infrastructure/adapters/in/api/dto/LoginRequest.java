package com.donatrack.auth.infrastructure.adapters.in.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El usuario no puede estar vacío") String username,
    @NotBlank(message = "La contraseña no puede estar vacía") String password) {}
