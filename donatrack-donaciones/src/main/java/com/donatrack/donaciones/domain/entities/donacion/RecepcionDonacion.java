package com.donatrack.donaciones.domain.entities.donacion;

import com.donatrack.donaciones.domain.entities.roles.Administrador;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecepcionDonacion {
  private UUID id;
  private LocalDate fechaRecepcion;
  private Donante donante;
  private Administrador registradoPor;
  private List<Donacion> donacionesResultantes;
  private List<Bien> bienesBrutos;

  public RecepcionDonacion(
      Donante donante, 
      Administrador registradoPor, 
      List<Bien> bienesBrutos, 
      List<Donacion> donacionesResultantes) {
    this.id = UUID.randomUUID();
    this.fechaRecepcion = LocalDate.now();
    this.donante = donante;
    this.registradoPor = registradoPor;
    this.bienesBrutos = bienesBrutos;
    this.donacionesResultantes = donacionesResultantes;
  }
}
