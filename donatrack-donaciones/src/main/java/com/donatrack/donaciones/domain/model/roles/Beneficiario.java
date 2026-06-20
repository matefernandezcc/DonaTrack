package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.necesidades.Necesidad;
import com.donatrack.donaciones.domain.model.donacion.Foto;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Beneficiario extends Rol {
  private List<Donacion> donacionesAsignadas;
  private List<Necesidad> necesidadesDeclaradas;

  public Beneficiario() {
    super();
    this.donacionesAsignadas = new ArrayList<>();
    this.necesidadesDeclaradas = new ArrayList<>();
  }

  @Override
  public boolean esValidoParaHumana() { 
    return false; // Una persona humana no puede ser beneficiaria
  }

  @Override
  public boolean esValidoParaJuridica() { 
    return true; 
  }

  // se registra la necesidad del beneficiario
  public void registrarNecesidad(Necesidad n) {
    this.necesidadesDeclaradas.add(n);
  }

  // Se confirma que el beneficiario recibió la donación asignada que le fue asignada
  public boolean confirmarRecepcion(Donacion d, List<Foto> fotosComprobante) {
    // Verificamos que la donación estuviera asignada a este beneficiario
    if (this.donacionesAsignadas.contains(d)) {
      d.cambiarEstado(EstadoDonacion.ENTREGADA, "Confirmada por beneficiario", null);
      // (Opcional) Las fotos se podrían guardar en la donación como comprobante de que el
      // beneficiario firmó la recepción.
      return true;
    } else {
      // Error: La donación no pertenece a este beneficiario.
      return false;
    }
  }

  public List<EstadoDonacion> verEstadoDonaciones(List<Donacion> donacionesAsignadas) {
    return donacionesAsignadas.stream().map(d -> d.getEstado()).toList();
  }

  public void verUbicacionCamion() {
    // Queda vacío. Requerirá integración con API de mapas en el frontend.
  }
}
