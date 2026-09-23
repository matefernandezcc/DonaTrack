package com.donatrack.logistica.infrastructure.adapters.in.api.dto;

import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Payload recibido desde el servicio optimizador externo de rutas")
public class CallbackPlanificacionRequest {
    @Schema(description = "Identificador de la solicitud de planificación", example = "31008064-071a-4d7a-ac37-33318f7d9842")
    private UUID idSolicitud;

    @Schema(description = "Listado de rutas optimizadas calculadas")
    private List<RutaDeReparto> rutas;
}
