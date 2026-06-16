package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.roles.strategyAdministrador.ImportadorStrategy;
import com.donatrack.donaciones.domain.model.Deposito;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Administrador extends Rol {
  @Getter @Setter private Deposito depositoAsignado;

  @Getter @Setter private ImportadorStrategy estrategiaImportador;
  @Getter @Setter private List<UUID> camionesIds;

  public Administrador(Deposito depositoAsignado) {
    super();
    this.depositoAsignado = depositoAsignado;
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
      // Error: Primero debe asignarle una estrategia de importación al Administrador.
      return false;
    }
  }



  public void asignarDonacionFinal(Donacion d, Beneficiario b) {
    b.getDonacionesAsignadas().add(d);
    d.asignar(b);
  }

  public void actualizarDonacionesVencidas() {
    if (this.depositoAsignado != null) {
      this.depositoAsignado.auditarVencidos();
    }
  }

  public void asociarDonacion(Donacion d, Donante donante) {
    donante.agregarDonacion(d);
  }

  public void administrarCamiones() {
    // Queda vacío. Requerirá interfaz gráfica de gestión de camiones.
  }

  public void verRankingMensual() {
    // Queda vacío. Requerirá motor de base de datos para procesar métricas de los donantes.
  }
}
