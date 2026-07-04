package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MockDonacionOriginalRepository implements DonacionOriginalRepository {

    private final List<DonacionOriginal> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(DonacionOriginal donacionOriginal) {
        baseDeDatosMock.removeIf(r -> r.getId().equals(donacionOriginal.getId()));
        baseDeDatosMock.add(donacionOriginal);
    }

    @Override
    public Optional<DonacionOriginal> buscarPorId(UUID id) {
        return baseDeDatosMock.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<DonacionOriginal> buscarPorIdDonacion(UUID idDonacion) {
        return baseDeDatosMock.stream()
                .filter(r -> r.getDonacionesSegmentadas() != null && 
                             r.getDonacionesSegmentadas().stream().anyMatch(d -> d.getId().equals(idDonacion)))
                .findFirst();
    }
}
