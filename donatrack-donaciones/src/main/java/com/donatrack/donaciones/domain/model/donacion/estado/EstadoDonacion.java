package com.donatrack.donaciones.domain.model.donacion.estado;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;
import com.donatrack.donaciones.domain.enums.EstadoDonacionEnum;

public abstract class EstadoDonacion {

  protected Donacion donacion;

  public EstadoDonacion(Donacion donacion) {
    this.donacion = donacion;
  }

  public void asignar(Beneficiario beneficiario) {
    throw new IllegalStateException("Transición no permitida: asignar");
  }

  public void planificarRuta() {
    throw new IllegalStateException("Transición no permitida: planificar ruta");
  }

  public void iniciarTraslado() {
    throw new IllegalStateException("Transición no permitida: iniciar traslado");
  }

  public void entregar() {
    throw new IllegalStateException("Transición no permitida: entregar");
  }

  public void fallarEntrega(String justificacion) {
    throw new IllegalStateException("Transición no permitida: fallar entrega");
  }

  public void marcarVencida() {
    throw new IllegalStateException("Transición no permitida: marcar vencida");
  }

  public void recibirEnDeposito() {
    throw new IllegalStateException("Transición no permitida: recibir en depósito");
  }

  public abstract EstadoDonacionEnum getValorEnum();
}
