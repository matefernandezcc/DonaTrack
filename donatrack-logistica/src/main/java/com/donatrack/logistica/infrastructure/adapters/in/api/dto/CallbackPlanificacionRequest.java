package com.donatrack.logistica.infrastructure.adapters.in.api.dto;

import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CallbackPlanificacionRequest {
    private UUID idSolicitud;
    private List<RutaDeReparto> rutas;
}
