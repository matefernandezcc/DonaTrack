package com.donatrack.donaciones.application.ports.out;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;

import java.util.List;

public interface BeneficiarioRepository {
    List<Beneficiario> buscarTodos();
}
