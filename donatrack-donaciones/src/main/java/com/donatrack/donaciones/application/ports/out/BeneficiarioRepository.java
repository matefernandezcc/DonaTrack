package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioRepository {
    List<Beneficiario> buscarTodos();
    Optional<Beneficiario> buscarPorId(UUID id);
    void guardar(Beneficiario beneficiario);
    void eliminar(UUID id);
}
