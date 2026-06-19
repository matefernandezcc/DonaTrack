package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.strategyAdministrador.importador.ImportadorStrategy;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Administrador extends Rol {
  @Getter @Setter private UUID idDepositoAsignado;

  @Getter @Setter private ImportadorStrategy estrategiaImportador;
  @Getter @Setter private List<UUID> camionesIds;

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

  public boolean importarDonantesMasivos(String rutaArchivo) {
    if (this.estrategiaImportador != null) {
      this.estrategiaImportador.importar(rutaArchivo);
      return true;
    } else {
      return false;
    }
  }

  public void asignarDonacionFinal(Donacion d, Beneficiario b) {
    b.getDonacionesAsignadas().add(d);
    d.asignar(b);
  }

  public void asociarDonacion(Donacion d, Donante donante) {
    donante.agregarDonacion(d);
  }

  public void administrarCamiones() {
    // Pendiente: Requerirá integración con Servicio de Logística.
  }

  public void verRankingMensual() {
    // Pendiente: Requerirá integración con Servicio de Incentivos.
  }
}
