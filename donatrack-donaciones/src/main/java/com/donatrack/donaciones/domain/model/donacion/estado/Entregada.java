package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.enums.EstadoDonacionEnum;

public class Entregada extends EstadoDonacion {

  public Entregada(Donacion donacion) {
    super(donacion);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.ENTREGADA;
  }
}
