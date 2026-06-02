package com.donatrack.donaciones.domain.model;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.strategy.EstrategiaSegmentacion;
import com.donatrack.donaciones.domain.factory.DonacionFactory;
import java.time.LocalDate;
import java.util.ArrayList;
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
  private List<EstrategiaSegmentacion> estrategiasSegmentacion;
  private DonacionFactory donacionFactory = new DonacionFactory();

  public RecepcionDonacion(Donante donante, Administrador registradoPor, List<EstrategiaSegmentacion> estrategiasSegmentacion) {
    this.id = UUID.randomUUID();
    this.fechaRecepcion = LocalDate.now();
    this.donante = donante;
    this.registradoPor = registradoPor;
    this.donacionesResultantes = new ArrayList<>();
    this.estrategiasSegmentacion = estrategiasSegmentacion;
  }

  public List<Donacion> procesar(List<Bien> bienesBrutos) {
    // 1. Lista inicial
    List<List<Bien>> listasDeBienes = new ArrayList<>();
    listasDeBienes.add(bienesBrutos);

    // 2. Se ejecuta el pipeline
    for (EstrategiaSegmentacion estrategia : estrategiasSegmentacion) {
      listasDeBienes = estrategia.segmentar(listasDeBienes);
    }

    // 3. Cada lista de bienes se convierte en una Donacion
    for (List<Bien> listaFinal : listasDeBienes) {
      Donacion nuevaDonacion = donacionFactory.crearDesdeBienes(listaFinal);
      if (nuevaDonacion != null) {
        donacionesResultantes.add(nuevaDonacion);
      }
    }

    return donacionesResultantes;
  }
}
