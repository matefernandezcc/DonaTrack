package com.donatrack.donaciones.domain.entities.roles;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador.ImportadorStrategy;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Administrador extends Rol {
  private UUID idDepositoAsignado;
  private ImportadorStrategy estrategiaImportador;
  private List<UUID> camionesIds;

  public Administrador(UUID idDepositoAsignado) {
    super();
    this.idDepositoAsignado = idDepositoAsignado;
  }

  @Override
  public boolean esValidoParaHumana() { 
    return true; 
  }

  @Override
  public boolean esValidoParaJuridica() { 
    return false; // Una persona jurídica no puede ser administrador
  }

  public boolean importarDonantesMasivos(Archivo archivo) {
    if (this.estrategiaImportador != null) {
      this.estrategiaImportador.importar(archivo);
      return true;
    } else {
      return false;
    }
  }

  public void asignarDonacionFinal(Donacion d, Beneficiario b) {
    b.getDonacionesAsignadas().add(d);
    d.setEntidadAsignada(b);
  }

  public void asociarDonacion(DonacionOriginal d, Donante donante) {
    donante.agregarDonacion(d);
  }

  public void administrarCamiones() {
    // Pendiente: Requerirá integración con Servicio de Logística.
  }

  public void verRankingMensual() {
    // Pendiente: Requerirá integración con Servicio de Incentivos.
  }
}
