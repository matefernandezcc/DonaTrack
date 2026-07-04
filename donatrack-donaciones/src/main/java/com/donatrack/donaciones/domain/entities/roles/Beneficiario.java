package com.donatrack.donaciones.domain.entities.roles;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.necesidades.Necesidad;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Beneficiario extends Rol {
  private List<Donacion> donacionesAsignadas;
  private List<Necesidad> necesidadesDeclaradas;
  private String correoRepresentante;

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

  // --- Métodos del Diagrama de Clases ---

  public void registrarNecesidad(Necesidad n) {
    this.necesidadesDeclaradas.add(n);
  }

  public boolean confirmarRecepcion(Donacion d, List<String> fotos) {
    if (this.donacionesAsignadas.contains(d)) {
      d.cambiarEstado(EstadoDonacion.ENTREGADA, "Confirmada por beneficiario", null);
      // Se asocian las urls de las fotos a la donación
      if (fotos != null) {
          for (String url : fotos) {
              d.addFoto(new com.donatrack.donaciones.domain.entities.donacion.Foto("", url));
          }
      }
      return true;
    } else {
      return false;
    }
  }

  public List<EstadoDonacion> EstadoDonaciones() {
    return donacionesAsignadas.stream().map(d -> d.getEstado()).toList();
  }

  public void UbicacionCamion() {
    // Requerirá integración de mapas
  }
}
