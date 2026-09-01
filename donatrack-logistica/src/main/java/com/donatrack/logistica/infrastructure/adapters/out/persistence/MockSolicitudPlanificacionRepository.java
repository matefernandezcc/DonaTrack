package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.planificacion.SolicitudPlanificacion;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//@Repository
public class MockSolicitudPlanificacionRepository implements SolicitudPlanificacionRepositoryPort {

    private final List<SolicitudPlanificacion> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(SolicitudPlanificacion solicitud) {
        baseDeDatosMock.removeIf(s -> s.getId().equals(solicitud.getId()));
        baseDeDatosMock.add(solicitud);
    }

    @Override
    public Optional<SolicitudPlanificacion> buscarPorId(UUID id) {
        return baseDeDatosMock.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }
}
