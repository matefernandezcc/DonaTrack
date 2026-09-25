package com.donatrack.donaciones.application.ports.in;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Solicitud de recepción de bienes brutos donados")
public record CargaBienesRequestDTO(
    @Schema(
            description = "ID de la persona con rol Donante",
            example = "a1111111-1111-4111-8111-111111111111",
            requiredMode = Schema.RequiredMode.REQUIRED)
        UUID idDonante,
    @Schema(
            description = "ID de la persona administradora que recibe los bienes",
            example = "a2222222-2222-4222-8222-222222222222",
            requiredMode = Schema.RequiredMode.REQUIRED)
        UUID idAdministrador,
    @Schema(
            description = "Lista de bienes a registrar (mínimo 1)",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<BienDTO> bienesBrutos) {}
