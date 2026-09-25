package com.donatrack.donaciones.application.ports.in;

import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Solicitud de cambio de estado de una donación")
public record CambioEstadoRequestDTO(
    @Schema(
            description =
                "Nuevo estado de la donación. Valores: EN_DEPOSITO, ASIGNADA, LISTA_PARA_ENTREGAR, EN_TRASLADO, ENTREGADA, ENTREGA_FALLIDA, VENCIDA",
            example = "EN_TRASLADO",
            requiredMode = Schema.RequiredMode.REQUIRED)
        EstadoDonacion nuevoEstado,
    @Schema(
            description = "Observación del cambio de estado",
            example = "Cambio de estado manual por administrador")
        String observacion,
    @Schema(description = "ID del usuario que realiza el cambio (opcional)", nullable = true)
        UUID idUsuario) {}
