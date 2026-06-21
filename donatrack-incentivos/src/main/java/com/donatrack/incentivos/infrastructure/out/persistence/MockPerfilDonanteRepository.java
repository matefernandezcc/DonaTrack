package com.donatrack.incentivos.infrastructure.out.persistence;

import com.donatrack.incentivos.domain.repository.PerfilDonanteRepository;

import com.donatrack.incentivos.domain.entities.PerfilDonante;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MockPerfilDonanteRepository implements PerfilDonanteRepository {

    private final ConcurrentHashMap<UUID, PerfilDonante> data = new ConcurrentHashMap<>();

    @Override
    public Optional<PerfilDonante> findById(UUID donanteId) {
        return Optional.ofNullable(data.get(donanteId));
    }

    @Override
    public void save(PerfilDonante perfil) {
        data.put(perfil.getDonanteId(), perfil);
    }

    @Override
    public List<PerfilDonante> findAll() {
        return new ArrayList<>(data.values());
    }
}
