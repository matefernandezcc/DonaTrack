package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.SolicitudPlanificacionRepository;
import com.donatrack.logistica.domain.entities.SolicitudPlanificacion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockSolicitudPlanificacionRepository implements SolicitudPlanificacionRepository {

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

    @Override
    public List<SolicitudPlanificacion> buscarTodas() {
        return new ArrayList<>(baseDeDatosMock);
    }
}
