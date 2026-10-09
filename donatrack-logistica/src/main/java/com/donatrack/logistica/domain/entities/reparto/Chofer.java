package com.donatrack.logistica.domain.entities.reparto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chofer {
  private UUID id;
  private String legajo;
  private String nombre;
}
