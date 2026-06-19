package com.donatrack.donaciones.domain.model.roles;

import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.donacion.RecepcionDonacion;
import com.donatrack.donaciones.domain.model.enums.EstadoDonacion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class Donante extends Rol {
  @Getter @Setter private List<Donacion> donacionesRealizadas;
  @Getter private List<RecepcionDonacion> historialRecepciones;

  public Donante() {
    super();
    this.donacionesRealizadas = new ArrayList<>();
    this.historialRecepciones = new ArrayList<>();
  }

  @Override
  public boolean esValidoParaHumana() { 
    return true; 
  }

  @Override
  public boolean esValidoParaJuridica() { 
    return true; 
  }

  // --- Métodos de comportamiento ---

  public void agregarDonacion(Donacion d) {
    this.donacionesRealizadas.add(d);
  }

  public void agregarRecepcion(RecepcionDonacion r) {
    this.historialRecepciones.add(r);
  }

  public List<EstadoDonacion> consultarEstadoTodasLasDonaciones(
      List<Donacion> donacionesRealizadas) {
    return donacionesRealizadas.stream().map(d -> d.getEstado()).toList();
  }

  public EstadoDonacion consultarEstadoDonacion(Donacion d) {
    return d.getEstado();
  }

  public void filtrarDonaciones(EstadoDonacion estado, String categoria) {
    // Lógica para filtrar el historial de donaciones realizadas por este usuario
  }

  public void verUbicacionCamion() {
    // Lógica para consultar la ubicación en tiempo real del camión asignado
  }
}
