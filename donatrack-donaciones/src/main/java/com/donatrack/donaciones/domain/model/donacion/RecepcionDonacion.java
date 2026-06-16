package com.donatrack.donaciones.domain.model.donacion;

import com.donatrack.donaciones.domain.model.roles.Administrador;
import com.donatrack.donaciones.domain.model.roles.Donante;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class RecepcionDonacion {
  @Getter private UUID id;
  @Getter @Setter private LocalDate fechaRecepcion;
  @Getter @Setter private Donante donante;
  @Getter @Setter private Administrador registradoPor;
  @Getter private List<Donacion> donacionesResultantes;
  @Getter private List<Bien> bienesBrutos;

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
