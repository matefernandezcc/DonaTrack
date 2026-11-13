package com.donatrack.notificaciones.infrastructure.adapters.in.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacionResponseDTO(
    UUID id,
    String destinatario,
    String mensaje,
    String medio,
    LocalDateTime fechaEnvio,
    boolean completada) {}
