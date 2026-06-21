package com.donatrack.incentivos.domain.repository;

import com.donatrack.incentivos.domain.entities.PerfilDonante;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilDonanteRepository {
    Optional<PerfilDonante> findById(UUID donanteId);
    void save(PerfilDonante perfil);
    List<PerfilDonante> findAll();
}
