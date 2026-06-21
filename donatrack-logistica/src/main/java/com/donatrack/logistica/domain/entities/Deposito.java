package com.donatrack.logistica.domain.entities;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa un depósito físico de la organización.
 * En el contexto de Logística, el depósito es el nodo de partida
 * para la generación de rutas de los camiones.
 */
@Getter
@Setter
public class Deposito {
  private UUID id;
  private String nombre;
  private int capacidadMaxima;
  private Direccion direccion;

  public Deposito(UUID id, String nombre, int capacidadMaxima, Direccion direccion) {
    this.id = id != null ? id : UUID.randomUUID();
    this.nombre = nombre;
    this.capacidadMaxima = capacidadMaxima;
    this.direccion = direccion;
  }
}
