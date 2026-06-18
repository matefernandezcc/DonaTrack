package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacionEnum;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;

public class EnDeposito extends EstadoDonacion {

  public EnDeposito(Donacion donacion) {
    super(donacion);
  }

  @Override
  public void asignar(Beneficiario beneficiario) {
    donacion.setEntidadAsignada(beneficiario);
    donacion.cambiarEstado(new Asignada(donacion), "Donación asignada a entidad", null);
  }

  @Override
  public void marcarVencida() {
    donacion.cambiarEstado(new Vencida(donacion), "Donación marcada como vencida por administrador", null);
  }

  @Override
  public EstadoDonacionEnum getValorEnum() {
    return EstadoDonacionEnum.EN_DEPOSITO;
  }
}
