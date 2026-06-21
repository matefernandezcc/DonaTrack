package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.domain.entities.donacion.RecepcionDonacion;
import com.donatrack.donaciones.application.ports.out.RecepcionDonacionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockRecepcionDonacionRepository implements RecepcionDonacionRepository {

    private final List<RecepcionDonacion> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(RecepcionDonacion recepcion) {
        baseDeDatosMock.removeIf(r -> r.getId().equals(recepcion.getId()));
        baseDeDatosMock.add(recepcion);
    }

    @Override
    public Optional<RecepcionDonacion> buscarPorId(UUID id) {
        return baseDeDatosMock.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }
}
