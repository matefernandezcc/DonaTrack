package com.donatrack.auth.infrastructure.adapters.in.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String username,

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    String password,

    @NotBlank(message = "El nombre es requerido")
    String nombre,

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    String email,

    String rol
) {}

