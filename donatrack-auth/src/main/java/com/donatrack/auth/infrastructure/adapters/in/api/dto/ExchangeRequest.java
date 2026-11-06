package com.donatrack.auth.infrastructure.adapters.in.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeRequest(@NotBlank(message = "El código no puede estar vacío") String code) {}
