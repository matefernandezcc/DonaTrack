package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.strategy.AsignacionStrategy;
import com.donatrack.donaciones.domain.strategy.ImportadorStrategy;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Administrador extends Rol {
  @Getter @Setter private Deposito depositoAsignado;
  @Getter @Setter private AsignacionStrategy estrategiaAsignacion;
  @Getter @Setter private ImportadorStrategy estrategiaImportador;
  @Getter @Setter private List<Camion> camiones;

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

  public void importarDonantesMasivos(String rutaArchivo) {
    if (this.estrategiaImportador != null) {
      this.estrategiaImportador.importar(rutaArchivo);
    } else {
      throw new IllegalStateException(
          "Error: Primero debe asignarle una estrategia de importación al Administrador.");
    }
  }

  public List<Donacion> obtenerRecomendacionAsignacion(
      List<Donacion> donacionesDisponibles, List<Necesidad> necesidadesDeclaradas) {
    if (this.estrategiaAsignacion != null) {
      return this.estrategiaAsignacion.recomendarNecesidades(
          donacionesDisponibles, necesidadesDeclaradas);
    } else {
      throw new IllegalStateException(
          "Error: Primero debe asignarle una estrategia de asignación al Administrador.");
    }
  }

  public void asignarDonacionFinal(Donacion d, Beneficiario b) {
    b.getDonacionesAsignadas().add(d);

    HistorialEstado nuevoHistorial =
        new HistorialEstado(
            EstadoDonacion.ASIGNADA, "Donación asignada a entidad por administrador.", this);

    d.registrarCambioEstado(nuevoHistorial);
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
