package com.donatrack.logistica.infrastructure.adapters.out.persistence;

import com.donatrack.logistica.application.ports.out.ItemPlanificacionRepositoryPort;
import com.donatrack.logistica.domain.entities.ItemPlanificacion;
//import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

//@Repository
public class MockItemPlanificacionRepository implements ItemPlanificacionRepositoryPort {

    private final List<ItemPlanificacion> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(ItemPlanificacion item) {
        baseDeDatosMock.removeIf(i -> i.getIdDonacionOriginal().equals(item.getIdDonacionOriginal()));
        baseDeDatosMock.add(item);
    }

    @Override
    public List<ItemPlanificacion> obtenerTodos() {
        return baseDeDatosMock;
    }
}
