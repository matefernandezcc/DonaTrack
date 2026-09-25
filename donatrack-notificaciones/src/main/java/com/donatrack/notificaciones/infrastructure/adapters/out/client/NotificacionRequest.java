package com.donatrack.notificaciones.infrastructure.adapters.out.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud de envío de notificación")
public record NotificacionRequest(
    @Schema(
            description = "Dirección del destinatario (email, teléfono o WhatsApp según el medio)",
            example = "donante@donatrack.org",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String destinatario,
    @Schema(
            description = "Contenido del mensaje a enviar",
            example = "Tu donación fue entregada con éxito",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String mensaje,
    @Schema(
            description = "Medio de envío: EMAIL, SMS o WHATSAPP",
            example = "EMAIL",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String medio) {}
