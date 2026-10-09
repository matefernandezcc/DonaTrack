package com.donatrack.notificaciones.infrastructure.adapters.out.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud de envío de notificación")
public record NotificacionRequest(
    @Schema(
            description =
                "Dirección o identificador del destinatario (email, teléfono o usuario de Discord según el medio)",
            example = "donante@donatrack.org",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String destinatario,
    @Schema(
            description = "Contenido del mensaje a enviar",
            example = "Tu donación fue entregada con éxito",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String mensaje,
    @Schema(
            description = "Medio de envío: EMAIL, SMS, WHATSAPP o DISCORD",
            example = "DISCORD",
            allowableValues = {"EMAIL", "SMS", "WHATSAPP", "DISCORD"},
            requiredMode = Schema.RequiredMode.REQUIRED)
        String medio) {}
