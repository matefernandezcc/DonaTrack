package com.donatrack.logistica.application.ports.out;

import java.util.List;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;

public interface ItemPlanificacionRepositoryPort {
    void guardar(ItemPlanificacion item);

    List<ItemPlanificacion> obtenerTodos();

    void eliminarTodos(List<ItemPlanificacion> items);
}