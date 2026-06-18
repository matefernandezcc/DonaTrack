package com.donatrack.donaciones.domain.repository;

import com.donatrack.donaciones.domain.model.roles.Beneficiario;

import java.util.List;

public interface BeneficiarioRepository {
    List<Beneficiario> buscarTodos();
}
