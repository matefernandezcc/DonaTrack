package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.domain.repository.BeneficiarioRepository;

import com.donatrack.donaciones.domain.entities.roles.Beneficiario;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MockBeneficiarioRepository implements BeneficiarioRepository {

    private final List<Beneficiario> baseDeDatosMock = new ArrayList<>();

    @Override
    public List<Beneficiario> buscarTodos() {
        return new ArrayList<>(baseDeDatosMock);
    }
}
