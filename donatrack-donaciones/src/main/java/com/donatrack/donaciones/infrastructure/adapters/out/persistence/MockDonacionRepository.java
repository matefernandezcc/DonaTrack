package com.donatrack.donaciones.infrastructure.adapters.out.persistence;

import com.donatrack.donaciones.domain.repository.DonacionRepository;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacion;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MockDonacionRepository implements DonacionRepository {

    private final List<Donacion> baseDeDatosMock = new ArrayList<>();

    @Override
    public List<Donacion> buscarPorEstado(EstadoDonacion estado) {
        return baseDeDatosMock.stream()
                .filter(d -> d.getEstado() == estado)
                .toList();
    }

    @Override
    public java.util.Optional<Donacion> buscarPorId(java.util.UUID id) {
        return baseDeDatosMock.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    @Override
    public void guardar(Donacion donacion) {
        baseDeDatosMock.removeIf(d -> d.getId().equals(donacion.getId()));
        baseDeDatosMock.add(donacion);
    }
}
