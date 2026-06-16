package com.donatrack.donaciones.domain.model.roles.strategyAdministrador.asignador;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.necesidades.Necesidad;

import java.util.List;

public class PorPrioridad implements AsignacionStrategy {
  @Override
  public List<Donacion> recomendarNecesidades(
      List<Donacion> donaciones, List<Necesidad> necesidades) {
    return null;
  }
}
