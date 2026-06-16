package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.enums.EstadoDonacionEnum;

public class Asignada extends EstadoDonacion {

  public Asignada(Donacion donacion) {
    super(donacion);
  }

  @Override
  public void planificarRuta() {
    donacion.cambiarEstado(new ListaParaEntregar(donacion), "Ruta planificada", null);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.ASIGNADA;
  }
}
