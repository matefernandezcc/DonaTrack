package com.donatrack.logistica.application.ports.out;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import java.util.List;

public interface ItemPlanificacionRepositoryPort {
  void guardar(ItemPlanificacion item);

  List<ItemPlanificacion> obtenerTodos();

  void eliminarTodos(List<ItemPlanificacion> items);
}
