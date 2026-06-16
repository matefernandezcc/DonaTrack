package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.enums.EstadoDonacionEnum;

public class Vencida extends EstadoDonacion {

  public Vencida(Donacion donacion) {
    super(donacion);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.VENCIDA;
  }
}
