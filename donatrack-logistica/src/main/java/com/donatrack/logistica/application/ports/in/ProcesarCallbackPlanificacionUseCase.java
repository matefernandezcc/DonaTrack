package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.reparto.RutaDeReparto;
import java.util.List;
import java.util.UUID;

public interface ProcesarCallbackPlanificacionUseCase {
    void procesarCallback(UUID idSolicitud, List<RutaDeReparto> rutas);
}
