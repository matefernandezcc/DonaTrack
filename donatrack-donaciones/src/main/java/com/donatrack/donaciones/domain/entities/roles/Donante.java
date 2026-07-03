package com.donatrack.donaciones.domain.entities.roles;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante extends Rol {
  private List<DonacionOriginal> donacionesRealizadas;

  public Donante() {
    super();
    this.donacionesRealizadas = new ArrayList<>();
  }

  @Override
  public boolean esValidoParaHumana() { 
    return true; 
  }

  @Override
  public boolean esValidoParaJuridica() { 
    return true; 
  }

  // --- Métodos del Diagrama de Clases ---

  public void agregarDonacion(DonacionOriginal d) {
    this.donacionesRealizadas.add(d);
  }

  public EstadoDonacion consultarEstadoDonacion(Donacion d) {
    return d.getEstado();
  }

  public void filtrarDonaciones(EstadoDonacion estado, String categoria) {
    // Lógica de filtrado en base al historial de donaciones
  }

  public void UbicacionCamion() {
    // Integración futura de GPS
  }
}
