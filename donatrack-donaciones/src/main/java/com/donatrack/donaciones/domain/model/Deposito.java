package com.donatrack.donaciones.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class Deposito {
  @Getter @Setter private UUID id;
  @Getter private List<Donacion> donacionesDisponibles;
  @Getter @Setter private String nombre;
  @Getter @Setter private int capacidadMaxima;
  @Getter @Setter private Direccion direccion;

  public Deposito(String nombre, int capacidadMaxima, Direccion direccion) {
    this.id = UUID.randomUUID();
    this.nombre = nombre;
    this.capacidadMaxima = capacidadMaxima;
    this.direccion = direccion;
    this.donacionesDisponibles = new ArrayList<>();
  }

  // --- Implementación de la lógica de negocio ---
  public void auditarVencidos() {
    LocalDate hoy = LocalDate.now();

    // Usamos un Iterator para poder remover elementos de la lista de forma segura mientras la
    // recorremos
    Iterator<Donacion> iterador = donacionesDisponibles.iterator();

    while (iterador.hasNext()) {
      Donacion donacion = iterador.next();
      boolean tieneVencidos = false;

      for (Bien bien : donacion.getBienes()) {
        // Si el bien tiene fecha de vencimiento y ya pasó la fecha de hoy
        if (bien.getFechaVencimiento() != null && bien.getFechaVencimiento().isBefore(hoy)) {
          tieneVencidos = true;
          break; // Con un solo bien vencido, apartamos la donación entera
        }
      }

      if (tieneVencidos) {
        // Aquí podrías guardar la donación en una lista de "descartados",
        // pero por lo pronto la retiramos del depósito.
        iterador.remove();
      }
    }
  }

  public void ingresarDonacion(Donacion d) {
    this.donacionesDisponibles.add(d);
  }
}
