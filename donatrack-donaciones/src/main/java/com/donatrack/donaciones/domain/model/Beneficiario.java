package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Beneficiario extends Rol {
  @Getter @Setter private List<Donacion> donacionesAsignadas;
  @Getter @Setter private List<Necesidad> necesidadesDeclaradas;

  public Beneficiario() {
    super();
    this.donacionesAsignadas = new ArrayList<>();
    this.necesidadesDeclaradas = new ArrayList<>();
  }

  // se registra la necesidad del beneficiario
  public void registrarNecesidad(Necesidad n) {
    this.necesidadesDeclaradas.add(n);
  }

  // Se confirma que el beneficiario recibió la donación asignada que le fue asignada
  public void confirmarRecepcion(Donacion d, List<Foto> fotosComprobante) {
    // Verificamos que la donación estuviera asignada a este beneficiario
    if (this.donacionesAsignadas.contains(d)) {
      d.setEstado(EstadoDonacion.ENTREGADA);
      // (Opcional) Las fotos se podrían guardar en la donación como comprobante de que el
      // beneficiario firmó la recepción.
    } else {
      throw new IllegalStateException("Error: La donación no pertenece a este beneficiario.");
    }
  }

  public List<EstadoDonacion> verEstadoDonaciones(List<Donacion> donacionesAsignadas) {
    return donacionesAsignadas.stream().map(Donacion::getEstado).toList();
  }

  public void verUbicacionCamion() {
    // Queda vacío. Requerirá integración con API de mapas en el frontend.
  }
}
