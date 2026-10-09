package com.donatrack.logistica.application.ports.in;

import com.donatrack.logistica.domain.entities.planificacion.ItemPlanificacion;
import java.util.List;

public interface ListarItemsPendientesPort {
  List<ItemPlanificacion> listar();
}
