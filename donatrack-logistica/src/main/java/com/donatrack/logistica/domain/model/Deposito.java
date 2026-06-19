package com.donatrack.logistica.domain.model;

import com.donatrack.logistica.domain.model.ubicacion.Direccion;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa un depósito físico de la organización.
 * En el contexto de Logística, el depósito es el nodo de partida
 * para la generación de rutas de los camiones.
 */
public class Deposito {
  @Getter @Setter private UUID id;
  @Getter @Setter private String nombre;
  @Getter @Setter private int capacidadMaxima;
  @Getter @Setter private Direccion direccion;

  public Deposito(UUID id, String nombre, int capacidadMaxima, Direccion direccion) {
    this.id = id != null ? id : UUID.randomUUID();
    this.nombre = nombre;
    this.capacidadMaxima = capacidadMaxima;
    this.direccion = direccion;
  }
}
